package com.maksimowiczm.foodyou.ai.domain

/** Outcome of an AI connection validation probe (Milestone 2, Story 22). */
sealed interface AiValidationResult {
    /** The endpoint accepted the credentials and returned a successful response. */
    data object Success : AiValidationResult

    /** The probe failed; [message] is the endpoint or transport error, safe to show to the user. */
    data class Failure(val message: String) : AiValidationResult
}

/**
 * Performs a single live one-token chat-completions call to verify that an entered
 * key/endpoint/model combination works (Milestone 2, Story 22).
 *
 * One code path, no provider-specific handling: success is any 2xx response; failure surfaces the
 * returned error string verbatim. The key travels only in the request's authorization header and is
 * never logged or included in the failure message.
 */
interface AiConnectionValidator {
    suspend fun validate(apiKey: String, endpoint: String, model: String): AiValidationResult
}
