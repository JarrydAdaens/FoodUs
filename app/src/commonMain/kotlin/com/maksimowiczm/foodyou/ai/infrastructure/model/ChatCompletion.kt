package com.maksimowiczm.foodyou.ai.infrastructure.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Minimal OpenRouter / OpenAI-compatible chat-completions DTOs for multimodal (text + image)
 * requests. Only the fields the AI food scanner needs are modelled.
 */
@Serializable
internal data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    // Serialized only when set (the module's Json uses explicitNulls = false), so existing scan and
    // query requests are unaffected; the validator sets it to 1 to keep its probe cheap (Story 22).
    @SerialName("max_tokens") val maxTokens: Int? = null,
)

@Serializable internal data class ChatMessage(val role: String, val content: List<ContentPart>)

@Serializable
internal sealed interface ContentPart

@Serializable
@SerialName("text")
internal data class TextContent(val text: String) : ContentPart

@Serializable
@SerialName("image_url")
internal data class ImageContent(@SerialName("image_url") val imageUrl: ImageUrl) : ContentPart

@Serializable internal data class ImageUrl(val url: String)

@Serializable
internal data class ChatCompletionResponse(val choices: List<Choice> = emptyList())

@Serializable internal data class Choice(val message: ResponseMessage? = null)

@Serializable internal data class ResponseMessage(val content: String? = null)
