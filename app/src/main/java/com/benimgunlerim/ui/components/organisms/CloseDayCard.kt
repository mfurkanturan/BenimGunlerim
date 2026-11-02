package com.benimgunlerim.ui.components.organisms

import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.theme.AppTokens

/** Günü kapatma satırı: her zaman gece göğü yüzeyi; akşamları alt metin "güneş battı" der. */
@Composable
fun CloseDayCard(
    isDayClosed: Boolean,
    onCloseDayClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEvening: Boolean = false,
    closedMessage: String? = null,
) {
    Row(
        modifier = modifier
            .testTag(TestTags.TodayCloseDayCard)
            .fillMaxWidth()
            .heightIn(min = AppTokens.Layout.fabDefaultSize)
            .clip(RoundedCornerShape(AppTokens.Radius.lg))
            .background(AppTheme.colors.night.sky)
            .clickable(enabled = !isDayClosed, onClick = onCloseDayClick)
            .padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
    ) {
        Icon(
            imageVector = Icons.Rounded.Bedtime,
            contentDescription = null,
            tint = AppTheme.colors.night.moon,
            modifier = Modifier.size(AppTokens.IconSize.md),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.none),
        ) {
            Text(
                text = stringResource(
                    if (isDayClosed) R.string.today_close_day_done_title else R.string.close_day_row_title,
                ),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = AppTheme.colors.night.text,
            )
            Text(
                text = when {
                    isDayClosed -> closedMessage ?: stringResource(R.string.today_close_day_done_desc)
                    isEvening -> stringResource(R.string.close_day_row_subtitle_evening)
                    else -> stringResource(R.string.close_day_row_subtitle_day)
                },
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.night.muted,
            )
        }
        if (!isDayClosed) {
            Box(
                modifier = Modifier
                    .size(AppTokens.IconSize.lg)
                    .clip(CircleShape)
                    .background(AppTheme.colors.night.card),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = stringResource(R.string.today_close_day_action),
                    tint = AppTheme.colors.night.text,
                    modifier = Modifier.size(AppTokens.IconSize.sm),
                )
            }
        }
    }
}
