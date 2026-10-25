package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Embedded
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.maksimowiczm.foodyou.common.infrastructure.room.Minerals
import com.maksimowiczm.foodyou.common.infrastructure.room.Nutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.Vitamins

/**
 * One snapshotted item belonging to a [MealEntity]. Holds the item's name and full
 * portion-total nutrition so applying the Meal recreates the entry without depending on any
 * live product or recipe. [position] preserves display order.
 */
@Entity(
    tableName = "MealTemplateItem",
    foreignKeys =
        [
            ForeignKey(
                entity = MealEntity::class,
                parentColumns = ["id"],
                childColumns = ["templateId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index(value = ["templateId"])],
)
data class MealItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "templateId") val mealId: Long,
    val name: String,
    val position: Int,
    @Embedded val nutrients: Nutrients,
    @Embedded val vitamins: Vitamins,
    @Embedded val minerals: Minerals,
    @ColumnInfo(defaultValue = "NULL") val capturedPortionType: com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType? = null,
    @ColumnInfo(defaultValue = "NULL") val capturedPortionValue: Double? = null,
    @ColumnInfo(defaultValue = "NULL") val servingCount: Double? = null,
    @ColumnInfo(defaultValue = "NULL") val weightGrams: Double? = null,
    @ColumnInfo(defaultValue = "NULL") val portionDescription: com.maksimowiczm.foodyou.common.domain.food.PortionDescription? = null,
    @ColumnInfo(defaultValue = "NULL") val metadata: com.maksimowiczm.foodyou.common.domain.food.NutritionMetadata? = null,
)
