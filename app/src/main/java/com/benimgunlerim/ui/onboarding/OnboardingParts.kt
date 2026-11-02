package com.benimgunlerim.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.ui.components.core.StarIcon
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens

/** Tek seçimli seçenek kartı (ihtiyaç, tempo): seçiliyken mürekkep çerçeve + kayısı-soft dolgu. */
@Composable
internal fun ChoiceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    badge: String? = null,
) {
    val progress = AppTheme.colors.progress
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppTokens.Radius.lg),
        color = if (selected) progress.soft else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) AppTokens.BorderWidth.thin * 2 else AppTokens.BorderWidth.thin,
            if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AppTokens.Layout.fabDefaultSize)
            .semantics {
                this.selected = selected
                role = Role.RadioButton
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) progress.doneInk else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(AppTokens.IconSize.md),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (badge != null) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = progress.onSoft,
                        )
                    }
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Önerilen rutin/görev satırı: seçiliyse yıldız yanık — uygulamanın "her adım bir ışık" fikrini önceden gösterir. */
@Composable
internal fun StarSuggestionRow(
    name: String,
    icon: ImageVector,
    selected: Boolean,
    onToggle: () -> Unit,
    badge: String? = null,
) {
    val progress = AppTheme.colors.progress
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(AppTokens.Radius.lg),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(AppTokens.BorderWidth.thin, if (selected) progress.doneEdge else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AppTokens.Layout.fabDefaultSize)
            .semantics {
                this.selected = selected
                role = Role.Checkbox
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AppTokens.IconSize.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (badge != null) {
                    Text(badge, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = progress.doneInk)
                }
            }
            StarIcon(lit = selected, size = AppTokens.Star.row, litColor = progress.done, edgeColor = progress.doneEdge, idleColor = progress.idle)
        }
    }
}
