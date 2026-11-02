package com.benimgunlerim.ui.today

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.BuildConfig
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.DebugSkyPhase
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.layout.AppFab
import com.benimgunlerim.ui.components.layout.ScreenScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToRoutines: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onOpenRoutineDetail: (String) -> Unit = {},
) {
    val rawState by viewModel.uiState.collectAsState()
    val state = if (BuildConfig.DEBUG && DebugSkyPhase.forceEmptyToday) {
        rawState.copy(routines = emptyList(), tasks = emptyList(), overdueTasks = emptyList(), missedDay = null)
    } else {
        rawState
    }
    val today = viewModel.today()

    val showFabMenu = remember { mutableStateOf(false) }
    val showAddTaskSheet = remember { mutableStateOf(false) }
    val showAddRoutineSheet = remember { mutableStateOf(false) }
    val focusStep = remember { mutableStateOf<com.benimgunlerim.ui.components.calm.ResetCandidate?>(null) }
    val openTaskId = remember { mutableStateOf<String?>(null) }
    val showResetDialog = remember { mutableStateOf(false) }
    val showBrainDumpDialog = remember { mutableStateOf(false) }
    val showCloseSheet = remember { mutableStateOf(false) }
    val showMissedDaySheet = remember { mutableStateOf(false) }
    var dismissContextualReset by remember { mutableStateOf(false) }

    val levelUpEvent = remember { mutableStateOf<com.benimgunlerim.domain.model.GameEvent.LevelUp?>(null) }
    val achievementEvent = remember { mutableStateOf<com.benimgunlerim.domain.model.GameEvent.AchievementUnlocked?>(null) }
    val blockCompletionData = remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var miniBannerData by remember { mutableStateOf<Pair<String, String>?>(null) }

    val menuRoutineId = remember { mutableStateOf<String?>(null) }
    val editingRoutine = remember { mutableStateOf<TodayRoutineUi?>(null) }
    val archiveRoutineId = remember { mutableStateOf<String?>(null) }
    val deleteTaskId = remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val dayIsClosed = state.todayState?.closedAt != null
    val isEmptyToday = state.tasks.isEmpty() && state.routines.isEmpty()
    val totalCount = state.tasks.size + state.routines.size
    val activeRoutineIds = remember(state.routines) { state.routines.mapTo(mutableSetOf()) { it.id } }
    val completedCount = state.tasks.count { it.isCompleted } +
        state.completedRoutineIds.count { it in activeRoutineIds }
    val userLevel = com.benimgunlerim.domain.GameEngine.calculateLevel(state.gameState.totalXp).level

    TodayEventEffects(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        onLevelUp = { levelUpEvent.value = it },
        onAchievementUnlocked = { achievementEvent.value = it },
        onBlockCompletion = { title, subtitle, badge -> blockCompletionData.value = Triple(title, subtitle, badge) },
        onMiniBanner = { miniBannerData = it },
    )

    ScreenScaffold(
        modifier = Modifier.testTag(TestTags.TodayRoot),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = null,
    ) { contentPadding ->
        val summaryState = remember(
            dayIsClosed,
            isEmptyToday,
            totalCount,
            completedCount,
            userLevel,
            miniBannerData,
            state.gameState.celebrationEffectsEnabled,
            dismissContextualReset,
        ) {
            TodayListSummaryState(
                dayIsClosed = dayIsClosed,
                isEmptyToday = isEmptyToday,
                totalCount = totalCount,
                completedCount = completedCount,
                userLevel = userLevel,
                miniBannerData = miniBannerData,
                celebrationEffectsEnabled = state.gameState.celebrationEffectsEnabled,
                dismissContextualReset = dismissContextualReset,
            )
        }
        val listCallbacks = remember(onNavigateToRoutines) {
            TodayListEventCallbacks(
                onNavigateToRoutines = onNavigateToRoutines,
                onResetClick = { showResetDialog.value = true },
                onDismissContextualReset = { dismissContextualReset = true },
                onMissedDayReviewClick = { showMissedDaySheet.value = true },
                onAddTaskClick = { showAddTaskSheet.value = true },
                onAddRoutineClick = { showAddRoutineSheet.value = true },
                onOpenTask = { openTaskId.value = it },
                onOpenQuickActions = { showFabMenu.value = true },
                onOpenRoutineMenu = { menuRoutineId.value = it },
                onCloseDayClick = { showCloseSheet.value = true },
                onRequestDeleteTask = { deleteTaskId.value = it },
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToPlan = onNavigateToPlan,
            )
        }

        TodayContentList(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            state = state,
            today = today,
            viewModel = viewModel,
            summaryState = summaryState,
            callbacks = listCallbacks,
            focus = focusStep.value,
            onClearFocus = { focusStep.value = null },
        )
    }

    val modalStates = remember {
        TodayModalStates(
            showFabMenu = showFabMenu,
            showResetDialog = showResetDialog,
            showBrainDumpDialog = showBrainDumpDialog,
            showAddTaskSheet = showAddTaskSheet,
            showAddRoutineSheet = showAddRoutineSheet,
            focusStep = focusStep,
            openTaskId = openTaskId,
            showCloseSheet = showCloseSheet,
            showMissedDaySheet = showMissedDaySheet,
            menuRoutineId = menuRoutineId,
            editingRoutine = editingRoutine,
            levelUpEvent = levelUpEvent,
            achievementEvent = achievementEvent,
            blockCompletionData = blockCompletionData,
            archiveRoutineId = archiveRoutineId,
            deleteTaskId = deleteTaskId,
        )
    }

    TodayModalsHost(
        state = state,
        today = today,
        viewModel = viewModel,
        completedCount = completedCount,
        totalCount = totalCount,
        modals = modalStates,
    )
}
