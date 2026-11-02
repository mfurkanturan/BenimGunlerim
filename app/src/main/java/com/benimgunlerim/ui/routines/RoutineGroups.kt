package com.benimgunlerim.ui.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.DayPhase
import com.benimgunlerim.ui.theme.dayPhaseFor
import java.time.LocalTime

/** Rutinin günün hangi vaktine ait olduğu: hatırlatma saatine göre; saatsizse "gün boyu" (null). */
internal fun RoutineCardUi.phase(): DayPhase? =
    preferredTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() }?.let(::dayPhaseFor)

/** Gruplar Bugün göğüyle aynı sırada: sabah → öğle → akşam → gece → gün boyu. */
internal val RoutineGroupOrder: List<DayPhase?> = listOf(DayPhase.Sabah, DayPhase.Ogle, DayPhase.Aksam, DayPhase.Gece, null)

internal fun groupLabelRes(phase: DayPhase?): Int = when (phase) {
    DayPhase.Sabah -> R.string.routines_group_morning
    DayPhase.Ogle -> R.string.routines_group_noon
    DayPhase.Aksam -> R.string.routines_group_evening
    DayPhase.Gece -> R.string.routines_group_night
    null -> R.string.routines_group_anytime
}

private fun groupIcon(phase: DayPhase?): ImageVector = when (phase) {
    DayPhase.Sabah -> Icons.Rounded.WbSunny
    DayPhase.Ogle -> Icons.Rounded.LightMode
    DayPhase.Aksam -> Icons.Rounded.WbTwilight
    DayPhase.Gece -> Icons.Rounded.Bedtime
    null -> Icons.Rounded.AllInclusive
}

/** Vakit başlığı: küçük güneş/ay ikonu + "Sabah", sağda "1 / 2 bugün". */
@Composable
internal fun RoutineGroupHeader(label: String, phase: DayPhase?, progressText: String) {
    Row(
        modifier = Modifier.padding(top = AppTokens.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
    ) {
        Icon(
            groupIcon(phase),
            contentDescription = null,
            tint = if (phase == DayPhase.Gece) AppTheme.colors.night.card else AppTheme.colors.progress.doneInk,
            modifier = Modifier.size(AppTokens.IconSize.sm),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        Text(
            text = progressText,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun RoutineGroup(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) { content() }
}
