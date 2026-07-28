package com.maksimowiczm.foodyou.app.ui.settings.relay

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.relay.domain.RelayCheckResult
import com.maksimowiczm.foodyou.relay.domain.RelayConnectionChecker
import com.maksimowiczm.foodyou.relay.domain.RelaySettings
import com.maksimowiczm.foodyou.relay.domain.RelayUrlValidationResult
import com.maksimowiczm.foodyou.relay.domain.RelayUrlValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Backs the relay settings screen (Milestone 3, Story 15). Loads the persisted relay URL once into
 * an editable field, validates it as HTTPS on save, and runs the capability check on explicit user
 * action. The URL is deliberately private and is never logged.
 */
internal class RelaySettingsViewModel(
    private val repository: UserPreferencesRepository<RelaySettings>,
    private val connectionChecker: RelayConnectionChecker,
) : ViewModel() {

    val url = TextFieldState()

    /**
     * The failing validation result for the current draft, or `null` when the field is valid or
     * intentionally empty. Only [RelayUrlValidationResult.NotHttps] and
     * [RelayUrlValidationResult.Malformed] are ever published here.
     */
    private val _urlError = MutableStateFlow<RelayUrlValidationResult?>(null)
    val urlError: StateFlow<RelayUrlValidationResult?> = _urlError.asStateFlow()

    private val _check = MutableStateFlow<RelayCheckUiState>(RelayCheckUiState.Idle)
    val check: StateFlow<RelayCheckUiState> = _check.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observe().first().url?.let(url::setTextAndPlaceCursorAtEnd)
        }
    }

    fun save() {
        when (val result = RelayUrlValidator.validate(url.text.toString())) {
            RelayUrlValidationResult.Unset -> persist(null)
            is RelayUrlValidationResult.Valid -> persist(result.url)
            else -> _urlError.value = result
        }
    }

    fun checkConnection() {
        if (_check.value == RelayCheckUiState.Checking) return

        val result = RelayUrlValidator.validate(url.text.toString())
        if (result !is RelayUrlValidationResult.Valid) {
            // Nothing usable to probe: surface why rather than firing a request at nothing.
            _urlError.value = result.takeIf { it != RelayUrlValidationResult.Unset }
            _check.value = RelayCheckUiState.Idle
            return
        }

        _urlError.value = null
        _check.value = RelayCheckUiState.Checking
        viewModelScope.launch {
            _check.value =
                when (val outcome = connectionChecker.check(result.url)) {
                    RelayCheckResult.Success -> RelayCheckUiState.Success
                    RelayCheckResult.Unknown -> RelayCheckUiState.Unknown
                    is RelayCheckResult.Unreachable ->
                        RelayCheckUiState.Unreachable(outcome.message)
                }
        }
    }

    private fun persist(validatedUrl: String?) {
        _urlError.value = null
        _check.value = RelayCheckUiState.Idle
        viewModelScope.launch { repository.update { RelaySettings(url = validatedUrl) } }
    }
}
