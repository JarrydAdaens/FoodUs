package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimate
import kotlin.test.*

class AiScanReviewSessionTest {
    private val estimates = listOf(AiFoodEstimate("Bread", calories = 100.0),
        AiFoodEstimate("Bread", calories = 100.0), AiFoodEstimate("Chicken", protein = 35.0))

    @Test fun selectionPreservesResponseOrderAndIdenticalItemsRemainSeparate() {
        val initial = AiScanReviewSession.create("1", estimates)
        assertEquals(3, initial.items.map { it.id }.distinct().size)
        val selected = initial.select("1:1", false).start()
        assertEquals(listOf("1:0", "1:2"), selected.queue)
        assertEquals(estimates[0], selected.next?.estimate)
        assertNull(initial.copy(selectedIds = emptySet()).start().next)
    }

    @Test fun successAloneAdvancesAndRepeatedOrOutOfOrderAcknowledgementsDoNothing() {
        val initial = AiScanReviewSession.create("1", estimates).start()
        assertEquals(initial, initial.acknowledgeSaved("1:1"))
        val firstSaved = initial.acknowledgeSaved("1:0")
        assertEquals(1, firstSaved.savedIds.size)
        assertEquals("1:1", firstSaved.next?.id)
        assertEquals(firstSaved, firstSaved.acknowledgeSaved("1:0"))
        assertEquals(firstSaved, firstSaved.select("1:0", true))
        val complete = firstSaved.acknowledgeSaved("1:1").acknowledgeSaved("1:2")
        assertEquals(3, complete.savedIds.size)
        assertNull(complete.start().next)
    }

    @Test fun cancellationAndPartialFailureRetainUnsavedItemsWithoutResavingSiblings() {
        val partial = AiScanReviewSession.create("1", estimates).start().acknowledgeSaved("1:0")
        // Returning without an acknowledgement leaves the canceled/failed item unsaved.
        val resumed = partial.select("1:1", false).start()
        assertEquals(listOf("1:2"), resumed.queue)
        assertEquals(setOf("1:0"), resumed.savedIds)
        assertEquals(1, resumed.selectedCount)
        assertEquals(estimates[2], resumed.next?.estimate)
    }

    @Test fun previousResultGenerationCannotAcknowledgeANewScan() {
        val newScan = AiScanReviewSession.create("2", estimates).start()
        assertEquals(newScan, newScan.acknowledgeSaved("1:0"))
        assertEquals(newScan, newScan.select("1:0", true))
    }

    @Test fun finalSelectedSuccessCompletesQueueWhileUnselectedItemsRemainUnsaved() {
        val started = AiScanReviewSession.create("1", estimates).select("1:1", false).start()
        val partial = started.acknowledgeSaved("1:0")
        assertEquals("1:2", partial.next?.id)
        val completed = partial.acknowledgeSaved("1:2")
        assertNull(completed.next)
        assertEquals(setOf("1:0", "1:2"), completed.savedIds)
        assertEquals(0, completed.selectedCount)
        assertEquals(completed, completed.acknowledgeSaved("1:2"))
        assertEquals(completed, completed.acknowledgeSaved("stale:2"))
        assertFalse("1:1" in completed.savedIds, "Unselected sibling must not be counted as persisted")
    }
}
