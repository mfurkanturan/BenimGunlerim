package com.benimgunlerim.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.benimgunlerim.BuildConfig
import com.benimgunlerim.R
import com.benimgunlerim.domain.GameEngine
import com.benimgunlerim.data.MAX_USER_NAME_LENGTH
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.DayPhase
import com.benimgunlerim.ui.theme.skyPaletteFor

/**
 * Profil kartı: sabah göğü zemininde Mino ve sen. İsim yoksa "Sana nasıl seslenelim?" diye sorar;
 * dokununca isim düzenlenir. Altında seviye ve XP — uygulamanın seni tanıdığını hissettiren tek yer.
 */
@Composable
internal fun SettingsProfileSection(userName: String, totalXp: Int, onEditClick: () -> Unit) {
    val sky = skyPaletteFor(DayPhase.Sabah)
    val name = userName.trim()
    val title = if (name.isEmpty()) stringResource(R.string.settings_profile_ask) else stringResource(R.string.settings_profile_hello, name)
    val level = GameEngine.calculateLevel(totalXp).level
    val sub = stringResource(R.string.settings_profile_level, level, totalXp)
    val editCd = stringResource(R.string.settings_name_dialog_title)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTokens.Radius.lg))
            .background(sky.sky)
            .clickable(onClick = onEditClick)
            .clearAndSetSemantics {
                contentDescription = "$title. $sub. $editCd"
                role = Role.Button
            }
            .padding(AppTokens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
    ) {
        Image(
            painter = painterResource(R.drawable.mascot_mino),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(AppTokens.Sky.mascotHeight * 0.75f),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs)) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium, color = sky.onSky)
            Text(text = sub, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = sky.onSkyMuted)
        }
        Icon(
            Icons.Rounded.Edit,
            contentDescription = null,
            tint = sky.onSky,
            modifier = Modifier
                .size(AppTokens.TouchTarget.compact)
                .clip(CircleShape)
                .background(sky.chip)
                .padding(AppTokens.Spacing.xs),
        )
    }
}

/** Sayfa sonu: küçük Mino + uygulama adı/sürüm + slogan. */
@Composable
internal fun SettingsFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = AppTokens.Spacing.md)
            .heightIn(min = AppTokens.TouchTarget.min),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
    ) {
        Image(
            painter = painterResource(R.drawable.mascot_mino),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(AppTokens.IconSize.xl),
        )
        Text(
            text = stringResource(R.string.settings_footer_slogan),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.app_name) + " · " +
                stringResource(R.string.settings_version_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
internal fun SettingsNameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_name_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(MAX_USER_NAME_LENGTH) },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.onboarding_name_placeholder)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name) }) {
                Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
