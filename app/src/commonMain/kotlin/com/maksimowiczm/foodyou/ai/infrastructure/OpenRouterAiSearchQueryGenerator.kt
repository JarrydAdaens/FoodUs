package com.maksimowiczm.foodyou.ai.infrastructure

import com.maksimowiczm.foodyou.ai.domain.AiQueryResult
import com.maksimowiczm.foodyou.ai.domain.AiSearchQueryGenerator
import com.maksimowiczm.foodyou.ai.domain.AiSearchQueryParser
import com.maksimowiczm.foodyou.ai.domain.AiSearchQueryPrompt
import com.maksimowiczm.foodyou.ai.infrastructure.model.ChatCompletionRequest
import com.maksimowiczm.foodyou.ai.infrastructure.model.ChatCompletionResponse
import com.maksimowiczm.foodyou.ai.infrastructure.model.ChatMessage
import com.maksimowiczm.foodyou.ai.infrastructure.model.TextContent
import com.maksimowiczm.foodyou.common.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException

/**
 * [AiSearchQueryGenerator] backed by the same OpenRouter-compatible chat-completions endpoint as the
 * AI food scanner (Milestone 2, Story 9). This is a text-only sibling call: it reuses the shared
 * [HttpClient], [AppConfig], and DTOs rather than duplicating the transport. The key is never logged.
 */
internal class OpenRouterAiSearchQueryGenerator(
    private val client: HttpClient,
    private val appConfig: AppConfig,
) : AiSearchQueryGenerator {

    override suspend fun generateQuery(mealName: String?, note: String): AiQueryResult {
        val apiKey = appConfig.aiApiKey
        if (apiKey.isBlank()) return AiQueryResult.NotConfigured
        if (note.isBlank()) return AiQueryResult.Failure("Nothing to search for.")

        return try {
            val prompt = AiSearchQueryPrompt.build(mealName, note)
            val request =
                ChatCompletionRequest(
                    model = appConfig.aiModel,
                    messages =
                        listOf(ChatMessage(role = "user", content = listOf(TextContent(prompt)))),
                )

            val response =
                client.post(appConfig.aiEndpoint) {
                    bearerAuth(apiKey)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

            if (!response.status.isSuccess()) {
                return AiQueryResult.Failure("AI request failed (${response.status.value}).")
            }

            val content =
                response.body<ChatCompletionResponse>().choices.firstOrNull()?.message?.content
                    ?: return AiQueryResult.Failure("The AI returned an empty response.")

            AiSearchQueryParser.parse(content)?.let(AiQueryResult::Success)
                ?: AiQueryResult.Failure("Could not understand the AI response.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AiQueryResult.Failure(e.message ?: "AI request failed.")
        }
    }
}
