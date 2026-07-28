package com.maksimowiczm.foodyou.app.ui.settings.relay

/** UI state of the relay settings "Check connection" button (Milestone 3, Story 15). */
internal sealed interface RelayCheckUiState {
    /** No check has run yet. */
    data object Idle : RelayCheckUiState

    /** A probe is in flight. */
    data object Checking : RelayCheckUiState

    /** The relay answered and reported its capabilities. */
    data object Success : RelayCheckUiState

    /** The relay could not be reached; [message] is the transport detail when one is available. */
    data class Unreachable(val message: String?) : RelayCheckUiState

    /** The relay's capabilities could not be determined — see `PendingContractRelayConnectionChecker`. */
    data object Unknown : RelayCheckUiState
}
