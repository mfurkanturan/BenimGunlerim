@file:Suppress("SpellCheckingInspection", "LongMethod")

package com.benimgunlerim.ui.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.R
import com.benimgunlerim.domain.model.RoutineTargetType
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.core.AppButtonVariant
import com.benimgunlerim.ui.components.core.AppDivider
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.layout.AppFab
import com.benimgunlerim.ui.components.layout.ScreenScaffold
import com.benimgunlerim.ui.components.molecules.EmptyState
import com.benimgunlerim.ui.components.molecules.ScreenTitle
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.components.organisms.AddRoutineSheet
import com.benimgunlerim.ui.components.organisms.RoutineRow
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun RoutinesScreen(
    viewModel: RoutinesViewModel = hiltViewModel(),
    onOpenRoutineDetail: (String) -> Unit = {},
) {
    val routines by viewModel.routines.collectAsState()
    val archivedRoutines by viewModel.archivedRoutines.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    val longestChain = remember(routines) { routines.maxOfOrNull { it.currentStreak } ?: 0 }

    val everyDayLabel = stringResource(R.string.routines_every_day)
    val daysCountLabel = stringResource(R.string.routines_days_count)

    ScreenScaffold(
        modifier = Modifier.testTag(TestTags.RoutinesRoot),
        floatingActionButton = {
            AppFab(
                onClick = { showAddSheet = true },
                icon = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.routines_add_cd),
                modifier = Modifier.testTag(TestTags.RoutinesFab),
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.md),
        ) {
            ScreenTitle(
                title = stringResource(R.string.routines_header_title),
                subtitle = if (routines.isNotEmpty()) stringResource(R.string.routines_header_count, routines.size) else null,
            )
            if (routines.isEmpty()) {
                EmptyState(
                    emoji = "🔥",
                    title = stringResource(R.string.routines_empty_title),
                    description = stringResource(R.string.routines_empty_body),
                    action = {
                        AppButton(
                            text = stringResource(R.string.routines_empty_cta),
                            onClick = { showAddSheet = true },
                        )
                    },
                )
            } else {
                WeekPathCard(routines = routines, longestChain = longestChain)

                // Vakitlere göre gruplar: sabah → öğle → akşam → gece → gün boyu
                val grouped = routines.groupBy { it.phase() }
                RoutineGroupOrder.forEach { phase ->
                    val items = grouped[phase].orEmpty()
                    if (items.isNotEmpty()) RoutineGroup {
                        val doneToday = items.count { it.last7Days.lastOrNull() == true }
                        RoutineGroupHeader(
                            label = stringResource(groupLabelRes(phase)),
                            phase = phase,
                            progressText = stringResource(R.string.routines_group_progress, doneToday, items.size),
                        )
                        AppSurface(radius = AppTokens.Radius.lg, padding = AppTokens.Spacing.none) {
                            items.forEachIndexed { index, routine ->
                                key(routine.id) {
                                    if (index > 0) AppDivider()
                                    RoutineRow(
                                        title = routine.name,
                                        isCompletedToday = routine.last7Days.lastOrNull() == true,
                                        onToggle = { viewModel.toggleRoutine(routine.id) },
                                        weekHistory = routine.last7Days,
                                        targetDays = routine.targetDays,
                                        streakCount = routine.currentStreak,
                                        preferredTime = routine.preferredTime,
                                        targetDaysSummary = if (routine.targetDays.size == 7) {
                                            everyDayLabel
                                        } else {
                                            String.format(daysCountLabel, routine.targetDays.size)
                                        },
                                        targetType = routine.targetType,
                                        targetValue = routine.targetValue,
                                        targetUnit = routine.targetUnit,
                                        currentValue = routine.currentValue,
                                        onProgressChange = { viewModel.updateRoutineProgress(routine.id, it) },
                                        useSurface = false,
                                        modifier = Modifier.routineReorderGesture(routine, routines, viewModel::reorderRoutines),
                                        onClick = { onOpenRoutineDetail(routine.id) },
                                    )
                                }
                            }
                        }
                    }
                }

                if (archivedRoutines.isNotEmpty()) {
                    ArchivedRoutinesSection(archivedRoutines, onUnarchive = { viewModel.unarchiveRoutine(it) })
                }
            }
        }
    }

    if (showAddSheet) {
        AddRoutineSheet(
            onDismiss = { showAddSheet = false },
            onSave = { title, targetDays, reminderTime, category, targetCount, unit ->
                viewModel.addRoutine(
                    name = title,
                    targetDays = targetDays,
                    preferredTime = reminderTime,
                    targetType = if (targetCount != null) RoutineTargetType.GOAL.value else RoutineTargetType.CHECK.value,
                    targetValue = targetCount,
                    targetUnit = unit,
                    category = category,
                )
            },
        )
    }
}
