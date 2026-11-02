package com.benimgunlerim.ui.today

import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.StarIcon
import com.benimgunlerim.ui.theme.AppTokens

private const val UNSELECTED_BG_ALPHA = 0.14f
private const val UNSELECTED_BORDER_ALPHA = 0.20f
private const val UNSELECTED_ENERGY_ALPHA = 0.12f
private val ItemHeight = 44.dp
private val ItemCornerRadius = 10.dp
private val EnergyLevels = listOf(1, 2, 3, 4, 5)
private const val NightStarLimit = 7

/** Gece sahnesinin üstü: hilal ve bugün yakılan ışıklar; başlık "N ışık yaktın." */
@Composable
internal fun CloseDayStep0Summary(completedCount: Int, totalCount: Int, progress: Float, overdueCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
    ) {
        Icon(
            imageVector = Icons.Rounded.Bedtime,
            contentDescription = null,
            tint = AppTheme.colors.night.moon,
            modifier = Modifier.size(AppTokens.IconSize.lg),
        )
        Spacer(Modifier.weight(1f))
        repeat(totalCount.coerceAtMost(NightStarLimit)) { i ->
            StarIcon(
                lit = i < completedCount,
                size = AppTokens.Sky.starSmall,
                litColor = AppTheme.colors.night.moon,
                edgeColor = AppTheme.colors.night.moon,
                idleColor = AppTheme.colors.night.line,
                dashed = true,
            )
        }
    }
    Text(
        text = if (completedCount > 0) {
            stringResource(R.string.night_lights_title, completedCount)
        } else {
            stringResource(R.string.night_no_lights_title)
        },
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        text = stringResource(if (completedCount > 0) R.string.night_lights_subtitle else R.string.night_no_lights_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (totalCount == 0) {
        Text(
            stringResource(R.string.today_close_step0_empty_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
            SummaryTile("$completedCount / $totalCount", stringResource(R.string.today_close_step0_completed), AppTheme.colors.night.moon, Modifier.weight(1f))
            SummaryTile("%${(progress * 100).toInt()}", stringResource(R.string.today_close_step0_success), AppTheme.colors.night.moon, Modifier.weight(1f))
            if (overdueCount > 0) SummaryTile("$overdueCount", stringResource(R.string.today_close_step0_overdue), AppTheme.colors.progress.done, Modifier.weight(1f))
        }
    }
}

@Composable
internal fun CloseDayStep1MoodEnergy(
    mood: Int,
    onMoodChange: (Int) -> Unit,
    energy: Int,
    onEnergyChange: (Int) -> Unit,
    moodLabels: List<String>,
    moodColors: List<Color>,
) {
    Text(stringResource(R.string.today_close_step1_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
    Text(
        stringResource(R.string.today_close_step1_subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.today_close_step1_mood_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            moodColors.forEachIndexed { i, col ->
                Box(
                    Modifier.weight(1f).height(ItemHeight).clip(RoundedCornerShape(ItemCornerRadius))
                        .background(if (mood == i) col else col.copy(alpha = UNSELECTED_BG_ALPHA))
                        .border(1.dp, if (mood == i) col else col.copy(alpha = UNSELECTED_BORDER_ALPHA), RoundedCornerShape(ItemCornerRadius))
                        .clickable { onMoodChange(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(moodLabels[i], style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (mood == i) FontWeight.ExtraBold else FontWeight.Normal), color = if (mood == i) AppTheme.colors.night.sky else col, maxLines = 1)
                }
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.today_close_step1_energy_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EnergyLevels.forEach { e ->
                Box(
                    Modifier.weight(1f).height(ItemHeight).clip(RoundedCornerShape(ItemCornerRadius))
                        .background(if (energy == e) AppTheme.colors.night.moon else AppTheme.colors.night.moon.copy(alpha = UNSELECTED_ENERGY_ALPHA))
                        .border(
                            1.dp,
                            if (energy == e) AppTheme.colors.night.moon else AppTheme.colors.night.moon.copy(alpha = UNSELECTED_BORDER_ALPHA),
                            RoundedCornerShape(ItemCornerRadius),
                        )
                        .clickable { onEnergyChange(e) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$e", style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (energy == e) FontWeight.ExtraBold else FontWeight.Normal), color = if (energy == e) AppTheme.colors.night.sky else AppTheme.colors.night.moon)
                }
            }
        }
    }
}
