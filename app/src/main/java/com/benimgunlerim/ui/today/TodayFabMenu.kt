package com.benimgunlerim.ui.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.molecules.SheetSectionLabel
import com.benimgunlerim.ui.components.molecules.SheetTitle
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens

/**
 * Hızlı menü: önce "ekle" (görev, rutin), sonra "sakinleş" (kafam dolu, biraz dur, hafiflet).
 * Sayfa sistem gezinme çubuğunun üstünde biter; uzun ekranda kaydırılır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayFabMenuSheet(
    isLightDayMode: Boolean,
    onDismiss: () -> Unit,
    onAddTaskClick: () -> Unit,
    onAddRoutineClick: () -> Unit,
    onBrainDumpClick: () -> Unit,
    onResetClick: () -> Unit,
    onToggleLightDayClick: () -> Unit,
) {
    val progress = AppTheme.colors.progress
    val night = AppTheme.colors.night
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTokens.Spacing.xl)
                .padding(bottom = AppTokens.Spacing.md)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
        ) {
            SheetTitle(stringResource(R.string.quick_actions_title))
            SheetSectionLabel(stringResource(R.string.quick_actions_add_label), Modifier.padding(top = AppTokens.Spacing.xs))
            QuickActionRow(
                title = stringResource(R.string.quick_action_add_task_title),
                subtitle = stringResource(R.string.quick_action_add_task_subtitle),
                icon = Icons.Rounded.Add,
                iconTint = MaterialTheme.colorScheme.primary,
                iconBg = MaterialTheme.colorScheme.primaryContainer,
                onClick = onAddTaskClick,
            )
            QuickActionRow(
                title = stringResource(R.string.quick_action_add_routine_title),
                subtitle = stringResource(R.string.quick_action_add_routine_subtitle),
                icon = Icons.Rounded.Repeat,
                iconTint = progress.doneInk,
                iconBg = progress.soft,
                onClick = onAddRoutineClick,
            )
            SheetSectionLabel(stringResource(R.string.quick_actions_calm_label), Modifier.padding(top = AppTokens.Spacing.sm))
            QuickActionRow(
                title = stringResource(R.string.braindump_title),
                subtitle = stringResource(R.string.quick_action_braindump_subtitle),
                icon = Icons.Rounded.Psychology,
                iconTint = AppTheme.colors.status.info.fg,
                iconBg = AppTheme.colors.status.info.container,
                onClick = onBrainDumpClick,
            )
            QuickActionRow(
                title = stringResource(R.string.reset_title),
                subtitle = stringResource(R.string.quick_action_reset_subtitle),
                icon = Icons.Rounded.SelfImprovement,
                iconTint = night.moon,
                iconBg = night.sky,
                onClick = onResetClick,
            )
            QuickActionRow(
                title = stringResource(if (isLightDayMode) R.string.light_day_disable_btn else R.string.light_day_title),
                subtitle = stringResource(
                    if (isLightDayMode) R.string.quick_action_light_day_on_subtitle else R.string.quick_action_light_day_off_subtitle,
                ),
                icon = Icons.Rounded.Spa,
                badge = if (isLightDayMode) stringResource(R.string.quick_action_active_badge) else null,
                iconTint = AppTheme.colors.status.attention.fg,
                iconBg = AppTheme.colors.status.attention.container,
                onClick = onToggleLightDayClick,
            )
        }
    }
}

@Composable
private fun QuickActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    badge: String? = null,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppTokens.Radius.lg),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(AppTokens.BorderWidth.thin, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().heightIn(min = AppTokens.Layout.fabDefaultSize),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
        ) {
            Box(
                modifier = Modifier.size(AppTokens.TouchTarget.compact).background(iconBg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(AppTokens.IconSize.sm))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (badge != null) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = AppTheme.colors.progress.onSoft,
                            modifier = Modifier
                                .background(AppTheme.colors.progress.soft, RoundedCornerShape(AppTokens.Radius.pill))
                                .padding(horizontal = AppTokens.Spacing.xs, vertical = AppTokens.Spacing.xxs),
                        )
                    }
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
