package com.benimgunlerim.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.molecules.SheetSaveButton
import com.benimgunlerim.ui.components.molecules.SheetTitle
import com.benimgunlerim.ui.theme.AppTokens

/**
 * Giriş / kayıt / şifre sıfırlama — tek ekran, üç mod. Doğrulama alan alan, Firebase hataları
 * anlaşılır Türkçeye çevrilir. Başarılı girişten sonra eşitleme çakışması varsa [SyncConflictDialog].
 */
@Composable
fun AuthScreen(
    initialMode: AuthMode,
    viewModel: AccountViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    var mode by rememberSaveable { mutableStateOf(initialMode) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var consent by rememberSaveable { mutableStateOf(false) }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    val ui by viewModel.ui.collectAsState()
    val focus = LocalFocusManager.current

    LaunchedEffect(ui.finished) { if (ui.finished) onFinished() }
    LaunchedEffect(mode) { viewModel.clearMessages() }

    val submit = {
        focus.clearFocus()
        viewModel.submit(mode, email, password, confirm, consent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppTokens.Spacing.xl)
            .padding(bottom = AppTokens.Spacing.xl)
            .navigationBarsPadding()
            .testTag(TestTags.AuthScreen),
        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(top = AppTokens.Spacing.xs)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.auth_back))
        }
        SheetTitle(
            stringResource(
                when (mode) {
                    AuthMode.Login -> R.string.auth_login_title
                    AuthMode.Register -> R.string.auth_register_title
                    AuthMode.Reset -> R.string.auth_reset_title
                },
            ),
        )
        Text(
            text = stringResource(
                when (mode) {
                    AuthMode.Login -> R.string.auth_login_body
                    AuthMode.Register -> R.string.auth_register_body
                    AuthMode.Reset -> R.string.auth_reset_body
                },
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = { Text(stringResource(R.string.auth_email)) },
            isError = ui.emailError,
            supportingText = if (ui.emailError) ({ Text(stringResource(R.string.auth_err_email)) }) else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = if (mode == AuthMode.Reset) ImeAction.Done else ImeAction.Next),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().padding(top = AppTokens.Spacing.sm).testTag(TestTags.AuthEmailField),
        )

        if (mode != AuthMode.Reset) {
            val transform = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
            val toggle = @Composable {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = stringResource(if (showPassword) R.string.auth_hide_password else R.string.auth_show_password),
                    )
                }
            }
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.auth_password)) },
                isError = ui.passwordError,
                supportingText = {
                    when {
                        ui.passwordError && mode == AuthMode.Register -> Text(stringResource(R.string.auth_err_password_register))
                        ui.passwordError -> Text(stringResource(R.string.auth_err_password_login))
                        mode == AuthMode.Register -> Text(stringResource(R.string.auth_password_hint))
                    }
                },
                singleLine = true,
                visualTransformation = transform,
                trailingIcon = toggle,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (mode == AuthMode.Register) ImeAction.Next else ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.AuthPasswordField),
            )
        }

        if (mode == AuthMode.Register) {
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text(stringResource(R.string.auth_password_confirm)) },
                isError = ui.confirmError,
                supportingText = if (ui.confirmError) ({ Text(stringResource(R.string.auth_err_confirm)) }) else null,
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = consent, onCheckedChange = { consent = it })
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.auth_consent),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (ui.consentError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onOpenPrivacy, modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min)) {
                        Text(stringResource(R.string.auth_consent_link))
                    }
                }
            }
        }

        val serverError = ui.error
        if (serverError != null) {
            Text(
                text = stringResource(serverError.messageRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        if (ui.resetSent) {
            Text(
                text = stringResource(R.string.auth_reset_sent),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        if (ui.busy) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = AppTokens.Spacing.sm), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(modifier = Modifier.size(AppTokens.IconSize.lg))
            }
        } else {
            SheetSaveButton(
                text = stringResource(
                    when (mode) {
                        AuthMode.Login -> R.string.auth_submit_login
                        AuthMode.Register -> R.string.auth_submit_register
                        AuthMode.Reset -> R.string.auth_submit_reset
                    },
                ),
                onClick = submit,
                modifier = Modifier.padding(top = AppTokens.Spacing.sm).testTag(TestTags.AuthSubmit),
            )
        }

        when (mode) {
            AuthMode.Login -> {
                TextButton(onClick = { mode = AuthMode.Reset }) { Text(stringResource(R.string.auth_forgot)) }
                TextButton(onClick = { mode = AuthMode.Register }) { Text(stringResource(R.string.auth_to_register)) }
            }
            AuthMode.Register -> TextButton(onClick = { mode = AuthMode.Login }) { Text(stringResource(R.string.auth_to_login)) }
            AuthMode.Reset -> TextButton(onClick = { mode = AuthMode.Login }) { Text(stringResource(R.string.auth_to_login)) }
        }
    }

    ui.conflict?.let { conflict ->
        SyncConflictDialog(conflict = conflict, onKeepCloud = { viewModel.resolveConflict(true) }, onKeepPhone = { viewModel.resolveConflict(false) })
    }
}
