package com.maksimowiczm.foodyou.app.ui.food.diary.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChipsDiarySlotPicker(state: ChipsDiarySlotPickerState, modifier: Modifier = Modifier) {
    Row(modifier) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Restaurant, contentDescription = null)
        }

        Spacer(Modifier.width(8.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.diarySlots.forEachIndexed { i, diarySlot ->
                InputChip(
                    selected = diarySlot == state.selectedDiarySlot,
                    onClick = { state.selectedDiarySlot = diarySlot },
                    label = { Text(diarySlot) },
                )
            }
        }
    }
}

@Composable
fun rememberChipsDiarySlotPickerState(diarySlots: List<String>, selectedDiarySlot: String?): ChipsDiarySlotPickerState {
    return rememberSaveable(
        diarySlots,
        selectedDiarySlot,
        saver =
            Saver(
                save = { listOf(it.selectedDiarySlot, it.diarySlots) },
                restore = {
                    @Suppress("UNCHECKED_CAST")
                    ChipsDiarySlotPickerState(
                        initialDiarySlots = it[1] as List<String>,
                        selectedDiarySlot = it[0] as String?,
                    )
                },
            ),
    ) {
        ChipsDiarySlotPickerState(initialDiarySlots = diarySlots, selectedDiarySlot = selectedDiarySlot)
    }
}

@Stable
class ChipsDiarySlotPickerState(initialDiarySlots: List<String>, selectedDiarySlot: String?) {
    init {
        require(initialDiarySlots.isNotEmpty()) { "Meals list cannot be empty" }
    }

    val diarySlots = initialDiarySlots

    var selectedDiarySlot by mutableStateOf(selectedDiarySlot)
}
