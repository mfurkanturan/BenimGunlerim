package com.benimgunlerim.ui.components.organisms

import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppBadge
import com.benimgunlerim.ui.components.core.AppBadgeVariant
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.core.StarToggle
import com.benimgunlerim.ui.theme.AppTokens

private const val PRIORITY_HIGH = 1

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskRow(
    title: String,
    isCompleted: Boolean,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    dueTime: String? = null,
    priority: Int = 2,
    category: String? = null,
    /** Başlığın altında tek satır gösterilir (görev notu). */
    note: String? = null,
    /** Geciken görevde ne zamandan kaldığı ("2 gün önceden"); dikkat rengiyle yazılır. */
    ageLabel: String? = null,
    tags: List<String> = emptyList(),
    useSurface: Boolean = true,
    showDelete: Boolean = true,
    showDragHandle: Boolean = false,
    dragHandleModifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    val textAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.5f else 1.0f,
        animationSpec = tween(durationMillis = 160),
        label = "task_text_alpha",
    )

    val handleToggle = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onToggleComplete()
    }

    val effectiveClick = onClick ?: handleToggle

    val rowContent = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AppTokens.Layout.fabDefaultSize)
                .padding(
                    vertical = AppTokens.Spacing.xxs,
                    horizontal = if (useSurface) AppTokens.Spacing.xs else AppTokens.Spacing.none,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
        ) {
            StarToggle(
                lit = isCompleted,
                onToggle = handleToggle,
                stateLabel = stringResource(if (isCompleted) R.string.star_state_lit else R.string.star_state_idle),
                actionLabel = stringResource(
                    if (isCompleted) R.string.today_task_uncomplete_cd else R.string.today_task_toggle_cd,
                ),
                litColor = AppTheme.colors.progress.done,
                edgeColor = AppTheme.colors.progress.doneEdge,
                idleColor = AppTheme.colors.progress.idle,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.none),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.ExtraBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!note.isNullOrBlank()) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Yüksek öncelik yalnızca bitmemiş görevde öne çıkar; normal/düşük için işaret yok.
                val showPriority = priority == PRIORITY_HIGH && !isCompleted
                if (dueTime != null || category != null || tags.isNotEmpty() || showPriority || ageLabel != null) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
                    ) {
                        if (ageLabel != null) {
                            Text(
                                text = ageLabel,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = AppTheme.colors.status.attention.fg,
                            )
                        }

                        if (showPriority) {
                            AppBadge(
                                text = stringResource(R.string.today_priority_badge_high),
                                variant = AppBadgeVariant.Warning,
                            )
                        }

                        if (dueTime != null) {
                            Text(
                                text = dueTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                            )
                        }

                        if (category != null) {
                            AppBadge(
                                text = category,
                                variant = AppBadgeVariant.Neutral,
                            )
                        }

                        tags.take(2).forEach { tag ->
                            AppBadge(
                                text = tag,
                                variant = AppBadgeVariant.Neutral,
                            )
                        }
                    }
                }
            }

            if (showDragHandle) {
                Icon(
                    imageVector = Icons.Rounded.DragHandle,
                    contentDescription = stringResource(R.string.task_drag_handle_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = dragHandleModifier
                        .padding(end = AppTokens.Spacing.xs)
                        .size(AppTokens.IconSize.md),
                )
            }

            if (showDelete) {
                IconButton(onClick = onDelete, modifier = Modifier.size(AppTokens.TouchTarget.min)) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.today_task_delete_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppTokens.IconSize.sm),
                    )
                }
            }
        }
    }

    if (useSurface) {
        AppSurface(
            modifier = modifier.clickable(onClick = effectiveClick),
            radius = AppTokens.Radius.lg,
            padding = AppTokens.Spacing.none,
        ) {
            rowContent()
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = effectiveClick),
        ) {
            rowContent()
        }
    }
}
