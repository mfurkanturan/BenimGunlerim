package com.benimgunlerim.ui.components.core

import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.theme.AppTokens

enum class AppBadgeVariant { Success, Warning, Error, Info, Neutral }

@Composable
fun AppBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: AppBadgeVariant = AppBadgeVariant.Neutral,
) {
    val (bg, fg) = when (variant) {
        AppBadgeVariant.Success -> AppTheme.colors.status.success.fg.copy(alpha = 0.12f) to AppTheme.colors.status.success.fg
        AppBadgeVariant.Warning -> AppTheme.colors.status.attention.fg.copy(alpha = 0.12f) to AppTheme.colors.status.attention.fg
        AppBadgeVariant.Error   -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)   to MaterialTheme.colorScheme.error
        AppBadgeVariant.Info    -> AppTheme.colors.status.info.fg.copy(alpha = 0.12f)    to AppTheme.colors.status.info.fg
        AppBadgeVariant.Neutral -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppTokens.Radius.sm))
            .background(bg)
            .padding(horizontal = AppTokens.Spacing.xs, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
        )
    }
}

// Renk doğrudan belirtilmek istendiğinde kullanılır (kategori rozeti gibi)
@Composable
fun AppColorBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppTokens.Radius.sm))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = AppTokens.Spacing.xs, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
