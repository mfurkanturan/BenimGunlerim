package com.benimgunlerim.ui.routines

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.DayPhase
import com.benimgunlerim.ui.theme.skyPaletteFor
import kotlin.math.PI
import kotlin.math.sin

private const val PathDays = 7

/** Yolun i. taşının (0 = 6 gün önce, 6 = bugün) kart içindeki konumu; tepeler boyunca hafif dalgalı. */
private fun stoneFraction(i: Int): Pair<Float, Float> {
    val x = 0.08f + i * (0.84f / (PathDays - 1))
    val y = 0.70f - 0.10f * sin(i / (PathDays - 1f) * PI.toFloat())
    return x to y
}

/**
 * Haftanın yolu: son 7 gün tepeler üzerinde yedi taş. Bir taş, o gün yapılan rutin oranı kadar
 * kayısı ışığıyla dolar; hiç yapılmayan gün boş halka. Mino bugünün taşında durur — "yavaş ama durmadan".
 */
@Composable
internal fun WeekPathCard(routines: List<RoutineCardUi>, longestChain: Int, modifier: Modifier = Modifier) {
    val sky = skyPaletteFor(DayPhase.Sabah)
    val fractions = remember(routines) {
        (0 until PathDays).map { i ->
            val done = routines.count { it.last7Days.getOrNull(it.last7Days.size - PathDays + i) == true }
            if (routines.isEmpty()) 0f else done.toFloat() / routines.size
        }
    }
    val activeDays = fractions.count { it > 0f }
    val done = AppTheme.colors.progress.done
    val edge = AppTheme.colors.progress.doneEdge
    val paper = MaterialTheme.colorScheme.surface
    val trail = sky.onSkyMuted.copy(alpha = 0.35f)
    val headline = if (activeDays > 0) {
        stringResource(R.string.routines_path_days, activeDays)
    } else {
        stringResource(R.string.routines_path_none)
    }
    val minoLine = if (longestChain > 0) {
        stringResource(R.string.mino_routines_chain, longestChain)
    } else {
        stringResource(R.string.mino_routines_empty_chain)
    }
    val cd = stringResource(R.string.routines_path_cd, activeDays)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTokens.Radius.lg))
            .background(sky.sky)
            .clearAndSetSemantics { contentDescription = "$headline. $minoLine. $cd" },
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(AppTokens.Sky.pathCardHeight)) {
            val w = maxWidth
            val h = maxHeight
            Canvas(modifier = Modifier.matchParentSize()) {
                val hillTop = size.height * 0.62f
                val back = Path().apply {
                    moveTo(0f, hillTop)
                    cubicTo(size.width * 0.3f, hillTop - size.height * 0.12f, size.width * 0.65f, hillTop + size.height * 0.1f, size.width, hillTop - size.height * 0.04f)
                    lineTo(size.width, size.height); lineTo(0f, size.height); close()
                }
                drawPath(back, sky.hillBack)
                val front = Path().apply {
                    moveTo(0f, size.height * 0.84f)
                    cubicTo(size.width * 0.4f, size.height * 0.74f, size.width * 0.7f, size.height * 0.92f, size.width, size.height * 0.8f)
                    lineTo(size.width, size.height); lineTo(0f, size.height); close()
                }
                drawPath(front, sky.hillFront)

                val points = (0 until PathDays).map { i ->
                    val (fx, fy) = stoneFraction(i)
                    Offset(size.width * fx, size.height * fy)
                }
                val stroke = AppTokens.Star.stroke.toPx()
                val trailPath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(trailPath, trail, style = Stroke(width = stroke * 2, pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 3, stroke * 3))))
                val r = AppTokens.Sky.pathStone.toPx() / 2f
                points.forEachIndexed { i, p ->
                    drawCircle(paper, radius = r, center = p)
                    val f = fractions[i]
                    if (f > 0f) {
                        drawCircle(done, radius = r * (0.45f + 0.55f * f), center = p)
                    }
                    drawCircle(if (f > 0f) edge else trail, radius = r, center = p, style = Stroke(width = stroke))
                }
            }
            // Mino bugünün taşında
            val (tx, ty) = stoneFraction(PathDays - 1)
            val mino = AppTokens.IconSize.xl
            Image(
                painter = painterResource(R.drawable.mascot_mino),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(mino)
                    .offset(x = w * tx - mino / 2, y = h * ty - mino - AppTokens.Sky.pathStone / 2 + AppTokens.Spacing.xxs),
            )
            Column(modifier = Modifier.padding(AppTokens.Spacing.md)) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineMedium,
                    color = sky.onSky,
                )
                Text(
                    text = minoLine,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = sky.onSkyMuted,
                    modifier = Modifier.fillMaxWidth(0.82f),
                )
            }
        }
    }
}
