package com.benimgunlerim.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.text.format.DateUtils
import com.benimgunlerim.R
import com.benimgunlerim.data.account.AccountState
import com.benimgunlerim.domain.account.SyncStatus
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.components.molecules.SheetSaveButton
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens

/** Ayarlar › Hesap: misafirse hesap aç/giriş; hesaptaysa yedek durumu, doğrulama, çıkış, hesabı silme. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountSection(viewModel: AccountViewModel, onRegister: () -> Unit, onLogin: () -> Unit) {
    if (!viewModel.isAvailable) return
    val state by viewModel.accountState.collectAsState()
    val ui by viewModel.ui.collectAsState()
    val lastSync by viewModel.lastSyncAt.collectAsState()
    val status by viewModel.syncStatus.collectAsState()
    var showSignOut by rememberSaveable { mutableStateOf(false) }
    var showDelete by rememberSaveable { mutableStateOf(false) }

    SectionBlock(title = stringResource(R.string.account_section), modifier = Modifier.testTag(TestTags.SettingsAccount)) {
        AppSurface(radius = AppTokens.Radius.lg, padding = AppTokens.Spacing.md) {
            when (val s = state) {
                is AccountState.SignedIn -> SignedInContent(
                    state = s,
                    lastSync = lastSync,
                    syncing = status == SyncStatus.Syncing,
                    onSync = viewModel::syncNow,
                    onResend = viewModel::resendVerification,
                    onCheckVerified = viewModel::checkVerified,
                    onSignOut = { showSignOut = true },
                    onDelete = { showDelete = true },
                )
                else -> Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm)) {
                        Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.account_guest_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                    }
                    Text(stringResource(R.string.account_guest_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SheetSaveButton(text = stringResource(R.string.gate_register), onClick = onRegister, modifier = Modifier.padding(top = AppTokens.Spacing.xs))
                    TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.gate_login)) }
                }
            }
            ui.info?.let {
                Text(
                    stringResource(it.messageRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = AppTokens.Spacing.xs).semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
    }

    if (showSignOut) SignOutDialog(onDismiss = { showSignOut = false }) { clear ->
        showSignOut = false
        viewModel.signOut(clear)
    }
    if (showDelete) DeleteAccountDialog(viewModel = viewModel, onDismiss = { showDelete = false })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SignedInContent(
    state: AccountState.SignedIn,
    lastSync: Long,
    syncing: Boolean,
    onSync: () -> Unit,
    onResend: () -> Unit,
    onCheckVerified: () -> Unit,
    onSignOut: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm)) {
            Icon(Icons.Rounded.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.account_signed_in_as, state.email),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                )
                Text(
                    text = when {
                        syncing -> stringResource(R.string.account_syncing)
                        lastSync > 0 && System.currentTimeMillis() - lastSync < DateUtils.MINUTE_IN_MILLIS ->
                            stringResource(R.string.account_last_sync_just_now)
                        lastSync > 0 -> stringResource(
                            R.string.account_last_sync,
                            DateUtils.getRelativeTimeSpanString(lastSync, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString(),
                        )
                        else -> stringResource(R.string.account_never_synced)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (!state.isEmailVerified) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = AppTokens.Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
            ) {
                Icon(
                    Icons.Rounded.MarkEmailUnread,
                    contentDescription = null,
                    tint = AppTheme.colors.status.attention.fg,
                    modifier = Modifier.size(AppTokens.IconSize.sm),
                )
                Text(stringResource(R.string.account_unverified), style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.status.attention.fg)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                TextButton(onClick = onResend) { Text(stringResource(R.string.account_resend)) }
                TextButton(onClick = onCheckVerified) { Text(stringResource(R.string.account_i_verified)) }
            }
        }
        OutlinedButton(
            onClick = onSync,
            enabled = !syncing,
            modifier = Modifier.fillMaxWidth().heightIn(min = AppTokens.TouchTarget.min).padding(top = AppTokens.Spacing.xs),
        ) { Text(stringResource(R.string.account_sync_now)) }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
            TextButton(onClick = onSignOut) { Text(stringResource(R.string.account_sign_out)) }
            TextButton(onClick = onDelete) { Text(stringResource(R.string.account_delete), color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun SignOutDialog(onDismiss: () -> Unit, onConfirm: (clearDevice: Boolean) -> Unit) {
    var clear by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.account_sign_out_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                Text(stringResource(R.string.account_sign_out_body))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = clear, onCheckedChange = { clear = it })
                    Text(stringResource(R.string.account_sign_out_clear), style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(clear) }) { Text(stringResource(R.string.account_sign_out)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun DeleteAccountDialog(viewModel: AccountViewModel, onDismiss: () -> Unit) {
    var password by rememberSaveable { mutableStateOf("") }
    val ui by viewModel.ui.collectAsState()
    LaunchedEffect(ui.info) { if (ui.info == AccountInfo.Deleted) onDismiss() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.account_delete_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm)) {
                Text(stringResource(R.string.account_delete_body))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.auth_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = ui.error != null,
                    supportingText = ui.error?.let { e -> { Text(stringResource(e.messageRes())) } },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.deleteAccount(password) }, enabled = password.isNotEmpty() && !ui.busy) {
                Text(stringResource(R.string.account_delete_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
