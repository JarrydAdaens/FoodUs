package com.maksimowiczm.foodyou.food.infrastructure.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.maksimowiczm.foodyou.common.domain.food.ReportedNutrition

@Entity(tableName = "Recipe")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val servings: Int?,
    val note: String?,
    val isLiquid: Boolean,
    @ColumnInfo(defaultValue = "NULL") val reportedNutrition: ReportedNutrition? = null,
)
