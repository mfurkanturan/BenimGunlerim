package com.benimgunlerim.ui.today

import com.benimgunlerim.ui.theme.AppTheme
import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.molecules.DashedAddRow
import com.benimgunlerim.ui.components.calm.ResetCandidate
import com.benimgunlerim.ui.components.organisms.CloseDayCard
import com.benimgunlerim.ui.components.organisms.DaySkyHeader
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.DayPhase
import com.benimgunlerim.ui.theme.skyPaletteFor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class TodayListSummaryState(
    val dayIsClosed: Boolean,
    val isEmptyToday: Boolean,
    val totalCount: Int,
    val completedCount: Int,
    val userLevel: Int,
    val miniBannerData: Pair<String, String>?,
    val celebrationEffectsEnabled: Boolean,
    val dismissContextualReset: Boolean,
)

internal data class TodayListEventCallbacks(
    val onNavigateToRoutines: () -> Unit,
    val onResetClick: () -> Unit,
    val onDismissContextualReset: () -> Unit,
    val onMissedDayReviewClick: () -> Unit,
    val onAddTaskClick: () -> Unit,
    val onAddRoutineClick: () -> Unit,
    val onOpenTask: (String) -> Unit,
    val onOpenQuickActions: () -> Unit,
    val onOpenRoutineMenu: (String) -> Unit,
    val onCloseDayClick: () -> Unit,
    val onRequestDeleteTask: (String) -> Unit,
    val onNavigateToSettings: () -> Unit,
    val onNavigateToPlan: () -> Unit,
)

private const val VisibleRoutineLimit = 5
private const val VisibleTaskLimit = 5
/** Hafif günde görünen en fazla rutin / açık görev; gerisi "bugün dinleniyor". */
private const val LightRoutineLimit = 2
private const val LightTaskLimit = 1

private val SideGutter = Modifier.padding(horizontal = AppTokens.Spacing.screenHorizontal)

