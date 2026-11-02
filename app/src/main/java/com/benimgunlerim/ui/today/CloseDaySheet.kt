package com.benimgunlerim.ui.today

import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTokens

private const val TOTAL_STEPS = 4
private const val LAST_STEP_INDEX = 3

@Suppress("LongMethod")
@Composable
internal fun CloseDaySheet(
    completedCount: Int,
    totalCount: Int,
    progress: Float,
    overdueCount: Int,
    onSave: (CloseDayDraft) -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var mood by rememberSaveable { mutableIntStateOf(3) }
    var energy by rememberSaveable { mutableIntStateOf(3) }
    var note by rememberSaveable { mutableStateOf("") }
    var bestMoment by rememberSaveable { mutableStateOf("") }
    var challenge by rememberSaveable { mutableStateOf("") }
    var tomorrowIntention by rememberSaveable { mutableStateOf("") }
    var carryTasks by rememberSaveable { mutableStateOf(overdueCount > 0) }

    val moodLabels = listOf(
        stringResource(R.string.today_close_step1_mood_very_bad),
        stringResource(R.string.today_close_step1_mood_bad),
        stringResource(R.string.today_close_step1_mood_normal),
        stringResource(R.string.today_close_step1_mood_good),
        stringResource(R.string.today_close_step1_mood_great),
    )
    val moodColors = AppTheme.colors.night.scale
    val nightButtonColors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.night.moon, contentColor = AppTheme.colors.night.sky)

    val closeScroll = rememberScrollState()
    val focusManager = LocalFocusManager.current
    Column(
        Modifier
            .verticalScroll(closeScroll)
            .padding(horizontal = AppTokens.Spacing.xl, vertical = AppTokens.Spacing.md)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.md),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs)) {
            repeat(TOTAL_STEPS) { i ->
                Box(
                    Modifier.weight(1f).height(AppTokens.Spacing.xxs).clip(RoundedCornerShape(AppTokens.Radius.pill))
                        .background(if (i <= step) AppTheme.colors.night.moon else MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }

        when (step) {
            0 -> CloseDayStep0Summary(completedCount, totalCount, progress, overdueCount)
            1 -> CloseDayStep1MoodEnergy(
                mood = mood,
                onMoodChange = { mood = it },
                energy = energy,
                onEnergyChange = { energy = it },
                moodLabels = moodLabels,
                moodColors = moodColors,
            )
            2 -> CloseDayStep2Reflection(
                bestMoment = bestMoment,
                onBestMomentChange = { bestMoment = it },
                challenge = challenge,
                onChallengeChange = { challenge = it },
                note = note,
                onNoteChange = { note = it },
                onNoteDone = { focusManager.clearFocus() },
            )
            3 -> CloseDayStep3IntentionAndCarry(
                tomorrowIntention = tomorrowIntention,
                onTomorrowIntentionChange = { tomorrowIntention = it },
                onIntentionDone = { focusManager.clearFocus() },
                overdueCount = overdueCount,
                carryTasks = carryTasks,
                onCarryTasksToggle = { carryTasks = !carryTasks },
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm)) {
            if (step > 0) {
                OutlinedSmallButton(stringResource(R.string.today_close_back_btn), Modifier.weight(1f)) { step-- }
            }
            val isLast = step >= LAST_STEP_INDEX
            Button(
                onClick = {
                    if (!isLast) {
                        step++
                    } else {
                        onSave(
                            CloseDayDraft(
                                mood = mood,
                                energy = energy,
                                note = note,
                                bestMoment = bestMoment,
                                challenge = challenge,
                                tomorrowIntention = tomorrowIntention,
                                carryOverdueTasks = carryTasks,
                            ),
                        )
                    }
                },
                modifier = Modifier.weight(1f).heightIn(min = AppTokens.TouchTarget.min),
                shape = RoundedCornerShape(AppTokens.Radius.pill),
                colors = nightButtonColors,
            ) {
                Text(
                    text = stringResource(if (isLast) R.string.night_good_night else R.string.today_close_next_btn),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                )
            }
        }
        Spacer(Modifier.height(AppTokens.Spacing.sm))
    }
}
