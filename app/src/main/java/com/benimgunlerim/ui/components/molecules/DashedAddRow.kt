package com.benimgunlerim.ui.components.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens

/**
 * Boş bir alanı doldurmaya davet eden kesik çizgili satır: içerik gibi değil, doldurulacak yer gibi durur.
 * Bugün (boş bölüm) ve Plan (boş gün) kullanır.
 */
@Composable
fun DashedAddRow(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val line = MaterialTheme.colorScheme.outline
    val radius = AppTokens.Radius.lg
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppTokens.Layout.fabDefaultSize)
            .clip(RoundedCornerShape(radius))
            .drawBehind {
                val stroke = AppTokens.BorderWidth.thin.toPx() * 1.5f
                drawRoundRect(
                    color = line,
                    cornerRadius = CornerRadius(radius.toPx()),
                    style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 4, stroke * 3))),
                )
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = AppTokens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, tint = AppTheme.colors.progress.doneInk, modifier = Modifier.size(AppTokens.IconSize.sm))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = AppTheme.colors.progress.doneInk,
        )
    }
}
