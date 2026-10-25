package com.maksimowiczm.foodyou.app.ui.goals.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.food.isComplete
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ObserveDiarySlotEntriesUseCase
import com.maksimowiczm.foodyou.goals.domain.repository.GoalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

internal class GoalsViewModel(
    private val goalsRepository: GoalsRepository,
    private val observeDiarySlotEntriesUseCase: ObserveDiarySlotEntriesUseCase,
) : ViewModel() {
    private val refresh = MutableStateFlow(0)
    val failedDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    fun retryRead(date: LocalDate) { failedDates.value = failedDates.value - date; refresh.value++ }
    private val diarySlotsFlows = mutableMapOf<LocalDate, StateFlow<GoalsScreenUiState?>>()

    fun observeUiStateByDate(date: LocalDate): StateFlow<GoalsScreenUiState?> {
        diarySlotsFlows[date]?.let {
            return it
        }

        val diarySlots =
            observeDiarySlotEntriesUseCase.observe(date).map { list ->
                list.map {
                    DiarySlotModel(
                        id = it.diarySlot.id,
                        name = it.diarySlot.name,
                        nutritionFacts = it.nutritionFacts,
                        incompleteFoods =
                            it.entries
                                .filterNot { it.nutritionFacts.isComplete }
                                .map { it.name }
                                .distinct(),
                    )
                }
            }
        val goal = goalsRepository.observeDailyGoals(date)

        val flow =
            refresh.flatMapLatest {
                combine(diarySlots, goal, ::GoalsScreenUiState)
                    .map<GoalsScreenUiState, GoalsScreenUiState?> { it }
                    .catch { failedDates.value = failedDates.value + date; emit(null) }
            }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(30_000),
                    initialValue = null,
                )

        diarySlotsFlows[date] = flow
        return flow
    }
}
