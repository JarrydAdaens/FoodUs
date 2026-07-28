package com.maksimowiczm.foodyou.relay.domain

import io.ktor.http.URLProtocol
import io.ktor.http.Url

/** Outcome of validating a user-entered relay URL (Milestone 3, Story 15). */
sealed interface RelayUrlValidationResult {
    /** The field is empty: the relay is deliberately unconfigured, which is not an error. */
    data object Unset : RelayUrlValidationResult

    /** The input is a usable relay base URL; [url] is the normalized value to persist. */
    data class Valid(val url: String) : RelayUrlValidationResult

    /** The input does not use the `https` scheme. Plain HTTP is rejected outright. */
    data object NotHttps : RelayUrlValidationResult

    /** The input uses `https` but is not a parseable URL with a host. */
    data object Malformed : RelayUrlValidationResult
}

/**
 * Validates the relay base URL the user types in settings (Milestone 3, Story 15).
 *
 * HTTPS is constitutional for this fork — the app talks to the relay only over TLS — so anything
 * that is not an `https://` URL is rejected rather than saved with a warning. The scheme is checked
 * before parsing because URL parsers silently assume a scheme for scheme-less input, which would
 * turn `my-relay.example` into a plain-HTTP address behind the user's back.
 */
object RelayUrlValidator {

    private const val HTTPS_PREFIX = "https://"
    private val AUTHORITY_TERMINATORS = charArrayOf('/', '?', '#')

    fun validate(input: String): RelayUrlValidationResult {
        val trimmed = input.trim()

        if (trimmed.isEmpty()) return RelayUrlValidationResult.Unset
        if (trimmed.any(Char::isWhitespace)) return RelayUrlValidationResult.Malformed
        if (!trimmed.startsWith(HTTPS_PREFIX, ignoreCase = true)) {
            return RelayUrlValidationResult.NotHttps
        }

        // Read the authority straight off the input: URL builders substitute a default host for a
        // missing one, which would quietly turn "https://" into a valid-looking localhost address.
        val authority = trimmed.drop(HTTPS_PREFIX.length).takeWhile { it !in AUTHORITY_TERMINATORS }
        if (authority.isEmpty()) return RelayUrlValidationResult.Malformed

        val parsed =
            runCatching { Url(trimmed) }.getOrNull() ?: return RelayUrlValidationResult.Malformed
        if (parsed.protocol != URLProtocol.HTTPS) return RelayUrlValidationResult.Malformed

        // Store without a trailing slash so later stories can append contract paths uniformly.
        return RelayUrlValidationResult.Valid(trimmed.trimEnd('/'))
    }
}