@Composable
internal fun TodayContentList(
    modifier: Modifier,
    contentPadding: PaddingValues,
    state: TodayUiState,
    today: LocalDate,
    viewModel: TodayViewModel,
    summaryState: TodayListSummaryState,
    callbacks: TodayListEventCallbacks,
    focus: ResetCandidate? = null,
    onClearFocus: () -> Unit = {},
) {
    var isEditingTasks by rememberSaveable { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("tr")) }

    val listState = rememberLazyListState()
    val palette = skyPaletteFor(state.dayPhase)
    val atSky by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    val scrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
    StatusBarIconsEffect(lightIcons = palette.isMoon && atSky)

    Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().testTag(TestTags.TodayList),
        contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
    ) {
        item(key = "sky") {
            DaySkyHeader(
                phase = state.dayPhase,
                greeting = greetingFor(state.dayPhase, state.gameState.userName),
                dateText = today.format(dateFormatter),
                litCount = summaryState.completedCount,
                totalCount = summaryState.totalCount,
                minoLine = minoLineFor(state.dayPhase, summaryState),
                onNotificationClick = callbacks.onNavigateToSettings,
            )
        }

        item(key = "mini_celebration") {
            if (summaryState.celebrationEffectsEnabled) summaryState.miniBannerData?.let { (msg, icon) ->
                com.benimgunlerim.ui.components.gamification.MiniCelebrationBanner(
                    message = msg,
                    icon = icon,
                    visible = true,
                    modifier = SideGutter,
                )
            }
        }

        val focusOpen = focus?.let { f ->
            if (f.isRoutine) state.routines.any { it.id == f.id } && f.id !in state.completedRoutineIds
            else state.tasks.any { it.id == f.id && !it.isCompleted }
        } == true
        if (focus != null && focusOpen) {
            item(key = "focus_step") {
                FocusCard(
                    title = focus.title,
                    onComplete = {
                        if (focus.isRoutine) viewModel.stepRoutine(focus.id) else viewModel.toggleTask(focus.id)
                        onClearFocus()
                    },
                    onClear = onClearFocus,
                    modifier = SideGutter,
                )
            }
        }

        if (state.isLightDayMode) {
            item(key = "light_day_banner") {
                LightDayBanner(onDisableClick = { viewModel.toggleLightDayMode(false) }, modifier = SideGutter)
            }
        } else if (!summaryState.dismissContextualReset && summaryState.totalCount >= 4 && summaryState.completedCount == 0) {
            item(key = "contextual_reset") {
                ContextualResetCard(
                    isLightDayMode = state.isLightDayMode,
                    onResetClick = callbacks.onResetClick,
                    onDismissClick = callbacks.onDismissContextualReset,
                    modifier = SideGutter,
                )
            }
        }

        val showInvite = state.routines.isEmpty() && state.tasks.isEmpty() &&
            state.overdueTasks.isEmpty() && !summaryState.dayIsClosed

        if (showInvite) {
            item(key = "empty_invite") {
                val context = LocalContext.current
                TodayEmptyInvite(
                    onPickStarter = { starter ->
                        val name = context.getString(starter.nameRes)
                        if (starter.isRoutine) viewModel.addRoutine(name) else viewModel.addTask(name)
                    },
                    onAddRoutine = callbacks.onAddRoutineClick,
                    onAddTask = callbacks.onAddTaskClick,
                    modifier = SideGutter,
                )
            }
        } else {
            val routineLimit = if (state.isLightDayMode) LightRoutineLimit else VisibleRoutineLimit
            val taskLimit = if (state.isLightDayMode) LightTaskLimit else VisibleTaskLimit
            val shownRoutines = if (state.isLightDayMode) {
                state.routines.sortedBy { it.id in state.completedRoutineIds }.take(routineLimit)
            } else {
                state.routines.take(routineLimit)
            }
            val restingCount = if (state.isLightDayMode) {
                (state.routines.size - shownRoutines.size) + (state.tasks.count { !it.isCompleted } - taskLimit).coerceAtLeast(0)
            } else 0
            item(key = "routines_section") {
                val routinesDone = state.routines.count { it.id in state.completedRoutineIds }
                TodaySection(
                    title = stringResource(R.string.today_routines_title),
                    trailing = {
                        if (!state.isLightDayMode && state.routines.size > VisibleRoutineLimit) {
                            TextButton(
                                onClick = callbacks.onNavigateToRoutines,
                                modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min),
                            ) { Text(stringResource(R.string.today_routines_view_all), color = AppTheme.colors.progress.doneInk) }
                        } else if (state.routines.isNotEmpty()) {
                            SectionProgress(routinesDone, state.routines.size)
                        }
                    },
                ) {
                    if (state.routines.isEmpty()) {
                        DashedAddRow(stringResource(R.string.today_routines_placeholder), onClick = callbacks.onAddRoutineClick)
                    } else {
                        RoutineStepList(
                            routines = shownRoutines,
                            completedRoutineIds = state.completedRoutineIds,
                            onStep = { viewModel.stepRoutine(it) },
                            onOpenMenu = callbacks.onOpenRoutineMenu,
                        )
                    }
                }
            }

            item(key = "tasks_section") {
                val openTasks = state.tasks.filterNot { it.isCompleted }
                TodaySection(
                    title = stringResource(R.string.today_tasks_title),
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (openTasks.size > 1) {
                                TextButton(
                                    onClick = { isEditingTasks = !isEditingTasks },
                                    modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min),
                                ) {
                                    Text(
                                        stringResource(if (isEditingTasks) R.string.today_tasks_done else R.string.today_tasks_edit),
                                        color = AppTheme.colors.progress.doneInk,
                                    )
                                }
                            }
                            if (state.tasks.size > VisibleTaskLimit || state.tasks.any { it.isCompleted }) {
                                TextButton(
                                    onClick = callbacks.onNavigateToPlan,
                                    modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min),
                                ) { Text(stringResource(R.string.today_view_all_tasks), color = AppTheme.colors.progress.doneInk) }
                            } else if (state.tasks.isNotEmpty()) {
                                SectionProgress(state.tasks.size - openTasks.size, state.tasks.size)
                            }
                        }
                    },
                ) {
                    if (openTasks.isEmpty()) {
                        // Görev bölümü boşken tek "ekle" yolu bu satır; TodayFab etiketi burada (ayrı ekle satırı gizli).
                        val addCd = stringResource(R.string.today_add_task_fab_cd)
                        DashedAddRow(
                            stringResource(R.string.today_tasks_placeholder),
                            onClick = callbacks.onAddTaskClick,
                            modifier = Modifier.testTag(TestTags.TodayFab).semantics { contentDescription = addCd },
                        )
                    } else {
                        TaskListContainer(
                            tasks = openTasks.take(taskLimit),
                            onToggleTask = { viewModel.toggleTask(it) },
                            onDeleteTask = callbacks.onRequestDeleteTask,
                            onReorder = { viewModel.reorderTodayTasks(it) },
                            isEditing = isEditingTasks,
                            onOpenTask = callbacks.onOpenTask,
                        )
                    }
                }
            }

            if (state.overdueTasks.isNotEmpty()) {
                item(key = "overdue_tasks_section") {
                    OverdueCard(
                        onMoveAll = { viewModel.moveAllOverdueTo(today) },
                        modifier = SideGutter,
                    ) {
                        TaskListContainer(
                            tasks = state.overdueTasks,
                            onToggleTask = { viewModel.toggleTask(it) },
                            onDeleteTask = callbacks.onRequestDeleteTask,
                            onReorder = { viewModel.reorderTodayTasks(it) },
                            onOpenTask = callbacks.onOpenTask,
                        )
                    }
                }
            }

            if (restingCount > 0) {
                item(key = "light_day_resting") {
                    Text(
                        text = stringResource(R.string.light_day_resting, restingCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = SideGutter,
                    )
                }
            }

            // Ayrı "ekle" satırı açık görev varken; görev bölümü boşken onun yer tutucusu ekleme yoludur.
            if (!summaryState.dayIsClosed && state.tasks.any { !it.isCompleted }) {
                item(key = "add_task_row") { AddStepRow(onClick = callbacks.onOpenQuickActions) }
            }
        }

        state.missedDay?.let { missed ->
            item(key = "missed_day_recovery") {
                val shortFormatter = remember { DateTimeFormatter.ofPattern("d MMMM", Locale("tr")) }
                MissedDayRow(
                    dateLabel = missed.format(shortFormatter),
                    onClick = callbacks.onMissedDayReviewClick,
                    modifier = SideGutter.testTag(TestTags.TodayMissedDayBanner),
                )
            }
        }

        item(key = "close_day_card") {
            if (showInvite) {
                QuietCloseDayRow(onClick = callbacks.onCloseDayClick, modifier = SideGutter)
            } else {
                CloseDayCard(
                    isDayClosed = summaryState.dayIsClosed,
                    onCloseDayClick = callbacks.onCloseDayClick,
                    isEvening = state.dayPhase == DayPhase.Aksam || state.dayPhase == DayPhase.Gece,
                    modifier = SideGutter,
                )
            }
        }
    }
    if (scrolled) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(if (atSky) palette.sky else MaterialTheme.colorScheme.background),
        )
    }
    }
}

