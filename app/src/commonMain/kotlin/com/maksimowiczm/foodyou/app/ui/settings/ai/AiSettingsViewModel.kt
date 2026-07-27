package com.maksimowiczm.foodyou.app.ui.settings.ai

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.ai.domain.AiConnectionValidator
import com.maksimowiczm.foodyou.ai.domain.AiRuntimeConfig
import com.maksimowiczm.foodyou.ai.domain.AiSettings
import com.maksimowiczm.foodyou.ai.domain.AiValidationResult
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Backs the AI settings screen (Milestone 2, Story 22). Loads the persisted [AiSettings] once into
 * four editable text fields, saves them back (blank persisted as unset), and runs the Validate probe
 * against the current field drafts. Field values, including the key, are never logged.
 */
internal class AiSettingsViewModel(
    private val repository: UserPreferencesRepository<AiSettings>,
    private val validator: AiConnectionValidator,
) : ViewModel() {

    val apiKey = TextFieldState()
    val endpoint = TextFieldState()
    val model = TextFieldState()
    val systemPrompt = TextFieldState()

    /** Placeholder text shown when the endpoint/model fields are blank (the effective defaults). */
    val endpointPlaceholder: String = AiRuntimeConfig.DEFAULT_ENDPOINT
    val modelPlaceholder: String = AiRuntimeConfig.DEFAULT_MODEL

    private val _validation = MutableStateFlow<AiValidationUiState>(AiValidationUiState.Idle)
    val validation: StateFlow<AiValidationUiState> = _validation.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = repository.observe().first()
            settings.apiKey?.let(apiKey::setTextAndPlaceCursorAtEnd)
            settings.endpoint?.let(endpoint::setTextAndPlaceCursorAtEnd)
            settings.model?.let(model::setTextAndPlaceCursorAtEnd)
            settings.userSystemPrompt?.let(systemPrompt::setTextAndPlaceCursorAtEnd)
        }
    }

    fun save() {
        val updated =
            AiSettings(
                apiKey = apiKey.trimmedOrNull(),
                endpoint = endpoint.trimmedOrNull(),
                model = model.trimmedOrNull(),
                userSystemPrompt = systemPrompt.trimmedOrNull(),
            )
        viewModelScope.launch { repository.update { updated } }
    }

    fun validate() {
        if (_validation.value == AiValidationUiState.Validating) return
        _validation.value = AiValidationUiState.Validating
        viewModelScope.launch {
            val result =
                validator.validate(
                    apiKey = apiKey.text.toString().trim(),
                    endpoint = endpoint.trimmedOrNull() ?: AiRuntimeConfig.DEFAULT_ENDPOINT,
                    model = model.trimmedOrNull() ?: AiRuntimeConfig.DEFAULT_MODEL,
                )
            _validation.value =
                when (result) {
                    AiValidationResult.Success -> AiValidationUiState.Success
                    is AiValidationResult.Failure -> AiValidationUiState.Error(result.message)
                }
        }
    }

    private fun TextFieldState.trimmedOrNull(): String? =
        text.toString().trim().takeIf { it.isNotBlank() }
}
