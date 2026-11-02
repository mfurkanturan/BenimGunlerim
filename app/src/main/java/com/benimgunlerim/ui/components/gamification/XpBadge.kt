package com.benimgunlerim.ui.components.gamification

import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun XpBadge(xp: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppTokens.Radius.pill))
            .background(AppTheme.colors.reward.xpSoft)
            .padding(horizontal = AppTokens.Spacing.sm, vertical = AppTokens.Spacing.xxs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "⚡ $xp XP",
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.reward.xp,
        )
    }
}

