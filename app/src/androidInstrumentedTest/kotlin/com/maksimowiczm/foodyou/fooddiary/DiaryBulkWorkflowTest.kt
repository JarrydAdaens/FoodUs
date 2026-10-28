package com.maksimowiczm.foodyou.fooddiary

import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Rect
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.ui.developer.inspection.*
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.app.ui.home.meals.card.*
import com.maksimowiczm.foodyou.app.ui.home.shared.rememberHomeState
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.common.extension.now
import org.jetbrains.compose.resources.stringResource
import com.maksimowiczm.foodyou.common.domain.userpreferences.*
import com.maksimowiczm.foodyou.food.ProductTestClock
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.*
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.Assert.*
import org.junit.Test
import org.koin.compose.KoinIsolatedContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Drives the actual card menu and selector in both production layouts with private synthetic data. */
@SdkSuppress(minSdkVersion = 30)
class DiaryBulkWorkflowTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private var todayLabel = ""
    private var inspector: UiInspectorState? = null
    private lateinit var db: FoodYouDatabase
    private val sourceDate = LocalDate(2020, 2, 3)
    private val sourceName = "Synthetic source slot"
    private val targetName = "Synthetic target slot"
    private val driver = ScanTestDriver(instrumentation, { decor }, { instrumentation.targetContext.packageName }, { null })

    @Test fun verticalHistoricalMenuCopyMoveDeleteAndEmptyCopyFrom() = workflow(DiarySlotsCardsLayout.Vertical)
    @Test fun horizontalHistoricalMenuCopyMoveDeleteAndEmptyCopyFrom() = workflow(DiarySlotsCardsLayout.Horizontal)

    private fun workflow(layout: DiarySlotsCardsLayout) = fixture(layout) {
        val initial = entries(3901)
        assertSourceEnergy("300")
        menu(Res.string.diary_bulk_copy_to)
        inspect(Res.string.diary_bulk_confirm, "DiaryBulkDialog.ConfirmText.1", "DiaryBulkDialog.AlertDialog.1")
        assertFalse(driver.control(label(Res.string.diary_bulk_confirm)).isEnabled)
        driver.findText(label(Res.string.diary_bulk_same))
        // Calendar is a nested real surface and cancellation keeps the captured endpoint unchanged.
        click(Res.string.action_choose_other_date)
        inspect(Res.string.positive_ok, "ChipsDatePicker.Text.1", "ChipsDatePicker.DatePickerDialog.1")
        click(Res.string.action_cancel)
        // An actual Today chip changes day while retaining the original slot.
        driver.clickText(todayLabel)
        click(Res.string.diary_bulk_confirm)
        driver.await("copy to another day retains captured historical source") {
            runBlocking { db.manualDiaryEntryDao.entries(3901, LocalDate.now().toEpochDays()).size == 2 }
        }
        assertSourceEnergy("300")
        assertEquals(initial, entries(3901)); assertEquals(1, entries(3902).size)

        menu(Res.string.diary_bulk_copy_to)
        driver.clickText(targetName)
        click(Res.string.diary_bulk_confirm)
        driver.await("copy appends once") { entries(3902).size == 3 }
        assertSourceEnergy("300")
        assertEquals(initial, entries(3901))

        menu(Res.string.diary_bulk_move)
        driver.clickText(targetName)
        // Edit source after preview: actual production confirmation must reject it.
        runBlocking { val row = db.manualDiaryEntryDao.entries(3901, sourceDate.toEpochDays()).first(); db.manualDiaryEntryDao.update(row.copy(description = "Synthetic concurrent edit")) }
        click(Res.string.diary_bulk_confirm)
        driver.findText(label(Res.string.diary_bulk_stale))
        inspect(Res.string.diary_bulk_stale, "DiaryBulkDialog.Error.1", "DiaryBulkDialog.AlertDialog.1")
        assertEquals(3, entries(3902).size)
        click(Res.string.diary_bulk_refresh)
        click(Res.string.diary_bulk_confirm)
        driver.await("move empties captured historical slot") { entries(3901).isEmpty() && entries(3902).size == 5 }
        assertSourceEnergy(label(Res.string.em_dash))

        menu(Res.string.diary_bulk_copy_from)
        assertFalse(driver.control(label(Res.string.diary_bulk_confirm)).isEnabled)
        driver.clickText(targetName)
        click(Res.string.diary_bulk_confirm)
        driver.await("Copy From populates an empty slot") { entries(3901).size == 5 }
        assertSourceEnergy("650")

        menu(Res.string.diary_bulk_delete)
        driver.findText(label(Res.string.diary_bulk_delete_help))
        inspect(Res.string.diary_bulk_confirm, "DiaryBulkDialog.ConfirmText.1", "DiaryBulkDialog.AlertDialog.1")
        click(Res.string.action_cancel)
        assertEquals(5, entries(3901).size)
        menu(Res.string.diary_bulk_delete)
        click(Res.string.diary_bulk_confirm)
        try {
            driver.await("Delete All only empties selected day-slot") { entries(3901).isEmpty() }
        } catch (failure: Throwable) {
            throw AssertionError("Synthetic Delete All counts: source=" + entries(3901).size +
                ", target=" + entries(3902).size, failure)
        }
        assertEquals(5, entries(3902).size)
        assertEquals(2, runBlocking { db.manualDiaryEntryDao.entries(3901, LocalDate.now().toEpochDays()).size })
    }

    private fun assertSourceEnergy(expected: String) {
        driver.await("source card energy refreshes to expected total") {
            val source = driver.findText(sourceName)
            var ancestor: AccessibilityNodeInfo? = source
            repeat(12) {
                val current = ancestor ?: return@await false
                val labels = driver.descendants(current).flatMap { it.text?.toString()?.lines()?.asSequence() ?: emptySequence() }.toList()
                if (targetName in labels) return@await false
                val energyLabel = labels.indexOf(label(Res.string.unit_kcal))
                if (energyLabel >= 0) {
                    // Compose can merge card text into one accessibility node. In either form
                    // ValueColumn presents the kcal label immediately followed by its value.
                    return@await labels.getOrNull(energyLabel + 1) == expected
                }
                ancestor = current.parent
            }
            false
        }
    }

    private fun label(resource: StringResource) = runBlocking { getString(resource) }
    private fun click(resource: StringResource) {
        driver.await("enabled action") { driver.control(label(resource)).isEnabled }
        driver.clickText(label(resource))
    }
    private fun menu(resource: StringResource) {
        // Both vertical cards expose the same action; resolve it within the captured source card.
        var source = driver.findText(sourceName)
        var menu: AccessibilityNodeInfo? = null
        repeat(12) {
            if (menu == null) {
                val nodes = driver.descendants(source).toList()
                check(nodes.none { it.text?.toString()?.lines()?.contains(targetName) == true }) {
                    "Source menu must resolve before reaching the sibling card"
                }
                menu = nodes.firstOrNull { it.isVisibleToUser &&
                    (it.contentDescription?.toString() == label(Res.string.action_saved_meal_menu) ||
                        it.text?.toString()?.lines()?.contains(label(Res.string.action_saved_meal_menu)) == true) }
                if (menu == null) source.parent?.let { source = it }
            }
        }
        var control = requireNotNull(menu) { "Source card exposes its own Meal menu" }
        repeat(12) { if (!control.isClickable) control.parent?.let { control = it } }
        assertTrue(control.isEnabled)
        assertTrue(control.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        instrumentation.waitForIdleSync()
        if (resource == Res.string.diary_bulk_copy_to) {
            driver.findText(label(resource))
            val folder = java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "story40").apply { mkdirs() }
            java.io.FileOutputStream(java.io.File(folder, "slot-menu.png")).use {
                requireNotNull(instrumentation.uiAutomation.takeScreenshot()).compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            inspect(resource, "MealMenu.BulkText.1", "MealMenu.DropdownMenu.1")
        }
        click(resource)
    }
    private fun entries(slot: Long) = runBlocking { db.manualDiaryEntryDao.entries(slot, sourceDate.toEpochDays()) }

    private fun inspect(resource: StringResource, expectedName: String, expectedSurface: String) {
        if (resource == Res.string.diary_bulk_stale) {
            // The multiline error can be partially visible behind the fixed dialog action row.
            var ancestor: AccessibilityNodeInfo? = driver.findText(label(resource)).parent
            while (ancestor != null && !ancestor.isScrollable) ancestor = ancestor.parent
            ancestor?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            instrumentation.waitForIdleSync()
        }
        var bounds = Rect()
        var stableFrames = 0
        driver.await("Held modal label geometry settles") {
            val current = Rect().also(driver.findText(label(resource))::getBoundsInScreen)
            stableFrames = if (current == bounds) stableFrames + 1 else 0
            bounds = current
            stableFrames >= 2
        }
        var before: UiSelectionSnapshot? = null
        instrumentation.runOnMainSync { before = inspector?.snapshot }
        val down = SystemClock.uptimeMillis()
        val x = bounds.exactCenterX(); val y = bounds.exactCenterY()
        fun event(action: Int) { MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, x, y, 0).also {
            try { assertTrue(instrumentation.uiAutomation.injectInputEvent(it, true)) } finally { it.recycle() }
        } }
        event(MotionEvent.ACTION_DOWN)
        try {
            driver.await("Held element creates a fresh developer selection") {
                var changed = false
                instrumentation.runOnMainSync { changed = inspector?.snapshot != null && inspector?.snapshot !== before }
                changed
            }
        } finally { event(MotionEvent.ACTION_UP) }
        instrumentation.waitForIdleSync()
        var selection: UiSelectionSnapshot? = null
        instrumentation.runOnMainSync { selection = inspector?.snapshot }
        assertNotNull("Real held element produces a developer selection", selection)
        val selectedName = selection!!.selected.descriptor.name
        // The label and its owning button identify the same held Confirm action.
        val expectedNames = if (expectedName == "DiaryBulkDialog.ConfirmText.1")
            setOf(expectedName, "DiaryBulkDialog.Confirm.1") else setOf(expectedName)
        if (selectedName !in expectedNames) {
            java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "story38-bulk-inspection.txt")
                .writeText("Expected " + expectedNames + "; native bounds=" + bounds + "\n" + selection!!.copyText(true))
            instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                java.io.FileOutputStream(java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "story38-bulk-inspection.png")).use {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                }
                bitmap.recycle()
            }
        }
        assertTrue("Expected held action $expectedNames; actual $selectedName", selectedName in expectedNames)
        assertEquals(expectedSurface, selection!!.surface.name)
        assertTrue(selection!!.selected.descriptor.source.contains(if (expectedName.startsWith("MealMenu")) "DiarySlotCard.kt" else if (expectedName.startsWith("Chips")) "ChipsDatePicker.kt" else "DiaryBulkDialog.kt"))
    }

    private fun fixture(layout: DiarySlotsCardsLayout, block: () -> Unit) {
        val context = instrumentation.targetContext
        db = Room.inMemoryDatabaseBuilder(context, FoodYouDatabase::class.java).build()
        val foods = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
        val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
        val slots = RoomDiarySlotRepository(db.diarySlotDao)
        val meals = RoomMealRepository(db, db.mealDao)
        val preferences = object : UserPreferencesRepository<DiarySlotsPreferences> {
            private val state = MutableStateFlow(DiarySlotsPreferences(layout, false, false))
            override fun observe() = state
            override suspend fun update(transform: DiarySlotsPreferences.() -> DiarySlotsPreferences) { state.value = transform(state.value) }
        }
        runBlocking {
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(3901, sourceName, 0, 0, 23, 59))
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(3902, targetName, 0, 0, 23, 59))
            manual.insert("Synthetic first", 3901, sourceDate, NutritionFacts(energy = NutrientValue.Complete(100.0)), ProductTestClock.now())
            manual.insert("Synthetic second", 3901, sourceDate, NutritionFacts(energy = NutrientValue.Complete(200.0)), ProductTestClock.now())
            manual.insert("Synthetic existing", 3902, sourceDate, NutritionFacts(energy = NutrientValue.Complete(50.0)), ProductTestClock.now())
        }
        val isolated = koinApplication { modules(module {
            viewModel { DiarySlotsCardsViewModel(ObserveDiarySlotEntriesUseCase(slots, preferences, foods, manual, ProductTestClock), foods, manual,
                SaveMealUseCase(foods, manual, meals, db), ApplyMealUseCase(meals, manual, slots, db, ProductTestClock), meals, preferences,
                RoomDiaryBulkRepository(db, db.measurementDao, db.manualDiaryEntryDao, db.diarySlotDao)) }
        }) }
        val automation = instrumentation.uiAutomation
        val originalInfo = automation.serviceInfo
        val flags = originalInfo.flags
        try {
            automation.serviceInfo = automation.serviceInfo.apply { this.flags = this.flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS }
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.setContent { KoinIsolatedContext(isolated) { MaterialTheme {
                        UiInspectionContent(true, true, {}) {
                            todayLabel = stringResource(Res.string.headline_today, LocalDateFormatter.current.formatDateShort(LocalDate.now()))
                            val inspection = LocalUiInspector.current
                            SideEffect { inspector = inspection }
                            val home = rememberHomeState(sourceDate)
                            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                                DiarySlotsCards(home, { _, _ -> }, { _, _ -> }, { _, _ -> }, { _, _ -> }, { _, _, _ -> }, {}, PaddingValues(8.dp))
                            }
                        }
                    } } }
                }
                instrumentation.waitForIdleSync()
                driver.findText(sourceName)
                block()
            }
        } finally {
            isolated.close(); db.close(); decor = null; inspector = null
            automation.serviceInfo = originalInfo.apply { this.flags = flags }
        }
    }
}
