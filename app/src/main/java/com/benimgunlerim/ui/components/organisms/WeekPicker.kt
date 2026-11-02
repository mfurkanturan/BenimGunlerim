package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.DayPhase
import com.benimgunlerim.ui.theme.SkyPalette
import com.benimgunlerim.ui.theme.skyPaletteFor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private const val MaxMiniRays = 8
private const val PastAlpha = 0.45f

/**
 * Hafta ufku: yedi gün, yedi küçük gök. Her günün mini güneşi o gün planlı görev sayısı kadar ışın
 * taşır (en çok 8); planı olmayan gün sadece soluk bir halka. Geçmiş günler batmış gibi soluk,
 * seçili gün mürekkep dolgu, bugün kayısı nokta. Altta tepeler — Bugün ekranındaki manzaranın haftalık hali.
 */
@Composable
fun WeekPicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectToday: () -> Unit,
    modifier: Modifier = Modifier,
    taskCountByDate: Map<LocalDate, Int> = emptyMap(),
    weekStart: LocalDate = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1),
) {
    val haptic = LocalHapticFeedback.current
    val today = remember { LocalDate.now() }
    val tr = remember { Locale("tr") }
    val daysOfWeek = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", tr) }
    val sky = skyPaletteFor(DayPhase.Ogle)
    val showTodayAction = selectedDate != today

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppTokens.Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedDate.format(monthFormatter).replaceFirstChar { it.uppercase(tr) },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (showTodayAction) {
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectToday()
                }) {
                    Text(
                        text = stringResource(R.string.plan_today_action),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = AppTheme.colors.progress.doneInk,
                    )
                }
            }
            IconButton(onClick = onPreviousWeek, modifier = Modifier.size(AppTokens.TouchTarget.min)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.plan_previous_week_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onNextWeek, modifier = Modifier.size(AppTokens.TouchTarget.min)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.plan_next_week_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Ufuk: gök zemin + altta tepeler, üstünde 7 gün
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTokens.Radius.lg))
                .background(sky.sky),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val hills = AppTokens.Sky.weekHills.toPx()
                val base = size.height - hills
                val back = Path().apply {
                    moveTo(0f, base + hills * 0.35f)
                    cubicTo(size.width * 0.25f, base - hills * 0.05f, size.width * 0.55f, base + hills * 0.55f, size.width, base + hills * 0.15f)
                    lineTo(size.width, size.height); lineTo(0f, size.height); close()
                }
                val front = Path().apply {
                    moveTo(0f, base + hills * 0.8f)
                    cubicTo(size.width * 0.35f, base + hills * 0.45f, size.width * 0.7f, base + hills * 0.95f, size.width, base + hills * 0.6f)
                    lineTo(size.width, size.height); lineTo(0f, size.height); close()
                }
                drawPath(back, sky.hillBack)
                drawPath(front, sky.hillFront)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTokens.Spacing.xxs, vertical = AppTokens.Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
            ) {
                daysOfWeek.forEach { date ->
                    DayColumn(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        isPast = date.isBefore(today),
                        taskCount = taskCountByDate[date] ?: 0,
                        sky = sky,
                        locale = tr,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDateSelected(date)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayColumn(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isPast: Boolean,
    taskCount: Int,
    sky: SkyPalette,
    locale: Locale,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ink = MaterialTheme.colorScheme.onSurface
    val paper = MaterialTheme.colorScheme.surface
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val countText = pluralStringResource(R.plurals.plan_day_task_count, taskCount, taskCount)
    val cd = "${date.dayOfMonth} $dayName, $countText"
    val textColor = if (isSelected) paper else sky.onSky.copy(alpha = if (isPast) 0.6f else 1f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppTokens.Radius.md))
            .background(if (isSelected) ink else Color.Transparent)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = cd
                selected = isSelected
            }
            .padding(vertical = AppTokens.Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
        MiniSun(
            rays = taskCount.coerceAtMost(MaxMiniRays),
            sunColor = if (isSelected) AppTheme.colors.night.moon else sky.sun,
            rayColor = if (isSelected) AppTheme.colors.night.moon else AppTheme.colors.progress.done,
            ringColor = if (isSelected) paper.copy(alpha = 0.5f) else sky.onSkyMuted.copy(alpha = 0.45f),
            alpha = if (isPast && !isSelected) PastAlpha else 1f,
        )
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = textColor,
        )
        Box(
            modifier = Modifier
                .size(AppTokens.BottomNav.indicatorDot)
                .clip(CircleShape)
                .background(if (isToday) AppTheme.colors.progress.done else Color.Transparent),
        )
    }
}

/** Gün başına küçük güneş: görev yoksa soluk halka, varsa dolu çekirdek + görev sayısı kadar ışın. */
@Composable
private fun MiniSun(rays: Int, sunColor: Color, rayColor: Color, ringColor: Color, alpha: Float) {
    Canvas(modifier = Modifier.size(AppTokens.Sky.miniSun)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val core = AppTokens.Sky.miniSunCore.toPx()
        val stroke = AppTokens.Star.stroke.toPx()
        if (rays == 0) {
            drawCircle(ringColor, radius = core, center = center, style = Stroke(width = stroke), alpha = alpha)
            return@Canvas
        }
        drawCircle(sunColor, radius = core, center = center, alpha = alpha)
        drawCircle(rayColor, radius = core, center = center, style = Stroke(width = stroke), alpha = alpha)
        val gap = stroke * 1.5f
        val length = AppTokens.Sky.miniRay.toPx()
        for (i in 0 until rays) {
            val a = Math.toRadians(-90.0 + i * (360.0 / MaxMiniRays))
            val dir = Offset(cos(a).toFloat(), sin(a).toFloat())
            drawLine(
                rayColor,
                start = center + dir * (core + gap),
                end = center + dir * (core + gap + length),
                strokeWidth = stroke * 1.4f,
                cap = StrokeCap.Round,
                alpha = alpha,
            )
        }
    }
}
