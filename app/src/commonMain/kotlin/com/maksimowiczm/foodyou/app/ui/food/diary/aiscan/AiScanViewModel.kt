package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.maksimowiczm.foodyou.ai.domain.AiFoodScanner
import com.maksimowiczm.foodyou.ai.domain.AiScanResult
import com.maksimowiczm.foodyou.ai.domain.ObserveAiConfigured
import com.maksimowiczm.foodyou.common.domain.food.FoodSource
import com.maksimowiczm.foodyou.food.search.domain.FoodSearch
import com.maksimowiczm.foodyou.food.search.domain.FoodSearchUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class AiScanViewModel(
    private val aiFoodScanner: AiFoodScanner,
    private val foodSearchUseCase: FoodSearchUseCase,
    observeAiConfigured: ObserveAiConfigured,
) : ViewModel() {

    /**
     * Whether a usable AI configuration resolves (a non-blank key from user settings or the
     * developer fallback). Drives the "not configured" state; reactive so saving a key in AI
     * settings enables the Submit affordance without a restart.
     */
    val aiConfigured: StateFlow<Boolean> =
        observeAiConfigured()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Optional per-scan hint (layer 3): in-memory screen state only, never persisted. */
    val hint = TextFieldState()

    private val _state = MutableStateFlow<AiScanUiState>(AiScanUiState.NoPhoto)
    val state: StateFlow<AiScanUiState> = _state.asStateFlow()

    fun onPhotoCaptured(jpeg: ByteArray) {
        _state.value = AiScanUiState.Captured(jpeg)
    }

    fun onDiscard() {
        _state.value = AiScanUiState.NoPhoto
    }

    @OptIn(ExperimentalUuidApi::class)
    fun submit() {
        val jpeg = _state.value.jpeg ?: return
        _state.value = AiScanUiState.Scanning(jpeg)
        val hintText = hint.text.toString().trim().takeIf { it.isNotBlank() }
        viewModelScope.launch {
            _state.value =
                when (val result = aiFoodScanner.scan(jpeg, hintText)) {
                    is AiScanResult.Success -> if (result.estimates.isEmpty()) {
                        AiScanUiState.Failed(jpeg, "No usable foods were identified. Try another photo.")
                    } else AiScanUiState.Result(
                        jpeg, AiScanReviewSession.create(Uuid.random().toString(), result.estimates), result.rejectedCount,
                    )
                    is AiScanResult.Failure -> AiScanUiState.Failed(jpeg, result.message)
                    AiScanResult.NotConfigured ->
                        AiScanUiState.Failed(jpeg, NOT_CONFIGURED_MESSAGE)
                }
        }
    }

    fun select(id: String, selected: Boolean) {
        val current = _state.value as? AiScanUiState.Result ?: return
        _state.value = current.copy(review = current.review.select(id, selected))
    }

    fun startSelected(): ScanReviewItem? {
        val current = _state.value as? AiScanUiState.Result ?: return null
        val updated = current.review.start()
        _state.value = current.copy(review = updated)
        return updated.next
    }

    fun acknowledgeSaved(id: String): ScanReviewItem? {
        val current = _state.value as? AiScanUiState.Result ?: return null
        val updated = current.review.acknowledgeSaved(id)
        if (updated == current.review) return null
        _state.value = current.copy(review = updated)
        return updated.next
    }

    /** Searches locally saved custom foods (the user's own foods) by the AI-guessed name. */
    fun searchCustomFoods(query: String): Flow<PagingData<FoodSearch>> =
        foodSearchUseCase
            .search(query = query, source = FoodSource.Type.User, excludedRecipeId = null)
            .cachedIn(viewModelScope)

    private companion object {
        const val NOT_CONFIGURED_MESSAGE = "AI is not configured. Add a key in AI settings."
    }
}
