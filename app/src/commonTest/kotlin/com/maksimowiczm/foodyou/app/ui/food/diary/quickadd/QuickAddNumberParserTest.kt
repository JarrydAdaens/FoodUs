package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import com.maksimowiczm.foodyou.app.ui.common.form.ParseResult
import kotlin.test.Test
import kotlin.test.assertEquals

class QuickAddNumberParserTest {
    private val parse = quickAddDoubleParser()

    @Test fun rejectsNonFiniteAndOverflowForAllQuickAddNumericFields() {
        listOf("NaN", "Infinity", "-Infinity", "1e309", "-1e309").forEach { input ->
            assertEquals<ParseResult<Double?, QuickAddFormFieldError>>(
                ParseResult.Failure(QuickAddFormFieldError.InvalidNumber), parse(input), input,
            )
        }
    }

    @Test fun preservesFiniteNumbersBlankAndExistingNegativeValidationBoundary() {
        mapOf("0" to 0.0, "1.25" to 1.25, "-2.5" to -2.5, "1e308" to 1e308,
            "" to null, "   " to null).forEach { (input, value) ->
            assertEquals<ParseResult<Double?, QuickAddFormFieldError>>(
                ParseResult.Success(value), parse(input), input,
            )
        }
    }
}
