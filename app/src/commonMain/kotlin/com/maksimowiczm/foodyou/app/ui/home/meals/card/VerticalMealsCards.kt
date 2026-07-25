package com.maksimowiczm.foodyou.app.ui.home.meals.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer

@Composable
internal fun VerticalMealsCards(
    meals: List<MealModel>?,
    onAdd: (mealId: Long) -> Unit,
    onQuickAdd: (mealId: Long) -> Unit,
    onAiScan: (mealId: Long) -> Unit,
    onFastText: (mealId: Long) -> Unit,
    onEditEntry: (MealEntryModel) -> Unit,
    onDeleteEntry: (MealEntryModel) -> Unit,
    onLongClick: (mealId: Long) -> Unit,
    shimmer: Shimmer,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (meals == null) {
            repeat(4) { MealCardSkeleton(shimmer) }
        } else {
            meals.forEach { meal ->
                MealCard(
                    meal = meal,
                    onAddFood = { onAdd(meal.id) },
                    onQuickAdd = { onQuickAdd(meal.id) },
                    onAiScan = { onAiScan(meal.id) },
                    onFastText = { onFastText(meal.id) },
                    onEditEntry = onEditEntry,
                    onDeleteEntry = onDeleteEntry,
                    onLongClick = { onLongClick(meal.id) },
                )
            }
        }
    }
}
