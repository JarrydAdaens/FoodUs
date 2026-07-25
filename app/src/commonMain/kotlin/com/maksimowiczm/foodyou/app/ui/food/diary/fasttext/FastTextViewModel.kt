package com.maksimowiczm.foodyou.app.ui.food.diary.fasttext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.NutrientValue.Companion.toNutrientValue
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * Backs the fast-text placeholder capture (Milestone 2, Story 8). Saves a zero-nutrition
 * [placeholder][com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntry] carrying only a
 * name and optional description, reusing the existing manual diary entry mechanism so it renders and
 * deletes like any other entry while adding nothing to the day's nutrition totals.
 */
internal class FastTextViewModel(
    private val mealId: Long,
    private val date: LocalDate,
    private val manualDiaryEntryRepository: ManualDiaryEntryRepository,
    private val dateProvider: DateProvider,
) : ViewModel() {

    private val eventChannel = Channel<FastTextEvent>()
    val events = eventChannel.receiveAsFlow()

    fun savePlaceholder(name: String, description: String) {
        viewModelScope.launch {
            manualDiaryEntryRepository.insert(
                name = name.trim(),
                mealId = mealId,
                date = date,
                // Zero-calorie marker: energy and macros are explicitly zero so the entry renders
                // "0 kcal" like a normal entry without contributing to daily totals.
                nutritionFacts =
                    NutritionFacts(
                        energy = 0.0.toNutrientValue(),
                        proteins = 0.0.toNutrientValue(),
                        carbohydrates = 0.0.toNutrientValue(),
                        fats = 0.0.toNutrientValue(),
                    ),
                createdAt = dateProvider.now(),
                description = description.trim().ifBlank { null },
                isPlaceholder = true,
            )

            eventChannel.send(FastTextEvent.Saved)
        }
    }
}
