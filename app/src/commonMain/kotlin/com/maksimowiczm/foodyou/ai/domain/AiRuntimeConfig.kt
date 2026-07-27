package com.maksimowiczm.foodyou.ai.domain

import com.maksimowiczm.foodyou.common.config.AppConfig

/**
 * Effective AI configuration used at call time (Milestone 2, Story 22).
 *
 * Resolution precedence, per field: the user-entered [AiSettings] value (when non-blank) wins,
 * then the developer BuildConfig fallback in [AppConfig] (blank by default in public builds), then
 * a public domain default. The API key has no default — a blank key means the AI features are not
 * configured and no request is made.
 */
data class AiRuntimeConfig(val apiKey: String, val endpoint: String, val model: String) {

    /** True when a usable API key resolved, mirroring the scanners' "not configured" guard. */
    val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    companion object {
        const val DEFAULT_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"
        const val DEFAULT_MODEL = "openai/gpt-4o-mini"

        fun resolve(settings: AiSettings, appConfig: AppConfig): AiRuntimeConfig =
            AiRuntimeConfig(
                apiKey = firstNonBlank(settings.apiKey, appConfig.aiApiKey) ?: "",
                endpoint =
                    firstNonBlank(settings.endpoint, appConfig.aiEndpoint) ?: DEFAULT_ENDPOINT,
                model = firstNonBlank(settings.model, appConfig.aiModel) ?: DEFAULT_MODEL,
            )

        private fun firstNonBlank(vararg candidates: String?): String? =
            candidates.firstOrNull { !it.isNullOrBlank() }
    }
}
