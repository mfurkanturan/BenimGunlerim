@file:Suppress("SpellCheckingInspection", "LongMethod")

package com.benimgunlerim.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppDivider
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.layout.ScreenScaffold
import com.benimgunlerim.ui.components.molecules.MetricCard
import com.benimgunlerim.ui.components.molecules.ScreenTitle
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.components.organisms.AchievementRow
import com.benimgunlerim.ui.theme.AppTokens
import java.time.LocalDate

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel(),
    onNavigateToAchievements: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val today = remember { LocalDate.now() }

    val completedText = stringResource(R.string.progress_unit_completed)

    ScreenScaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.md),
        ) {
            ScreenTitle(
                title = stringResource(R.string.days_title),
                subtitle = stringResource(R.string.days_subtitle),
            )
            DaysNightSky(days = state.last30Days, today = today)
            DaysLevelCard(totalXp = state.gameState.totalXp)

            DaysStatsCard(
                currentStreak = state.currentStreak,
                bestStreak = state.bestStreak,
                routineRate = state.routineHitRate,
                taskRate = state.taskHitRate,
            )


            // Başarımlar
            SectionBlock(
                title = stringResource(R.string.progress_achievements_title),
                trailingContent = {
                    Text(
                        text = stringResource(
                            R.string.progress_achievements_subtitle,
                            state.totalAchievements,
                            state.unlockedAchievements.size,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            ) {
                if (state.unlockedAchievements.isNotEmpty()) {
                    AppSurface(radius = AppTokens.Radius.lg, padding = AppTokens.Spacing.none) {
                        Column {
                            state.unlockedAchievements.forEachIndexed { index, achievement ->
                                if (index > 0) AppDivider()
                                AchievementRow(
                                    title = stringResource(achievement.titleRes),
                                    description = stringResource(achievement.descriptionRes),
                                    icon = achievement.emoji,
                                    isUnlocked = true,
                                    progress = 1f,
                                    progressText = completedText,
                                )
                            }
                        }
                    }
                } else {
                    AppSurface(radius = AppTokens.Radius.lg) {
                        Text(
                            text = stringResource(R.string.progress_achievements_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
