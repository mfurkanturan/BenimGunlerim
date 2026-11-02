package com.benimgunlerim.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.StarIcon
import com.benimgunlerim.ui.components.organisms.turkishDayShortCode
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DaysPerWeek = 7

/**
 * Ayın gece göğü: hedefli her gün bir yıldız; art arda hedefli günler ince çizgiyle birleşir —
 * seriler takımyıldızı olur. Bugün kayısı halka, hedefsiz geçmiş gün sönük nokta, gelecek gün soluk tarih.
 * Bugün ekranı gündüz göğüyse, Günlerim günlerin biriktiği gece göğüdür.
 */
@Composable
internal fun DaysNightSky(days: List<ProgressDayUi>, today: LocalDate, modifier: Modifier = Modifier) {
    val night = AppTheme.colors.night
    val tr = remember { Locale("tr") }
    val month = remember(today) { YearMonth.from(today) }
    val litDates = remember(days) { days.filter { it.dailyScore > 0 }.mapTo(mutableSetOf()) { LocalDate.parse(it.date) } }
    val litThisMonth = litDates.count { YearMonth.from(it) == month }
    val leading = month.atDay(1).dayOfWeek.value - 1
    val cells: List<LocalDate?> = List(leading) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    val rows = (cells.size + DaysPerWeek - 1) / DaysPerWeek
    val monthLabel = remember(month) {
        month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM", tr)).replaceFirstChar { it.uppercase(tr) }
    }
    val summary = stringResource(R.string.days_month_summary, litThisMonth)
    val longestRun = remember(litDates, month) { longestRunIn(litDates, month) }
    val runText = if (longestRun > 1) stringResource(R.string.days_constellation, longestRun) else stringResource(R.string.days_constellation_none)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTokens.Radius.lg))
            .background(night.sky)
            .padding(AppTokens.Spacing.md)
            .clearAndSetSemantics { contentDescription = "$monthLabel. $summary. $runText" },
        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
            Icon(Icons.Rounded.Bedtime, contentDescription = null, tint = night.moon, modifier = Modifier.size(AppTokens.IconSize.md))
            Text(monthLabel, style = MaterialTheme.typography.headlineMedium, color = night.text, modifier = Modifier.weight(1f))
            Text(summary, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold), color = night.moon)
        }
        Text(runText, style = MaterialTheme.typography.bodyMedium, color = night.muted)

        val dayCodes = turkishDayShortCode()
        Row(modifier = Modifier.fillMaxWidth().padding(top = AppTokens.Spacing.xs)) {
            DayOfWeek.entries.forEach { dow ->
                Text(
                    text = dayCodes.getValue(dow),
                    style = MaterialTheme.typography.labelSmall,
                    color = night.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cell: Dp = maxWidth / DaysPerWeek
            Box(modifier = Modifier.fillMaxWidth().height(cell * rows)) {
                // Takımyıldızı çizgileri: art arda yanık günler
                Canvas(modifier = Modifier.matchParentSize()) {
                    val c = cell.toPx()
                    fun center(index: Int) = Offset((index % DaysPerWeek) * c + c / 2f, (index / DaysPerWeek) * c + c / 2f)
                    val stroke = AppTokens.Star.stroke.toPx()
                    cells.forEachIndexed { i, date ->
                        if (date != null && i % DaysPerWeek != DaysPerWeek - 1 && date in litDates && date.plusDays(1) in litDates) {
                            drawLine(night.moon.copy(alpha = 0.45f), center(i), center(i + 1), strokeWidth = stroke, cap = StrokeCap.Round)
                        }
                    }
                }
                cells.forEachIndexed { i, date ->
                    if (date == null) return@forEachIndexed
                    Box(
                        modifier = Modifier
                            .offset(x = cell * (i % DaysPerWeek), y = cell * (i / DaysPerWeek))
                            .size(cell),
                        contentAlignment = Alignment.Center,
                    ) {
                        NightDay(date = date, lit = date in litDates, today = today, cell = cell)
                    }
                }
            }
        }
    }
}

@Composable
private fun NightDay(date: LocalDate, lit: Boolean, today: LocalDate, cell: Dp) {
    val night = AppTheme.colors.night
    val isToday = date == today
    val ring = AppTheme.colors.progress.done
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(cell)) {
        if (isToday) {
            Canvas(modifier = Modifier.size(cell * 0.82f)) {
                drawCircle(ring, style = Stroke(width = AppTokens.Star.stroke.toPx() * 1.4f))
            }
        }
        when {
            lit -> StarIcon(lit = true, size = AppTokens.Sky.starSmall, litColor = night.moon, edgeColor = night.moon, idleColor = night.line)
            date.isAfter(today) -> Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = night.muted.copy(alpha = 0.45f),
            )
            isToday -> Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = night.text,
            )
            else -> Canvas(modifier = Modifier.size(AppTokens.BottomNav.indicatorDot)) { drawCircle(night.line) }
        }
    }
}

/** Ay içindeki en uzun art arda hedefli gün sayısı (takımyıldızın en uzun kolu). */
internal fun longestRunIn(lit: Set<LocalDate>, month: YearMonth): Int {
    var best = 0
    var run = 0
    for (d in 1..month.lengthOfMonth()) {
        if (month.atDay(d) in lit) { run++; best = maxOf(best, run) } else run = 0
    }
    return best
}
