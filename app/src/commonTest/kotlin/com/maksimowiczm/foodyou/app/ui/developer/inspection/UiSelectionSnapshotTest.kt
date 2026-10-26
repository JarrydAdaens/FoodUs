package com.maksimowiczm.foodyou.app.ui.developer.inspection

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiSelectionSnapshotTest {
    @Test
    fun shortCopyIsTheExactSearchableIdentifier() {
        assertEquals("diary.mealCard.addFoodButton", snapshot().copyText(false))
    }

    @Test
    fun fullCopyRetainsSourceOwnershipAndRepeatedInstanceAncestry() {
        val xml = snapshot().copyText(true)
        assertTrue(xml.startsWith("<ui-selection format=\"foodus-ui-v1\""))
        assertTrue(xml.contains("ancestry=\"declared-source\" snapshot=\"true\""))
        assertTrue(xml.contains("composable=\"androidx.compose.material3.IconButton\""))
        assertTrue(xml.contains("owner=\"com.example.MealCard\""))
        assertTrue(xml.contains("call-site-owner=\"com.example.DiaryScreen\""))
        assertTrue(xml.contains("call-site-source=\"app/src/commonMain/kotlin/DiaryScreen.kt\""))
        assertTrue(xml.indexOf("name=\"diary.mealList\"") < xml.indexOf("name=\"diary.mealCard\""))
        assertTrue(xml.contains("occurrence=\"2\""))
        assertTrue(xml.contains("enabled=\"false\""))
        assertTrue(xml.contains("left=\"10.0\" top=\"20.0\" right=\"30.0\" bottom=\"40.0\""))
        assertEquals(xml, snapshot().copyText(true))
    }

    @Test
    fun snapshotCopiesAncestryInsteadOfRetainingAMutableRegistryList() {
        val ancestors = mutableListOf(UiElementSnapshot(descriptor("diary.mealList"), 1))
        val snapshot = snapshot(ancestors)
        ancestors.clear()
        assertEquals(1, snapshot.ancestors.size)
    }

    @Test
    fun everyXmlAttributeIsEscaped() {
        val selected = UiElementSnapshot(
            descriptor("diary.special").copy(source = "app/A&B<C>D\"E'F.kt"),
            occurrence = 1,
        )
        val xml = UiSelectionSnapshot(descriptor("diary"), descriptor("main"), emptyList(), selected)
            .copyText(true)
        assertTrue(xml.contains("source=\"app/A&amp;B&lt;C&gt;D&quot;E&apos;F.kt\""))
        assertFalse(xml.contains("A&B"))
    }

    @Test
    fun malformedMetadataCannotCreateFakeLocatorsOrXml() {
        val descriptor = descriptor("diary")
        listOf("", "diary item", "diary<item", "123", "diary\nitem").forEach {
            assertFailsWith<IllegalArgumentException> { descriptor.copy(name = it) }
        }
        listOf("Button", "com.example.<anonymous>", "https://example.invalid").forEach {
            assertFailsWith<IllegalArgumentException> { descriptor.copy(composable = it) }
            assertFailsWith<IllegalArgumentException> { descriptor.copy(owner = it) }
        }
        listOf("/app/File.kt", "C:/File.kt", "../File.kt", "app/../File.kt", "app//File.kt",
            "app/./File.kt", "app\\File.kt", "app/File.txt", "app/File\n.kt").forEach {
            assertFailsWith<IllegalArgumentException> { descriptor.copy(source = it) }
        }
        assertFailsWith<IllegalArgumentException> { descriptor.copy(callSiteOwner = "com.example.Screen") }
        assertFailsWith<IllegalArgumentException> { descriptor.copy(callSiteSource = "app/Screen.kt") }
    }

    @Test
    fun invalidRuntimeGeometryAndOccurrencesAreRejected() {
        assertFailsWith<IllegalArgumentException> { UiElementSnapshot(descriptor("diary"), 0) }
        assertFailsWith<IllegalArgumentException> { UiInspectionBounds(Float.NaN, 0f, 1f, 1f) }
        assertFailsWith<IllegalArgumentException> { UiInspectionBounds(0f, 0f, Float.POSITIVE_INFINITY, 1f) }
        assertFailsWith<IllegalArgumentException> { UiInspectionBounds(2f, 0f, 1f, 1f) }
    }

    private fun descriptor(name: String) = UiSourceDescriptor(
        name = name,
        composable = "androidx.compose.material3.IconButton",
        owner = "com.example.MealCard",
        source = "app/src/commonMain/kotlin/MealCard.kt",
    )

    private fun snapshot(
        ancestors: List<UiElementSnapshot> = listOf(
            UiElementSnapshot(descriptor("diary.mealList"), 1),
            UiElementSnapshot(descriptor("diary.mealCard"), 2),
        )
    ) = UiSelectionSnapshot(
        screen = descriptor("diary"),
        surface = descriptor("main"),
        ancestors = ancestors,
        selected = UiElementSnapshot(
            descriptor = descriptor("diary.mealCard.addFoodButton").copy(
                callSiteOwner = "com.example.DiaryScreen",
                callSiteSource = "app/src/commonMain/kotlin/DiaryScreen.kt",
            ),
            occurrence = 1,
            enabled = false,
            bounds = UiInspectionBounds(10f, 20f, 30f, 40f),
        ),
    )
}
