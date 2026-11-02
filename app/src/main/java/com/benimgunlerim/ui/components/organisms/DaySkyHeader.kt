package com.benimgunlerim.ui.components.organisms

import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.benimgunlerim.R
import kotlin.math.cos
import kotlin.math.sin
import com.benimgunlerim.ui.components.core.starPath
import com.benimgunlerim.ui.components.molecules.SpeechBubble
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BenimGunlerimTheme
import com.benimgunlerim.ui.theme.DayPhase
import com.benimgunlerim.ui.theme.SkyPalette
import com.benimgunlerim.ui.theme.skyPaletteFor

/** Gece yıldızlarının konumu (genişlik, başlık yüksekliği oranı) — ayın çevresinde, ona değmeden. */
private val StarSpots = listOf(
    Offset(0.62f, 0.12f),
    Offset(0.93f, 0.16f),
    Offset(0.64f, 0.42f),
    Offset(0.95f, 0.46f),
    Offset(0.84f, 0.58f),
    Offset(0.72f, 0.60f),
    Offset(0.56f, 0.28f),
)

/** Gündüz güneşin ışın sayısı; ilerleme oranı kadarı yanar. */
private const val SunRays = 8

/**
 * Bugün ekranının başlığı: günün saatine göre renklenen düz bir gök, iki tepe ve Mino.
 * Bugünün adımları göğe ışık olarak düşer: gündüz güneşin ışınları, gece ayın çevresindeki yıldızlar.
 * Tamamlanan her adım bir ışını/yıldızı yakar. Durum çubuğunun altına uzanır.
 */
@Composable
fun DaySkyHeader(
    phase: DayPhase,
    greeting: String,
    dateText: String,
    litCount: Int,
    totalCount: Int,
    minoLine: String,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = skyPaletteFor(phase)
    val statusBarPx = WindowInsets.statusBars.getTop(LocalDensity.current).toFloat()
    val lightsCd = stringResource(R.string.sky_lights_cd, litCount, totalCount)
    val paper = MaterialTheme.colorScheme.background

    Box(modifier = modifier.fillMaxWidth().background(palette.sky)) {
        Canvas(modifier = Modifier.matchParentSize().clipToBounds()) {
            val headerHeight = size.height - statusBarPx
            if (palette.isMoon) {
                drawMoon(palette, statusBarPx, headerHeight)
                val visible = totalCount.coerceAtMost(StarSpots.size)
                for (i in 0 until visible) {
                    val spot = StarSpots[i]
                    val starPx = (if (i % 2 == 0) AppTokens.Sky.starLarge else AppTokens.Sky.starSmall).toPx()
                    drawSkyStar(
                        center = Offset(size.width * spot.x, statusBarPx + headerHeight * spot.y),
                        sizePx = starPx,
                        lit = i < litCount.coerceAtMost(visible),
                        palette = palette,
                    )
                }
            } else {
                drawSunWithRays(palette, statusBarPx, headerHeight, litCount, totalCount)
            }
            drawHills(palette)
            drawPaperLip(paper)
        }

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .height(AppTokens.Sky.headerHeight)
                .fillMaxWidth()
                .padding(
                    start = AppTokens.Spacing.screenHorizontal,
                    end = AppTokens.Spacing.screenHorizontal,
                    top = AppTokens.Spacing.xs,
                    bottom = AppTokens.Sky.sheetOverlap + AppTokens.Spacing.xxs,
                ),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.onSkyMuted,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onNotificationClick) {
                    Box(
                        modifier = Modifier.size(AppTokens.TouchTarget.compact).background(palette.chip, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsNone,
                            contentDescription = stringResource(R.string.app_notifications_cd),
                            tint = palette.onSky,
                            modifier = Modifier.size(AppTokens.IconSize.sm),
                        )
                    }
                }
            }
            Text(
                text = greeting,
                style = MaterialTheme.typography.displayLarge,
                color = palette.onSky,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.66f),
            )
            if (totalCount > 0) {
                Text(
                    text = stringResource(R.string.sky_lights_caption, litCount, totalCount),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.onSkyMuted,
                    modifier = Modifier
                        .padding(top = AppTokens.Spacing.xs)
                        .semantics { contentDescription = lightsCd },
                )
            } else {
                Text(
                    text = stringResource(R.string.sky_empty_caption),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = palette.onSkyMuted,
                    modifier = Modifier.padding(top = AppTokens.Spacing.xs),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                Image(
                    painter = painterResource(R.drawable.mascot_mino),
                    contentDescription = stringResource(R.string.mascot_content_description),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(AppTokens.Sky.mascotWidth, AppTokens.Sky.mascotHeight),
                )
                SpeechBubble(
                    text = minoLine,
                    background = if (palette.isMoon) AppTheme.colors.night.card else MaterialTheme.colorScheme.surface,
                    textColor = if (palette.isMoon) AppTheme.colors.night.text else MaterialTheme.colorScheme.onSurface,
                    borderColor = null,
                    modifier = Modifier
                        .padding(bottom = AppTokens.Spacing.md)
                        .widthIn(max = AppTokens.Sky.bubbleWidth),
                )
            }
        }
    }
}

private fun DrawScope.drawMoon(palette: SkyPalette, top: Float, headerHeight: Float) {
    val radius = AppTokens.Sky.sunDiameter.toPx() / 4f
    val center = Offset(size.width * 0.78f, top + headerHeight * palette.sunY)
    drawCircle(palette.sun, radius = radius, center = center)
    // Hilal: ayın üstüne gök renginde kaydırılmış bir daire
    drawCircle(palette.sky, radius = radius * 0.94f, center = center + Offset(radius * 0.48f, -radius * 0.32f))
}

