package com.maksimowiczm.foodyou.app.ui.food.diary.placeholder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.ai.domain.AiQueryResult
import com.maksimowiczm.foodyou.ai.domain.AiSearchQueryGenerator
import com.maksimowiczm.foodyou.common.config.AppConfig
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs the placeholder resolution meta screen (Milestone 2, Story 9). Loads the fast-text
 * placeholder so its saved name/description can frame the three resolution routes (search, quick
 * add, AI), generates a better search query on demand via the shared [AiSearchQueryGenerator], and
 * removes the placeholder once the user has logged the real entry.
 */
internal class PlaceholderMetaViewModel(
    private val manualEntryId: Long,
    private val manualDiaryEntryRepository: ManualDiaryEntryRepository,
    mealRepository: MealRepository,
    private val aiSearchQueryGenerator: AiSearchQueryGenerator,
    appConfig: AppConfig,
) : ViewModel() {

    private val aiConfigured = appConfig.aiApiKey.isNotBlank()

    private val eventChannel = Channel<PlaceholderMetaEvent>()
    val events = eventChannel.receiveAsFlow()

    private val generating = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    private val entryFlow =
        manualDiaryEntryRepository.observe(ManualDiaryEntryId(manualEntryId))

    private val mealNameFlow =
        entryFlow.flatMapLatest { entry ->
            if (entry == null) flowOf(null)
            else mealRepository.observeMeal(entry.mealId).map { it?.name }
        }

    val uiState: StateFlow<PlaceholderMetaUiState> =
        combine(entryFlow, mealNameFlow, generating, error) { entry, mealName, generating, error ->
                if (entry == null) {
                    PlaceholderMetaUiState(loaded = false, aiConfigured = aiConfigured)
                } else {
                    PlaceholderMetaUiState(
                        loaded = true,
                        epochDay = entry.date.toEpochDays(),
                        mealId = entry.mealId,
                        name = entry.name,
                        description = entry.description,
                        mealName = mealName,
                        aiConfigured = aiConfigured,
                        generatingQuery = generating,
                        errorMessage = error,
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = PlaceholderMetaUiState(aiConfigured = aiConfigured),
            )

    /** Asks the model to turn the placeholder note into a food-search query and emits the result. */
    fun generateQuery() {
        val current = uiState.value
        if (!current.loaded || generating.value) return

        viewModelScope.launch {
            error.value = null
            generating.value = true
            val result = aiSearchQueryGenerator.generateQuery(current.mealName, current.note())
            generating.value = false

            when (result) {
                is AiQueryResult.Success ->
                    eventChannel.send(
                        PlaceholderMetaEvent.QueryGenerated(
                            epochDay = current.epochDay,
                            mealId = current.mealId,
                            query = result.query,
                        )
                    )

                is AiQueryResult.Failure -> error.value = result.message
                AiQueryResult.NotConfigured -> error.value = NOT_CONFIGURED_MESSAGE
            }
        }
    }

    /** Removes the placeholder once the real entry has been logged through one of the routes. */
    fun removePlaceholder() {
        viewModelScope.launch {
            manualDiaryEntryRepository.delete(ManualDiaryEntryId(manualEntryId))
            eventChannel.send(PlaceholderMetaEvent.Deleted)
        }
    }

    private fun PlaceholderMetaUiState.note(): String =
        buildString {
            append(name.trim())
            description?.trim()?.takeIf { it.isNotBlank() }?.let { append(" — ").append(it) }
        }

    private companion object {
        const val NOT_CONFIGURED_MESSAGE = "AI is not configured in this build."
    }
}
