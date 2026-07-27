package com.maksimowiczm.foodyou.ai.domain

/** Outcome of an AI food-scan request. */
sealed interface AiScanResult {
    data class Success(val estimate: AiFoodEstimate) : AiScanResult

    /** No API key is configured (user settings and developer fallback are blank); no request was made. */
    data object NotConfigured : AiScanResult

    /** The request or its response failed; [message] is safe to show to the user. */
    data class Failure(val message: String) : AiScanResult
}

/**
 * Sends a downscaled food photo to the AI endpoint and returns a structured estimate
 * (Milestone 2, Story 6).
 *
 * This is the reusable endpoint boundary: Story 9's text-only AI flow will share the same
 * infrastructure client behind a sibling operation rather than duplicating the transport.
 */
interface AiFoodScanner {
    /** @param jpeg a downscaled JPEG-encoded photo of the food. */
    suspend fun scan(jpeg: ByteArray): AiScanResult
}
