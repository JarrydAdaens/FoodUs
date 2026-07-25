package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.maksimowiczm.foodyou.ai.domain.AiFoodScanner
import com.maksimowiczm.foodyou.ai.domain.AiScanResult
import com.maksimowiczm.foodyou.common.config.AppConfig
import com.maksimowiczm.foodyou.common.domain.food.FoodSource
import com.maksimowiczm.foodyou.food.search.domain.FoodSearch
import com.maksimowiczm.foodyou.food.search.domain.FoodSearchUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class AiScanViewModel(
    private val aiFoodScanner: AiFoodScanner,
    private val foodSearchUseCase: FoodSearchUseCase,
    appConfig: AppConfig,
) : ViewModel() {

    /** Whether a private-build API key was baked in. Drives the "not configured" state. */
    val aiConfigured: Boolean = appConfig.aiApiKey.isNotBlank()

    private val _state = MutableStateFlow<AiScanUiState>(AiScanUiState.NoPhoto)
    val state: StateFlow<AiScanUiState> = _state.asStateFlow()

    fun onPhotoCaptured(jpeg: ByteArray) {
        _state.value = AiScanUiState.Captured(jpeg)
    }

    fun onDiscard() {
        _state.value = AiScanUiState.NoPhoto
    }

    fun askAi() {
        val jpeg = _state.value.jpeg ?: return
        _state.value = AiScanUiState.Scanning(jpeg)
        viewModelScope.launch {
            _state.value =
                when (val result = aiFoodScanner.scan(jpeg)) {
                    is AiScanResult.Success -> AiScanUiState.Result(jpeg, result.estimate)
                    is AiScanResult.Failure -> AiScanUiState.Failed(jpeg, result.message)
                    AiScanResult.NotConfigured ->
                        AiScanUiState.Failed(jpeg, NOT_CONFIGURED_MESSAGE)
                }
        }
    }

    /** Searches locally saved custom foods (the user's own foods) by the AI-guessed name. */
    fun searchCustomFoods(query: String): Flow<PagingData<FoodSearch>> =
        foodSearchUseCase
            .search(query = query, source = FoodSource.Type.User, excludedRecipeId = null)
            .cachedIn(viewModelScope)

    private companion object {
        const val NOT_CONFIGURED_MESSAGE = "AI is not configured in this build."
    }
}
