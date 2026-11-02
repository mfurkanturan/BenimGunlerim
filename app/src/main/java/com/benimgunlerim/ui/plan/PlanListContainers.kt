package com.benimgunlerim.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppDivider
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.organisms.SwipeToDelete
import com.benimgunlerim.ui.components.organisms.TaskRow
import com.benimgunlerim.ui.theme.AppTokens
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Günün görev listesi. Silme satırı sola kaydırarak (geri alma snackbar'da); satırda yalnızca
 * sıralama tutamacı kalır. [today] verilirse geçmiş tarihli görevlerde "ne zamandan kaldı" yazar.
 */
@Composable
fun PlanTaskListContainer(
    tasks: List<PlanTaskUi>,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onReorder: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate? = null,
) {
    if (tasks.isEmpty()) return

    AppSurface(radius = AppTokens.Radius.lg, padding = AppTokens.Spacing.none, modifier = modifier) {
        Column {
            tasks.forEachIndexed { index, task ->
                key(task.id) {
                    if (index > 0) AppDivider()
                    SwipeToDelete(onDelete = { onDeleteTask(task.id) }) {
                        TaskRow(
                            title = task.title,
                            isCompleted = task.isCompleted,
                            onToggleComplete = { onToggleTask(task.id) },
                            onDelete = { onDeleteTask(task.id) },
                            dueTime = task.startTime,
                            priority = task.priority,
                            category = task.category,
                            note = task.note,
                            ageLabel = today?.let { ageLabel(task, it) },
                            useSurface = false,
                            showDelete = false,
                            showDragHandle = tasks.size > 1,
                            // Kaydırırken arkadaki kırmızı zemin satırın içinden görünmesin.
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            dragHandleModifier = reorderModifier(task, tasks, onReorder),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ageLabel(task: PlanTaskUi, today: LocalDate): String? {
    val planned = task.plannedDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
    val days = ChronoUnit.DAYS.between(planned, today).toInt()
    return when {
        days <= 0 -> null
        days == 1 -> stringResource(R.string.plan_task_age_yesterday)
        else -> pluralStringResource(R.plurals.plan_task_age_days, days, days)
    }
}

@Composable
private fun reorderModifier(
    task: PlanTaskUi,
    tasks: List<PlanTaskUi>,
    onReorder: (List<String>) -> Unit,
): Modifier {
    val thresholdPx = with(LocalDensity.current) { AppTokens.TouchTarget.min.toPx() }
    var accumulatedDrag by remember(task.id, tasks.map { it.id }) { mutableFloatStateOf(0f) }

    return Modifier.pointerInput(task.id, tasks.map { it.id }) {
        detectDragGesturesAfterLongPress(
            onDragStart = { accumulatedDrag = 0f },
            onDragCancel = { accumulatedDrag = 0f },
            onDragEnd = { accumulatedDrag = 0f },
        ) { _, drag ->
            accumulatedDrag += drag.y
            val from = tasks.indexOfFirst { it.id == task.id }
            val direction = when {
                accumulatedDrag >= thresholdPx -> 1
                accumulatedDrag <= -thresholdPx -> -1
                else -> 0
            }
            val to = (from + direction).coerceIn(0, tasks.lastIndex)
            if (direction != 0 && from != to) {
                val reordered = tasks.toMutableList().apply { add(to, removeAt(from)) }
                onReorder(reordered.map { it.id })
                accumulatedDrag = 0f
            }
        }
    }
}
