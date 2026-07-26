package com.maksimowiczm.foodyou.ai.infrastructure

import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimateParser
import com.maksimowiczm.foodyou.ai.domain.AiFoodScanner
import com.maksimowiczm.foodyou.ai.domain.AiScanResult
import com.maksimowiczm.foodyou.ai.infrastructure.model.ChatCompletionRequest
import com.maksimowiczm.foodyou.ai.infrastructure.model.ChatCompletionResponse
import com.maksimowiczm.foodyou.ai.infrastructure.model.ChatMessage
import com.maksimowiczm.foodyou.ai.infrastructure.model.ImageContent
import com.maksimowiczm.foodyou.ai.infrastructure.model.ImageUrl
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
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * [AiFoodScanner] backed by an OpenRouter-compatible chat-completions endpoint (Milestone 2,
 * Story 6). The endpoint, model, and private API key come from [AppConfig], which is baked in at
 * build time. The key is never logged.
 */
internal class OpenRouterAiFoodScanner(private val client: HttpClient, private val appConfig: AppConfig) :
    AiFoodScanner {

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun scan(jpeg: ByteArray): AiScanResult {
        val apiKey = appConfig.aiApiKey
        if (apiKey.isBlank()) return AiScanResult.NotConfigured

        return try {
            val dataUrl = "data:image/jpeg;base64," + Base64.encode(jpeg)
            val request =
                ChatCompletionRequest(
                    model = appConfig.aiModel,
                    messages =
                        listOf(
                            ChatMessage(
                                role = "user",
                                content =
                                    listOf(TextContent(PROMPT), ImageContent(ImageUrl(dataUrl))),
                            )
                        ),
                )

            val response =
                client.post(appConfig.aiEndpoint) {
                    bearerAuth(apiKey)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

            if (!response.status.isSuccess()) {
                return AiScanResult.Failure("AI request failed (${response.status.value}).")
            }

            val content =
                response.body<ChatCompletionResponse>().choices.firstOrNull()?.message?.content
                    ?: return AiScanResult.Failure("The AI returned an empty response.")

            AiFoodEstimateParser.parse(content)?.let(AiScanResult::Success)
                ?: AiScanResult.Failure("Could not understand the AI response.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AiScanResult.Failure(e.message ?: "AI request failed.")
        }
    }

    private companion object {
        // Embedded Australian (Victoria) locale prompt requesting a strict JSON schema.
        const val PROMPT =
            "We are Australians living in Victoria, Australia. This photo is my food. Please " +
                "identify it using Australian food knowledge and typical Australian serving sizes. " +
                "Respond with ONLY a JSON object (no Markdown, no commentary) with exactly these " +
                "keys: \"name\" (string), \"certainty\" (number between 0 and 1), \"calories\" " +
                "(kilocalories, number), \"protein\" (grams, number), \"fat\" (grams, number), " +
                "\"fibre\" (grams, number), \"sugar\" (grams, number). If unsure, still provide " +
                "your best estimate."
    }
}
