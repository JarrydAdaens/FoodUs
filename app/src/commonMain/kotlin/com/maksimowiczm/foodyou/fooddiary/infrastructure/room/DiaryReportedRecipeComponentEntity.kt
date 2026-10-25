package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Entity
import androidx.room.ForeignKey
import com.maksimowiczm.foodyou.common.domain.food.ReportedRecipeComponent

/** Captured text/facts and opaque provenance; no foreign key or local ID to live catalogue data. */
@Entity(
    tableName = "DiaryReportedRecipeComponent",
    primaryKeys = ["recipeId", "position"],
    foreignKeys = [ForeignKey(entity = DiaryRecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE)],
)
data class DiaryReportedRecipeComponentEntity(val recipeId: Long, val position: Int, val component: ReportedRecipeComponent)
