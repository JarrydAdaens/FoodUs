package com.maksimowiczm.foodyou.ai.domain

import com.maksimowiczm.foodyou.common.config.AppConfig
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Observes whether the AI features have a usable configuration — a resolvable, non-blank API key —
 * reusing the same [AiRuntimeConfig] precedence the scanners apply at call time (Milestone 2, Story
 * 21). UI gates observe this instead of reading the BuildConfig key directly, so a user-entered key
 * enables the AI affordances even when the developer fallback is blank.
 */
class ObserveAiConfigured(
    private val aiSettingsRepository: UserPreferencesRepository<AiSettings>,
    private val appConfig: AppConfig,
) {
    operator fun invoke(): Flow<Boolean> =
        aiSettingsRepository.observe().map { AiRuntimeConfig.resolve(it, appConfig).isConfigured }
}
