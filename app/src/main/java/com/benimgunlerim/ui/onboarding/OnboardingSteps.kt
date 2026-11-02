package com.benimgunlerim.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.benimgunlerim.R
import com.benimgunlerim.data.MAX_USER_NAME_LENGTH
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.molecules.BigTextField
import com.benimgunlerim.ui.components.molecules.SheetSaveButton
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens

internal data class NeedOption(val id: String, val titleRes: Int, val subtitleRes: Int, val icon: ImageVector)

internal data class IntensityOption(
    val id: String,
    val titleRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector,
    val recommended: Boolean = false,
)

@Composable
internal fun StepHeading(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** 1 · Gece: tanışma. */
@Composable
internal fun StepWelcome(userName: String, onNameChange: (String) -> Unit) {
    StepHeading(stringResource(R.string.onboarding_welcome_title), stringResource(R.string.onboarding_welcome_body))
    BigTextField(
        value = userName,
        onValueChange = { onNameChange(it.take(MAX_USER_NAME_LENGTH)) },
        placeholder = stringResource(R.string.onboarding_name_label),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
    )
}

/** 2 · Şafak: ihtiyaç. */
@Composable
internal fun StepNeed(options: List<NeedOption>, selected: NeedOption, onSelect: (NeedOption) -> Unit) {
    StepHeading(stringResource(R.string.onboarding_p1_title), stringResource(R.string.onboarding_p1_subtitle))
    options.forEach { need ->
        ChoiceCard(
            icon = need.icon,
            title = stringResource(need.titleRes),
            subtitle = stringResource(need.subtitleRes),
            selected = need == selected,
            onClick = { onSelect(need) },
        )
    }
}

/** 3 · Sabah: tempo. */
@Composable
internal fun StepTempo(options: List<IntensityOption>, selected: IntensityOption, onSelect: (IntensityOption) -> Unit) {
    StepHeading(stringResource(R.string.onboarding_p2_title), stringResource(R.string.onboarding_p2_subtitle))
    options.forEach { option ->
        ChoiceCard(
            icon = option.icon,
            title = stringResource(option.titleRes),
            subtitle = stringResource(option.subtitleRes),
            selected = option == selected,
            onClick = { onSelect(option) },
            badge = if (option.recommended) stringResource(R.string.onboarding_recommended) else null,
        )
    }
}

/** 4 · Öğleye doğru: ilk adımlar (rutinler + ilk görev), yıldız satırları. */
@Composable
internal fun StepFirstSteps(
    suggestions: List<Pair<SuggestedRoutine, String>>,
    selectedNames: Set<String>,
    onToggleRoutine: (String) -> Unit,
    taskTitle: String,
    taskSelected: Boolean,
    onToggleTask: () -> Unit,
) {
    StepHeading(stringResource(R.string.onboarding_routines_title), stringResource(R.string.onboarding_routines_subtitle))
    suggestions.forEach { (suggestion, name) ->
        StarSuggestionRow(
            name = name,
            icon = suggestion.icon,
            selected = name in selectedNames,
            onToggle = { onToggleRoutine(name) },
            badge = stringResource(R.string.today_starter_routine),
        )
    }
    StarSuggestionRow(
        name = taskTitle,
        icon = Icons.Rounded.EditNote,
        selected = taskSelected,
        onToggle = onToggleTask,
        badge = stringResource(R.string.today_starter_task),
    )
}

/** 5 · Öğle: gökyüzü hazır; özet + nazik bildirim izni. */
@Composable
internal fun StepReady(
    userName: String,
    routineCount: Int,
    taskCount: Int,
    notifGranted: Boolean,
    notifSkipped: Boolean,
    onGrant: () -> Unit,
    onSkipNotif: () -> Unit,
) {
    val name = userName.trim()
    StepHeading(
        title = if (name.isEmpty()) stringResource(R.string.onboarding_ready_title) else stringResource(R.string.onboarding_ready_title_named, name),
        body = stringResource(R.string.onboarding_ready_body, routineCount, taskCount),
    )
    AppSurface(radius = AppTokens.Radius.lg, padding = AppTokens.Spacing.md) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm)) {
            Icon(Icons.Rounded.Notifications, contentDescription = null, tint = AppTheme.colors.progress.doneInk, modifier = Modifier.size(AppTokens.IconSize.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.onboarding_notif_soft_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(stringResource(R.string.onboarding_notif_soft_body), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        when {
            notifGranted -> Row(
                modifier = Modifier.padding(top = AppTokens.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = AppTheme.colors.status.success.fg, modifier = Modifier.size(AppTokens.IconSize.sm))
                Text(
                    stringResource(R.string.onboarding_notif_granted_badge),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = AppTheme.colors.status.success.fg,
                )
            }
            !notifSkipped -> Row(
                modifier = Modifier.fillMaxWidth().padding(top = AppTokens.Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onSkipNotif) {
                    Text(stringResource(R.string.onboarding_notif_later), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SheetSaveButton(text = stringResource(R.string.onboarding_notif_grant), onClick = onGrant, modifier = Modifier.weight(1f))
            }
        }
    }
}
