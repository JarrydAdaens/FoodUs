package com.maksimowiczm.foodyou.ai.infrastructure

import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimateParser
import com.maksimowiczm.foodyou.ai.domain.AiFoodScanner
import com.maksimowiczm.foodyou.ai.domain.AiRuntimeConfig
import com.maksimowiczm.foodyou.ai.domain.AiScanPrompt
import com.maksimowiczm.foodyou.ai.domain.AiScanResult
import com.maksimowiczm.foodyou.ai.domain.AiSettings
import com.maksimowiczm.foodyou.ai.infrastructure.model.ChatCompletionRequest
import com.maksimowiczm.foodyou.ai.infrastructure.model.ChatCompletionResponse
import com.maksimowiczm.foodyou.ai.infrastructure.model.ChatMessage
import com.maksimowiczm.foodyou.ai.infrastructure.model.ImageContent
import com.maksimowiczm.foodyou.ai.infrastructure.model.ImageUrl
import com.maksimowiczm.foodyou.ai.infrastructure.model.TextContent
import com.maksimowiczm.foodyou.common.config.AppConfig
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
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
import kotlinx.coroutines.flow.first

/**
 * [AiFoodScanner] backed by an OpenRouter-compatible chat-completions endpoint (Milestone 2,
 * Story 6; three-layer prompt assembly added in Story 21). The endpoint, model, and API key resolve
 * at call time from the user-entered [AiSettings] first, falling back to the [AppConfig] developer
 * values (Story 22).
 *
 * Each request is assembled from up to three additive layers: layer 1 is the baked, personal-data-
 * free [AiScanPrompt]; layer 2 is the user's optional persisted system prompt, sent as a system
 * message when set; layer 3 is the optional per-scan [hint], carried in the same user message as the
 * photo and applied to this call only. The key is never logged.
 */
internal class OpenRouterAiFoodScanner(
    private val client: HttpClient,
    private val appConfig: AppConfig,
    private val aiSettingsRepository: UserPreferencesRepository<AiSettings>,
) : AiFoodScanner {

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun scan(jpeg: ByteArray, hint: String?): AiScanResult {
        val settings = aiSettingsRepository.observe().first()
        val config = AiRuntimeConfig.resolve(settings, appConfig)
        if (!config.isConfigured) return AiScanResult.NotConfigured

        return try {
            val dataUrl = "data:image/jpeg;base64," + Base64.encode(jpeg)
            val cleanedHint = hint?.trim()?.takeIf { it.isNotBlank() }
            val userMessage =
                ChatMessage(
                    role = "user",
                    content =
                        buildList {
                            add(TextContent(AiScanPrompt.PROMPT))
                            cleanedHint?.let { add(TextContent(it)) }
                            add(ImageContent(ImageUrl(dataUrl)))
                        },
                )
            val request =
                ChatCompletionRequest(
                    model = config.model,
                    messages = listOfNotNull(userSystemMessage(settings.userSystemPrompt), userMessage),
                )

            val response =
                client.post(config.endpoint) {
                    bearerAuth(config.apiKey)
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
}
