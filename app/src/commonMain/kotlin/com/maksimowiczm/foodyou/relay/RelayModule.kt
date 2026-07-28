package com.maksimowiczm.foodyou.relay

import com.maksimowiczm.foodyou.relay.infrastructure.relayInfrastructureModule
import org.koin.dsl.module

/**
 * Relay feature slice (Milestone 3): the transport-facing side of household multiplayer. Story 15
 * seeds it with the user-entered relay URL, the configured signal, and the capability-check seam.
 */
val relayModule = module { relayInfrastructureModule() }
