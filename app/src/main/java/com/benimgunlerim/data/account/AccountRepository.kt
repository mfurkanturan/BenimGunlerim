package com.benimgunlerim.data.account

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** E-posta/şifre hesabı: kayıt, giriş, doğrulama, şifre sıfırlama, çıkış, hesabı silme. */
interface AccountRepository {
    val isAvailable: Boolean
    val state: Flow<AccountState>
    fun currentState(): AccountState
    suspend fun register(email: String, password: String): AccountResult<Unit>
    suspend fun signIn(email: String, password: String): AccountResult<Unit>
    suspend fun sendPasswordReset(email: String): AccountResult<Unit>
    suspend fun sendEmailVerification(): AccountResult<Unit>
    /** Doğrulama bağlantısına tıklandıktan sonra durumu tazeler. */
    suspend fun reloadUser(): AccountResult<Boolean>
    fun signOut()
    /** Silme işlemi Firebase'de yakın zamanda giriş gerektirir; şifreyle yeniden doğrular, sonra siler. */
    suspend fun reauthenticate(password: String): AccountResult<Unit>
    suspend fun deleteUser(): AccountResult<Unit>
}

@Singleton
class FirebaseAccountRepository @Inject constructor(
    @ApplicationContext context: Context,
) : AccountRepository {

    override val isAvailable: Boolean = FirebaseApp.getApps(context).isNotEmpty()

    private val auth: FirebaseAuth? = if (isAvailable) FirebaseAuth.getInstance() else null

    // reload() AuthStateListener'ı tetiklemez; doğrulama gibi değişiklikler buradan yayınlanır.
    private val refreshes = MutableSharedFlow<AccountState>(extraBufferCapacity = 1)

    override val state: Flow<AccountState> = auth?.let { a ->
        merge(
            callbackFlow {
                val listener = FirebaseAuth.AuthStateListener { trySend(stateOf(it)) }
                a.addAuthStateListener(listener)
                awaitClose { a.removeAuthStateListener(listener) }
            },
            refreshes,
        )
    } ?: flowOf(AccountState.Unavailable)

    override fun currentState(): AccountState = auth?.let(::stateOf) ?: AccountState.Unavailable

    private fun stateOf(a: FirebaseAuth): AccountState {
        val user = a.currentUser ?: return AccountState.Guest
        return AccountState.SignedIn(user.uid, user.email.orEmpty(), user.isEmailVerified)
    }

    override suspend fun register(email: String, password: String): AccountResult<Unit> = call {
        val result = it.createUserWithEmailAndPassword(email.trim(), password).await()
        // Doğrulama e-postası hemen gönderilir; gönderilemezse kayıt yine geçerlidir.
        runCatching { result.user?.sendEmailVerification()?.await() }
    }

    override suspend fun signIn(email: String, password: String): AccountResult<Unit> = call {
        it.signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun sendPasswordReset(email: String): AccountResult<Unit> = call {
        it.sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun sendEmailVerification(): AccountResult<Unit> = call {
        it.currentUser?.sendEmailVerification()?.await()
    }

    override suspend fun reloadUser(): AccountResult<Boolean> {
        val a = auth ?: return AccountResult.Failure(AccountError.Unavailable)
        return runCatching {
            a.currentUser?.reload()?.await()
            refreshes.tryEmit(stateOf(a))
            a.currentUser?.isEmailVerified == true
        }.fold({ AccountResult.Success(it) }, { AccountResult.Failure(mapError(it)) })
    }

    override fun signOut() {
        auth?.signOut()
    }

    override suspend fun reauthenticate(password: String): AccountResult<Unit> = call {
        val user = it.currentUser ?: error("no user")
        user.reauthenticate(EmailAuthProvider.getCredential(user.email.orEmpty(), password)).await()
    }

    override suspend fun deleteUser(): AccountResult<Unit> = call {
        it.currentUser?.delete()?.await()
    }

    private suspend fun call(block: suspend (FirebaseAuth) -> Unit): AccountResult<Unit> {
        val a = auth ?: return AccountResult.Failure(AccountError.Unavailable)
        return runCatching { block(a) }.fold({ AccountResult.Success(Unit) }, { AccountResult.Failure(mapError(it)) })
    }
}

/** Firebase istisnalarını kullanıcıya anlatılabilir hatalara çevirir. */
internal fun mapError(t: Throwable): AccountError = when (t) {
    is FirebaseAuthWeakPasswordException -> AccountError.WeakPassword
    is FirebaseAuthUserCollisionException -> AccountError.EmailInUse
    is FirebaseAuthInvalidUserException -> AccountError.UserNotFound
    is FirebaseAuthRecentLoginRequiredException -> AccountError.RecentLoginRequired
    is FirebaseAuthInvalidCredentialsException ->
        if (t.errorCode == "ERROR_INVALID_EMAIL") AccountError.InvalidEmail else AccountError.WrongCredentials
    is FirebaseTooManyRequestsException -> AccountError.TooManyRequests
    is FirebaseNetworkException -> AccountError.Network
    else -> AccountError.Unknown
}
