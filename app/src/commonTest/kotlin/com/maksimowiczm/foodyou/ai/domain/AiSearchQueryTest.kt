package com.maksimowiczm.foodyou.ai.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiSearchQueryTest {

    @Test
    fun promptEmbedsMealAndNoteWithoutPersonalData() {
        val prompt =
            AiSearchQueryPrompt.build(
                mealName = "Dinner",
                note = "roast dinner",
            )

        // Meal name is lowercased into the sentence "eaten for dinner".
        assertTrue(prompt.contains("eaten for dinner"), "prompt should embed the meal context")
        assertTrue(prompt.contains("roast dinner"), "prompt should embed the note")
        // Layer 1 is pure machinery: no locale, nationality, or region baked in (Story 21).
        assertFalse(prompt.contains("Australian", ignoreCase = true), "no nationality in layer 1")
        assertFalse(prompt.contains("Melbourne", ignoreCase = true), "no city in layer 1")
        assertFalse(prompt.contains("Victoria", ignoreCase = true), "no region in layer 1")
    }

    @Test
    fun promptFallsBackWhenMealNameMissing() {
        val prompt = AiSearchQueryPrompt.build(mealName = null, note = "two sausage rolls")

        assertTrue(prompt.contains("eaten for a meal"), "blank meal should fall back to 'a meal'")
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
