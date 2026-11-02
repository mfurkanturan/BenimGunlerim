package com.benimgunlerim.ui.components.gamification

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTheme

/** Seviye atlandı: hare içinde büyük seviye numarası; aynı gece göğü kutlama kartı. */
@Composable
fun LevelUpDialog(
    level: Int,
    title: String,
    xpBonus: Int = 100,
    showParticles: Boolean = true,
    onDismiss: () -> Unit,
) {
    CelebrationCard(
        kicker = stringResource(R.string.celebration_level_up_title),
        title = stringResource(R.string.celebration_level_up_subtitle, level, title),
        body = stringResource(R.string.celebration_level_up_body),
        rewardText = if (xpBonus > 0) stringResource(R.string.celebration_xp_bonus_badge, xpBonus) else null,
        actionText = stringResource(R.string.celebration_action_continue),
        onDismiss = onDismiss,
        showParticles = showParticles,
    ) {
        Text(text = level.toString(), style = MaterialTheme.typography.displayLarge, color = AppTheme.colors.night.moon)
    }
}
