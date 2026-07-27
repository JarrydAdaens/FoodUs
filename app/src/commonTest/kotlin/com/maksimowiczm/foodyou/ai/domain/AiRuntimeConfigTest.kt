package com.maksimowiczm.foodyou.ai.domain

import com.maksimowiczm.foodyou.common.config.AppConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AiRuntimeConfigTest {

    @Test
    fun userValueWinsOverBuildConfigFallback() {
        val settings =
            AiSettings(
                apiKey = "user-key",
                endpoint = "https://user.example/v1/chat/completions",
                model = "user/model",
                userSystemPrompt = null,
            )
        val appConfig =
            fakeAppConfig(apiKey = "dev-key", endpoint = "https://dev.example", model = "dev/model")

        val config = AiRuntimeConfig.resolve(settings, appConfig)

        assertEquals("user-key", config.apiKey)
        assertEquals("https://user.example/v1/chat/completions", config.endpoint)
        assertEquals("user/model", config.model)
        assertTrue(config.isConfigured)
    }

    @Test
    fun blankUserValueFallsThroughToBuildConfig() {
        val settings =
            AiSettings(apiKey = "  ", endpoint = null, model = "", userSystemPrompt = null)
        val appConfig =
            fakeAppConfig(apiKey = "dev-key", endpoint = "https://dev.example", model = "dev/model")

        val config = AiRuntimeConfig.resolve(settings, appConfig)

        assertEquals("dev-key", config.apiKey)
        assertEquals("https://dev.example", config.endpoint)
        assertEquals("dev/model", config.model)
    }

    @Test
    fun blankEverywhereFallsToDomainDefaultsAndIsNotConfigured() {
        val settings =
            AiSettings(apiKey = null, endpoint = null, model = null, userSystemPrompt = null)
        val appConfig = fakeAppConfig(apiKey = "", endpoint = "", model = "")

        val config = AiRuntimeConfig.resolve(settings, appConfig)

        // The key has no default, so a fully-blank config is "not configured".
        assertEquals("", config.apiKey)
        assertFalse(config.isConfigured)
        // Endpoint and model fall back to the public domain defaults.
        assertEquals(AiRuntimeConfig.DEFAULT_ENDPOINT, config.endpoint)
        assertEquals(AiRuntimeConfig.DEFAULT_MODEL, config.model)
    }

    private fun fakeAppConfig(apiKey: String, endpoint: String, model: String): AppConfig =
        object : AppConfig {
            override val versionName = ""
            override val forkVersionName = ""
            override val contactEmailUri = ""
            override val translationUri = ""
            override val sourceCodeUri = ""
            override val issueTrackerUri = ""
            override val upstreamAuthorUri = ""
            override val privacyPolicyUri = ""
            override val openFoodFactsTermsOfUseUri = ""
            override val openFoodFactsPrivacyPolicyUri = ""
            override val foodDataCentralPrivacyPolicyUri = ""
            override val openFoodFactsWebsiteUri = ""
            override val foodDataCentralWebsiteUri = ""
            override val swissFoodCompositionDatabaseWebsiteUri = ""
            override val australianFoodCompositionDatabaseWebsiteUri = ""
            override val aiApiKey = apiKey
            override val aiEndpoint = endpoint
            override val aiModel = model
        }
}
