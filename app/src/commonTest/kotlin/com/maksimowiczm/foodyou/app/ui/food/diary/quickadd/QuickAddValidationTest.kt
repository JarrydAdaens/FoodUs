package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import com.maksimowiczm.foodyou.app.ui.common.form.ParseResult
import com.maksimowiczm.foodyou.app.ui.common.form.nonNegativeDoubleValidator
import com.maksimowiczm.foodyou.app.ui.common.form.nullableDoubleParser
import com.maksimowiczm.foodyou.app.ui.common.form.positiveDoubleValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Covers the pure validation and parsing rules behind the expanded Quick Add fields (Milestone 2,
 * Story 18): decimal parsing plus the serving-count, weight, and fibre constraints from spec §5.5.
 * The Compose form wires these exact helpers, so locking them down here guards the field rules
 * without a Compose harness.
 */
class QuickAddValidationTest {
    private val invalid = "invalid"
    private val negative = "negative"
    private val notPositive = "not_positive"

    private val parse = nullableDoubleParser(onNotANumber = { invalid })
    private val fibreRule = nonNegativeDoubleValidator(onNegative = { negative })
    private val quantityRule = positiveDoubleValidator(onNotPositive = { notPositive })

    // --- Decimal parsing (fibre / serving count / weight all use the same parser) ---

    @Test
    fun parses_decimal_values() {
        assertEquals<ParseResult<Double?, String>>(ParseResult.Success(1.5), parse("1.5"))
        assertEquals<ParseResult<Double?, String>>(ParseResult.Success(0.0), parse("0"))
    }

    @Test
    fun blank_input_parses_to_null_optional() {
        assertEquals<ParseResult<Double?, String>>(ParseResult.Success(null), parse(""))
        assertEquals<ParseResult<Double?, String>>(ParseResult.Success(null), parse("   "))
    }

    @Test
    fun non_numeric_input_fails() {
        assertEquals<ParseResult<Double?, String>>(ParseResult.Failure(invalid), parse("abc"))
    }

    // --- Fibre: non-negative, optional ---

    @Test
    fun fibre_accepts_non_negative_and_null() {
        assertNull(fibreRule(null))
        assertNull(fibreRule(0.0))
        assertNull(fibreRule(3.2))
    }

    @Test
    fun fibre_rejects_negative() {
        assertEquals(negative, fibreRule(-0.1))
    }

    // --- Serving count & weight: strictly positive when supplied, optional otherwise ---

    @Test
    fun quantity_accepts_positive_and_null() {
        assertNull(quantityRule(null))
        assertNull(quantityRule(1.0))
        assertNull(quantityRule(0.25))
    }

    @Test
    fun quantity_rejects_zero() {
        assertEquals(notPositive, quantityRule(0.0))
    }

    @Test
    fun quantity_rejects_negative() {
        assertEquals(notPositive, quantityRule(-5.0))
    }
}
