package com.maksimowiczm.foodyou.relay.domain

import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferences

/**
 * User-entered relay configuration persisted on-device (Milestone 3, Story 15).
 *
 * [url] is `null` when unset, which is the shipped state: unlike the AI endpoint there is
 * deliberately **no default, placeholder, or developer fallback address**. The owner's relay
 * address is private and must never appear in code, resources, tests, or the repository — every
 * device is pointed at a relay by the person holding it. The value is never logged or echoed.
 */
data class RelaySettings(val url: String?) : UserPreferences
