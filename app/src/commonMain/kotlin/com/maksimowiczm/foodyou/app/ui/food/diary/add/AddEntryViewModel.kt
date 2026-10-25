package com.maksimowiczm.foodyou.app.ui.food.diary.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.event.EventBus
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.common.extension.now
import com.maksimowiczm.foodyou.common.result.onError
import com.maksimowiczm.foodyou.common.result.onSuccess
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItem
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Product
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import com.maksimowiczm.foodyou.food.domain.repository.NutritionalItemHistoryRepository
import com.maksimowiczm.foodyou.food.domain.usecase.DeleteNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveMeasurementSuggestionsUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.event.NutritionalItemDiaryEntryCreatedEvent
import com.maksimowiczm.foodyou.fooddiary.domain.repository.DiarySlotRepository
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.CreateNutritionalItemDiaryEntryUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

internal class AddEntryViewModel(
    private val unpackUseCase: com.maksimowiczm.foodyou.fooddiary.domain.usecase.UnpackNutritionalItemDiaryEntryUseCase,
    private val createFoodDiaryEntryUseCase: CreateNutritionalItemDiaryEntryUseCase,
    observeFoodUseCase: ObserveNutritionalItemUseCase,
    foodHistoryRepository: NutritionalItemHistoryRepository,
    private val deleteFoodUseCase: DeleteNutritionalItemUseCase,
    observeMeasurementSuggestionsUseCase: ObserveMeasurementSuggestionsUseCase,
    diarySlotRepository: DiarySlotRepository,
    private val dateProvider: DateProvider,
    private val eventBus: EventBus,
    private val foodId: NutritionalItemId,
) : ViewModel() {

    private val _uiEventBus = Channel<AddEntryEvent>()
    val uiEvents = _uiEventBus.receiveAsFlow()

    private val domainFood =
        observeFoodUseCase
            .observe(foodId)
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                replay = 1,
            )

    val food: StateFlow<FoodModel?> =
        domainFood
            .map {
                it?.let {
                    when (it) {
                        is Product -> ProductModel(it)
                        is Recipe -> RecipeModel(it)
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    val foodHistory =
        foodHistoryRepository
            .observeFoodHistory(foodId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = emptyList(),
            )

    val diarySlots =
        diarySlotRepository
            .observeDiarySlots()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    val today =
        dateProvider
            .observeDate()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = LocalDate.now(),
            )

    val possibleMeasurementTypes =
        domainFood
            .filterNotNull()
            .flatMapLatest { it.possibleMeasurementTypes }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    val suggestions: StateFlow<List<Measurement>?> =
        combine(domainFood, observeMeasurementSuggestionsUseCase.observe(foodId, limit = 5)) { current, list ->
            list.filter { current?.nutritionFor(it) != null }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    val suggestedMeasurement: StateFlow<Measurement?> =
        domainFood
            .filterNotNull()
            .flatMapLatest { food ->
                suggestions.filterNotNull().flatMapLatest { list ->
                    list.firstOrNull { food.nutritionFor(it) != null }?.let(::flowOf) ?: food.defaultMeasurement
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    fun deleteFood() {
        viewModelScope.launch {
            deleteFoodUseCase
                .delete(foodId)
                .onSuccess { _uiEventBus.send(AddEntryEvent.FoodDeleted) }
                .onError {
                    // Explode
                    error("Failed to delete food with ID $foodId")
                }
        }
    }

    fun addEntry(measurement: Measurement, diarySlotId: Long, date: LocalDate) {
        if (saving) return
        saving = true
        viewModelScope.launch {
            try {
            val food = domainFood.firstOrNull()
            if (food == null) {
                _uiEventBus.send(AddEntryEvent.SaveFailed)
                return@launch
            }
            val diaryFood = food.toDiaryFood()

            createFoodDiaryEntryUseCase
                .createDiaryEntry(
                    measurement = measurement,
                    diarySlotId = diarySlotId,
                    date = date,
                    food = diaryFood,
                )
                .onSuccess {
                    eventBus.publish(
                        NutritionalItemDiaryEntryCreatedEvent(
                            foodId = food.id,
                            timestamp = dateProvider.nowInstant(),
                            measurement = measurement,
                        )
                    )
                    _uiEventBus.send(AddEntryEvent.EntryAdded)
                }
                .onError {
                    // Explode
                    _uiEventBus.send(AddEntryEvent.SaveFailed)
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiEventBus.send(AddEntryEvent.SaveFailed)
            } finally { saving = false }
        }
    }

    private var saving = false

    fun unpack(measurement: Measurement, diarySlotId: Long, date: LocalDate) {
        if (saving) return
        saving = true
        viewModelScope.launch {
            try {
                val food = domainFood.firstOrNull() as? Recipe
                if (food == null) {
                    _uiEventBus.send(AddEntryEvent.SaveFailed)
                    return@launch
                }
                unpackUseCase.append(food.toDiaryFood() as com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeSnapshot, measurement, diarySlotId, date)
                    .onSuccess { _uiEventBus.send(AddEntryEvent.EntryAdded) }
                    .onError { _uiEventBus.send(AddEntryEvent.SaveFailed) }
            } finally { saving = false }
        }
    }

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
