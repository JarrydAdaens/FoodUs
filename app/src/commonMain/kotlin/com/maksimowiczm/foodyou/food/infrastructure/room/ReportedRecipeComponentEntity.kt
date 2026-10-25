package com.maksimowiczm.foodyou.food.infrastructure.room

import androidx.room.Entity
import androidx.room.ForeignKey
import com.maksimowiczm.foodyou.common.domain.food.ReportedRecipeComponent

/** Only the owning Recipe is a foreign key; optional provenance never cascades component loss. */
@Entity(
    tableName = "ReportedRecipeComponent",
    primaryKeys = ["recipeId", "position"],
    foreignKeys = [ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE)],
)
data class ReportedRecipeComponentEntity(val recipeId: Long, val position: Int, val component: ReportedRecipeComponent)
