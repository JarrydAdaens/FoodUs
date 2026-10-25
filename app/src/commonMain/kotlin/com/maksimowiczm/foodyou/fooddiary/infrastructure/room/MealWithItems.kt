package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Embedded
import androidx.room.Relation

/** A Meal header joined with all of its items, used for reads. */
data class MealWithItems(
    @Embedded val meal: MealEntity,
    @Relation(parentColumn = "id", entityColumn = "templateId")
    val items: List<MealItemEntity>,
)