/**
 * Güneş ve çevresindeki ışınlar: adımların tamamlanma oranı kadar ışın yanar (dolu, kalın),
 * kalanlar soluk ince çizgi. Hiç adım yoksa ışınsız sade güneş.
 */
private fun DrawScope.drawSunWithRays(palette: SkyPalette, top: Float, headerHeight: Float, lit: Int, total: Int) {
    val radius = AppTokens.Sky.sunDiameter.toPx() * 0.36f
    val center = Offset(size.width * 0.72f, top + headerHeight * palette.sunY)
    // Hiç hedef yoksa güneş soluk: gök henüz boş
    drawCircle(palette.sun, radius = radius, center = center, alpha = if (total > 0) 1f else 0.5f)
    if (total <= 0) return
    val litRays = if (lit >= total) SunRays else ((lit.toFloat() / total) * SunRays).toInt()
    val gap = AppTokens.Spacing.xs.toPx()
    val length = AppTokens.Sky.rayLength.toPx()
    val litStroke = AppTokens.Sky.rayStroke.toPx()
    val idleStroke = AppTokens.Star.stroke.toPx()
    for (i in 0 until SunRays) {
        // Tepeden başlayıp saat yönünde yanar
        val a = Math.toRadians(-90.0 + i * (360.0 / SunRays))
        val dir = Offset(cos(a).toFloat(), sin(a).toFloat())
        val start = center + dir * (radius + gap)
        val end = center + dir * (radius + gap + length)
        if (i < litRays) {
            drawLine(palette.starLit, start, end, strokeWidth = litStroke, cap = StrokeCap.Round)
        } else {
            drawLine(palette.starIdle.copy(alpha = 0.35f), start, end, strokeWidth = idleStroke, cap = StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawHills(palette: SkyPalette) {
    val hillsHeight = AppTokens.Sky.hillsHeight.toPx()
    val sx = size.width / 390f
    val sy = hillsHeight / 130f
    val baseY = size.height - hillsHeight
    fun p(x: Float, y: Float) = Offset(x * sx, baseY + y * sy)
    val back = Path().apply {
        moveTo(p(0f, 58f).x, p(0f, 58f).y)
        cubicTo(p(70f, 30f).x, p(70f, 30f).y, p(130f, 34f).x, p(130f, 34f).y, p(196f, 52f).x, p(196f, 52f).y)
        cubicTo(p(262f, 70f).x, p(262f, 70f).y, p(320f, 40f).x, p(320f, 40f).y, p(390f, 46f).x, p(390f, 46f).y)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    val front = Path().apply {
        moveTo(p(0f, 96f).x, p(0f, 96f).y)
        cubicTo(p(80f, 70f).x, p(80f, 70f).y, p(150f, 88f).x, p(150f, 88f).y, p(220f, 82f).x, p(220f, 82f).y)
        cubicTo(p(290f, 76f).x, p(290f, 76f).y, p(340f, 64f).x, p(340f, 64f).y, p(390f, 78f).x, p(390f, 78f).y)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(back, palette.hillBack)
    drawPath(front, palette.hillFront)
}

private fun DrawScope.drawSkyStar(center: Offset, sizePx: Float, lit: Boolean, palette: SkyPalette) {
    val path = starPath(Size(sizePx, sizePx))
    val stroke = AppTokens.Star.stroke.toPx()
    translate(center.x - sizePx / 2f, center.y - sizePx / 2f) {
        if (lit) {
            drawPath(path, palette.starLit)
        } else {
            drawPath(
                path,
                palette.starIdle,
                style = Stroke(
                    width = stroke,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 1.4f, stroke * 1.4f)),
                ),
            )
        }
    }
}

@Preview(name = "Sabah · 0/3", widthDp = 390)
@Composable
private fun DaySkyHeaderMorningPreview() = BenimGunlerimTheme {
    DaySkyHeader(DayPhase.Sabah, "Günaydın,\nFurkan.", "1 Kasım, Pazar", 0, 3, "Güne tek bir adımla başla, yeter.", {})
}

@Preview(name = "Öğle · 1/3", widthDp = 390)
@Composable
private fun DaySkyHeaderNoonPreview() = BenimGunlerimTheme {
    DaySkyHeader(DayPhase.Ogle, "İyi günler.", "1 Kasım, Pazar", 1, 3, "1 ışık yandı. Bir tane daha?", {})
}

@Preview(name = "Akşam · 2/3", widthDp = 390)
@Composable
private fun DaySkyHeaderEveningPreview() = BenimGunlerimTheme {
    DaySkyHeader(DayPhase.Aksam, "Gün batıyor,\nFurkan.", "1 Kasım, Pazar", 2, 3, "2 ışık yandı. Bir tane daha?", {})
}

@Preview(name = "Gece · 3/3", widthDp = 390)
@Composable
private fun DaySkyHeaderNightPreview() = BenimGunlerimTheme {
    DaySkyHeader(DayPhase.Gece, "İyi geceler.", "1 Kasım, Pazar", 3, 3, "Bütün ışıklar yandı.", {})
}

/** Gökün altındaki kâğıt kenarı: içerik göğün üzerine kayan bir sayfa gibi başlar. */
private fun DrawScope.drawPaperLip(paper: Color) {
    val lip = AppTokens.Sky.sheetOverlap.toPx()
    val corner = CornerRadius(lip, lip)
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = 0f,
                top = size.height - lip,
                right = size.width,
                bottom = size.height,
                topLeftCornerRadius = corner,
                topRightCornerRadius = corner,
            ),
        )
    }
    drawPath(path, paper)
}
