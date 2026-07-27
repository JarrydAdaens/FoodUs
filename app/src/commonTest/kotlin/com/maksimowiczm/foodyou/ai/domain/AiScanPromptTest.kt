package com.maksimowiczm.foodyou.ai.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AiScanPromptTest {

    @Test
    fun promptKeepsTheJsonSchemaMachinery() {
        val prompt = AiScanPrompt.PROMPT

        // The parser depends on exactly these seven keys and the JSON-only instruction.
        listOf("name", "certainty", "calories", "protein", "fat", "fibre", "sugar").forEach { key ->
            assertTrue(prompt.contains("\"$key\""), "prompt should demand the \"$key\" JSON key")
        }
        assertTrue(prompt.contains("ONLY a JSON object"), "prompt should demand JSON-only output")
    }

    @Test
    fun promptCarriesNoPersonalData() {
        val prompt = AiScanPrompt.PROMPT

        // Layer 1 ships with zero personal data — locale belongs to the user's system prompt.
        assertFalse(prompt.contains("Australian", ignoreCase = true), "no nationality in layer 1")
        assertFalse(prompt.contains("Victoria", ignoreCase = true), "no region in layer 1")
        assertFalse(prompt.contains("Melbourne", ignoreCase = true), "no city in layer 1")
    }
}
