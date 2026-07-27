package com.maksimowiczm.foodyou.ai.infrastructure

import com.maksimowiczm.foodyou.ai.infrastructure.model.ChatMessage
import com.maksimowiczm.foodyou.ai.infrastructure.model.TextContent

/**
 * Builds the optional layer-2 system message from the user's persisted system prompt (Milestone 2,
 * Story 21). Returns null when the prompt is unset or blank, so no system message is sent. Shared by
 * both the scan and query-generation calls, which each prepend it ahead of their user message.
 */
internal fun userSystemMessage(userSystemPrompt: String?): ChatMessage? =
    userSystemPrompt?.trim()?.takeIf { it.isNotBlank() }?.let { prompt ->
        ChatMessage(role = "system", content = listOf(TextContent(prompt)))
    }
