package com.maksimowiczm.foodyou.app.ui.food.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItem
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveMeasurementSuggestionsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

internal class MeasureIngredientViewModel(
    foodId: NutritionalItemId,
    observeFoodUseCase: ObserveNutritionalItemUseCase,
    observeMeasurementSuggestionsUseCase: ObserveMeasurementSuggestionsUseCase,
) : ViewModel() {
    val food: StateFlow<NutritionalItem?> =
        observeFoodUseCase
            .observe(foodId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    val possibleMeasurements: StateFlow<List<MeasurementType>?> =
        food
            .filterNotNull()
            .flatMapLatest { food -> food.possibleMeasurementTypes }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    val suggestions: StateFlow<List<Measurement>?> =
        observeMeasurementSuggestionsUseCase
            .observe(foodId, limit = 10)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )
}

// These extensions will probably be moved into business when user would be able to choose between
// metric and imperial measurements. This is why they are wrapped in Flow, so they can be
// easily converted to the appropriate measurement system later.
private val NutritionalItem.possibleMeasurementTypes: Flow<List<MeasurementType>>
    get() =
        flowOf(
            supportedMeasurementTypes()
        )

private val NutritionalItem.defaultMeasurement: Flow<Measurement>
    get() =
        flowOf(
            when {
                nutritionFor(Measurement.Serving(1.0)) != null -> Measurement.Serving(1.0)
                nutritionFor(Measurement.Package(1.0)) != null -> Measurement.Package(1.0)
                isLiquid -> Measurement.Milliliter(100.0)
                else -> Measurement.Gram(100.0)
            }
        )
