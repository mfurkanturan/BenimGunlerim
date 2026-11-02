package com.benimgunlerim.ui.components.gamification

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.benimgunlerim.R

/** Başarım kazanıldı: gökyüzüne yeni bir yıldız. Rozetin emojisi ay ışığı haresinin ortasında. */
@Composable
fun AchievementDialog(
    id: String = "",
    emoji: String,
    title: String,
    description: String,
    xpReward: Int = 50,
    showParticles: Boolean = false,
    onDismiss: () -> Unit,
) {
    CelebrationCard(
        kicker = stringResource(R.string.celebration_achievement_unlocked_title),
        title = title,
        body = description,
        rewardText = if (xpReward > 0) stringResource(R.string.celebration_xp_reward_badge, xpReward) else null,
        actionText = stringResource(R.string.celebration_achievement_action),
        onDismiss = onDismiss,
        showParticles = showParticles,
    ) {
        Text(text = emoji, style = TextStyle(fontSize = EmojiSize))
    }
}

private val EmojiSize = 44.sp
