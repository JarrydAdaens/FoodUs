package com.maksimowiczm.foodyou.food.domain.usecase

import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.domain.measurement.type
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItem
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.repository.NutritionalItemMeasurementSuggestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ObserveMeasurementSuggestionsUseCase(
    private val observeFoodUseCase: ObserveNutritionalItemUseCase,
    private val repository: NutritionalItemMeasurementSuggestionRepository,
) {
    fun observe(foodId: NutritionalItemId, limit: Int): Flow<List<Measurement>> =
        observeFoodUseCase.observe(foodId).flatMapLatest { food ->
            if (food == null) {
                flowOf(emptyList())
            } else {
                repository.observeByFoodId(foodId = foodId, limit = limit).map { list ->
                    list.fillMissingMeasurements(food)
                }
            }
        }
}

private fun List<Measurement>.fillMissingMeasurements(food: NutritionalItem): List<Measurement> {
    val mutable = toMutableList()

    if (food.nutritionFor(Measurement.Serving(1.0)) != null) {
        mutable.add(Measurement.Serving(Measurement.Serving.DEFAULT))
    }

    if (food.nutritionFor(Measurement.Package(1.0)) != null) {
        mutable.add(Measurement.Package(Measurement.Package.DEFAULT))
    }

    if (food.isLiquid) {
        mutable.add(Measurement.Milliliter(Measurement.Milliliter.DEFAULT))
        mutable.add(Measurement.FluidOunce(Measurement.FluidOunce.DEFAULT))
    } else {
        mutable.add(Measurement.Gram(Measurement.Gram.DEFAULT))
        mutable.add(Measurement.Ounce(Measurement.Ounce.DEFAULT))
    }

    return mutable.distinct().filter {
        food.nutritionFor(it) != null
    }
}

private val NutritionalItem.possibleMeasurementTypes: List<MeasurementType>
    get() =
        MeasurementType.entries.filter { type ->
            when (type) {
                MeasurementType.Gram -> !isLiquid
                MeasurementType.Ounce -> !isLiquid
                MeasurementType.Milliliter -> isLiquid
                MeasurementType.FluidOunce -> isLiquid
                MeasurementType.Package -> totalWeight != null
                MeasurementType.Serving -> servingWeight != null
            }
        }
