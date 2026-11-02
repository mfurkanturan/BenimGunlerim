package com.benimgunlerim.ui.components.calm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.StarIcon
import com.benimgunlerim.ui.components.molecules.SegmentedPills
import com.benimgunlerim.ui.components.molecules.SheetSaveButton
import com.benimgunlerim.ui.components.molecules.SheetTitle
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens

private val Durations = listOf(30, 60, 120)
private const val MaxCandidates = 3

@Composable
internal fun ResetIntro(durationSeconds: Int, onDurationSelect: (Int) -> Unit, onStart: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.md)) {
        SheetTitle(stringResource(R.string.reset_title))
        Text(stringResource(R.string.reset_subtitle), style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.night.muted)
        SegmentedPills(
            options = listOf(
                stringResource(R.string.reset_duration_30s),
                stringResource(R.string.reset_duration_60s),
                stringResource(R.string.reset_duration_120s),
            ),
            selectedIndex = Durations.indexOf(durationSeconds).coerceAtLeast(0),
            onSelect = { onDurationSelect(Durations[it]) },
        )
        SheetSaveButton(text = stringResource(R.string.reset_start), onClick = onStart, modifier = Modifier.padding(top = AppTokens.Spacing.xs))
    }
}

@Composable
internal fun ResetBreathing(elapsedMs: Long, totalMs: Long, onEnough: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.lg),
    ) {
        val left = ((totalMs - elapsedMs).coerceAtLeast(0L) + 999L) / 1_000L
        Text(
            text = stringResource(R.string.reset_seconds_left, left),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = AppTheme.colors.night.muted,
        )
        BreathingMoon(elapsedMs = elapsedMs, totalMs = totalMs)
        TextButton(onClick = onEnough, modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min)) {
            Text(stringResource(R.string.reset_enough), color = AppTheme.colors.night.muted)
        }
    }
}

/** Nefesten sonra: bugünün açık hedeflerinden biri seçilir ve Bugün'de "şimdi sadece bu" olur. */
@Composable
internal fun ResetOneStep(
    candidates: List<ResetCandidate>,
    isLightDay: Boolean,
    onPick: (ResetCandidate) -> Unit,
    onLightDay: () -> Unit,
    onDone: () -> Unit,
) {
    val night = AppTheme.colors.night
    Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm)) {
        SheetTitle(stringResource(R.string.reset_completed_title))
        Text(
            text = stringResource(if (candidates.isEmpty()) R.string.reset_no_candidates else R.string.reset_completed_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = night.muted,
        )
        candidates.take(MaxCandidates).forEach { candidate ->
            Surface(
                onClick = { onPick(candidate) },
                shape = RoundedCornerShape(AppTokens.Radius.lg),
                color = night.card,
                border = BorderStroke(AppTokens.BorderWidth.thin, night.line),
                modifier = Modifier.fillMaxWidth().heightIn(min = AppTokens.Layout.fabDefaultSize),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
                ) {
                    StarIcon(lit = false, size = AppTokens.Star.row, litColor = night.moon, edgeColor = night.moon, idleColor = night.moon)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(candidate.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = night.text)
                        Text(
                            stringResource(if (candidate.isRoutine) R.string.today_starter_routine else R.string.today_starter_task),
                            style = MaterialTheme.typography.bodySmall,
                            color = night.muted,
                        )
                    }
                }
            }
        }
        if (!isLightDay) {
            TextButton(onClick = onLightDay, modifier = Modifier.fillMaxWidth().heightIn(min = AppTokens.TouchTarget.min)) {
                Text(stringResource(R.string.reset_light_day), color = night.moon, fontWeight = FontWeight.Bold)
            }
        }
        TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth().heightIn(min = AppTokens.TouchTarget.min)) {
            Text(stringResource(R.string.reset_continue), color = night.muted, textAlign = TextAlign.Center)
        }
    }
}
