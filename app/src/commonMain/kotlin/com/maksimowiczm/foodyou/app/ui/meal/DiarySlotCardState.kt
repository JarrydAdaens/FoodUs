package com.maksimowiczm.foodyou.app.ui.meal

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.maksimowiczm.foodyou.app.ui.common.form.FormField
import com.maksimowiczm.foodyou.app.ui.common.form.nonBlankStringValidator
import com.maksimowiczm.foodyou.app.ui.common.form.rememberFormField
import com.maksimowiczm.foodyou.app.ui.common.form.stringParser
import com.maksimowiczm.foodyou.common.compose.extension.Saver
import com.maksimowiczm.foodyou.common.extension.now
import kotlinx.datetime.LocalTime

@Composable
internal fun rememberDiarySlotCardState(diarySlot: DiarySlotModel?): DiarySlotCardState {
    val name =
        rememberFormField(
            initialValue = diarySlot?.name ?: "",
            parser = stringParser(),
            validator = nonBlankStringValidator(onEmpty = { "Meal name cannot be empty" }),
            textFieldState = rememberTextFieldState(diarySlot?.name ?: ""),
        )

    val now = LocalTime.now()
    val fromTime =
        rememberSaveable(diarySlot, stateSaver = LocalTime.Companion.Saver) {
            mutableStateOf(diarySlot?.from ?: now)
        }

    val toTime =
        rememberSaveable(diarySlot, stateSaver = LocalTime.Companion.Saver) {
            mutableStateOf(diarySlot?.to ?: LocalTime(hour = (now.hour + 1) % 24, minute = now.minute))
        }

    val isAllDay = rememberSaveable(diarySlot) { mutableStateOf(diarySlot?.isAllDay ?: false) }

    val isModified =
        remember(name, fromTime, toTime) {
            derivedStateOf {
                if (diarySlot != null) {
                    val input =
                        name.value != diarySlot.name ||
                            fromTime.value != diarySlot.from ||
                            toTime.value != diarySlot.to

                    if (diarySlot.isAllDay) {
                        input
                    } else {
                        input || isAllDay.value != diarySlot.isAllDay
                    }
                } else {
                    name.value.isNotEmpty()
                }
            }
        }

    return remember(name, fromTime, toTime, isModified, isAllDay) {
        DiarySlotCardState(
            name = name,
            fromTime = fromTime,
            toTime = toTime,
            isAllDay = isAllDay,
            isModified = isModified,
        )
    }
}

internal class DiarySlotCardState(
    val name: FormField<String, String>,
    fromTime: MutableState<LocalTime>,
    toTime: MutableState<LocalTime>,
    isModified: State<Boolean>,
    isAllDay: MutableState<Boolean>,
) {
    var fromTime by fromTime
    var toTime by toTime
    val isModified by isModified
    var isAllDay by isAllDay

    val isValid by derivedStateOf { name.error == null }

    fun intoDiarySlotModel(id: Long): DiarySlotModel =
        DiarySlotModel(
            id = id,
            name = name.value,
            from = fromTime,
            to = if (isAllDay) fromTime else toTime,
            isAllDay = isAllDay,
        )
}
