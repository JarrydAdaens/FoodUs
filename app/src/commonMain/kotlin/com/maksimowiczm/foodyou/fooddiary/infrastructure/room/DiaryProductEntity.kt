package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis
import com.maksimowiczm.foodyou.common.domain.food.PortionDescription
import com.maksimowiczm.foodyou.common.domain.food.NutritionMetadata
import com.maksimowiczm.foodyou.common.infrastructure.room.ProductSourceType
import com.maksimowiczm.foodyou.common.infrastructure.room.Minerals
import com.maksimowiczm.foodyou.common.infrastructure.room.Nutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.Vitamins

@Entity(tableName = "DiaryProduct")
data class DiaryProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @Embedded val nutrients: Nutrients,
    @Embedded val vitamins: Vitamins,
    @Embedded val minerals: Minerals,
    val packageWeight: Double?,
    val servingWeight: Double?,
    val isLiquid: Boolean,
    val sourceType: ProductSourceType,
    val sourceUrl: String?,
    val note: String?,
    @ColumnInfo(defaultValue = "'METRIC_100'") val factBasis: ProductFactBasis = ProductFactBasis.METRIC_100,
    @ColumnInfo(defaultValue = "NULL") val referencePortion: PortionDescription? = null,
    @ColumnInfo(defaultValue = "NULL") val nutritionMetadata: NutritionMetadata? = null,
)
