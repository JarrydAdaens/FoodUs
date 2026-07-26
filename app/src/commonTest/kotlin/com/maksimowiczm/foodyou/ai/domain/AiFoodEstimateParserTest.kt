package com.maksimowiczm.foodyou.ai.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AiFoodEstimateParserTest {

    @Test
    fun parsesCleanJson() {
        val json =
            """{"name":"Meat pie","certainty":0.9,"calories":450,"protein":15,"fat":25,""" +
                """"fibre":2,"sugar":3}"""

        val estimate = AiFoodEstimateParser.parse(json)

        assertEquals("Meat pie", estimate?.name)
        assertEquals(0.9, estimate?.certainty)
        assertEquals(450.0, estimate?.calories)
        assertEquals(15.0, estimate?.protein)
        assertEquals(25.0, estimate?.fat)
        assertEquals(2.0, estimate?.fibre)
        assertEquals(3.0, estimate?.sugar)
    }

    @Test
    fun parsesJsonWrappedInMarkdownAndToleratesAliasesAndClamping() {
        val content =
            "Sure! Here is the result:\n```json\n" +
                """{"name":"Flat white","certainty":1.2,"kcal":120,"fiber":0}""" +
                "\n```\nEnjoy."

        val estimate = AiFoodEstimateParser.parse(content)

        assertEquals("Flat white", estimate?.name)
        assertEquals(1.0, estimate?.certainty) // clamped into 0..1
        assertEquals(120.0, estimate?.calories) // "kcal" alias
        assertEquals(0.0, estimate?.fibre) // "fiber" alias
        assertNull(estimate?.protein) // absent stays null
    }

    @Test
    fun returnsNullWhenNoUsableJsonOrName() {
        assertNull(AiFoodEstimateParser.parse("no json here"))
        assertNull(AiFoodEstimateParser.parse("""{"certainty":0.5,"calories":100}"""))
    }
}
