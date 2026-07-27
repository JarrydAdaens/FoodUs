package com.maksimowiczm.foodyou.ai.domain

import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferences

/**
 * User-entered AI configuration persisted on-device (Milestone 2, Story 22).
 *
 * Every field is nullable, where `null` means "unset" so the runtime falls back to the developer
 * BuildConfig value and finally the domain default (see [AiRuntimeConfig]). The [apiKey] is a
 * credential: it lives only in on-device storage and is never logged or echoed.
 *
 * [userSystemPrompt] is stored here but not yet consumed — Story 21's three-layer prompt assembly
 * reads it as its layer-2 personal-context field.
 */
data class AiSettings(
    val apiKey: String?,
    val endpoint: String?,
    val model: String?,
    val userSystemPrompt: String?,
) : UserPreferences
