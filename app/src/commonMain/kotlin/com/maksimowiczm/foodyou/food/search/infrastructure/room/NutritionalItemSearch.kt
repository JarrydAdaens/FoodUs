package com.maksimowiczm.foodyou.food.search.infrastructure.room

import androidx.room.Embedded
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.infrastructure.room.Minerals
import com.maksimowiczm.foodyou.common.infrastructure.room.Nutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.Vitamins

data class NutritionalItemSearch(
    val productId: Long?,
    val recipeId: Long?,
    val headline: String,
    val isLiquid: Boolean,
    @Embedded val nutrients: Nutrients?,
    @Embedded val vitamins: Vitamins?,
    @Embedded val minerals: Minerals?,
    val totalWeight: Double?,
    val servingWeight: Double?,
    val measurementType: MeasurementType?,
    val measurementValue: Double?,
    val factBasis:com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis?=null,
    val referencePortion:com.maksimowiczm.foodyou.common.domain.food.PortionDescription?=null,
    val reportedNutrition:com.maksimowiczm.foodyou.common.domain.food.ReportedNutrition?=null,
    val recipeYield:Int?=null,
)
