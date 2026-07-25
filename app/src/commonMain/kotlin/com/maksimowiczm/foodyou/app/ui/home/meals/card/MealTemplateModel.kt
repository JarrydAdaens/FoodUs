package com.maksimowiczm.foodyou.app.ui.home.meals.card

import androidx.compose.runtime.Immutable
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplateId

/** Lightweight view model for a saved meal template shown in the apply-template list. */
@Immutable
internal data class MealTemplateModel(
    val id: MealTemplateId,
    val name: String,
    val itemCount: Int,
)
