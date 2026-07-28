package com.maksimowiczm.foodyou.relay.infrastructure

import com.maksimowiczm.foodyou.relay.domain.RelayCheckResult
import com.maksimowiczm.foodyou.relay.domain.RelayConnectionChecker

/**
 * The shipped [RelayConnectionChecker] while the foodus-relay wire contract is unwritten
 * (Milestone 3, Story 15).
 *
 * It always reports [RelayCheckResult.Unknown]. The real probe needs the version/capability
 * endpoint's path, method, and response schema, all of which are owned by the foodus-relay
 * repository and unspecified as of 2026-07-28. Guessing a path would bake a fictional contract into
 * the app and give the user a confident-looking failure that means nothing, so the app says
 * "unknown" honestly instead. Replacing this with the Ktor-backed probe is a change behind
 * [RelayConnectionChecker] plus its Koin binding — no consumer moves.
 */
internal class PendingContractRelayConnectionChecker : RelayConnectionChecker {
    override suspend fun check(baseUrl: String): RelayCheckResult = RelayCheckResult.Unknown
}
