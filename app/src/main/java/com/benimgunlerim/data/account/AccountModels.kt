package com.benimgunlerim.data.account

/** Hesap durumu. Misafir: veri yalnız telefonda. Hesap: veri ayrıca bulutta yedeklenir. */
sealed interface AccountState {
    /** Firebase yapılandırılmamış (google-services.json yok) — uygulama yalnız misafir modunda çalışır. */
    data object Unavailable : AccountState

    data object Guest : AccountState

    data class SignedIn(
        val uid: String,
        val email: String,
        val isEmailVerified: Boolean,
    ) : AccountState
}

/** Kullanıcıya gösterilecek, anlamı belli hata türleri (Firebase istisnalarından eşlenir). */
enum class AccountError {
    InvalidEmail,
    WeakPassword,
    WrongCredentials,
    EmailInUse,
    UserNotFound,
    TooManyRequests,
    Network,
    RecentLoginRequired,
    Unavailable,
    Unknown,
}

sealed interface AccountResult<out T> {
    data class Success<T>(val value: T) : AccountResult<T>
    data class Failure(val error: AccountError) : AccountResult<Nothing>
}

/** Açılışta kullanıcının yaptığı seçim. [Unset] ise karşılama/giriş ekranı gösterilir. */
enum class EntryChoice { Unset, Guest, Account }

/** Buluttaki yedeğin özeti (içerik indirilmeden karar verebilmek için). */
data class CloudBackupMeta(
    val updatedAtMillis: Long,
    val taskCount: Int,
    val routineCount: Int,
)
