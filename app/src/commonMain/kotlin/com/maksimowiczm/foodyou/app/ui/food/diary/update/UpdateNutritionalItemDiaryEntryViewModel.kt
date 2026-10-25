package com.maksimowiczm.foodyou.app.ui.food.diary.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.common.extension.now
import com.maksimowiczm.foodyou.common.result.onError
import com.maksimowiczm.foodyou.common.result.onSuccess
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.repository.DiarySlotRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.NutritionalItemDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.UnpackNutritionalItemDiaryEntryUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.UpdateNutritionalItemDiaryEntryUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

internal class UpdateNutritionalItemDiaryEntryViewModel(
    private val entryId: NutritionalItemDiaryEntryId,
    private val updateFoodDiaryEntryUseCase: UpdateNutritionalItemDiaryEntryUseCase,
    private val unpackDiaryEntryError: UnpackNutritionalItemDiaryEntryUseCase,
    entryRepository: NutritionalItemDiaryEntryRepository,
    diarySlotRepository: DiarySlotRepository,
    dateProvider: DateProvider,
) : ViewModel() {

    val diarySlots =
        diarySlotRepository
            .observeDiarySlots()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = emptyList(),
            )

    val entry =
        entryRepository
            .observe(entryId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    val possibleMeasurementTypes =
        entry
            .filterNotNull()
            .flatMapLatest { entry -> entry.food.possibleMeasurementTypes }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    val suggestions: StateFlow<List<Measurement>?> =
        entry
            .filterNotNull()
            .flatMapLatest { entry ->
                entry.food.suggestions.map { (listOf(entry.measurement) + it).distinct() }
            }
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

    private val _uiEvents = Channel<UpdateEntryEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    fun save(measurement: Measurement, diarySlotId: Long, date: LocalDate) {
        if (saving) return
        saving = true
        viewModelScope.launch {
            try {
            updateFoodDiaryEntryUseCase
                .update(id = entryId, measurement = measurement, diarySlotId = diarySlotId, date = date)
                .onSuccess { _uiEvents.send(UpdateEntryEvent.Saved) }
                .onError {
                    // Explode
                    _uiEvents.send(UpdateEntryEvent.SaveFailed)
                }

            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiEvents.send(UpdateEntryEvent.SaveFailed)
            } finally { saving = false }
        }
    }

    private var saving = false

    fun unpack(measurement: Measurement, diarySlotId: Long, date: LocalDate) {
        if (saving) return
        saving = true
        viewModelScope.launch {
            try {
                unpackDiaryEntryError.unpack(entryId, measurement, diarySlotId, date)
                    .onSuccess { _uiEvents.send(UpdateEntryEvent.Saved) }
                    .onError { _uiEvents.send(UpdateEntryEvent.SaveFailed) }
            } finally { saving = false }
        }
    }

}

// These extensions will probably be moved into business when user would be able to choose between
// metric and imperial measurements. This is why they are wrapped in Flow, so they can be
// easily converted to the appropriate measurement system later.

private val NutritionalItemSnapshot.possibleMeasurementTypes: Flow<List<MeasurementType>>
    get() =
        flowOf(
            supportedMeasurementTypes()
        )

private val NutritionalItemSnapshot.suggestions: Flow<List<Measurement>>
    get() =
        possibleMeasurementTypes.map { list ->
            list.map {
                when (it) {
                    MeasurementType.Gram -> Measurement.Gram(Measurement.Gram.DEFAULT)
                    MeasurementType.Ounce -> Measurement.Ounce(Measurement.Ounce.DEFAULT)
                    MeasurementType.Package -> Measurement.Package(Measurement.Package.DEFAULT)
                    MeasurementType.Serving -> Measurement.Serving(Measurement.Serving.DEFAULT)
                    MeasurementType.Milliliter ->
                        Measurement.Milliliter(Measurement.Milliliter.DEFAULT)
                    MeasurementType.FluidOunce ->
                        Measurement.FluidOunce(Measurement.FluidOunce.DEFAULT)
                }
            }
        }
