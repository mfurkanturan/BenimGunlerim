package com.benimgunlerim.ui.components.calm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens
import kotlin.math.PI
import kotlin.math.cos

/** Bir nefes döngüsü: 4 sn al, 6 sn ver (uzun nefes verme sakinleştirir). */
internal const val InhaleMs = 4_000L
internal const val ExhaleMs = 6_000L
internal const val BreathCycleMs = InhaleMs + ExhaleMs
private const val MinScale = 0.62f

/** Döngü içinde nefes alınıyor mu? Etiket ve çember aynı saatten okur — hiç ayrışmaz. */
internal fun isInhaling(elapsedMs: Long): Boolean = elapsedMs % BreathCycleMs < InhaleMs

/** Çemberin göreli boyutu (MinScale..1): alırken büyür, verirken küçülür; kenarlarda yumuşak. */
internal fun breathScale(elapsedMs: Long): Float {
    val t = elapsedMs % BreathCycleMs
    val p = if (t < InhaleMs) t.toFloat() / InhaleMs else 1f - (t - InhaleMs).toFloat() / ExhaleMs
    val eased = (1f - cos(p * PI.toFloat())) / 2f
    return MinScale + (1f - MinScale) * eased
}

/**
 * Nefes ayı: ay ışığı dairesi nefesle büyüyüp küçülür; çevresindeki halka kalan süreyi gösterir.
 * Tek kaynak [elapsedMs] — zamanlayıcı, çember ve "Nefes al / ver" yazısı birlikte ilerler.
 */
@Composable
internal fun BreathingMoon(elapsedMs: Long, totalMs: Long, modifier: Modifier = Modifier) {
    val night = AppTheme.colors.night
    val scale = breathScale(elapsedMs)
    val progress = (elapsedMs.toFloat() / totalMs).coerceIn(0f, 1f)
    val label = stringResource(if (isInhaling(elapsedMs)) R.string.reset_breathe_in else R.string.reset_breathe_out)
    Box(modifier = modifier.size(AppTokens.Calm.breathingCircleOuter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val r = size.minDimension / 2f
            val stroke = AppTokens.Calm.breathingBorder.toPx()
            drawCircle(night.line, radius = r - stroke, style = Stroke(width = stroke))
            drawArc(
                color = night.moon,
                startAngle = -90f,
                sweepAngle = 360f * (1f - progress),
                useCenter = false,
                topLeft = Offset(stroke, stroke),
                size = Size(size.width - stroke * 2, size.height - stroke * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            val inner = (r - stroke * 4) * scale
            drawCircle(night.moon, radius = inner * 1.12f, alpha = 0.12f)
            drawCircle(night.moon, radius = inner, alpha = 0.9f)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = night.sky,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}
