package com.benimgunlerim.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.molecules.SheetSaveButton
import com.benimgunlerim.ui.onboarding.OnboardingSky
import com.benimgunlerim.ui.theme.AppTokens

/** İlk açılış: Hesap aç / Giriş yap / Misafir olarak devam et. Gece göğü, Mino ile karşılar. */
@Composable
fun AccountGateScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    onGuest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(TestTags.AccountGate),
    ) {
        OnboardingSky(step = 0, totalSteps = 1, minoLine = stringResource(R.string.onboarding_mino_1), onSkip = null)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTokens.Spacing.xl, vertical = AppTokens.Spacing.sm)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
        ) {
            Text(stringResource(R.string.gate_title), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(stringResource(R.string.gate_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SheetSaveButton(
                text = stringResource(R.string.gate_register),
                onClick = onRegister,
                modifier = Modifier.padding(top = AppTokens.Spacing.sm),
            )
            OutlinedButton(
                onClick = onLogin,
                shape = RoundedCornerShape(AppTokens.Radius.pill),
                modifier = Modifier.fillMaxWidth().heightIn(min = AppTokens.Layout.fabDefaultSize),
            ) {
                Text(stringResource(R.string.gate_login), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            }
            TextButton(
                onClick = onGuest,
                modifier = Modifier.fillMaxWidth().heightIn(min = AppTokens.TouchTarget.min).testTag(TestTags.AccountGuestButton),
            ) {
                Text(stringResource(R.string.gate_guest), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = stringResource(R.string.gate_guest_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
            )
        }
    }
}
