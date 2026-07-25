package com.maksimowiczm.foodyou.ai.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiSearchQueryTest {

    @Test
    fun promptEmbedsLocaleMealAndNote() {
        val prompt =
            AiSearchQueryPrompt.build(
                mealName = "Dinner",
                note = "roast dinner at my uncle's house",
            )

        assertTrue(prompt.contains("Australian"), "prompt should state the locale")
        assertTrue(prompt.contains("Melbourne"), "prompt should state the city")
        // Meal name is lowercased into the sentence "had for dinner".
        assertTrue(prompt.contains("had for dinner"), "prompt should embed the meal context")
        assertTrue(prompt.contains("roast dinner at my uncle's house"), "prompt should embed note")
    }

    @Test
    fun promptFallsBackWhenMealNameMissing() {
        val prompt = AiSearchQueryPrompt.build(mealName = null, note = "two sausage rolls")

        assertTrue(prompt.contains("had for a meal"), "blank meal should fall back to 'a meal'")
        assertTrue(prompt.contains("two sausage rolls"))
    }

    @Test
    fun parserStripsLabelsQuotesFencesAndTrailingProse() {
        assertEquals("chicken parmigiana", AiSearchQueryParser.parse("\"chicken parmigiana\""))
        assertEquals(
            "beef lasagna",
            AiSearchQueryParser.parse("Search query: beef lasagna\nThis should match well."),
        )
        assertEquals(
            "flat white coffee",
            AiSearchQueryParser.parse("```\nflat white coffee\n```"),
        )
        assertNull(AiSearchQueryParser.parse("   \n  \n"))
    }
}
