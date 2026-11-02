package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.molecules.BigTextField
import com.benimgunlerim.ui.components.molecules.DayToggle
import com.benimgunlerim.ui.components.molecules.OptionPill
import com.benimgunlerim.ui.components.molecules.SheetSaveButton
import com.benimgunlerim.ui.components.molecules.SheetSectionLabel
import com.benimgunlerim.ui.components.molecules.SheetTitle
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.today.TaskTimePickerDialog
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private const val DEFAULT_TIME_HOUR = 9
private val Weekdays = DayOfWeek.entries.filter { it.value <= 5 }.toSet()
private val Weekend = DayOfWeek.entries.filter { it.value > 5 }.toSet()

/**
 * Rutin ekleme/düzenleme sayfası: büyük ad alanı, yuvarlak gün düğmeleri + hazır seçimler
 * (her gün / hafta içi / hafta sonu), ayrıntılar hap olarak (hatırlatma, kategori, sayılı hedef), tek "Kaydet".
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Suppress("LongMethod")
@Composable
fun AddRoutineSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, targetDays: Set<DayOfWeek>, reminderTime: String?, category: String, targetCount: Int?, unit: String?) -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    initialTitle: String = "",
    initialDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    initialReminderTime: String? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val defaultCategory = stringResource(R.string.routine_sheet_category_default)
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var selectedDays by rememberSaveable { mutableStateOf(initialDays) }
    var category by rememberSaveable { mutableStateOf(defaultCategory) }
    var showCategoryField by rememberSaveable { mutableStateOf(false) }
    var reminderTime by rememberSaveable { mutableStateOf(initialReminderTime.orEmpty()) }
    var isGoalRoutine by rememberSaveable { mutableStateOf(false) }
    var targetValueText by rememberSaveable { mutableStateOf("") }
    var targetUnit by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val tr = Locale("tr")

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderTime.takeIf { it.length == 5 }?.substring(0, 2)?.toIntOrNull() ?: DEFAULT_TIME_HOUR,
            initialMinute = reminderTime.takeIf { it.length == 5 }?.substring(3, 5)?.toIntOrNull() ?: 0,
            is24Hour = true,
        )
        TaskTimePickerDialog(
            state = timePickerState,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                reminderTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                showTimePicker = false
            },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTokens.Spacing.xl)
                .padding(bottom = AppTokens.Spacing.md)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.md),
        ) {
            SheetTitle(stringResource(if (isEditMode) R.string.routine_sheet_edit_title else R.string.routine_sheet_add_title))

            BigTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) isError = false
                },
                placeholder = stringResource(R.string.routine_sheet_name_label),
                isError = isError,
            )

            SheetSectionLabel(stringResource(R.string.routine_sheet_repeat_days_label))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val codes = turkishDayShortCode()
                DayOfWeek.entries.forEach { day ->
                    val isSelected = day in selectedDays
                    DayToggle(
                        label = codes.getValue(day),
                        selected = isSelected,
                        contentDescription = day.getDisplayName(TextStyle.FULL, tr),
                        onClick = {
                            selectedDays = when {
                                !isSelected -> selectedDays + day
                                selectedDays.size > 1 -> selectedDays - day
                                else -> selectedDays
                            }
                        },
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                PresetPill(stringResource(R.string.routine_preset_everyday), selectedDays.size == 7) { selectedDays = DayOfWeek.entries.toSet() }
                PresetPill(stringResource(R.string.routine_preset_weekdays), selectedDays == Weekdays) { selectedDays = Weekdays }
                PresetPill(stringResource(R.string.routine_preset_weekend), selectedDays == Weekend) { selectedDays = Weekend }
            }

            SheetSectionLabel(stringResource(R.string.add_task_details_label))
            val clearCd = stringResource(R.string.sheet_clear_cd)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
            ) {
                OptionPill(
                    icon = Icons.Rounded.Notifications,
                    text = reminderTime.ifBlank { stringResource(R.string.routine_reminder_add) },
                    selected = reminderTime.isNotBlank(),
                    onClick = { showTimePicker = true },
                    onClear = if (reminderTime.isNotBlank()) ({ reminderTime = "" }) else null,
                    clearContentDescription = clearCd,
                )
                OptionPill(
                    icon = Icons.Rounded.LocalOffer,
                    text = category.ifBlank { defaultCategory },
                    selected = category.isNotBlank() && category != defaultCategory,
                    onClick = { showCategoryField = !showCategoryField },
                )
                OptionPill(
                    icon = Icons.Rounded.Numbers,
                    text = stringResource(R.string.routine_sheet_goal_switch_label),
                    selected = isGoalRoutine,
                    onClick = { isGoalRoutine = !isGoalRoutine },
                )
            }
            if (showCategoryField) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.routine_sheet_category_label)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (isGoalRoutine) {
                Text(
                    text = stringResource(R.string.routine_sheet_goal_switch_desc_on),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                    OutlinedTextField(
                        value = targetValueText,
                        onValueChange = { input -> targetValueText = input.filter { it.isDigit() }.take(4) },
                        label = { Text(stringResource(R.string.routine_sheet_target_value_label)) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = targetUnit,
                        onValueChange = { targetUnit = it },
                        label = { Text(stringResource(R.string.routine_sheet_target_unit_label)) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.weight(1.4f),
                    )
                }
            }

            SheetSaveButton(
                text = stringResource(R.string.action_save),
                onClick = {
                    if (title.isBlank()) {
                        isError = true
                    } else {
                        val targetCount = if (isGoalRoutine) targetValueText.toIntOrNull() else null
                        val unit = if (isGoalRoutine) targetUnit.trim().ifBlank { null } else null
                        onSave(title.trim(), selectedDays, reminderTime.ifBlank { null }, category.trim().ifBlank { defaultCategory }, targetCount, unit)
                        onDismiss()
                    }
                },
                modifier = Modifier.padding(top = AppTokens.Spacing.xs),
            )
        }
    }
}

@Composable
private fun PresetPill(text: String, selected: Boolean, onClick: () -> Unit) {
    OptionPill(icon = Icons.Rounded.Repeat, text = text, selected = selected, onClick = onClick)
}
