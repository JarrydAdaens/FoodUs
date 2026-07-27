package com.maksimowiczm.foodyou.ai.domain

/** Outcome of an AI search-query generation request (Milestone 2, Story 9). */
sealed interface AiQueryResult {
    /** [query] is a food-search query describing the placeholder meal. */
    data class Success(val query: String) : AiQueryResult

    /** No API key is configured (user settings and developer fallback are blank); no request was made. */
    data object NotConfigured : AiQueryResult

    /** The request or its response failed; [message] is safe to show to the user. */
    data class Failure(val message: String) : AiQueryResult
}

/**
 * Turns a fast-text placeholder's free text into a better food-search query (Milestone 2, Story 9).
 *
 * Unlike [AiFoodScanner], this is text-only: no photo and no macro estimation. It reuses the same
 * endpoint client and configuration seam behind a sibling operation rather than duplicating the
 * transport.
 */
interface AiSearchQueryGenerator {
    /**
     * @param mealName the meal being logged into (e.g. "Lunch"), or null when unknown.
     * @param note the placeholder's name and optional description, as typed by the user.
     */
    suspend fun generateQuery(mealName: String?, note: String): AiQueryResult
}
