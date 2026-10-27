package com.maksimowiczm.foodyou.app.navigation

import androidx.lifecycle.SavedStateHandle
import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimateParser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class ScanQuickAddHandoffTest {
    @Test fun actualRouteRoundTripAndAcknowledgementKeepFullSnapshotAndGenerationId() {
        val estimate = assertNotNull(AiFoodEstimateParser.parse("""{"name":"Pie","calories":780,"protein":35,"fat":38,"fibre":6,"sugar":12,"vitaminC":0.012,"servingCount":2.5,"weightGrams":250}"""))
        val route = ScanQuickAddHandoff.route(123, 7, estimate, "2:1")
        val restored = Json.decodeFromString<FoodDiaryCreateQuickAdd>(Json.encodeToString(route))
        assertEquals(estimate, AiFoodEstimateParser.parse(assertNotNull(restored.scanEstimateJson)))
        assertEquals("2:1", restored.scanItemId)
        val handle = SavedStateHandle()
        ScanQuickAddHandoff.acknowledge(restored, handle)
        assertEquals("2:1", handle.get<String>(ScanQuickAddHandoff.SAVED_ITEM_KEY))
    }

    @Test fun cancelledQueueRestartCapturesNewSlotWhileContinuationKeepsOriginal() {
        val state = SavedStateHandle()
        ScanQuickAddHandoff.beginQueue(state, 7)
        assertEquals(7L, ScanQuickAddHandoff.targetSlot(state, 9))
        // Cancel, change the header, then explicitly start another review queue.
        ScanQuickAddHandoff.beginQueue(state, 9)
        assertEquals(9L, ScanQuickAddHandoff.targetSlot(state, 7))
        ScanQuickAddHandoff.finishQueue(state)
        assertEquals(11L, ScanQuickAddHandoff.targetSlot(state, 11))
    }

    @Test fun manualQuickAddNeverAcknowledgesAScanItem() {
        val handle = SavedStateHandle()
        ScanQuickAddHandoff.acknowledge(FoodDiaryCreateQuickAdd(1, 2), handle)
        assertNull(handle.get<String>(ScanQuickAddHandoff.SAVED_ITEM_KEY))
    }
}
