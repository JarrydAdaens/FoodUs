package com.maksimowiczm.foodyou.app.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

class DiarySlotRouteCompatibilityTest {
    @Test
    fun existingQuickAddRouteRestoresSlotAndKeepsLegacySerializedKey() {
        val legacy = """{"epochDay":20751,"mealId":2501,"prefillName":"Synthetic item"}"""
        val restored = Json.decodeFromString<FoodDiaryCreateQuickAdd>(legacy)
        assertEquals(2501L, restored.diarySlotId)
        assertEquals(20751L, restored.epochDay)
        assertEquals("Synthetic item", restored.prefillName)

        val encoded = Json.parseToJsonElement(Json.encodeToString(restored)).jsonObject
        assertEquals(2501L, encoded.getValue("mealId").jsonPrimitive.long)
        assertFalse(encoded.containsKey("diarySlotId"))
    }
}
