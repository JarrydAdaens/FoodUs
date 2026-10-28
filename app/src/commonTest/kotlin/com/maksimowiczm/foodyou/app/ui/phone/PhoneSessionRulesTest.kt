package com.maksimowiczm.foodyou.app.ui.phone

import com.maksimowiczm.foodyou.app.ui.food.library.orderLibrary
import com.maksimowiczm.foodyou.settings.domain.entity.LibrarySortOrder
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.ai.domain.*
import com.maksimowiczm.foodyou.ai.infrastructure.scanUserMessage
import com.maksimowiczm.foodyou.ai.infrastructure.model.*
import kotlinx.datetime.LocalTime
import kotlin.test.*

class PhoneSessionRulesTest {
    private data class Row(val id: String, val name: String, val created: Long?, val calories: Double?)
    private val rows = listOf(Row("2", "Beta", 20, 0.0), Row("1", "alpha", 10, 200.0),
        Row("3", "Alpha", 10, 200.0), Row("4", "Unknown", null, null))
    private fun sorted(order: LibrarySortOrder, data: List<Row> = rows) =
        orderLibrary(data, order, Row::name, Row::id, Row::created, Row::calories).map { it.id }

    @Test fun allSixOrdersAreStableAndUnknownsStayLastInBothDirections() {
        assertEquals(listOf("1", "3", "2", "4"), sorted(LibrarySortOrder.Alphabetical))
        assertEquals(listOf("4", "2", "1", "3"), sorted(LibrarySortOrder.ReverseAlphabetical))
        assertEquals(listOf("1", "3", "2", "4"), sorted(LibrarySortOrder.OldestCreated))
        assertEquals(listOf("2", "1", "3", "4"), sorted(LibrarySortOrder.NewestCreated))
        assertEquals(listOf("2", "1", "3", "4"), sorted(LibrarySortOrder.LowestCalories))
        assertEquals(listOf("1", "3", "2", "4"), sorted(LibrarySortOrder.HighestCalories))
    }
    @Test fun caloriesRejectNonfiniteNegativeAndMissingValuesAndIncludeOffscreenItems() {
        val data = (1..180).map { Row(it.toString(), "Synthetic $it", null, it.toDouble()) } +
            listOf(Row("bad", "Invalid", null, Double.NaN), Row("infinite", "Infinite", null, Double.POSITIVE_INFINITY),
                Row("negative", "Negative", null, -1.0))
        assertEquals("180", sorted(LibrarySortOrder.HighestCalories, data).first())
        assertTrue(sorted(LibrarySortOrder.LowestCalories, data).takeLast(3).toSet() == setOf("bad", "infinite", "negative"))
    }
    @Test fun persistedSortNamesRoundTripAndUnknownValuesUseDefault() {
        LibrarySortOrder.entries.forEach { assertEquals(it, LibrarySortOrder.fromStored(it.name)) }
        assertEquals(LibrarySortOrder.Alphabetical, LibrarySortOrder.fromStored(null))
        assertEquals(LibrarySortOrder.Alphabetical, LibrarySortOrder.fromStored("future-option"))
    }
    private fun slot(id: Long, from: String, to: String, rank: Int = 0) = DiarySlot(id, "Synthetic $id", LocalTime.parse(from), LocalTime.parse(to), rank)
    @Test fun timedSlotsWinOverAllDayAndOverlapsUseRankThenIdentity() {
        val allDay = slot(1, "00:00", "00:00", -100)
        val first = slot(2, "08:00", "12:00", 1)
        val second = slot(3, "11:00", "14:00", 1)
        val slots = listOf(allDay, second, first)
        assertEquals(first, selectDiarySlot(slots, LocalTime(12, 0)))
        assertEquals(second, selectDiarySlot(slots, LocalTime(12, 1)))
        assertEquals(allDay, selectDiarySlot(slots, LocalTime(15, 0)))
        assertEquals(second.id, selectDiarySlot(listOf(first, second.copy(rank = 0)), LocalTime(11, 30))?.id)
    }
    @Test fun overnightNanosecondBoundaryGapsAndEmptySlotsAreExplicit() {
        val night = slot(1, "22:00", "03:00")
        assertTrue(night.containsTime(LocalTime(23, 59, 59, 999_999_999)))
        assertTrue(night.containsTime(LocalTime(0, 0)))
        assertTrue(night.containsTime(LocalTime(3, 0)))
        assertFalse(night.containsTime(LocalTime(3, 0, 0, 1)))
        assertNull(selectDiarySlot(listOf(night), LocalTime(12, 0)))
        assertNull(selectDiarySlot(emptyList(), LocalTime(12, 0)))
    }
    @Test fun textOnlyHasNoImageContentAndKeepsDescriptionSeparateFromMachinery() {
        val request = AiFoodScanRequest(description = "  two eggs and toast  ")
        assertTrue(request.isValid)
        val message = scanUserMessage(request)
        assertEquals("user", message.role)
        assertTrue(message.content.all { it is TextContent })
        assertEquals("two eggs and toast", (message.content.last() as TextContent).text)
        val prompt = (message.content.first() as TextContent).text
        assertFalse(prompt.contains("This photo"))
        assertFalse(prompt.contains("pictured portion"))
        assertTrue(prompt.contains("absolute totals"))
        assertTrue(prompt.contains("null for unknown"))
        assertTrue(prompt.contains("\"foods\""))
    }
    @Test fun imageOnlyAndCombinedInputKeepTheExistingImageContract() {
        for (description in listOf(null, "one bowl")) {
            val message = scanUserMessage(AiFoodScanRequest(byteArrayOf(1, 2, 3), description))
            assertEquals(1, message.content.filterIsInstance<ImageContent>().size)
            assertEquals("data:image/jpeg;base64,AQID", message.content.filterIsInstance<ImageContent>().single().imageUrl.url)
            assertTrue((message.content.first() as TextContent).text.contains("This photo"))
        }
    }
    @Test fun emptyInputCannotBecomeARequestButTextWithAnEmptyImageIsValid() {
        listOf(AiFoodScanRequest(), AiFoodScanRequest(description = "  "), AiFoodScanRequest(byteArrayOf())).forEach {
            assertFalse(it.isValid)
            assertFailsWith<IllegalArgumentException> { scanUserMessage(it) }
        }
        assertTrue(scanUserMessage(AiFoodScanRequest(byteArrayOf(), "soup")).content.none { it is ImageContent })
    }
}
