package com.maksimowiczm.foodyou.ai.infrastructure

import com.maksimowiczm.foodyou.ai.domain.AiConnectionValidator
import com.maksimowiczm.foodyou.ai.domain.AiValidationResult
import com.maksimowiczm.foodyou.ai.infrastructure.model.ChatCompletionRequest
import com.maksimowiczm.foodyou.ai.infrastructure.model.ChatMessage
import com.maksimowiczm.foodyou.ai.infrastructure.model.TextContent
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException

/**
 * [AiConnectionValidator] backed by the shared OpenRouter-compatible chat-completions client
 * (Milestone 2, Story 22). Sends one trivial one-token request; a 2xx response is a success, and any
 * other outcome surfaces the endpoint's response body (or the transport exception message) verbatim.
 * The key travels only in the authorization header and is never logged.
 */
internal class OpenRouterAiConnectionValidator(private val client: HttpClient) :
    AiConnectionValidator {

    override suspend fun validate(
        apiKey: String,
        endpoint: String,
        model: String,
    ): AiValidationResult =
        try {
            val request =
                ChatCompletionRequest(
                    model = model,
                    messages =
                        listOf(ChatMessage(role = "user", content = listOf(TextContent("Hi")))),
                    maxTokens = 1,
                )

            val response =
                client.post(endpoint) {
                    bearerAuth(apiKey)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

            if (response.status.isSuccess()) {
                AiValidationResult.Success
            } else {
                val body = response.bodyAsText().trim()
                val detail = body.ifBlank { response.status.toString() }
                AiValidationResult.Failure(detail)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AiValidationResult.Failure(e.message ?: "Validation request failed.")
        }
}
