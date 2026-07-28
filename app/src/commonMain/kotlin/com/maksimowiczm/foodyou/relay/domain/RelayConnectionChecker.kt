package com.maksimowiczm.foodyou.relay.domain

/** Outcome of a relay connection/capability check (Milestone 3, Story 15). */
sealed interface RelayCheckResult {
    /** The relay answered and reported its capabilities. */
    data object Success : RelayCheckResult

    /**
     * The relay could not be reached or refused the probe; [message] is the transport or endpoint
     * detail when one is available, and `null` when there is nothing useful to show.
     */
    data class Unreachable(val message: String?) : RelayCheckResult

    /**
     * The relay's capabilities cannot be determined. Today this is the only result the shipped
     * implementation returns: the version/capability endpoint lives in the foodus-relay wire
     * contract, which is not published yet, so the app has nothing legitimate to call.
     */
    data object Unknown : RelayCheckResult
}

/**
 * Probes the configured relay's version/capability endpoint (Milestone 3, Story 15).
 *
 * This is the seam that the capability-aware UI rule depends on: relay-backed features check here
 * before exposing themselves, and treat anything other than [RelayCheckResult.Success] as "not
 * available yet". The probe runs only on explicit user action — there is no background polling.
 *
 * The endpoint path, request shape, and response schema are owned by the foodus-relay wire
 * contract. Until contract v1 is published, the implementation behind this interface reports
 * [RelayCheckResult.Unknown] rather than guessing a path; wiring the real probe is a change behind
 * this interface only.
 */
interface RelayConnectionChecker {
    suspend fun check(baseUrl: String): RelayCheckResult
}
