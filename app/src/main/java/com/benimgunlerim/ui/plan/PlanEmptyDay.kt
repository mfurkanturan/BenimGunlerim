package com.benimgunlerim.ui.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.molecules.DashedAddRow
import com.benimgunlerim.ui.components.molecules.MinoBubble
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens
import java.time.LocalDate

/** Plan'da hazır başlangıç görevleri; onboarding metinleriyle ortak. */
private val PlanStarterRes = listOf(
    R.string.onboarding_task_prepare_list,
    R.string.plan_starter_prepare_next,
    R.string.onboarding_suggest_weekly_review,
)

/**
 * Seçili gün boşken: güne göre tek cümle + (bugün/ileri ise) kesik çizgili ekleme yeri ve hızlı çipler.
 * Geçmiş günde ekleme yok; bugüne dönüş önerilir. FAB bu durumda gizlenir, ekleme yolu tektir.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PlanEmptyDay(
    date: LocalDate,
    today: LocalDate,
    onAdd: () -> Unit,
    onPickStarter: (Int) -> Unit,
    onBackToToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPast = date.isBefore(today)
    val message = stringResource(
        when {
            isPast -> R.string.plan_empty_past
            date == today -> R.string.plan_empty_today
            date == today.plusDays(1) -> R.string.plan_empty_tomorrow
            else -> R.string.plan_empty_future
        },
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm)) {
        MinoBubble(text = message, mascotSize = AppTokens.IconSize.xl)
        if (isPast) {
            Text(
                text = stringResource(R.string.plan_empty_past_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onBackToToday, modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min)) {
                Text(
                    text = stringResource(R.string.plan_back_to_today),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = AppTheme.colors.progress.doneInk,
                )
            }
        } else {
            val addCd = stringResource(R.string.plan_add_task_fab_cd)
            DashedAddRow(
                text = stringResource(R.string.plan_empty_day_cta),
                onClick = onAdd,
                modifier = Modifier
                    .testTag(TestTags.PlanFab)
                    .semantics { contentDescription = addCd },
            )
            Text(
                text = stringResource(R.string.plan_starters_title),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = AppTokens.Spacing.xs),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
            ) {
                PlanStarterRes.forEach { res -> StarterChip(res = res, onClick = { onPickStarter(res) }) }
            }
        }
    }
}

@Composable
private fun StarterChip(res: Int, onClick: () -> Unit) {
    val label = stringResource(res)
    val cd = stringResource(R.string.plan_starter_add_cd, label)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppTokens.Radius.pill),
        color = Color.Transparent,
        border = BorderStroke(AppTokens.BorderWidth.thin, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .heightIn(min = AppTokens.TouchTarget.min)
            .clearAndSetSemantics { contentDescription = cd },
    ) {
        Text(
            text = "+ $label",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.sm),
        )
    }
}
