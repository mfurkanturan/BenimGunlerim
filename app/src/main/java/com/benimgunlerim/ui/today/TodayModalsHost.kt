package com.benimgunlerim.ui.today

import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.ui.components.calm.BrainDumpDialog
import com.benimgunlerim.ui.components.calm.ResetDialog
import com.benimgunlerim.ui.components.calm.ResetCandidate
import com.benimgunlerim.ui.components.gamification.AchievementDialog
import com.benimgunlerim.ui.components.gamification.BlockCompletionSheet
import com.benimgunlerim.ui.components.gamification.LevelUpDialog
import com.benimgunlerim.ui.components.organisms.AddRoutineSheet
import com.benimgunlerim.ui.components.organisms.AddTaskSheet
import com.benimgunlerim.ui.components.organisms.RoutineActionsSheet
import com.benimgunlerim.ui.components.organisms.RoutineArchiveConfirmDialog
import java.time.LocalDate

/**
 * Hosts every dialog/bottom-sheet/modal that [TodayScreen] can show, driven by
 * hoisted [TodayModalStates] so this file owns no state of its own.
 */
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
internal fun TodayModalsHost(
    state: TodayUiState,
    today: LocalDate,
    viewModel: TodayViewModel,
    completedCount: Int,
    totalCount: Int,
    modals: TodayModalStates,
) {
    TaskDetailHost(state, today, viewModel, modals)

    if (modals.showFabMenu.value) {
        TodayFabMenuSheet(
            isLightDayMode = state.isLightDayMode,
            onDismiss = { modals.showFabMenu.value = false },
            onAddTaskClick = {
                modals.showFabMenu.value = false
                modals.showAddTaskSheet.value = true
            },
            onAddRoutineClick = {
                modals.showFabMenu.value = false
                modals.showAddRoutineSheet.value = true
            },
            onBrainDumpClick = {
                modals.showFabMenu.value = false
                modals.showBrainDumpDialog.value = true
            },
            onResetClick = {
                modals.showFabMenu.value = false
                modals.showResetDialog.value = true
            },
            onToggleLightDayClick = {
                modals.showFabMenu.value = false
                viewModel.toggleLightDayMode(!state.isLightDayMode)
            },
        )
    }

    if (modals.showResetDialog.value) {
        // Rutin ve görev dönüşümlü: ilk üç öneride ikisi de görünsün.
        val openRoutines = state.routines.filterNot { it.id in state.completedRoutineIds }.map { ResetCandidate(it.id, it.name, isRoutine = true) }
        val openTasks = state.tasks.filterNot { it.isCompleted }.map { ResetCandidate(it.id, it.title, isRoutine = false) }
        val candidates = (0 until maxOf(openRoutines.size, openTasks.size)).flatMap { i ->
            listOfNotNull(openRoutines.getOrNull(i), openTasks.getOrNull(i))
        }
        ResetDialog(
            onDismiss = { modals.showResetDialog.value = false },
            onEnableLightDay = { viewModel.toggleLightDayMode(true) },
            candidates = candidates,
            onPickStep = { modals.focusStep.value = it },
            isLightDay = state.isLightDayMode,
        )
    }

    if (modals.showBrainDumpDialog.value) {
        BrainDumpDialog(
            onDismiss = { modals.showBrainDumpDialog.value = false },
            onAddTasks = { titles -> viewModel.addTasksFromBrainDump(titles) },
        )
    }

    if (modals.showAddTaskSheet.value) {
        AddTaskSheet(
            onDismiss = { modals.showAddTaskSheet.value = false },
            onSave = { title, _, startTime, category, priority, reminderTime ->
                viewModel.addTask(title, null, today, startTime, category, priority, reminderTime)
            },
            initialDate = today.toString(),
        )
    }

    if (modals.showCloseSheet.value) {
        CloseSheetModal(
            completedCount = completedCount,
            totalCount = totalCount,
            overdueCount = state.overdueTasks.size,
            onDismiss = { modals.showCloseSheet.value = false },
            onSave = { draft ->
                viewModel.saveDailySummaryWithOptionalCarry(draft)
                modals.showCloseSheet.value = false
            },
        )
    }

    if (modals.showMissedDaySheet.value && state.missedDay != null) {
        val missed = state.missedDay
        MissedDayReviewSheet(
            date = missed,
            completedCount = state.missedDayCompletedCount,
            totalCount = state.missedDayTotalCount,
            pendingTaskCount = state.missedDayPendingTaskCount,
            onDismiss = { modals.showMissedDaySheet.value = false },
            onCloseAndStart = { mood, carryOver ->
                viewModel.closeMissedDayWithReview(date = missed, mood = mood, carryOverPendingTasks = carryOver)
                modals.showMissedDaySheet.value = false
            },
            onArchiveAsIs = {
                viewModel.autoSaveMissedDay(missed)
                modals.showMissedDaySheet.value = false
            },
        )
    }

    modals.menuRoutineId.value?.let { routineId ->
        RoutineActionsSheet(
            onDismiss = { modals.menuRoutineId.value = null },
            onEdit = {
                modals.menuRoutineId.value = null
                modals.editingRoutine.value = state.routines.firstOrNull { it.id == routineId }
            },
            onSkip = {
                viewModel.skipRoutine(routineId)
                modals.menuRoutineId.value = null
            },
            onDelete = {
                modals.archiveRoutineId.value = routineId
                modals.menuRoutineId.value = null
            },
        )
    }

    modals.archiveRoutineId.value?.let { routineId ->
        RoutineArchiveConfirmDialog(
            onDismiss = { modals.archiveRoutineId.value = null },
            onConfirm = {
                viewModel.archiveRoutine(routineId)
                modals.archiveRoutineId.value = null
            },
        )
    }

    modals.deleteTaskId.value?.let { taskId ->
        val task = state.tasks.firstOrNull { it.id == taskId }
            ?: state.overdueTasks.firstOrNull { it.id == taskId }
        if (task != null) {
            TaskDeleteConfirmDialog(
                taskTitle = task.title,
                onDismiss = { modals.deleteTaskId.value = null },
                onConfirm = {
                    viewModel.deleteTask(taskId)
                    modals.deleteTaskId.value = null
                },
            )
        } else {
            modals.deleteTaskId.value = null
        }
    }

    if (modals.showAddRoutineSheet.value) {
        AddRoutineSheet(
            onDismiss = { modals.showAddRoutineSheet.value = false },
            onSave = { title, targetDays, reminderTime, category, targetCount, unit ->
                viewModel.addRoutine(title, targetDays, reminderTime, targetCount, unit, category)
            },
        )
    }

    modals.editingRoutine.value?.let { routine ->
        AddRoutineSheet(
            onDismiss = { modals.editingRoutine.value = null },
            onSave = { title, targetDays, reminderTime, _, _, _ ->
                viewModel.updateRoutine(routine.id, title, targetDays, reminderTime)
            },
            isEditMode = true,
            initialTitle = routine.name,
            initialDays = routine.targetDays,
            initialReminderTime = routine.preferredTime,
        )
    }

    if (state.gameState.celebrationEffectsEnabled) modals.levelUpEvent.value?.let { ev ->
        LevelUpDialog(
            level = ev.level,
            title = stringResource(ev.titleRes),
            xpBonus = ev.xpBonus,
            onDismiss = { modals.levelUpEvent.value = null },
        )
    }

    if (state.gameState.celebrationEffectsEnabled) modals.achievementEvent.value?.let { ev ->
        AchievementDialog(
            emoji = ev.emoji,
            title = stringResource(ev.titleRes),
            description = stringResource(ev.descriptionRes),
            xpReward = ev.xpReward,
            showParticles = true,
            onDismiss = { modals.achievementEvent.value = null },
        )
    }

    if (state.gameState.celebrationEffectsEnabled) modals.blockCompletionData.value?.let { (title, subtitle, badge) ->
        BlockCompletionSheet(
            title = title,
            subtitle = subtitle,
            badgeText = badge,
            isLightDayMode = state.isLightDayMode,
            onCloseDayClick = {
                modals.blockCompletionData.value = null
                modals.showCloseSheet.value = true
            },
            onContinueClick = { modals.blockCompletionData.value = null },
            onDismiss = { modals.blockCompletionData.value = null },
        )
    }
}

