package com.benimgunlerim.ui.components.molecules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.ui.theme.AppTheme
import com.benimgunlerim.ui.theme.AppTokens

// Ekleme/düzenleme sayfalarının (görev, rutin) ortak parçaları — tek görsel dil.

@Composable
fun SheetTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
fun SheetSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

/** Büyük, çerçevesiz ana alan: "Ne yapacaksın?" */
@Composable
fun BigTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    TextField(
        keyboardOptions = keyboardOptions,
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.outline)
        },
        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        singleLine = true,
        isError = isError,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Ayrıntı hapı: "Saat ekle", "09:30", "Kategori ekle"… Seçiliyken kayısı-soft dolgu;
 * [onClear] verilirse sağda kaldır düğmesi.
 */
@Composable
fun OptionPill(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClear: (() -> Unit)? = null,
    clearContentDescription: String? = null,
) {
    val progress = AppTheme.colors.progress
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppTokens.Radius.pill),
        color = if (selected) progress.soft else Color.Transparent,
        border = if (selected) null else BorderStroke(AppTokens.BorderWidth.thin, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .heightIn(min = AppTokens.TouchTarget.min)
            .semantics { this.selected = selected },
    ) {
        Row(
            modifier = Modifier.padding(start = AppTokens.Spacing.sm, end = if (onClear != null) AppTokens.Spacing.none else AppTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) progress.doneInk else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(AppTokens.IconSize.sm),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (selected) progress.onSoft else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = AppTokens.Spacing.sm),
            )
            if (onClear != null) {
                IconButton(onClick = onClear, modifier = Modifier.size(AppTokens.TouchTarget.min)) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = clearContentDescription,
                        tint = progress.onSoft,
                        modifier = Modifier.size(AppTokens.IconSize.xs),
                    )
                }
            }
        }
    }
}

/** Tek seçimli hap grubu (öncelik gibi): seçili olan dolu, diğerleri çerçeveli. */
@Composable
fun SegmentedPills(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Surface(
                onClick = { onSelect(index) },
                shape = RoundedCornerShape(AppTokens.Radius.pill),
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                border = if (isSelected) null else BorderStroke(AppTokens.BorderWidth.thin, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .heightIn(min = AppTokens.TouchTarget.min)
                    .semantics {
                        selected = isSelected
                        role = Role.RadioButton
                    },
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.sm),
                )
            }
        }
    }
}

/** Haftanın günleri için yuvarlak açma/kapama düğmesi. */
@Composable
fun DayToggle(label: String, selected: Boolean, onClick: () -> Unit, contentDescription: String, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (selected) null else BorderStroke(AppTokens.BorderWidth.thin, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .size(AppTokens.TouchTarget.min)
            .semantics {
                this.selected = selected
                this.contentDescription = contentDescription
                role = Role.Checkbox
            },
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun SheetSaveButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(AppTokens.Radius.pill),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier.fillMaxWidth().heightIn(min = AppTokens.Layout.fabDefaultSize),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
    }
}
