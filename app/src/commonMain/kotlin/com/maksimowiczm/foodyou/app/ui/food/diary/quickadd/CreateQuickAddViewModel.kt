package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

internal class CreateQuickAddViewModel(
    private val mealId: Long,
    private val date: LocalDate,
    private val manualDiaryEntryRepository: ManualDiaryEntryRepository,
    private val dateProvider: DateProvider,
) : ViewModel() {

    private val eventChannel = Channel<QuickAddUiEvent>()
    val uiEvents = eventChannel.receiveAsFlow()
    private val _saveState = MutableStateFlow(CreateQuickAddSaveState())
    val saveState = _saveState.asStateFlow()

    fun addEntry(
        name: String,
        nutritionFacts: NutritionFacts,
        description: String?,
        servingCount: Double?,
        weightGrams: Double?,
    ) {
        _saveState.value = _saveState.value.start() ?: return
        viewModelScope.launch {
            try {
                manualDiaryEntryRepository.insert(
                    name = name,
                    mealId = mealId,
                    date = date,
                    nutritionFacts = nutritionFacts,
                    createdAt = dateProvider.now(),
                    description = description,
                    servingCount = servingCount,
                    weightGrams = weightGrams,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _saveState.value = _saveState.value.failure()
                return@launch
            }
            // Once insertion succeeds, never turn a later navigation cancellation into a retry.
            _saveState.value = _saveState.value.success()
            eventChannel.send(QuickAddUiEvent.Saved)
        }
    }
}
