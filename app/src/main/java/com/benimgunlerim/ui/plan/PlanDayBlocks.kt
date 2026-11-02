package com.benimgunlerim.ui.plan

import com.benimgunlerim.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTokens

/** Seçili günün başlığı: "2 Kasım, Pazartesi" + Bugün/Yarın çipi + görev sayısı. */
@Composable
internal fun PlanDayHeader(dayText: String, relativeLabel: String?, countText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
        ) {
            Text(
                text = dayText,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (relativeLabel != null) {
                Text(
                    text = relativeLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = AppTheme.colors.progress.onSoft,
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppTokens.Radius.pill))
                        .background(AppTheme.colors.progress.soft)
                        .padding(horizontal = AppTokens.Spacing.xs, vertical = AppTokens.Spacing.xxs),
                )
            }
        }
        Text(
            text = countText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Önceki günlerden kalan görevler: kayısı-soft kart, tek dokunuşla seçili güne taşınır. */
@Composable
internal fun PlanOverdueCard(moveLabel: String, onMoveAll: () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTokens.Radius.lg))
            .background(AppTheme.colors.progress.soft)
            .padding(AppTokens.Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.plan_overdue_tasks_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = AppTheme.colors.progress.onSoft,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onMoveAll, modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min)) {
                Text(moveLabel, color = AppTheme.colors.progress.doneInk)
            }
        }
        content()
    }
}
