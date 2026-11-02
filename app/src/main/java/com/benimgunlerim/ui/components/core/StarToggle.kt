package com.benimgunlerim.ui.components.core

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import com.benimgunlerim.ui.theme.AppTokens
import kotlin.math.cos
import kotlin.math.sin

/** Beş köşeli yıldız; [size] içine ortalanır. */
fun starPath(size: Size): Path {
    val cx = size.width / 2f
    val cy = size.height / 2f + size.height * 0.04f
    val outer = minOf(size.width, size.height) / 2f * 0.96f
    val inner = outer * 0.46f
    return Path().apply {
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outer else inner
            val angle = Math.toRadians(-90.0 + i * 36.0)
            val point = Offset(cx + (r * cos(angle)).toFloat(), cy + (r * sin(angle)).toFloat())
            if (i == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        }
        close()
    }
}

/**
 * Dekoratif yıldız. [lit] true ise dolu, değilse kenar çizgisi; [dashed] sönük yıldızı kesik çizer.
 */
@Composable
fun StarIcon(
    lit: Boolean,
    size: Dp,
    litColor: Color,
    edgeColor: Color,
    idleColor: Color,
    modifier: Modifier = Modifier,
    dashed: Boolean = false,
) {
    Canvas(modifier = modifier.size(size)) {
        val path = starPath(this.size)
        val strokeWidth = AppTokens.Star.stroke.toPx()
        if (lit) {
            drawPath(path, litColor)
            drawPath(path, edgeColor, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
        } else {
            drawPath(
                path,
                idleColor,
                style = Stroke(
                    width = strokeWidth,
                    join = StrokeJoin.Round,
                    pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(strokeWidth * 1.6f, strokeWidth * 1.6f)) else null,
                ),
            )
        }
    }
}

/**
 * Tamamlama kontrolü: yanan / sönük yıldız. 48dp dokunma alanı, ekran okuyucuda onay kutusu.
 */
@Composable
fun StarToggle(
    lit: Boolean,
    onToggle: () -> Unit,
    stateLabel: String,
    modifier: Modifier = Modifier,
    litColor: Color,
    edgeColor: Color,
    idleColor: Color,
    actionLabel: String? = null,
    enabled: Boolean = true,
) {
    val scale by animateFloatAsState(
        targetValue = if (lit) 1f else 0.92f,
        animationSpec = tween(AppTokens.Motion.fast),
        label = "star_scale",
    )
    Box(
        modifier = modifier
            .size(AppTokens.TouchTarget.min)
            .toggleable(value = lit, enabled = enabled, role = Role.Checkbox, onValueChange = { onToggle() })
            .semantics {
                stateDescription = stateLabel
                if (actionLabel != null) contentDescription = actionLabel
            },
        contentAlignment = Alignment.Center,
    ) {
        StarIcon(
            lit = lit,
            size = AppTokens.Star.row,
            litColor = litColor,
            edgeColor = edgeColor,
            idleColor = idleColor,
            modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        )
    }
}
