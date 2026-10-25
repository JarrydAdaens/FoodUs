package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maksimowiczm.foodyou.common.domain.food.ReportedNutrition

@Entity(tableName = "DiaryRecipe")
data class DiaryRecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val servings: Int?,
    val isLiquid: Boolean,
    val note: String?,
    @ColumnInfo(defaultValue = "0") val calculationVersion: Int = 0,
    @ColumnInfo(defaultValue = "NULL") val reportedNutrition: ReportedNutrition? = null,
)
