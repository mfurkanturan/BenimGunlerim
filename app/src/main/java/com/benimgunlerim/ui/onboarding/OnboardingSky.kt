package com.benimgunlerim.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.molecules.SpeechBubble
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.DayPhase
import com.benimgunlerim.ui.theme.skyPaletteFor
import kotlin.math.cos
import kotlin.math.sin

/** Onboarding bir gün doğumu: her adım göğü biraz daha aydınlatır. */
private val StepPhases = listOf(DayPhase.Gece, DayPhase.Aksam, DayPhase.Sabah, DayPhase.Ogle, DayPhase.Ogle)

/** Güneşin (ya da ilk adımda ayın) gök yüksekliğine oranla merkezi: ufuktan tepeye yükselir. */
private val StepSunY = listOf(0.30f, 0.86f, 0.62f, 0.42f, 0.30f)

private val NightStars = listOf(Offset(0.12f, 0.22f), Offset(0.30f, 0.12f), Offset(0.52f, 0.28f), Offset(0.88f, 0.18f), Offset(0.70f, 0.50f))

/**
 * Adım adım doğan gök: gece (ay + yıldızlar) → şafak → sabah → öğle → tam güneş (8 ışın).
 * Üstte adım noktaları ve "Atla"; altta tepeler ve Mino'nun konuşma balonu.
 */
@Composable
internal fun OnboardingSky(
    step: Int,
    totalSteps: Int,
    minoLine: String?,
    onSkip: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val target = skyPaletteFor(StepPhases[step.coerceIn(0, StepPhases.lastIndex)])
    val anim = tween<androidx.compose.ui.graphics.Color>(AppTokens.Motion.slow)
    val sky by animateColorAsState(target.sky, anim, label = "onb_sky")
    val hillBack by animateColorAsState(target.hillBack, anim, label = "onb_hill_back")
    val hillFront by animateColorAsState(target.hillFront, anim, label = "onb_hill_front")
    val onSky by animateColorAsState(target.onSky, anim, label = "onb_on_sky")
    val sunY by animateFloatAsState(StepSunY[step.coerceIn(0, StepSunY.lastIndex)], tween(AppTokens.Motion.slow), label = "onb_sun_y")
    val isNight = step == 0
    val fullSun = step == totalSteps - 1
    val rayColor = AppTheme.colors.progress.done
    val stepCd = stringResource(R.string.onboarding_step_format, step + 1, totalSteps)
    val paper = MaterialTheme.colorScheme.background

    Box(modifier = modifier.fillMaxWidth().background(sky)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val r = AppTokens.Sky.sunDiameter.toPx() * 0.32f
            val center = Offset(size.width * 0.74f, size.height * sunY)
            if (isNight) {
                NightStars.forEach { drawCircle(target.starLit, radius = AppTokens.Star.stroke.toPx() * 1.6f, center = Offset(size.width * it.x, size.height * it.y)) }
                drawCircle(target.sun, radius = r * 0.7f, center = center)
                drawCircle(sky, radius = r * 0.66f, center = center + Offset(r * 0.32f, -r * 0.2f))
            } else {
                drawCircle(target.sun, radius = r, center = center)
                if (fullSun) {
                    val stroke = AppTokens.Sky.rayStroke.toPx()
                    val gap = AppTokens.Spacing.xs.toPx()
                    val len = AppTokens.Sky.rayLength.toPx()
                    for (i in 0 until 8) {
                        val a = Math.toRadians(-90.0 + i * 45.0)
                        val d = Offset(cos(a).toFloat(), sin(a).toFloat())
                        drawLine(rayColor, center + d * (r + gap), center + d * (r + gap + len), strokeWidth = stroke, cap = StrokeCap.Round)
                    }
                }
            }
            drawOnboardingHills(hillBack, hillFront)
            val lip = AppTokens.Sky.sheetOverlap.toPx()
            drawPath(
                Path().apply {
                    addRoundRect(RoundRect(0f, size.height - lip, size.width, size.height, topLeftCornerRadius = CornerRadius(lip), topRightCornerRadius = CornerRadius(lip)))
                },
                color = paper,
            )
        }
        Column(modifier = Modifier.statusBarsPadding().height(AppTokens.Sky.onboardingSky).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppTokens.Spacing.screenHorizontal),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (totalSteps > 1) {
                    StepDots(step = step, total = totalSteps, color = onSky, modifier = Modifier.weight(1f).semantics { contentDescription = stepCd })
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
                if (onSkip != null) {
                    TextButton(onClick = onSkip) {
                        Text(stringResource(R.string.onboarding_skip), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold), color = onSky)
                    }
                }
            }
            Box(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.padding(start = AppTokens.Spacing.md, bottom = AppTokens.Spacing.md),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
            ) {
                Image(
                    painter = painterResource(R.drawable.mascot_mino),
                    contentDescription = stringResource(R.string.mascot_content_description),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(AppTokens.Sky.mascotWidth, AppTokens.Sky.mascotHeight),
                )
                if (minoLine != null) {
                    SpeechBubble(
                        text = minoLine,
                        background = if (isNight) AppTheme.colors.night.card else MaterialTheme.colorScheme.surface,
                        textColor = if (isNight) AppTheme.colors.night.text else MaterialTheme.colorScheme.onSurface,
                        borderColor = null,
                        modifier = Modifier.padding(bottom = AppTokens.Spacing.md).widthIn(max = AppTokens.Sky.bubbleWidth),
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOnboardingHills(back: androidx.compose.ui.graphics.Color, front: androidx.compose.ui.graphics.Color) {
    val h = AppTokens.Sky.hillsHeight.toPx() * 0.8f
    val base = size.height - h
    drawPath(
        Path().apply {
            moveTo(0f, base + h * 0.35f)
            cubicTo(size.width * 0.3f, base, size.width * 0.6f, base + h * 0.45f, size.width, base + h * 0.2f)
            lineTo(size.width, size.height); lineTo(0f, size.height); close()
        },
        back,
    )
    drawPath(
        Path().apply {
            moveTo(0f, base + h * 0.75f)
            cubicTo(size.width * 0.35f, base + h * 0.5f, size.width * 0.7f, base + h * 0.9f, size.width, base + h * 0.6f)
            lineTo(size.width, size.height); lineTo(0f, size.height); close()
        },
        front,
    )
}

/** Adım noktaları: geçilenler dolu, şimdiki uzun hap. */
@Composable
private fun StepDots(step: Int, total: Int, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .height(AppTokens.BottomNav.indicatorDot)
                    .size(width = if (i == step) AppTokens.Spacing.xl else AppTokens.BottomNav.indicatorDot, height = AppTokens.BottomNav.indicatorDot)
                    .clip(RoundedCornerShape(AppTokens.Radius.pill))
                    .background(color.copy(alpha = if (i <= step) 1f else 0.3f)),
            )
        }
    }
}
