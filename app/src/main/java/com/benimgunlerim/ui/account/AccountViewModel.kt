package com.benimgunlerim.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.data.account.AccountError
import com.benimgunlerim.data.account.AccountPrefs
import com.benimgunlerim.data.account.AccountRepository
import com.benimgunlerim.data.account.AccountResult
import com.benimgunlerim.data.account.AccountState
import com.benimgunlerim.data.account.EntryChoice
import com.benimgunlerim.domain.account.CloudSyncCoordinator
import com.benimgunlerim.domain.account.SyncOutcome
import com.benimgunlerim.domain.account.SyncStatus
import com.benimgunlerim.domain.account.SyncTrigger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthMode { Login, Register, Reset }

/** Form ve işlem durumu — tek kaynak; ekranlar sadece çizer. */
data class AuthUiState(
    val busy: Boolean = false,
    val error: AccountError? = null,
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val confirmError: Boolean = false,
    val consentError: Boolean = false,
    val resetSent: Boolean = false,
    /** Hesaba girildi ve eşitleme bitti — ekran yönlendirir. */
    val finished: Boolean = false,
    /** İlk eşitleme çakışması: kullanıcı bulut/telefon seçecek. */
    val conflict: SyncOutcome.NeedsChoice? = null,
    val info: AccountInfo? = null,
)

enum class AccountInfo { VerificationSent, Verified, NotVerifiedYet, Synced, SyncFailed, SignedOut, Deleted }

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val account: AccountRepository,
    private val prefs: AccountPrefs,
    private val sync: CloudSyncCoordinator,
) : ViewModel() {

    val isAvailable: Boolean = account.isAvailable || (com.benimgunlerim.BuildConfig.DEBUG && com.benimgunlerim.ui.theme.DebugSkyPhase.forceAccountUi)
    val accountState: StateFlow<AccountState> =
        account.state.stateIn(viewModelScope, SharingStarted.Eagerly, account.currentState())
    val entryChoice: StateFlow<EntryChoice> = prefs.entryChoice
    val lastSyncAt: StateFlow<Long> = prefs.lastSyncAt
    val syncStatus: StateFlow<SyncStatus> = sync.status

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    init {
        // Doğrulama bağlantısı uygulama dışında tıklanır; açılışta önbellekteki eski durum sessizce tazelenir.
        val s = accountState.value
        if (s is AccountState.SignedIn && !s.isEmailVerified) viewModelScope.launch { account.reloadUser() }
    }

    fun continueAsGuest() = prefs.setEntryChoice(EntryChoice.Guest)

    fun clearMessages() = _ui.update { AuthUiState() }

    fun submit(mode: AuthMode, email: String, password: String, confirm: String, consent: Boolean) {
        val v = AuthValidation.validate(mode, email, password, confirm, consent)
        _ui.update {
            it.copy(
                emailError = !v.emailOk, passwordError = !v.passwordOk, confirmError = !v.confirmOk,
                consentError = !v.consentOk, error = null,
            )
        }
        if (!v.allOk) return
        viewModelScope.launch {
            _ui.update { it.copy(busy = true) }
            val result = when (mode) {
                AuthMode.Login -> account.signIn(email, password)
                AuthMode.Register -> account.register(email, password)
                AuthMode.Reset -> account.sendPasswordReset(email)
            }
            when (result) {
                is AccountResult.Failure -> _ui.update { it.copy(busy = false, error = result.error) }
                is AccountResult.Success -> if (mode == AuthMode.Reset) {
                    _ui.update { it.copy(busy = false, resetSent = true) }
                } else {
                    afterSignIn()
                }
            }
        }
    }

    /** Hesaba girildi: misafir verisi taşınır ya da bulut indirilir; ikisi de doluysa sorulur. */
    private suspend fun afterSignIn() {
        when (val outcome = sync.sync(SyncTrigger.Start)) {
            is SyncOutcome.NeedsChoice -> _ui.update { it.copy(busy = false, conflict = outcome) }
            is SyncOutcome.Failed -> {
                // Giriş başarılı; eşitleme sonra yeniden denenir. Kullanıcı bekletilmez.
                prefs.setEntryChoice(EntryChoice.Account)
                _ui.update { it.copy(busy = false, finished = true, info = AccountInfo.SyncFailed) }
            }
            else -> {
                prefs.setEntryChoice(EntryChoice.Account)
                _ui.update { it.copy(busy = false, finished = true) }
            }
        }
    }

    fun resolveConflict(keepCloud: Boolean) {
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, conflict = null) }
            val outcome = sync.resolveFirstSync(keepCloud)
            _ui.update {
                it.copy(busy = false, finished = true, info = if (outcome is SyncOutcome.Failed) AccountInfo.SyncFailed else null)
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            val outcome = sync.sync(SyncTrigger.Manual)
            _ui.update { it.copy(info = if (outcome is SyncOutcome.Failed) AccountInfo.SyncFailed else AccountInfo.Synced) }
        }
    }

    fun resendVerification() {
        viewModelScope.launch {
            val r = account.sendEmailVerification()
            _ui.update { it.copy(info = if (r is AccountResult.Success) AccountInfo.VerificationSent else null, error = (r as? AccountResult.Failure)?.error) }
        }
    }

    fun checkVerified() {
        viewModelScope.launch {
            val r = account.reloadUser()
            _ui.update {
                it.copy(info = if ((r as? AccountResult.Success)?.value == true) AccountInfo.Verified else AccountInfo.NotVerifiedYet)
            }
        }
    }

    fun signOut(clearDevice: Boolean) {
        viewModelScope.launch {
            sync.signOut(clearDevice)
            _ui.update { it.copy(info = AccountInfo.SignedOut) }
        }
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            when (val r = sync.deleteAccount(password)) {
                is AccountResult.Failure -> _ui.update { it.copy(busy = false, error = r.error) }
                is AccountResult.Success -> _ui.update { it.copy(busy = false, info = AccountInfo.Deleted) }
            }
        }
    }
}

/** Form doğrulaması — saf, test edilir. */
object AuthValidation {
    const val MIN_PASSWORD = 8
    private val EmailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")

    data class Result(val emailOk: Boolean, val passwordOk: Boolean, val confirmOk: Boolean, val consentOk: Boolean) {
        val allOk get() = emailOk && passwordOk && confirmOk && consentOk
    }

    fun isEmailValid(email: String) = EmailRegex.matches(email.trim())

    /** Kayıtta: en az 8 karakter, en az bir harf ve bir rakam. */
    fun isStrongPassword(p: String) = p.length >= MIN_PASSWORD && p.any { it.isLetter() } && p.any { it.isDigit() }

    fun validate(mode: AuthMode, email: String, password: String, confirm: String, consent: Boolean): Result {
        val emailOk = isEmailValid(email)
        return when (mode) {
            AuthMode.Reset -> Result(emailOk, true, true, true)
            AuthMode.Login -> Result(emailOk, password.isNotEmpty(), true, true)
            AuthMode.Register -> Result(emailOk, isStrongPassword(password), confirm == password, consent)
        }
    }
}
