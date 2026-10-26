package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class UpdateQuickAddViewModel(
    id: ManualDiaryEntryId,
    private val manualDiaryEntryRepository: ManualDiaryEntryRepository,
    private val dateProvider: DateProvider,
) : ViewModel() {

    private val eventChannel = Channel<QuickAddUiEvent>()
    val uiEvents = eventChannel.receiveAsFlow()

    val readFailed = MutableStateFlow(false)
    private val refresh = MutableStateFlow(0)
    fun retryRead() { readFailed.value = false; refresh.value++ }
    val entry = refresh.flatMapLatest {
        manualDiaryEntryRepository.observe(id).catch { readFailed.value = true; emit(null) }
    }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    fun updateEntry(
        name: String,
        nutritionFacts: NutritionFacts,
        description: String?,
        servingCount: Double?,
        weightGrams: Double?,
    ) {
        val entry = entry.value

        if (entry == null) {
            return
        }

        viewModelScope.launch {
            val updatedEntry =
                entry.copy(
                    name = name,
                    nutritionFacts = nutritionFacts,
                    description = description,
                    servingCount = servingCount,
                    weightGrams = weightGrams,
                    capturedPortion = if (servingCount != entry.servingCount || weightGrams != entry.weightGrams) null else entry.capturedPortion,
                    updatedAt = dateProvider.now(),
                )

            manualDiaryEntryRepository.update(updatedEntry)

            eventChannel.send(QuickAddUiEvent.Saved)
        }
    }
}
