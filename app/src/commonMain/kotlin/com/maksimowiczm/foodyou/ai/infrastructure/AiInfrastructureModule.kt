package com.maksimowiczm.foodyou.ai.infrastructure

import com.maksimowiczm.foodyou.ai.domain.AiConnectionValidator
import com.maksimowiczm.foodyou.ai.domain.AiFoodScanner
import com.maksimowiczm.foodyou.ai.domain.AiSearchQueryGenerator
import com.maksimowiczm.foodyou.ai.domain.ObserveAiConfigured
import com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepository
import com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepositoryOf
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind

/**
 * Koin wiring for the reusable AI endpoint client (Milestone 2, Story 6). The [HttpClient] is
 * registered under a named qualifier following the per-data-source pattern used by the USDA and
 * Open Food Facts modules.
 */
fun Module.aiInfrastructureModule() {
    single(named(AiFoodScanner::class.qualifiedName!!)) {
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 60_000
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }
        }
    }

    // On-device persistence for the user-entered AI settings (Story 22).
    userPreferencesRepositoryOf(::DataStoreAiSettingsRepository)

    // Reactive "is the AI configured?" signal for UI gates (Story 21), reusing the runtime-config
    // precedence so a user-entered key enables affordances even with a blank BuildConfig fallback.
    factory { ObserveAiConfigured(aiSettingsRepository = userPreferencesRepository(), appConfig = get()) }

    factory {
        OpenRouterAiFoodScanner(
            client = get(named(AiFoodScanner::class.qualifiedName!!)),
            appConfig = get(),
            aiSettingsRepository = userPreferencesRepository(),
        )
    }
        .bind<AiFoodScanner>()

    // Story 9's text-only query generation reuses the same endpoint client and config seam.
    factory {
        OpenRouterAiSearchQueryGenerator(
            client = get(named(AiFoodScanner::class.qualifiedName!!)),
            appConfig = get(),
            aiSettingsRepository = userPreferencesRepository(),
        )
    }
        .bind<AiSearchQueryGenerator>()

    // Story 22's Validate button probes the entered key/endpoint/model over the same client.
    factory { OpenRouterAiConnectionValidator(client = get(named(AiFoodScanner::class.qualifiedName!!))) }
        .bind<AiConnectionValidator>()
}
