package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Header row for a reusable meal template (Milestone 2, Story 13). Its items live in
 * [MealTemplateItemEntity], joined by [MealTemplateItemEntity.templateId].
 */
@Entity(tableName = "MealTemplate")
data class MealTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdEpochSeconds: Long,
)
