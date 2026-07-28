package com.maksimowiczm.foodyou.relay.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayUrlValidatorTest {

    @Test
    fun httpsUrlIsAccepted() {
        val result = RelayUrlValidator.validate("https://relay.example.test")

        assertEquals(RelayUrlValidationResult.Valid("https://relay.example.test"), result)
    }

    @Test
    fun surroundingWhitespaceAndTrailingSlashAreNormalizedAway() {
        val result = RelayUrlValidator.validate("  https://relay.example.test/  ")

        assertEquals(RelayUrlValidationResult.Valid("https://relay.example.test"), result)
    }

    @Test
    fun plainHttpIsRejected() {
        val result = RelayUrlValidator.validate("http://relay.example.test")

        assertEquals(RelayUrlValidationResult.NotHttps, result)
    }

    @Test
    fun schemelessInputIsRejectedRatherThanAssumedSecure() {
        val result = RelayUrlValidator.validate("relay.example.test")

        assertEquals(RelayUrlValidationResult.NotHttps, result)
    }

    @Test
    fun httpsWithoutHostIsMalformed() {
        val result = RelayUrlValidator.validate("https://")

        assertEquals(RelayUrlValidationResult.Malformed, result)
    }

    @Test
    fun blankInputMeansUnsetRatherThanInvalid() {
        assertEquals(RelayUrlValidationResult.Unset, RelayUrlValidator.validate(""))
        assertEquals(RelayUrlValidationResult.Unset, RelayUrlValidator.validate("   "))
    }
}