@Composable
private fun greetingFor(phase: DayPhase, userName: String): String {
    val name = userName.trim()
    return if (name.isEmpty()) {
        stringResource(
            when (phase) {
                DayPhase.Sabah -> R.string.greeting_morning
                DayPhase.Ogle -> R.string.greeting_noon
                DayPhase.Aksam -> R.string.greeting_evening
                DayPhase.Gece -> R.string.greeting_night
            },
        )
    } else {
        stringResource(
            when (phase) {
                DayPhase.Sabah -> R.string.greeting_morning_named
                DayPhase.Ogle -> R.string.greeting_noon_named
                DayPhase.Aksam -> R.string.greeting_evening_named
                DayPhase.Gece -> R.string.greeting_night_named
            },
            name,
        )
    }
}

@Composable
private fun minoLineFor(phase: DayPhase, summary: TodayListSummaryState): String = when {
    summary.dayIsClosed -> stringResource(R.string.mino_day_closed)
    summary.totalCount == 0 -> stringResource(R.string.mino_no_steps)
    summary.completedCount >= summary.totalCount -> stringResource(R.string.mino_all_lit)
    summary.completedCount > 0 -> stringResource(R.string.mino_some_lit, summary.completedCount)
    else -> when (phase) {
        DayPhase.Sabah -> stringResource(R.string.mino_zero_morning)
        DayPhase.Ogle -> stringResource(R.string.mino_zero_noon)
        DayPhase.Aksam -> stringResource(R.string.mino_zero_evening, summary.totalCount)
        DayPhase.Gece -> stringResource(R.string.mino_zero_night)
    }
}

@Composable
private fun TodaySection(
    title: String,
    trailing: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = SideGutter, verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            trailing()
        }
        content()
    }
}

@Composable
private fun AddStepRow(onClick: () -> Unit) {
    val cd = stringResource(R.string.today_add_task_fab_cd)
    Surface(
        onClick = onClick,
        modifier = SideGutter
            .fillMaxWidth()
            .heightIn(min = AppTokens.Layout.fabDefaultSize)
            .testTag(TestTags.TodayFab)
            .semantics { contentDescription = cd },
        shape = RoundedCornerShape(AppTokens.Radius.lg),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(AppTokens.BorderWidth.thin, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = AppTheme.colors.progress.doneInk, modifier = Modifier.size(AppTokens.IconSize.md))
            Text(
                text = stringResource(R.string.today_add_task_row),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = AppTheme.colors.progress.doneInk,
            )
        }
    }
}

@Composable
private fun MissedDayRow(dateLabel: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = AppTokens.TouchTarget.min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
    ) {
        Icon(Icons.Rounded.History, contentDescription = null, tint = AppTheme.colors.progress.doneInk, modifier = Modifier.size(AppTokens.IconSize.sm))
        Text(
            text = stringResource(R.string.today_missed_day_row, dateLabel),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onClick, modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min)) {
            Text(
                text = stringResource(R.string.today_missed_day_action_short),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    textDecoration = TextDecoration.Underline,
                ),
                color = AppTheme.colors.progress.doneInk,
            )
        }
    }
}

/** Durum çubuğu ikonları: gece göğünde açık, diğer her yerde koyu. Ekrandan çıkınca koyuya döner. */
@Composable
private fun StatusBarIconsEffect(lightIcons: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    val window = (view.context as? Activity)?.window ?: return
    DisposableEffect(lightIcons) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !lightIcons
        onDispose { WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true }
    }
}
