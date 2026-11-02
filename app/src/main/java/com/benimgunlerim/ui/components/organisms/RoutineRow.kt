package com.benimgunlerim.ui.components.organisms

import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.core.StarToggle
import com.benimgunlerim.ui.theme.AppTokens
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Rutin satırı: yanan/sönük yıldız + son 7 günün ışık zinciri (her nokta bir gün, bugün halkalı).
 * Sayılı hedeflerde −/+ ile ilerleme. State dışarıdan gelir, aksiyonlar lambda ile yukarı gider.
 */
@Composable
fun RoutineRow(
    title: String,
    isCompletedToday: Boolean,
    onToggle: () -> Unit,
    weekHistory: List<Boolean>,
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.progress.done,
    streakCount: Int = 0,
    preferredTime: String? = null,
    targetDaysSummary: String? = null,
    targetDays: Set<DayOfWeek> = emptySet(),
    category: String? = null,
    targetType: String = "check",
    targetValue: Int? = null,
    targetUnit: String? = null,
    currentValue: Float = 0f,
    onProgressChange: ((Float) -> Unit)? = null,
    useSurface: Boolean = true,
    onClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val handleToggle = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onToggle()
    }
    val effectiveClick = onClick ?: handleToggle
    val isCounted = targetType == "goal" && targetValue != null && targetValue > 1

    val rowContent = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AppTokens.Layout.fabDefaultSize)
                .padding(
                    vertical = AppTokens.Spacing.xs,
                    horizontal = if (useSurface) AppTokens.Spacing.xs else AppTokens.Spacing.xxs,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
        ) {
            StarToggle(
                lit = isCompletedToday,
                onToggle = handleToggle,
                stateLabel = stringResource(if (isCompletedToday) R.string.star_state_lit else R.string.star_state_idle),
                actionLabel = stringResource(
                    if (isCompletedToday) R.string.routine_row_mark_incomplete_cd else R.string.routine_row_mark_complete_cd,
                ),
                litColor = color,
                edgeColor = AppTheme.colors.progress.doneEdge,
                idleColor = AppTheme.colors.progress.idle,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val repeatDesc = targetDaysSummary ?: stringResource(R.string.today_routine_daily_short)
                Text(
                    text = listOfNotNull(preferredTime, repeatDesc).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (weekHistory.isNotEmpty()) {
                    LightChain(
                        history = weekHistory.takeLast(7),
                        targetDays = targetDays,
                        litColor = color,
                        streakCount = streakCount,
                    )
                }
                if (isCounted) {
                    CountStepper(
                        current = currentValue.toInt(),
                        target = targetValue ?: 1,
                        unit = targetUnit,
                        onProgressChange = onProgressChange?.let { change ->
                            { next: Float ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                change(next)
                            }
                        },
                    )
                }
            }

            IconButton(
                onClick = { onMenuClick?.invoke() ?: (onClick?.invoke() ?: handleToggle()) },
                modifier = Modifier.size(AppTokens.TouchTarget.min),
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.routine_row_details_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(AppTokens.IconSize.sm),
                )
            }
        }
    }

    if (useSurface) {
        AppSurface(
            modifier = modifier.fillMaxWidth().clickable(onClick = effectiveClick),
            radius = AppTokens.Radius.lg,
            padding = AppTokens.Spacing.none,
        ) { rowContent() }
    } else {
        Row(modifier = modifier.fillMaxWidth().clickable(onClick = effectiveClick)) { rowContent() }
    }
}

@Composable
private fun LightChain(
    history: List<Boolean>,
    targetDays: Set<DayOfWeek>,
    litColor: Color,
    streakCount: Int,
) {
    val today = remember { LocalDate.now() }
    val doneCount = history.count { it }
    val chainCd = stringResource(R.string.routine_chain_cd, doneCount)
    Row(
        modifier = Modifier.clearAndSetSemantics { contentDescription = chainCd },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Star.chainGap),
    ) {
        history.forEachIndexed { index, done ->
            val date = today.minusDays((history.lastIndex - index).toLong())
            val isTargetDay = targetDays.isEmpty() || date.dayOfWeek in targetDays
            val isToday = index == history.lastIndex
            val dot = if (isToday) AppTokens.Star.chainToday else AppTokens.Star.chainDot
            Box(
                modifier = Modifier
                    .size(dot)
                    .clip(CircleShape)
                    .then(
                        when {
                            done -> Modifier.background(litColor)
                            isToday -> Modifier.border(AppTokens.Star.stroke, litColor, CircleShape)
                            isTargetDay -> Modifier.background(MaterialTheme.colorScheme.outlineVariant)
                            else -> Modifier.border(AppTokens.Star.stroke, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        },
                    ),
            )
        }
        if (streakCount > 0) {
            Text(
                text = stringResource(R.string.routine_chain_label, streakCount),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = AppTheme.colors.progress.doneInk,
                modifier = Modifier.padding(start = AppTokens.Spacing.xxs),
            )
        }
    }
}

@Composable
private fun CountStepper(
    current: Int,
    target: Int,
    unit: String?,
    onProgressChange: ((Float) -> Unit)?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
    ) {
        if (onProgressChange != null) {
            IconButton(
                onClick = { onProgressChange(maxOf(0, current - 1).toFloat()) },
                enabled = current > 0,
                modifier = Modifier.size(AppTokens.TouchTarget.compact),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = stringResource(R.string.routine_row_decrement_cd),
                    modifier = Modifier.size(AppTokens.IconSize.xs),
                )
            }
        }
        Text(
            text = stringResource(R.string.step_routine_count, current, target, unit.orEmpty()).trim(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(AppTokens.Radius.pill))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = AppTokens.Spacing.xs, vertical = AppTokens.Spacing.xxs),
        )
        if (onProgressChange != null) {
            IconButton(
                onClick = { onProgressChange((current + 1).toFloat()) },
                modifier = Modifier.size(AppTokens.TouchTarget.compact),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.routine_row_increment_cd),
                    modifier = Modifier.size(AppTokens.IconSize.xs),
                )
            }
        }
    }
}
