package com.maksimowiczm.foodyou.app.ui.settings.ai

/** UI state of the AI settings Validate button (Milestone 2, Story 22). */
internal sealed interface AiValidationUiState {
    /** No validation has run yet, or the fields changed since the last run. */
    data object Idle : AiValidationUiState

    /** A probe is in flight. */
    data object Validating : AiValidationUiState

    /** The endpoint accepted the entered credentials. */
    data object Success : AiValidationUiState

    /** The probe failed; [message] is the endpoint or transport error. */
    data class Error(val message: String) : AiValidationUiState
}
