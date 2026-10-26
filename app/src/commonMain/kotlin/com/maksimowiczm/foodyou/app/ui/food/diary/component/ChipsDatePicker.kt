package com.maksimowiczm.foodyou.app.ui.food.diary.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.common.extension.minus
import com.maksimowiczm.foodyou.common.extension.now
import com.maksimowiczm.foodyou.common.extension.plus
import foodyou.app.generated.resources.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChipsDatePicker(state: ChipsDatePickerState, modifier: Modifier = Modifier) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        UiSourceScope(uiInspectionSource.element("ChipsDatePicker.DatePickerDialog.1", "androidx.compose.material3.DatePickerDialog", "ChipsDatePicker")) { DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.let(Instant::fromEpochMilliseconds)
                            ?.toLocalDateTime(TimeZone.UTC)
                            ?.date
                            ?.let(state::addAndSelect)

                        showDatePicker = false
                    }, modifier = Modifier.uiName("ChipsDatePicker.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "ChipsDatePicker")
                ) {
                    Text(stringResource(Res.string.positive_ok), modifier = Modifier.uiName("ChipsDatePicker.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ChipsDatePicker"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }, modifier = Modifier.uiName("ChipsDatePicker.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "ChipsDatePicker")) {
                    Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("ChipsDatePicker.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ChipsDatePicker"))
                }
            }, modifier = Modifier.uiName("ChipsDatePicker.DatePickerDialog.1.root", "androidx.compose.material3.DatePickerDialog", uiInspectionSource, "ChipsDatePicker")
        ) {
            DatePicker(state = datePickerState, modifier = Modifier.uiName("ChipsDatePicker.DatePicker.1", "androidx.compose.material3.DatePicker", uiInspectionSource, "ChipsDatePicker"))
        } }
    }

    Row((modifier).uiName("ChipsDatePicker.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "ChipsDatePicker")) {
        Box(modifier = (Modifier.size(48.dp)).uiName("ChipsDatePicker.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "ChipsDatePicker"), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.uiName("ChipsDatePicker.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ChipsDatePicker"))
        }

        Spacer(Modifier.width(8.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("ChipsDatePicker.FlowRow.1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "ChipsDatePicker")) {
            state.dates.forEach { date ->
                InputChip(
                    selected = state.selectedDate == date,
                    onClick = { state.selectDate(date) },
                    label = { Text(date.stringResource(state.today), modifier = Modifier.uiName("ChipsDatePicker.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ChipsDatePicker")) }, modifier = Modifier.uiName("ChipsDatePicker.InputChip.1", "androidx.compose.material3.InputChip", uiInspectionSource, "ChipsDatePicker")
                )
            }

            InputChip(
                selected = false,
                onClick = { showDatePicker = true },
                label = { Text(stringResource(Res.string.action_choose_other_date), modifier = Modifier.uiName("ChipsDatePicker.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "ChipsDatePicker")) }, modifier = Modifier.uiName("ChipsDatePicker.InputChip.2", "androidx.compose.material3.InputChip", uiInspectionSource, "ChipsDatePicker")
            )
        }
    }
}

@Composable
private fun LocalDate.stringResource(today: LocalDate): String {
    val formatter = LocalDateFormatter.current
    val str = formatter.formatDateShort(this)

    return when (this) {
        today -> stringResource(Res.string.headline_today, str)
        today.minus(1.days) -> stringResource(Res.string.headline_yesterday, str)
        today.plus(1.days) -> stringResource(Res.string.headline_tomorrow, str)
        else -> str
    }
}

@Composable
fun rememberChipsDatePickerState(
    today: LocalDate,
    initialDates: List<LocalDate>,
    selectedDate: LocalDate = initialDates.first(),
): ChipsDatePickerState =
    rememberSaveable(today, initialDates, selectedDate, saver = ChipsDatePickerState.saver) {
        ChipsDatePickerState(
            today = today,
            initialDates = initialDates,
            initialSelectedDate = selectedDate,
        )
    }

@Stable
class ChipsDatePickerState(
    val today: LocalDate,
    initialDates: List<LocalDate>,
    initialSelectedDate: LocalDate?,
) {
    private var datesSet by mutableStateOf(initialDates.ifEmpty { setOf(LocalDate.now()) })

    val dates by derivedStateOf { datesSet.sorted() }

    var selectedDate by mutableStateOf(initialSelectedDate ?: LocalDate.now())
        private set

    fun addAndSelect(date: LocalDate) {
        datesSet = (datesSet + date)

        selectedDate = date
    }

    fun selectDate(date: LocalDate) {
        if (date in datesSet) {
            selectedDate = date
        }
    }

    companion object {
        val saver: Saver<ChipsDatePickerState, List<Long>> =
            Saver(
                save = {
                    val mutableList = mutableListOf<Long>()

                    mutableList.add(it.today.toEpochDays())
                    mutableList.add(it.selectedDate.toEpochDays())
                    mutableList.addAll(it.datesSet.map(LocalDate::toEpochDays))

                    mutableList
                },
                restore = {
                    val selectedDate = it.firstOrNull()?.let(LocalDate::fromEpochDays)
                    val today = it.getOrNull(1)?.let(LocalDate::fromEpochDays) ?: LocalDate.now()
                    val dates = it.drop(2).map(LocalDate::fromEpochDays)

                    ChipsDatePickerState(
                        today = today,
                        initialDates = dates,
                        initialSelectedDate = selectedDate,
                    )
                },
            )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/component/ChipsDatePicker.kt",
)
