package com.benimgunlerim.ui.components.gamification

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.benimgunlerim.ui.components.molecules.SheetSaveButton
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.NightSurfaceTheme

/** Kartın gökyüzündeki küçük yıldızların konumu (genişlik, yükseklik oranı). */
private val Twinkles = listOf(
    Offset(0.10f, 0.10f), Offset(0.24f, 0.30f), Offset(0.86f, 0.12f), Offset(0.74f, 0.32f),
    Offset(0.92f, 0.44f), Offset(0.06f, 0.46f), Offset(0.40f, 0.06f), Offset(0.62f, 0.08f),
)

/**
 * Kutlama anı: gece göğü kartı, hafifçe göz kırpan yıldızlar, ortada ay ışığı haresi içinde rozet.
 * Başarım ve seviye ekranları bunu kullanır — "gökyüzüne yeni bir yıldız eklendi".
 */
@Composable
fun CelebrationCard(
    kicker: String,
    title: String,
    body: String,
    rewardText: String?,
    actionText: String,
    onDismiss: () -> Unit,
    showParticles: Boolean = false,
    badge: @Composable () -> Unit,
) {
    val night = AppTheme.colors.night
    val pulse = rememberInfiniteTransition(label = "celebration")
    val twinkle by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(AppTokens.Motion.pulse * 2, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "twinkle",
    )
    val halo by pulse.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(AppTokens.Motion.pulse * 3, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "halo",
    )

    Dialog(onDismissRequest = onDismiss) {
        NightSurfaceTheme {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppTokens.Radius.xxl))
                    .background(night.sky),
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val dot = AppTokens.Star.stroke.toPx() * 1.6f
                    Twinkles.forEachIndexed { i, p ->
                        val a = if (i % 2 == 0) twinkle else 1.35f - twinkle
                        drawCircle(night.moon, radius = dot, center = Offset(size.width * p.x, size.height * p.y), alpha = a.coerceIn(0.2f, 1f))
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(AppTokens.Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(AppTokens.Calm.resetCompletedIcon * 2)) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val r = size.minDimension / 2f
                            drawCircle(night.moon, radius = r * halo, alpha = 0.10f)
                            drawCircle(night.moon, radius = r * 0.78f, alpha = 0.16f)
                            drawCircle(night.moon, radius = r * 0.62f, style = Stroke(width = AppTokens.Star.stroke.toPx() * 1.5f))
                        }
                        badge()
                    }
                    Text(
                        text = kicker,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = night.moon,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = night.text,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(text = body, style = MaterialTheme.typography.bodyMedium, color = night.muted, textAlign = TextAlign.Center)
                    if (rewardText != null) {
                        Text(
                            text = rewardText,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = night.moon,
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppTokens.Radius.pill))
                                .background(night.card)
                                .padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.xs),
                        )
                    }
                    SheetSaveButton(text = actionText, onClick = onDismiss, modifier = Modifier.padding(top = AppTokens.Spacing.sm))
                }
                if (showParticles) SubtleCelebrationParticles(modifier = Modifier.matchParentSize())
            }
        }
    }
}
