package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Embedded
import androidx.room.Relation

/** A template header joined with all of its items, used for reads. */
data class MealTemplateWithItems(
    @Embedded val template: MealTemplateEntity,
    @Relation(parentColumn = "id", entityColumn = "templateId")
    val items: List<MealTemplateItemEntity>,
)
