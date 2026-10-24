package com.maksimowiczm.foodyou.ai.domain

import kotlin.test.*

class AiFoodEstimateCollectionParserTest {
    @Test fun singleCoherentDishAndLegacyObjectRemainOneCompleteItem() {
        val item = """{"name":"Pie","calories":780,"protein":35,"fat":38,"fibre":6,"sugar":12,"sodium":0.0012,"servingCount":2,"weightGrams":250}"""
        val legacy = assertNotNull(AiFoodEstimateCollectionParser.parse(item))
        val collection = assertNotNull(AiFoodEstimateCollectionParser.parse("""{"foods":[$item]}"""))
        assertEquals(legacy, collection)
        assertEquals(1, collection.estimates.size)
        assertEquals(780.0, collection.estimates.single().calories)
        assertNull(collection.estimates.single().carbohydrates)
        assertTrue(AiScanPrompt.PROMPT.contains("do not decompose"))
        assertTrue(AiScanPrompt.PROMPT.contains("\"foods\" array"))
    }

    @Test fun independentFoodsRetainOrderAndAllValidatedSnapshotValues() {
        val result = assertNotNull(AiFoodEstimateCollectionParser.parse("""```json
            {"foods":[{"name":"Bread","energyKj":418.4,"sugar":0},
            {"name":"Pasta","weightKg":0.25,"servingCount":2.5,"vitaminC":0.012},
            {"name":"Chicken","protein":35,"fat":-1}]}
            ```"""))
        assertEquals(listOf("Bread", "Pasta", "Chicken"), result.estimates.map { it.name })
        assertEquals(100.0, assertNotNull(result.estimates[0].calories), 0.000001)
        assertEquals(0.0, result.estimates[0].sugar)
        assertEquals(250.0, result.estimates[1].weightGrams)
        assertEquals(2.5, result.estimates[1].servingCount)
        assertEquals(0.012, result.estimates[1].vitaminC)
        assertNull(result.estimates[2].fat)
        assertEquals(0, result.rejectedCount)
    }

    @Test fun invalidMembersAreCountedWithoutErasingValidSiblings() {
        val result = assertNotNull(AiFoodEstimateCollectionParser.parse("""{"foods":[
            {"name":"Bread","calories":100},null,42,{},
            {"name":" "},{"name":"Bad","protein":{}},{"name":"Chicken","protein":35}] }"""))
        assertEquals(listOf("Bread", "Chicken"), result.estimates.map { it.name })
        assertEquals(5, result.rejectedCount)
    }

    @Test fun unusableResponsesNeverCreateAnEmptySuccessfulQueue() {
        listOf("", "no JSON", "{}", """{"foods":[]}""", """{"foods":[null,{}]}""",
            """{"foods":{}}""", """{"foods":[{"name":"Bread"}""").forEach {
            assertNull(AiFoodEstimateCollectionParser.parse(it), it)
        }
    }
}
