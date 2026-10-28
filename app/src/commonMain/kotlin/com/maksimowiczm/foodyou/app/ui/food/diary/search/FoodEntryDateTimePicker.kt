package com.maksimowiczm.foodyou.app.ui.food.diary.search

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.maksimowiczm.foodyou.app.ui.developer.inspection.*
import foodyou.app.generated.resources.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

/** Both steps edit a draft. Only final confirmation changes the logging destination. */
@Composable
internal fun FoodEntryDateTimePicker(
    date: LocalDate,
    time: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pickingTime by rememberSaveable { mutableStateOf(false) }
    var draftDay by rememberSaveable { mutableLongStateOf(date.toEpochDays().toLong()) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = draftDay * 86_400_000L)
    val timeState = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)
    if (!pickingTime) UiSourceScope(uiInspectionSource.element("FoodEntryDateTimePicker.DateDialog", "androidx.compose.material3.DatePickerDialog", "FoodEntryDateTimePicker")) {
        DatePickerDialog(onDismissRequest = onDismiss,
            modifier = modifier.uiName("FoodEntryDateTimePicker.DatePickerDialog.1", "androidx.compose.material3.DatePickerDialog", uiInspectionSource, "FoodEntryDateTimePicker"),
            confirmButton = {
                TextButton(enabled = dateState.selectedDateMillis != null, onClick = {
                    draftDay = requireNotNull(dateState.selectedDateMillis) / 86_400_000L
                    pickingTime = true
                }, modifier = Modifier.uiName("FoodEntryDateTimePicker.TextButton.dateConfirm", "androidx.compose.material3.TextButton", uiInspectionSource, "FoodEntryDateTimePicker")) {
                    Text(stringResource(Res.string.action_confirm), modifier = Modifier.uiName("FoodEntryDateTimePicker.Text.dateConfirm", "androidx.compose.material3.Text", uiInspectionSource, "FoodEntryDateTimePicker"))
                }
            }, dismissButton = {
                TextButton(onClick = onDismiss, modifier = Modifier.uiName("FoodEntryDateTimePicker.TextButton.dateCancel", "androidx.compose.material3.TextButton", uiInspectionSource, "FoodEntryDateTimePicker")) {
                    Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("FoodEntryDateTimePicker.Text.dateCancel", "androidx.compose.material3.Text", uiInspectionSource, "FoodEntryDateTimePicker"))
                }
            }) {
            DatePicker(state = dateState, modifier = Modifier.uiName("FoodEntryDateTimePicker.DatePicker.1", "androidx.compose.material3.DatePicker", uiInspectionSource, "FoodEntryDateTimePicker"))
        }
    } else UiSourceScope(uiInspectionSource.element("FoodEntryDateTimePicker.TimeDialog", "androidx.compose.material3.AlertDialog", "FoodEntryDateTimePicker")) {
        AlertDialog(onDismissRequest = onDismiss,
            modifier = modifier.uiName("FoodEntryDateTimePicker.AlertDialog.1", "androidx.compose.material3.AlertDialog", uiInspectionSource, "FoodEntryDateTimePicker"),
            title = { Text(stringResource(Res.string.food_entry_date_time), modifier = Modifier.uiName("FoodEntryDateTimePicker.Text.title", "androidx.compose.material3.Text", uiInspectionSource, "FoodEntryDateTimePicker")) },
            text = { TimeInput(state = timeState, modifier = Modifier.uiName("FoodEntryDateTimePicker.TimeInput.1", "androidx.compose.material3.TimeInput", uiInspectionSource, "FoodEntryDateTimePicker")) },
            confirmButton = {
                TextButton(onClick = { onConfirm(LocalDate.fromEpochDays(draftDay), LocalTime(timeState.hour, timeState.minute)) },
                    modifier = Modifier.uiName("FoodEntryDateTimePicker.TextButton.timeConfirm", "androidx.compose.material3.TextButton", uiInspectionSource, "FoodEntryDateTimePicker")) {
                    Text(stringResource(Res.string.action_confirm), modifier = Modifier.uiName("FoodEntryDateTimePicker.Text.timeConfirm", "androidx.compose.material3.Text", uiInspectionSource, "FoodEntryDateTimePicker"))
                }
            }, dismissButton = {
                TextButton(onClick = onDismiss, modifier = Modifier.uiName("FoodEntryDateTimePicker.TextButton.timeCancel", "androidx.compose.material3.TextButton", uiInspectionSource, "FoodEntryDateTimePicker")) {
                    Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("FoodEntryDateTimePicker.Text.timeCancel", "androidx.compose.material3.Text", uiInspectionSource, "FoodEntryDateTimePicker"))
                }
            })
    }
}

private val uiInspectionSource = UiSourceFile("com.maksimowiczm.foodyou.app.ui.food.diary.search",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/search/FoodEntryDateTimePicker.kt")
