package com.benimgunlerim.ui.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.onboarding.DayStarter
import com.benimgunlerim.ui.onboarding.OnboardingSuggestions
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens

/**
 * Tamamen boş gün: boşluk listesi yerine tek bir davet. Mino + tek dokunuşla eklenen hazır başlangıçlar
 * + "Rutin ekle / Görev ekle" seçimi (fark alt satırda anlatılır).
 */
@Composable
internal fun TodayEmptyInvite(
    onPickStarter: (DayStarter) -> Unit,
    onAddRoutine: () -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm)) {
        AppSurface(radius = AppTokens.Radius.lg, padding = AppTokens.Spacing.md) {
            Column {
                Text(
                    text = stringResource(R.string.today_invite_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.today_invite_mino),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier.padding(top = AppTokens.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
            ) {
                OnboardingSuggestions.dayStarters().forEach { starter ->
                    StarterRow(starter = starter, onClick = { onPickStarter(starter) })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
            ChoiceTile(
                icon = Icons.Rounded.Repeat,
                tint = AppTheme.colors.progress.doneInk,
                title = stringResource(R.string.today_choice_routine_title),
                subtitle = stringResource(R.string.today_choice_routine_sub),
                onClick = onAddRoutine,
                modifier = Modifier.weight(1f),
            )
            val taskCd = stringResource(R.string.today_add_task_fab_cd)
            ChoiceTile(
                icon = Icons.Rounded.CheckCircleOutline,
                tint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.today_choice_task_title),
                subtitle = stringResource(R.string.today_choice_task_sub),
                onClick = onAddTask,
                modifier = Modifier
                    .weight(1f)
                    .testTag(TestTags.TodayFab)
                    .semantics { contentDescription = taskCd },
            )
        }
    }
}

@Composable
private fun StarterRow(starter: DayStarter, onClick: () -> Unit) {
    val name = stringResource(starter.nameRes)
    val kind = stringResource(if (starter.isRoutine) R.string.today_starter_routine else R.string.today_starter_task)
    val cd = stringResource(R.string.today_starter_add_cd, name, kind)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppTokens.Radius.md),
        color = Color.Transparent,
        border = BorderStroke(AppTokens.BorderWidth.thin, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AppTokens.TouchTarget.min)
            .clearAndSetSemantics { contentDescription = cd },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppTokens.Spacing.sm, vertical = AppTokens.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
        ) {
            Icon(starter.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AppTokens.IconSize.sm))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(text = kind, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.Rounded.Add, contentDescription = null, tint = AppTheme.colors.progress.doneInk, modifier = Modifier.size(AppTokens.IconSize.sm))
        }
    }
}

@Composable
private fun ChoiceTile(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppSurface(modifier = modifier, radius = AppTokens.Radius.lg, padding = AppTokens.Spacing.sm, onClick = onClick) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(AppTokens.IconSize.md))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = AppTokens.Spacing.xxs),
        )
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
