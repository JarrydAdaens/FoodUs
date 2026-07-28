package com.maksimowiczm.foodyou.relay.domain

import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Observes whether this device has a relay address to talk to (Milestone 3, Story 15), mirroring
 * `ObserveAiConfigured`. Relay-backed features observe this instead of reading the settings entity
 * directly, so an unset relay leaves them hidden or greyed rather than failing at call time.
 *
 * This signal answers "is a relay configured", not "does that relay support feature X" — the latter
 * needs [RelayConnectionChecker] and the capability names the wire contract will define.
 */
class ObserveRelayConfigured(
    private val relaySettingsRepository: UserPreferencesRepository<RelaySettings>
) {
    operator fun invoke(): Flow<Boolean> =
        relaySettingsRepository.observe().map { !it.url.isNullOrBlank() }
}