/** Göreve dokununca açılan ayrıntı sayfası: başlık, not, tarih, saat, öncelik, alt görevler. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun TaskDetailHost(state: TodayUiState, today: LocalDate, viewModel: TodayViewModel, modals: TodayModalStates) {
    val taskId = modals.openTaskId.value ?: return
    val task = (state.tasks + state.overdueTasks).firstOrNull { it.id == taskId }
    if (task == null) {
        modals.openTaskId.value = null
        return
    }
    val subtasks by viewModel.subTasksFlow(task.id).collectAsState(initial = emptyList())
    val close = { modals.openTaskId.value = null }
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = close,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        TaskDetailSheet(
            task = task,
            today = today,
            subtasks = subtasks,
            interactionLocked = state.todayState?.closedAt != null,
            callbacks = TaskDetailCallbacks(
                onSave = { title, note, date, startTime, category, priority, reminderTime ->
                    viewModel.updateTask(task.id, title, note, date, startTime, category, priority, reminderTime)
                    close()
                },
                onMoveTomorrow = {
                    viewModel.moveTaskToTomorrow(task.id)
                    close()
                },
                onDelete = {
                    // Ayrıntı sayfası silmeyi zaten onaylattı; ikinci kez sorma.
                    close()
                    viewModel.deleteTask(task.id)
                },
                onAddSubTask = { viewModel.addSubTask(task.id, it) },
                onToggleSubTask = { viewModel.toggleSubTask(it) },
                onDeleteSubTask = { viewModel.deleteSubTask(it) },
            ),
        )
    }
}
