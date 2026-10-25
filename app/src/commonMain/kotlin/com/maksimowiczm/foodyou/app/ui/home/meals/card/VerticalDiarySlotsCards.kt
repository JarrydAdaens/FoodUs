package com.maksimowiczm.foodyou.app.ui.home.meals.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealId
import com.valentinilk.shimmer.Shimmer

@Composable
internal fun VerticalDiarySlotsCards(
    diarySlots: List<DiarySlotModel>?,
    onAdd: (diarySlotId: Long) -> Unit,
    onQuickAdd: (diarySlotId: Long) -> Unit,
    onAiScan: (diarySlotId: Long) -> Unit,
    onFastText: (diarySlotId: Long) -> Unit,
    onEditEntry: (DiarySlotEntryModel) -> Unit,
    onDeleteEntry: (DiarySlotEntryModel) -> Unit,
    onLongClick: (diarySlotId: Long) -> Unit,
    meals: List<MealModel>,
    onSaveMeal: (diarySlotId: Long, name: String, onResult: (Boolean) -> Unit) -> Unit,
    onApplyMeal: (MealId, diarySlotId: Long, onResult: (Boolean) -> Unit) -> Unit,
    onDeleteMeal: (MealId, onResult: (Boolean) -> Unit) -> Unit,
    shimmer: Shimmer,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (diarySlots == null) {
            repeat(4) { DiarySlotCardSkeleton(shimmer) }
        } else {
            diarySlots.forEach { diarySlot ->
                DiarySlotCard(
                    diarySlot = diarySlot,
                    onAddFood = { onAdd(diarySlot.id) },
                    onQuickAdd = { onQuickAdd(diarySlot.id) },
                    onAiScan = { onAiScan(diarySlot.id) },
                    onFastText = { onFastText(diarySlot.id) },
                    onEditEntry = onEditEntry,
                    onDeleteEntry = onDeleteEntry,
                    onLongClick = { onLongClick(diarySlot.id) },
                    meals = meals,
                    onSaveMeal = { name, result -> onSaveMeal(diarySlot.id, name, result) },
                    onApplyMeal = { id, result -> onApplyMeal(id, diarySlot.id, result) },
                    onDeleteMeal = onDeleteMeal,
                )
            }
        }
    }
}
