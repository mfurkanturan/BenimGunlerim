@file:Suppress("SpellCheckingInspection", "LongMethod")

package com.benimgunlerim.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.BuildConfig
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.DebugSkyPhase
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.layout.ScreenScaffold
import com.benimgunlerim.ui.components.molecules.DashedAddRow
import com.benimgunlerim.ui.components.molecules.ScreenTitle
import com.benimgunlerim.ui.components.organisms.AddTaskSheet
import com.benimgunlerim.ui.components.organisms.WeekPicker
import com.benimgunlerim.ui.theme.AppTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PlanScreen(
    viewModel: PlanViewModel = hiltViewModel(),
    onNavigateToToday: () -> Unit = {},
) {
    val rawState by viewModel.uiState.collectAsState()
    val state = if (BuildConfig.DEBUG && DebugSkyPhase.forceSamplePlan) rawState.withDebugSample() else rawState
    var showAddSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val undoLabel = stringResource(R.string.action_undo)
    val taskDeletedMsg = stringResource(R.string.plan_task_deleted_undo)
    val dayFormatter = remember { DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("tr")) }
    val today = remember { LocalDate.now() }

    LaunchedEffect(viewModel) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is PlanUiEffect.TaskDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = taskDeletedMsg,
                        actionLabel = undoLabel,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreDeletedTask(effect.taskId)
                    }
                }
                else -> Unit
            }
        }
    }

    ScreenScaffold(
        // Ekleme butonu listenin sonunda (kesik çizgili satır): yüzen buton satırların üstüne biniyordu.
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTags.PlanRoot),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.md),
        ) {
            item(key = "title") { ScreenTitle(title = stringResource(R.string.plan_title)) }

            item(key = "week_picker") {
                WeekPicker(
                    selectedDate = state.selectedDate,
                    onDateSelected = { date -> viewModel.selectDate(date) },
                    onPreviousWeek = { viewModel.selectPreviousWeek() },
                    onNextWeek = { viewModel.selectNextWeek() },
                    onSelectToday = { viewModel.selectToday() },
                    taskCountByDate = state.weeklyTaskCounts,
                    weekStart = state.weekStart,
                    modifier = Modifier.testTag(TestTags.PlanWeekPicker),
                )
            }

            item(key = "day_header") {
                PlanDayHeader(
                    dayText = state.selectedDate.format(dayFormatter),
                    relativeLabel = when (state.selectedDate) {
                        today -> stringResource(R.string.plan_day_today)
                        today.plusDays(1) -> stringResource(R.string.plan_day_tomorrow)
                        else -> null
                    },
                    countText = if (state.tasksForDay.isNotEmpty()) {
                        stringResource(R.string.plan_task_count_format, state.tasksForDay.size)
                    } else {
                        val plannedDays = state.weeklyTaskCounts.count { it.value > 0 }
                        if (plannedDays > 0) stringResource(R.string.plan_week_summary, plannedDays)
                        else stringResource(R.string.plan_week_summary_none)
                    },
                )
            }

            if (state.overdueTasks.isNotEmpty()) {
                item(key = "overdue_tasks") {
                    PlanOverdueCard(
                        moveLabel = stringResource(
                            when (state.selectedDate) {
                                today -> R.string.plan_move_overdue_to_today
                                today.plusDays(1) -> R.string.plan_move_overdue_to_tomorrow
                                else -> R.string.plan_move_overdue_to_selected
                            },
                        ),
                        onMoveAll = { viewModel.moveOverdueTasksToDate(state.selectedDate) },
                    ) {
                        PlanTaskListContainer(
                            tasks = state.overdueTasks,
                            onToggleTask = { viewModel.toggleTask(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onReorder = { viewModel.reorderTasks(it) },
                            today = today,
                        )
                    }
                }
            }

            if (state.tasksForDay.isEmpty()) {
                item(key = "empty_day") {
                    val context = LocalContext.current
                    PlanEmptyDay(
                        date = state.selectedDate,
                        today = today,
                        onAdd = { showAddSheet = true },
                        onPickStarter = { res -> viewModel.addTask(context.getString(res), state.selectedDate) },
                        onBackToToday = { viewModel.selectToday() },
                    )
                }
            } else {
                item(key = "tasks_for_day") {
                    PlanTaskListContainer(
                        tasks = state.tasksForDay,
                        onToggleTask = { viewModel.toggleTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onReorder = { viewModel.reorderTasks(it) },
                    )
                }
                if (!state.selectedDate.isBefore(today)) {
                    item(key = "add_row") {
                        val addCd = stringResource(R.string.plan_add_task_fab_cd)
                        DashedAddRow(
                            text = stringResource(R.string.plan_add_more_row),
                            onClick = { showAddSheet = true },
                            modifier = Modifier
                                .testTag(TestTags.PlanFab)
                                .semantics { contentDescription = addCd },
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTaskSheet(
            onDismiss = { showAddSheet = false },
            onSave = { title, dateStr, startTime, category, priority, reminderTime ->
                val date = runCatching { LocalDate.parse(dateStr) }.getOrDefault(state.selectedDate)
                viewModel.addTask(title, date, null, startTime, category, priority, reminderTime)
            },
            initialDate = state.selectedDate.toString(),
        )
    }
}
