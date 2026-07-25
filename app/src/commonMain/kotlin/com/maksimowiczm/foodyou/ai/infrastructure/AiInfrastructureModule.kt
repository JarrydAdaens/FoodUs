package com.maksimowiczm.foodyou.ai.infrastructure

import com.maksimowiczm.foodyou.ai.domain.AiFoodScanner
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

    factory {
        OpenRouterAiFoodScanner(
            client = get(named(AiFoodScanner::class.qualifiedName!!)),
            appConfig = get(),
        )
    }
        .bind<AiFoodScanner>()
}
