package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Header row for a reusable Meal. Its items live in [MealItemEntity], joined by
 * [MealItemEntity.mealId]. The legacy table name preserves existing databases and backups.
 */
@Entity(tableName = "MealTemplate")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdEpochSeconds: Long,
    @ColumnInfo(defaultValue = "NULL") val reportedNutrition: com.maksimowiczm.foodyou.common.domain.food.ReportedNutrition? = null,
    @ColumnInfo(defaultValue = "NULL") val metadata: com.maksimowiczm.foodyou.common.domain.food.NutritionMetadata? = null,
)
