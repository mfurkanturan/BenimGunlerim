package com.benimgunlerim.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.organisms.TaskRow
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun TaskListContainer(
    tasks: List<TodayTaskUi>,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onReorder: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    isEditing: Boolean = false,
    onOpenTask: ((String) -> Unit)? = null,
) {
    if (tasks.isEmpty()) return

    val rows = @Composable {
        tasks.forEachIndexed { index, task ->
            key(task.id) {
                TaskRow(
                    title = task.title,
                    isCompleted = task.isCompleted,
                    onToggleComplete = { onToggleTask(task.id) },
                    onClick = onOpenTask?.let { open -> { open(task.id) } },
                    onDelete = { onDeleteTask(task.id) },
                    dueTime = task.startTime,
                    priority = task.priority,
                    category = task.category,
                    useSurface = true,
                    showDelete = false,
                    showDragHandle = isEditing,
                    dragHandleModifier = if (isEditing) {
                        todayReorderModifier(task, tasks, onReorder)
                    } else {
                        Modifier
                    },
                )
                if (index != tasks.lastIndex) Spacer(Modifier.size(AppTokens.Spacing.xxs))
            }
        }
    }

    if (tasks.size == 1) {
        val task = tasks[0]
        key(task.id) {
            TaskRow(
                modifier = modifier,
                title = task.title,
                isCompleted = task.isCompleted,
                onToggleComplete = { onToggleTask(task.id) },
                onClick = onOpenTask?.let { open -> { open(task.id) } },
                onDelete = { onDeleteTask(task.id) },
                dueTime = task.startTime,
                priority = task.priority,
                category = task.category,
                showDelete = false,
                showDragHandle = isEditing,
                dragHandleModifier = if (isEditing) {
                    todayReorderModifier(task, tasks, onReorder)
                } else {
                    Modifier
                },
            )
        }
    } else {
        Column(modifier = modifier) { rows() }
    }
}

@Composable
private fun todayReorderModifier(
    task: TodayTaskUi,
    tasks: List<TodayTaskUi>,
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

