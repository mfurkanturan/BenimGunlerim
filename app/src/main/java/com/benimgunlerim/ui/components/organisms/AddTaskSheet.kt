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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.molecules.BigTextField
import com.benimgunlerim.ui.components.molecules.OptionPill
import com.benimgunlerim.ui.components.molecules.SegmentedPills
import com.benimgunlerim.ui.components.molecules.SheetSaveButton
import com.benimgunlerim.ui.components.molecules.SheetSectionLabel
import com.benimgunlerim.ui.components.molecules.SheetTitle
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.today.TaskTimePickerDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DEFAULT_TIME_HOUR = 9
private const val PRIORITY_HIGH = 1
private const val PRIORITY_NORMAL = 2

/**
 * Görev ekleme sayfası: hangi güne eklendiği en üstte (dokununca değişir), tek büyük başlık alanı,
 * ayrıntılar hap olarak (saat, kategori), öncelik tek seçimli, saat varsa hatırlatma, tek "Kaydet".
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Suppress("LongMethod")
@Composable
fun AddTaskSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, date: String, startTime: String?, category: String?, priority: Int, reminderTime: String?) -> Unit,
    modifier: Modifier = Modifier,
    initialDate: String = LocalDate.now().toString(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf(initialDate) }
    var selectedPriority by rememberSaveable { mutableIntStateOf(PRIORITY_NORMAL) }
    var category by rememberSaveable { mutableStateOf("") }
    var showCategoryField by rememberSaveable { mutableStateOf(false) }
    var time by rememberSaveable { mutableStateOf("") }
    var reminderEnabled by rememberSaveable { mutableStateOf(false) }
    var isError by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val date = runCatching { LocalDate.parse(selectedDate) }.getOrDefault(LocalDate.now())
    val today = remember { LocalDate.now() }
    val tr = remember { Locale("tr") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMMM, EEEE", tr) }
    val relative = when (date) {
        today -> stringResource(R.string.plan_day_today)
        today.plusDays(1) -> stringResource(R.string.plan_day_tomorrow)
        else -> null
    }
    val dateLabel = listOfNotNull(date.format(dateFormatter), relative).joinToString(" · ")

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = time.takeIf { it.length == 5 }?.substring(0, 2)?.toIntOrNull() ?: DEFAULT_TIME_HOUR,
            initialMinute = time.takeIf { it.length == 5 }?.substring(3, 5)?.toIntOrNull() ?: 0,
            is24Hour = true,
        )
        TaskTimePickerDialog(
            state = timePickerState,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                showTimePicker = false
            },
        )
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        selectedDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) { DatePicker(state = dateState) }
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
            SheetTitle(stringResource(R.string.add_task_sheet_title))
            OptionPill(
                icon = Icons.Rounded.CalendarToday,
                text = dateLabel,
                selected = true,
                onClick = { showDatePicker = true },
            )

            BigTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) isError = false
                },
                placeholder = stringResource(R.string.add_task_sheet_title_label),
                isError = isError,
                modifier = Modifier.testTag(TestTags.AddTaskTitleField),
            )

            SheetSectionLabel(stringResource(R.string.add_task_details_label))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
            ) {
                val clearCd = stringResource(R.string.sheet_clear_cd)
                OptionPill(
                    icon = Icons.Rounded.Schedule,
                    text = time.ifBlank { stringResource(R.string.add_task_time_add) },
                    selected = time.isNotBlank(),
                    onClick = { showTimePicker = true },
                    onClear = if (time.isNotBlank()) ({ time = ""; reminderEnabled = false }) else null,
                    clearContentDescription = clearCd,
                )
                OptionPill(
                    icon = Icons.Rounded.LocalOffer,
                    text = category.ifBlank { stringResource(R.string.add_task_category_add) },
                    selected = category.isNotBlank(),
                    onClick = { showCategoryField = true },
                    onClear = if (category.isNotBlank()) ({ category = ""; showCategoryField = false }) else null,
                    clearContentDescription = clearCd,
                )
            }
            if (showCategoryField) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it.take(MAX_CATEGORY_LENGTH) },
                    label = { Text(stringResource(R.string.today_add_task_category_label)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        showCategoryField = false
                    }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SheetSectionLabel(stringResource(R.string.add_task_sheet_priority_label))
            SegmentedPills(
                options = listOf(
                    stringResource(R.string.add_task_sheet_priority_high),
                    stringResource(R.string.add_task_sheet_priority_normal),
                    stringResource(R.string.add_task_sheet_priority_low),
                ),
                selectedIndex = selectedPriority - PRIORITY_HIGH,
                onSelect = { selectedPriority = it + PRIORITY_HIGH },
            )

            if (time.isNotBlank()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.today_reminder_switch_label),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }
            }

            SheetSaveButton(
                text = stringResource(R.string.action_save),
                onClick = {
                    if (title.isBlank()) {
                        isError = true
                    } else {
                        val startToSave = time.ifBlank { null }
                        val reminderToSave = if (reminderEnabled) startToSave else null
                        onSave(title.trim(), selectedDate, startToSave, category.trim().ifBlank { null }, selectedPriority, reminderToSave)
                        onDismiss()
                    }
                },
                modifier = Modifier.padding(top = AppTokens.Spacing.xs),
            )
        }
    }
}

private const val MAX_CATEGORY_LENGTH = 30
