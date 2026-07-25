package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.maksimowiczm.foodyou.common.infrastructure.room.Minerals
import com.maksimowiczm.foodyou.common.infrastructure.room.Nutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.Vitamins

@Entity(
    tableName = "ManualDiaryEntry",
    foreignKeys =
        [
            ForeignKey(
                entity = MealEntity::class,
                parentColumns = ["id"],
                childColumns = ["mealId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index(value = ["mealId"]), Index(value = ["dateEpochDay"])],
)
data class ManualDiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val dateEpochDay: Long,
    val name: String,
    @Embedded val nutrients: Nutrients,
    @Embedded val vitamins: Vitamins,
    @Embedded val minerals: Minerals,
    val createdEpochSeconds: Long,
    val updatedEpochSeconds: Long,
    // Fast-text placeholder support (Milestone 2, Story 8). A placeholder is a zero-nutrition manual
    // entry that only records what was eaten by name; [description] holds optional context and
    // [isPlaceholder] marks it so Story 9 can open its dedicated resolution editor instead of the
    // regular Quick Add editor. Both default to the non-placeholder values so existing manual /
    // Quick Add entries are unaffected.
    val description: String? = null,
    val isPlaceholder: Boolean = false,
)
