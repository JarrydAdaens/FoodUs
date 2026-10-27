package com.maksimowiczm.foodyou.app.ui.food.meals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.developer.inspection.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import foodyou.app.generated.resources.*
import kotlin.time.Instant
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LibraryMealDestination(meal: Meal, slots: List<DiarySlot>, busy: Boolean,
    onCancel: () -> Unit, onLoad: (LocalDate, Long) -> Unit) {
    var date by remember(meal.id) { mutableStateOf<LocalDate?>(null) }
    var slot by remember(meal.id) { mutableStateOf<Long?>(null) }
    var chooseDate by remember { mutableStateOf(false) }
    if (chooseDate) {
        val picker = rememberDatePickerState()
        UiSourceScope(uiInspectionSource.element("LibraryMealDestination.DatePickerDialog", "androidx.compose.material3.DatePickerDialog", "LibraryMealDestination")) {
            DatePickerDialog(onDismissRequest = { chooseDate = false },
                confirmButton = { TextButton(enabled = picker.selectedDateMillis != null, onClick = {
                    date = picker.selectedDateMillis?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date }
                    chooseDate = false
                }, modifier = Modifier.uiName("LibraryMealDestination.TextButton.library1", "androidx.compose.material3.TextButton", uiInspectionSource, "LibraryMealDestination")) { Text(stringResource(Res.string.positive_ok), modifier = Modifier.uiName("LibraryMealDestination.Text.library1", "androidx.compose.material3.Text", uiInspectionSource, "LibraryMealDestination")) } },
                dismissButton = { TextButton(onClick = { chooseDate = false }, modifier = Modifier.uiName("LibraryMealDestination.TextButton.library2", "androidx.compose.material3.TextButton", uiInspectionSource, "LibraryMealDestination")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("LibraryMealDestination.Text.library2", "androidx.compose.material3.Text", uiInspectionSource, "LibraryMealDestination")) } }, modifier = Modifier.uiName("LibraryMealDestination.DatePickerDialog.library1", "androidx.compose.material3.DatePickerDialog", uiInspectionSource, "LibraryMealDestination")) {
                DatePicker(picker, modifier = Modifier.uiName("LibraryMealDestination.DatePicker.library1", "androidx.compose.material3.DatePicker", uiInspectionSource, "LibraryMealDestination"))
            }
        }
    }
    UiSourceScope(uiInspectionSource.element("LibraryMealDestination.AlertDialog", "androidx.compose.material3.AlertDialog", "LibraryMealDestination")) {
        AlertDialog(onDismissRequest = { if(!busy) onCancel() },
            title = { Text(stringResource(Res.string.action_load_meal), modifier = Modifier.uiName("LibraryMealDestination.Text.library3", "androidx.compose.material3.Text", uiInspectionSource, "LibraryMealDestination")) },
            text = {
                Column(modifier = (Modifier.verticalScroll(rememberScrollState())).uiName("LibraryMealDestination.Column.library1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "LibraryMealDestination"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(meal.name, modifier = Modifier.uiName("LibraryMealDestination.Text.library4", "androidx.compose.material3.Text", uiInspectionSource, "LibraryMealDestination"))
                    TextButton(onClick = { chooseDate = true }, enabled = !busy, modifier = Modifier.uiName("LibraryMealDestination.TextButton.library3", "androidx.compose.material3.TextButton", uiInspectionSource, "LibraryMealDestination")) {
                        Text(date?.toString() ?: stringResource(Res.string.library_choose_day), modifier = Modifier.uiName("LibraryMealDestination.Text.library5", "androidx.compose.material3.Text", uiInspectionSource, "LibraryMealDestination"))
                    }
                    Text(stringResource(if(slots.isEmpty()) Res.string.library_no_slots else Res.string.library_choose_slot), modifier = Modifier.uiName("LibraryMealDestination.Text.library6", "androidx.compose.material3.Text", uiInspectionSource, "LibraryMealDestination"))
                    FlowRow (modifier = Modifier.uiName("LibraryMealDestination.FlowRow.library1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "LibraryMealDestination")) {
                        slots.forEach { destination ->
                            FilterChip(selected = slot == destination.id, enabled = !busy,
                                onClick = { slot = destination.id }, label = { Text(destination.name, modifier = Modifier.uiName("LibraryMealDestination.Text.library7", "androidx.compose.material3.Text", uiInspectionSource, "LibraryMealDestination")) }, modifier = Modifier.uiName("LibraryMealDestination.FilterChip.library1", "androidx.compose.material3.FilterChip", uiInspectionSource, "LibraryMealDestination"))
                        }
                    }
                }
            },
            confirmButton = { TextButton(enabled = !busy && date != null && slots.any { it.id == slot },
                onClick = { date?.let { day -> slot?.let { onLoad(day, it) } } }, modifier = Modifier.uiName("LibraryMealDestination.TextButton.library4", "androidx.compose.material3.TextButton", uiInspectionSource, "LibraryMealDestination")) { Text(stringResource(Res.string.action_load_meal), modifier = Modifier.uiName("LibraryMealDestination.Text.library8", "androidx.compose.material3.Text", uiInspectionSource, "LibraryMealDestination")) } },
            dismissButton = { TextButton(enabled = !busy, onClick = onCancel, modifier = Modifier.uiName("LibraryMealDestination.TextButton.library5", "androidx.compose.material3.TextButton", uiInspectionSource, "LibraryMealDestination")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("LibraryMealDestination.Text.library9", "androidx.compose.material3.Text", uiInspectionSource, "LibraryMealDestination")) } }, modifier = Modifier.uiName("LibraryMealDestination.AlertDialog.library1", "androidx.compose.material3.AlertDialog", uiInspectionSource, "LibraryMealDestination"))
    }
}
private val uiInspectionSource = UiSourceFile("com.maksimowiczm.foodyou.app.ui.food.meals",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/meals/LibraryMealDestination.kt")
