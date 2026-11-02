package com.benimgunlerim.ui.progress

import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.benimgunlerim.R
import com.benimgunlerim.domain.GameEngine
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.core.StarIcon
import com.benimgunlerim.ui.components.molecules.ProgressBar
import com.benimgunlerim.ui.theme.AppTokens
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


/** Seviye kartı: seviye, toplam XP ve bir sonraki seviyeye kalan. */
@Composable
internal fun DaysLevelCard(totalXp: Int, modifier: Modifier = Modifier) {
    val info = GameEngine.calculateLevel(totalXp)
    val level = info.level
    val intoLevel = info.currentXp
    val needed = info.xpForNextLevel
    AppSurface(modifier = modifier.fillMaxWidth(), radius = AppTokens.Radius.lg) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.days_level_title, level),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.days_level_xp, totalXp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        ProgressBar(
            progress = if (needed > 0) intoLevel.toFloat() / needed else 0f,
            color = AppTheme.colors.progress.done,
            modifier = Modifier.fillMaxWidth().padding(top = AppTokens.Spacing.sm),
        )
        Text(
            text = stringResource(R.string.days_level_next, level + 1, needed - intoLevel),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppTokens.Spacing.xs),
        )
    }
}

/** Dört sayı tek kartta: seri (şimdi / en iyi) ve rutin / görev tamamlama oranı. */
@Composable
internal fun DaysStatsCard(
    currentStreak: Int,
    bestStreak: Int,
    routineRate: Float,
    taskRate: Float,
    modifier: Modifier = Modifier,
) {
    val days = stringResource(R.string.progress_unit_days)
    AppSurface(modifier = modifier.fillMaxWidth(), radius = AppTokens.Radius.lg) {
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCell(stringResource(R.string.progress_metric_active_streak), "$currentStreak", days, Modifier.weight(1f))
            StatCell(stringResource(R.string.progress_metric_best_streak), "$bestStreak", days, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = AppTokens.Spacing.md)) {
            StatCell(stringResource(R.string.progress_balance_routine), "%${(routineRate * 100).toInt()}", null, Modifier.weight(1f))
            StatCell(stringResource(R.string.progress_balance_task), "%${(taskRate * 100).toInt()}", null, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, unit: String?, modifier: Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs)) {
            Text(value, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
            if (unit != null) {
                Text(
                    unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = AppTokens.Spacing.xxs),
                )
            }
        }
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
