package com.maksimowiczm.foodyou.app.ui.home.meals.card

import androidx.compose.runtime.Immutable
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealId

/** Lightweight view model for a saved diarySlot Meal shown in the apply-Meal list. */
@Immutable
internal data class MealModel(
    val id: MealId,
    val name: String,
    val itemCount: Int,
)
