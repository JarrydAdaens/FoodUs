package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.maksimowiczm.foodyou.common.infrastructure.room.Minerals
import com.maksimowiczm.foodyou.common.infrastructure.room.Nutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.Vitamins

/**
 * One snapshotted item belonging to a [MealTemplateEntity]. Holds the item's name and full
 * portion-total nutrition so applying the template recreates the entry without depending on any
 * live product or recipe. [position] preserves display order.
 */
@Entity(
    tableName = "MealTemplateItem",
    foreignKeys =
        [
            ForeignKey(
                entity = MealTemplateEntity::class,
                parentColumns = ["id"],
                childColumns = ["templateId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index(value = ["templateId"])],
)
data class MealTemplateItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val name: String,
    val position: Int,
    @Embedded val nutrients: Nutrients,
    @Embedded val vitamins: Vitamins,
    @Embedded val minerals: Minerals,
)
