package com.benimgunlerim.ui.today

import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppDivider
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.core.StarToggle
import com.benimgunlerim.ui.theme.AppTokens

/**
 * Bugünün rutinleri: tek kart, dikey; her satırda yanan/sönük yıldız. Tamamlanan rutin yerinde kalır,
 * sadece yıldızı yanar. Sayılı rutinde (örn. 2 bardak su) her dokunuş bir adım ilerletir.
 */
@Composable
fun RoutineStepList(
    routines: List<TodayRoutineUi>,
    completedRoutineIds: Set<String>,
    onStep: (String) -> Unit,
    onOpenMenu: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (routines.isEmpty()) return
    AppSurface(
        modifier = modifier,
        radius = AppTokens.Radius.lg,
        padding = AppTokens.Spacing.none,
    ) {
        Column {
            routines.forEachIndexed { index, routine ->
                key(routine.id) {
                    if (index > 0) AppDivider()
                    RoutineStepRow(
                        routine = routine,
                        lit = routine.id in completedRoutineIds,
                        onStep = { onStep(routine.id) },
                        onOpenMenu = { onOpenMenu(routine.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineStepRow(
    routine: TodayRoutineUi,
    lit: Boolean,
    onStep: () -> Unit,
    onOpenMenu: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val target = routine.targetValue ?: 1
    val isCounted = target > 1
    val current = routine.currentValue.toInt().coerceAtMost(target)
    val step = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onStep()
    }
    val meta = when {
        isCounted && !routine.targetUnit.isNullOrBlank() ->
            stringResource(R.string.step_routine_count, current, target, routine.targetUnit)
        lit -> stringResource(R.string.step_routine_meta_lit)
        else -> stringResource(R.string.step_routine_meta)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AppTokens.Layout.fabDefaultSize)
            .clickable(onClick = step)
            .padding(start = AppTokens.Spacing.xxs, end = AppTokens.Spacing.xxs, top = AppTokens.Spacing.xxs, bottom = AppTokens.Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
    ) {
        StarToggle(
            lit = lit,
            onToggle = step,
            stateLabel = stringResource(if (lit) R.string.star_state_lit else R.string.star_state_idle),
            actionLabel = stringResource(
                if (lit) R.string.routine_step_untoggle_cd else R.string.routine_step_toggle_cd,
                routine.name,
            ),
            litColor = AppTheme.colors.progress.done,
            edgeColor = AppTheme.colors.progress.doneEdge,
            idleColor = AppTheme.colors.progress.idle,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = routine.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = meta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (isCounted && routine.targetUnit.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.step_routine_count_short, current, target),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(AppTokens.Radius.pill))
                    .padding(horizontal = AppTokens.Spacing.xs, vertical = AppTokens.Spacing.xxs),
            )
        }
        IconButton(onClick = onOpenMenu, modifier = Modifier.size(AppTokens.TouchTarget.min)) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.routine_more_cd, routine.name),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(AppTokens.IconSize.sm),
            )
        }
    }
}
