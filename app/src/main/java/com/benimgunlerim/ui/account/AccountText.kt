package com.benimgunlerim.ui.account

import com.benimgunlerim.R
import com.benimgunlerim.data.account.AccountError

internal fun AccountError.messageRes(): Int = when (this) {
    AccountError.InvalidEmail -> R.string.auth_err_InvalidEmail
    AccountError.WeakPassword -> R.string.auth_err_WeakPassword
    AccountError.WrongCredentials -> R.string.auth_err_WrongCredentials
    AccountError.EmailInUse -> R.string.auth_err_EmailInUse
    AccountError.UserNotFound -> R.string.auth_err_UserNotFound
    AccountError.TooManyRequests -> R.string.auth_err_TooManyRequests
    AccountError.Network -> R.string.auth_err_Network
    AccountError.RecentLoginRequired -> R.string.auth_err_RecentLoginRequired
    AccountError.Unavailable -> R.string.auth_err_Unavailable
    AccountError.Unknown -> R.string.auth_err_Unknown
}

internal fun AccountInfo.messageRes(): Int = when (this) {
    AccountInfo.VerificationSent -> R.string.account_msg_VerificationSent
    AccountInfo.Verified -> R.string.account_msg_Verified
    AccountInfo.NotVerifiedYet -> R.string.account_msg_NotVerifiedYet
    AccountInfo.Synced -> R.string.account_msg_Synced
    AccountInfo.SyncFailed -> R.string.account_msg_SyncFailed
    AccountInfo.SignedOut -> R.string.account_msg_SignedOut
    AccountInfo.Deleted -> R.string.account_msg_Deleted
}
