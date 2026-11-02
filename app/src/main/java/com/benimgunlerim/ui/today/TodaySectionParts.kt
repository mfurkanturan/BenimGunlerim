package com.benimgunlerim.ui.today

import com.benimgunlerim.ui.components.core.StarToggle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.IconButton
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens

/** Bölüm başlığının sağındaki "1 / 3" ilerlemesi. */
@Composable
internal fun SectionProgress(done: Int, total: Int) {
    val cd = stringResource(R.string.today_section_progress_cd, done, total)
    Text(
        text = stringResource(R.string.today_section_progress, done, total),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
        color = if (total > 0 && done >= total) AppTheme.colors.progress.doneInk else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.clearAndSetSemantics { contentDescription = cd },
    )
}

/** Önceki günlerden kalan görevler: "dikkat" tonunda yumuşak kart, tek dokunuşla bugüne taşınır. */
@Composable
internal fun OverdueCard(onMoveAll: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val tone = AppTheme.colors.status.attention
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTokens.Radius.lg))
            .background(tone.container)
            .padding(AppTokens.Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.today_overdue_tasks_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = AppTheme.colors.progress.onSoft,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onMoveAll, modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min)) {
                Text(stringResource(R.string.today_move_all_overdue_to_today), color = tone.fg)
            }
        }
        content()
    }
}

/** Boş günde "Günü kapat": koca gece kartı yerine sessiz bir satır (kapatılacak hedef yok). */
@Composable
internal fun QuietCloseDayRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().testTag(TestTags.TodayCloseDayCard),
        horizontalArrangement = Arrangement.Center,
    ) {
        TextButton(onClick = onClick, modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min)) {
            Text(
                text = stringResource(R.string.close_day_row_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Reset'ten seçilen tek adım: Bugün'ün en üstünde, yıldızla tamamlanır ya da kaldırılır. */
@Composable
internal fun FocusCard(title: String, onComplete: () -> Unit, onClear: () -> Unit, modifier: Modifier = Modifier) {
    val progress = AppTheme.colors.progress
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTokens.Radius.lg))
            .background(progress.soft)
            .padding(start = AppTokens.Spacing.xxs, end = AppTokens.Spacing.xxs, top = AppTokens.Spacing.xs, bottom = AppTokens.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StarToggle(
            lit = false,
            onToggle = onComplete,
            stateLabel = stringResource(R.string.star_state_idle),
            actionLabel = title,
            litColor = progress.done,
            edgeColor = progress.doneEdge,
            idleColor = progress.doneEdge,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.today_focus_kicker),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = progress.doneInk,
            )
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = progress.onSoft)
        }
        IconButton(onClick = onClear, modifier = Modifier.size(AppTokens.TouchTarget.min)) {
            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.today_focus_clear_cd), tint = progress.onSoft, modifier = Modifier.size(AppTokens.IconSize.sm))
        }
    }
}
