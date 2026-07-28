package com.maksimowiczm.foodyou.relay.infrastructure

import com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepository
import com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepositoryOf
import com.maksimowiczm.foodyou.relay.domain.ObserveRelayConfigured
import com.maksimowiczm.foodyou.relay.domain.RelayConnectionChecker
import org.koin.core.module.Module
import org.koin.dsl.bind

/** Koin wiring for the relay slice (Milestone 3, Story 15). */
fun Module.relayInfrastructureModule() {
    // On-device persistence for the user-entered relay URL.
    userPreferencesRepositoryOf(::DataStoreRelaySettingsRepository)

    // Reactive "is a relay configured?" signal for the capability-aware UI gates in later stories.
    factory { ObserveRelayConfigured(relaySettingsRepository = userPreferencesRepository()) }

    // Placeholder probe until the foodus-relay wire contract defines the capability endpoint.
    factory { PendingContractRelayConnectionChecker() }.bind<RelayConnectionChecker>()
}
