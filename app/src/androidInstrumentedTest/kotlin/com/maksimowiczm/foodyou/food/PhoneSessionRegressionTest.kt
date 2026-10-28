package com.maksimowiczm.foodyou.food

import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.view.MotionEvent
import android.os.SystemClock
import com.maksimowiczm.foodyou.app.ui.developer.inspection.*
import com.maksimowiczm.foodyou.app.ui.food.diary.search.FoodEntryDateTimePicker
import android.view.View
import android.view.WindowManager
import androidx.test.core.app.launchActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.SdkSuppress
import androidx.room.Room
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.maksimowiczm.foodyou.app.navigation.FoodYouAppNavHost
import com.maksimowiczm.foodyou.app.navigation.ScanQuickAddHandoff
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.ui.shell.FoodUsAppShell
import com.maksimowiczm.foodyou.app.ui.theme.FoodYouTheme
import com.maksimowiczm.foodyou.app.ui.food.library.*
import com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.*
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.ai.domain.*
import com.maksimowiczm.foodyou.common.config.AppConfig
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.settings.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.repository.*
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.food.search.domain.NutritionalItemSearchUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.*
import foodyou.app.generated.resources.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Instant
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

/** Real Room and production navigation; all writes are restricted to a disposable emulator. */
@SdkSuppress(minSdkVersion = 30)
class PhoneSessionRegressionTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private val driver = ScanTestDriver(instrumentation, { decor }, { instrumentation.targetContext.packageName }, { null })
    private fun label(resource: StringResource) = runBlocking { getString(resource) }
    private fun click(resource: StringResource) = driver.clickText(label(resource))
    private fun emulatorOnly() = assumeTrue(Build.FINGERPRINT.contains("generic") || Build.MODEL.contains("sdk_gphone"))
    private fun root(): android.view.accessibility.AccessibilityNodeInfo? {
        val display = driver.onMain { decor?.display?.displayId ?: 0 }
        return instrumentation.uiAutomation.windowsOnAllDisplays.get(display).orEmpty().asSequence()
            .mapNotNull { it.root }.firstOrNull { it.packageName?.toString() == instrumentation.targetContext.packageName }
    }
    private fun visible(text: String): Boolean = root()?.let { driver.descendants(it).any { n -> n.isVisibleToUser &&
        (n.text?.toString()?.lines()?.contains(text) == true || n.contentDescription?.toString() == text) } } == true
    private fun capture(name: String) {
        val directory = java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "phone-session").apply { mkdirs() }
        java.io.FileOutputStream(java.io.File(directory, "$name.png")).use {
            assertTrue(requireNotNull(instrumentation.uiAutomation.takeScreenshot()).compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }
    private fun withWindows(block: () -> Unit) {
        val info = instrumentation.uiAutomation.serviceInfo
        val old = info.flags
        instrumentation.uiAutomation.serviceInfo = info.apply { flags = old or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS }
        try { block() } finally { decor = null; instrumentation.uiAutomation.serviceInfo = info.apply { flags = old } }
    }


    @Test fun dateAndTimePickerActionsExportActualSourceScopes() {
        emulatorOnly()
        var inspector: UiInspectorState? = null
        fun inspectAction(part: String, surface: String) {
            var bounds = Rect()
            var stable = 0
            driver.await("Picker action geometry stable") {
                val current = Rect().also(driver.findText(label(Res.string.action_confirm))::getBoundsInScreen)
                stable = if (current == bounds) stable + 1 else 0
                bounds = current
                stable >= 2
            }
            var before: UiSelectionSnapshot? = null
            instrumentation.runOnMainSync { before = inspector?.snapshot }
            val down = SystemClock.uptimeMillis()
            fun event(action: Int) {
                MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, bounds.exactCenterX(), bounds.exactCenterY(), 0).also {
                    try { assertTrue(instrumentation.uiAutomation.injectInputEvent(it, true)) } finally { it.recycle() }
                }
            }
            event(MotionEvent.ACTION_DOWN)
            try {
                driver.await("Picker exports a fresh held-element selection") {
                    driver.onMain { inspector?.snapshot != null && inspector?.snapshot !== before }
                }
            } finally { event(MotionEvent.ACTION_UP) }
            var heldSelection: UiSelectionSnapshot? = null
            instrumentation.runOnMainSync { heldSelection = inspector?.snapshot }
            val snapshot = requireNotNull(heldSelection)
            assertTrue(snapshot.selected.descriptor.name in setOf("FoodEntryDateTimePicker.Text.$part", "FoodEntryDateTimePicker.TextButton.$part"))
            assertEquals("FoodEntryDateTimePicker.$surface", snapshot.surface.name)
            assertTrue(snapshot.selected.descriptor.source.endsWith("FoodEntryDateTimePicker.kt"))
        }
        withWindows {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.setContent { MaterialTheme {
                        UiInspectionContent(true, true, {}) {
                            val currentInspector = LocalUiInspector.current
                            SideEffect { inspector = currentInspector }
                            FoodEntryDateTimePicker(LocalDate(2026, 10, 28), LocalTime(13, 25), {}, { _, _ -> })
                        }
                    } }
                }
                inspectAction("dateConfirm", "DateDialog")
                click(Res.string.action_confirm)
                inspectAction("timeConfirm", "TimeDialog")
            }
        }
    }

    @Test fun dateTimePickerCancellationAndConfirmationPreserveInputAndSaveToChosenDay() {
        emulatorOnly(); prepareSyntheticLibraryHost()
        val koin = GlobalContext.get()
        val now = koin.get<DateProvider>().now()
        val chosenDay = if (now.day == 15) 14 else 15
        val chosenDate = LocalDate(now.year, now.month, chosenDay)
        val slots = runBlocking { koin.get<DiarySlotRepository>().observeDiarySlots().first() }
        assertTrue("Synthetic emulator has meal slots", slots.isNotEmpty())
        val initialSlot = selectDiarySlot(slots, now.time) ?: slots.first()
        val targetTime = if (LocalTime(now.hour, now.minute) == LocalTime(13, 25)) LocalTime(13, 26) else LocalTime(13, 25)
        val expectedSlot = selectDiarySlot(slots, targetTime) ?: slots.first()
        val diary = koin.get<NutritionalItemDiaryEntryRepository>()
        val newName = "Synthetic S40 " + java.util.UUID.randomUUID().toString().take(6)
        lateinit var navigation: NavHostController
        val request = mutableStateOf<String?>(null)
        fun activeDay(): Long = driver.onMain {
            navigation.currentBackStackEntry!!.savedStateHandle.get<Long>(ScanQuickAddHandoff.ENTRY_DATE_KEY)
                ?: now.date.toEpochDays().toLong()
        }
        fun openAndPickDay() {
            click(Res.string.food_entry_date_time)
            var calendarRoot = driver.findText(label(Res.string.action_confirm))
            while (calendarRoot.parent != null) calendarRoot = requireNotNull(calendarRoot.parent)
            val dayPattern = Regex("(^|[^0-9])$chosenDay([^0-9]|$)")
            val calendarNodes = driver.descendants(calendarRoot).toList()
            var dayNode = calendarNodes.firstOrNull { node ->
                node.isClickable && listOfNotNull(node.text, node.contentDescription).any { dayPattern.containsMatchIn(it.toString()) }
            } ?: error("Synthetic calendar day missing: " + calendarNodes.mapNotNull {
                it.contentDescription?.toString() ?: it.text?.toString()
            }.joinToString(" | "))
            while (!dayNode.isClickable) dayNode = requireNotNull(dayNode.parent)
            assertTrue(dayNode.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK))
        }
        withWindows {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.setContent { FoodYouTheme {
                        navigation = rememberNavController()
                        FoodYouAppNavHost({}, navController = navigation, quickAddRequest = request.value,
                            onQuickAddHandled = { request.value = null })
                    } }
                }
                driver.findText(label(Res.string.action_go_to_settings))
                assertFalse("Log has no redundant app-name heading", visible(label(Res.string.app_name)))
                capture("story40-log")
                driver.onMain { request.value = now.toString() }
                if (selectDiarySlot(slots, now.time) == null) driver.clickText(initialSlot.name)
                click(Res.string.food_entry_ai)
                driver.setField(Res.string.label_ai_scan_hint, "Synthetic retained description")
                openAndPickDay()
                click(Res.string.action_cancel)
                assertEquals(now.date.toEpochDays().toLong(), activeDay())
                driver.assertField(Res.string.label_ai_scan_hint, "Synthetic retained description")
                openAndPickDay()
                click(Res.string.action_confirm)
                click(Res.string.action_cancel)
                assertEquals(now.date.toEpochDays().toLong(), activeDay())
                if (slots.size > 1) {
                    val clockSlot = initialSlot
                    val manualSlot = slots.firstOrNull { it.id != clockSlot.id && it.id != expectedSlot.id }
                        ?: slots.first { it.id != clockSlot.id }
                    driver.clickText(clockSlot.name)
                    driver.clickText(manualSlot.name)
                    openAndPickDay()
                    click(Res.string.action_confirm)
                    driver.findText(label(Res.string.food_entry_date_time))
                    click(Res.string.action_confirm)
                    driver.await("Date-only change preserves manual meal selection") {
                        activeDay() == chosenDate.toEpochDays().toLong() && visible(manualSlot.name)
                    }
                    driver.assertField(Res.string.label_ai_scan_hint, "Synthetic retained description")
                }
                openAndPickDay()
                click(Res.string.action_confirm)
                fun timeNodes(): List<android.view.accessibility.AccessibilityNodeInfo> {
                    instrumentation.uiAutomation.clearCache()
                    return instrumentation.uiAutomation.windowsOnAllDisplays.get(driver.onMain { decor!!.display.displayId })
                        .orEmpty().mapNotNull { it.root }.filter { it.packageName?.toString() == instrumentation.targetContext.packageName }
                        .flatMap { dialog -> driver.descendants(dialog).toList() }
                }
                fun setTimePart(part: String, value: String) {
                    timeNodes().firstOrNull { it.contentDescription?.toString() == "Select $part" }?.let { selector ->
                        var control = selector
                        while (!control.isClickable) control = requireNotNull(control.parent)
                        assertTrue(control.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK))
                    }
                    fun activeInput() = timeNodes().firstOrNull { input -> input.isEditable &&
                        driver.descendants(input).any { it.contentDescription?.toString()?.contains(part.removeSuffix("s"), ignoreCase = true) == true } }
                    driver.await("Time $part editor active") { activeInput() != null }
                    assertTrue(requireNotNull(activeInput()).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT,
                        android.os.Bundle().apply { putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value) }))
                }
                // Material exposes only the selected time part as an editable field.
                driver.findText(label(Res.string.food_entry_date_time))
                setTimePart("hours", targetTime.hour.toString().padStart(2, '0'))
                setTimePart("minutes", targetTime.minute.toString().padStart(2, '0'))
                click(Res.string.action_confirm)
                driver.await("Changed date committed to retained entry session") { activeDay() == chosenDate.toEpochDays().toLong() }
                if (selectDiarySlot(slots, targetTime) == null) driver.clickText(expectedSlot.name)
                driver.assertField(Res.string.label_ai_scan_hint, "Synthetic retained description")
                driver.findText(expectedSlot.name)
                capture("story40-date-time")
                click(Res.string.food_entry_search)
                click(Res.string.action_create)
                click(Res.string.headline_product)
                driver.findText(label(Res.string.product_name))
                assertEquals(chosenDate.toEpochDays().toLong(), driver.onMain { navigation.currentBackStackEntry!!.arguments!!.getLong("date") })
                driver.setField(Res.string.product_name, newName)
                driver.setField(Res.string.nutriment_proteins, "8")
                driver.setField(Res.string.nutriment_carbohydrates, "15")
                driver.setField(Res.string.nutriment_fats, "2")
                click(Res.string.action_save)
                driver.findText(label(Res.string.portion_amount_to_log))
                click(Res.string.action_save)
                driver.await("Saved Product uses chosen day and meal slot") {
                    runBlocking { diary.observeAll(expectedSlot.id, chosenDate).first().any { it.name == newName } }
                }
                assertFalse(runBlocking { diary.observeAll(expectedSlot.id, now.date).first().any { it.name == newName } })
            }
        }
    }

    @Test fun roomHistorySortAndTransitiveRecipeCalorieRefresh() = runBlocking {
        emulatorOnly()
        val db = Room.inMemoryDatabaseBuilder(instrumentation.targetContext, FoodYouDatabase::class.java).build()
        var model: LibraryViewModel? = null
        try {
            val products = RoomProductRepository(db.productDao)
            val recipes = RoomRecipeRepository(db.recipeDao, products)
            val history = RoomNutritionalItemHistoryRepository(db.foodEventDao)
            val sourceId = products.insertProduct("Synthetic source", null, null, "Synthetic note", false, null, null,
                ProductSource(ProductSource.Type.User), NutritionFacts(energy = NutrientValue.Complete(100.0)))
            history.insert(sourceId, NutritionalItemHistory.Created(Instant.fromEpochSeconds(100)))
            history.insert(sourceId, NutritionalItemHistory.Edited(Instant.fromEpochSeconds(999)))
            history.insert(sourceId, NutritionalItemHistory.Imported(Instant.fromEpochSeconds(200)))
            val name = products.observeLibraryNames().first().single()
            assertEquals(100L, name.createdEpochSeconds)
            assertEquals("Synthetic note", name.note)
            val source = products.observeProduct(sourceId).first()!!
            val first = recipes.insertRecipe("Synthetic derived", 1, null, false, listOf(RecipeIngredient(source, Measurement.Gram(100.0))))
            val nestedSource = recipes.observeRecipe(first).first()!!
            val nested = recipes.insertRecipe("Synthetic nested", 1, null, false, listOf(RecipeIngredient(nestedSource, Measurement.Serving(1.0))))
            val other = recipes.insertRecipe(Recipe(NutritionalItemId.Recipe(0), "Synthetic reference", null, emptyList(), null, false,
                ReportedNutrition(NutritionFacts(energy = NutrientValue.Complete(150.0)), ReportedBasis.REFERENCE_SERVING, PortionDescription(1.0, "bowl"))))
            val vm = withContext(Dispatchers.Main) { LibraryViewModel(products, recipes, ObserveNutritionalItemUseCase(products, recipes)) }.also { model = it }
            val ordering = recipes.observeLibraryNames().flatMapLatest { vm.orderedNames(it, LibrarySortOrder.HighestCalories) }
                .filterIsInstance<LibraryRead.Ready<List<NutritionalItemName>>>()
            assertEquals(other, ordering.first().value.first().id)
            val changed = async { withTimeout(15_000) { ordering.first { it.value.first().id != other } } }
            yield()
            products.updateProduct(source.copy(nutritionFacts = NutritionFacts(energy = NutrientValue.Complete(250.0))))
            val ids = changed.await().value.map { it.id }
            assertEquals(setOf(first, nested), ids.take(2).toSet())
            assertEquals(other, ids.last())
            // Editing row metadata must not lose the retained list while calorie keys refresh.
            val cached = vm.cachedOrder(LibraryTab.Recipes, "", LibrarySortOrder.HighestCalories)
            assertTrue(cached is LibraryRead.Ready)
            recipes.updateRecipe(recipes.observeRecipe(first).first()!!.copy(note = "Synthetic changed note"))
            assertTrue(vm.cachedOrder(LibraryTab.Recipes, "", LibrarySortOrder.HighestCalories) is LibraryRead.Ready)
            assertNull(recipes.observeLibraryNames().first().first { it.id == first }.createdEpochSeconds)
        } finally { withContext(Dispatchers.Main) { model?.viewModelScope?.cancel() }; db.close() }
    }

    @Test fun shellQuickEntryTabsSelectedSlotSettingsAndLibraryPersistence() {
        emulatorOnly(); prepareSyntheticLibraryHost()
        val koin = GlobalContext.get()
        val settings: UserPreferencesRepository<Settings> = koin.get(named(Settings::class.qualifiedName!!))
        runBlocking { settings.update { copy(librarySortOrder = LibrarySortOrder.NewestCreated) } }
        withWindows {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.setContent { FoodYouTheme { FoodUsAppShell({}) } }
                }
                driver.findText(label(Res.string.food_entry_add))
                click(Res.string.tab_notifications)
                click(Res.string.food_entry_add)
                driver.findText(label(Res.string.food_entry_search))
                driver.findText(label(Res.string.food_entry_ai))
                val slots = runBlocking { koin.get<DiarySlotRepository>().observeDiarySlots().first() }
                val selected = selectDiarySlot(slots, koin.get<DateProvider>().now().time)
                if (selected != null) {
                    driver.findText(selected.name)
                    if (slots.size > 1) {
                        driver.clickText(selected.name)
                        val target = slots.first { it.id != selected.id }
                        driver.clickText(target.name)
                        driver.findText(target.name)
                        click(Res.string.food_entry_ai)
                        driver.findText(label(Res.string.label_ai_scan_hint))
                        driver.setField(Res.string.label_ai_scan_hint, "Synthetic text-only soup")
                        click(Res.string.food_entry_search)
                        driver.findText(target.name)
                        click(Res.string.food_entry_ai)
                        driver.assertField(Res.string.label_ai_scan_hint, "Synthetic text-only soup")
                        driver.findText(target.name)
                        // Re-enter from another tab while the changed-slot Search session is retained.
                        click(Res.string.tab_notifications)
                        click(Res.string.food_entry_add)
                        val clockSlot = selectDiarySlot(slots, koin.get<DateProvider>().now().time)
                        if (clockSlot != null) driver.findText(clockSlot.name)
                        click(Res.string.action_go_back)
                        driver.findText(target.name)
                        driver.assertField(Res.string.label_ai_scan_hint, "Synthetic text-only soup")
                    }
                }
                capture("food-entry-tabs")
                click(Res.string.action_go_back)
                click(Res.string.tab_groups)
                click(Res.string.food_entry_add)
                driver.findText(label(Res.string.food_entry_search))
                click(Res.string.action_create)
                click(Res.string.food_entry_meals)
                driver.findText(label(Res.string.library_create_meal))
                click(Res.string.action_go_back)
                driver.findText(label(Res.string.food_entry_search))
                click(Res.string.action_go_back)
                click(Res.string.food_entry_add)
                driver.findText(label(Res.string.food_entry_search))
                click(Res.string.action_go_back)
                click(Res.string.action_go_to_settings)
                driver.await("Settings hides all shell actions") { !visible(label(Res.string.food_entry_add)) && !visible(label(Res.string.tab_notifications)) }
                click(Res.string.library_title)
                driver.findText(label(Res.string.library_sort_label).replace("%1\$s", label(Res.string.library_sort_newest)))
                val bounds = listOf(Res.string.library_recipes, Res.string.library_meals, Res.string.library_products).map {
                    var node = driver.findText(label(it))
                    while (!node.isClickable && !node.isSelected) node = requireNotNull(node.parent)
                    Rect().also(node::getBoundsInScreen)
                }
                assertTrue("All three tabs share equal widths", bounds.maxOf { it.width() } - bounds.minOf { it.width() } <= 2)
                assertFalse(visible(label(Res.string.food_entry_add)))
                driver.clickText(label(Res.string.library_sort_label).replace("%1\$s", label(Res.string.library_sort_newest)))
                click(Res.string.library_sort_za)
                driver.await("Library sort persisted") { runBlocking { settings.observe().first().librarySortOrder == LibrarySortOrder.ReverseAlphabetical } }
                click(Res.string.library_products)
                capture("library-layout")
                click(Res.string.library_create_product)
                driver.findText(label(Res.string.unit_choice_grams))
                driver.findText(label(Res.string.unit_choice_millilitres))
                click(Res.string.unit_choice_millilitres)
                var radio = driver.findText(label(Res.string.unit_choice_millilitres))
                while (!radio.isCheckable && !radio.isSelected) radio = requireNotNull(radio.parent)
                assertTrue("Millilitres radio is explicitly selected", radio.isChecked || radio.isSelected)
                capture("explicit-units")
                click(Res.string.action_go_back)
                driver.await("Return or discard confirmation") { visible(label(Res.string.action_discard)) || visible(label(Res.string.library_title)) }
                if (visible(label(Res.string.action_discard))) click(Res.string.action_discard)
                driver.findText(label(Res.string.library_title))
                click(Res.string.action_go_back)
                click(Res.string.action_go_back)
                driver.findText(label(Res.string.food_entry_add))
            }
        }
    }

    @Test fun diaryRowsAndTotalsShareNumericAnchorsAtDifferentTextSizes() {
        emulatorOnly(); prepareSyntheticLibraryHost()
        withWindows {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.setContent { FoodYouTheme {
                        androidx.compose.foundation.layout.Column {
                            com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem(
                                name = { androidx.compose.material3.Text("Synthetic alignment A") }, isRecipe = false, alignNutritionColumns = true,
                                calories = { androidx.compose.material3.Text("111 kcal") }, proteins = { androidx.compose.material3.Text("7 g") },
                                carbohydrates = { androidx.compose.material3.Text("22 g") }, fats = { androidx.compose.material3.Text("333 g") }, measurement = {})
                            com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem(
                                name = { androidx.compose.material3.Text("Synthetic alignment B with a longer title") }, isRecipe = true, alignNutritionColumns = true,
                                calories = { androidx.compose.material3.Text("222 kcal") }, proteins = { androidx.compose.material3.Text("70 g") },
                                carbohydrates = { androidx.compose.material3.Text("2 g") }, fats = { androidx.compose.material3.Text("33 g") }, measurement = {})
                            com.maksimowiczm.foodyou.app.ui.common.component.NutritionColumns(
                                modifier = androidx.compose.ui.Modifier.padding(horizontal = 16.dp),
                                calories = { androidx.compose.material3.Text("333 kcal") }, proteins = { androidx.compose.material3.Text("77 g") },
                                carbohydrates = { androidx.compose.material3.Text("24 g") }, fats = { androidx.compose.material3.Text("366 g") })
                        }
                    } }
                }
                fun right(text: String) = Rect().also(driver.findText(text)::getBoundsInScreen).right
                // Food rows have 16dp internal padding; the totals seam must receive the same inset.
                for ((a, b, total) in listOf(Triple("111 kcal", "222 kcal", "333 kcal"), Triple("7 g", "70 g", "77 g"),
                    Triple("22 g", "2 g", "24 g"), Triple("333 g", "33 g", "366 g"))) {
                    assertTrue("Entry numeric anchors agree", kotlin.math.abs(right(a) - right(b)) <= 2)
                    assertTrue("Totals align with entries", kotlin.math.abs(right(a) - right(total)) <= 2)
                }
                capture("numeric-columns")
            }
        }
    }

    @Test fun loadMealIsAvailableDirectlyOnTheSlotAndAppendsOnce() = runBlocking {
        emulatorOnly(); prepareSyntheticLibraryHost()
        val koin = GlobalContext.get()
        val meals = koin.get<MealRepository>()
        val manual = koin.get<ManualDiaryEntryRepository>()
        val slots = koin.get<DiarySlotRepository>().observeDiarySlots().first()
        val date = koin.get<DateProvider>().now().date
        val name = "Synthetic direct load " + java.util.UUID.randomUUID().toString().take(6)
        val meal = meals.save(name, listOf(MealItem("Synthetic direct constituent", NutritionFacts(
            energy = NutrientValue.Complete(135.0), proteins = NutrientValue.Complete(5.0),
            fats = NutrientValue.Complete(3.0), carbohydrates = NutrientValue.Complete(22.0)))))
        val before = slots.associate { it.id to manual.observeAll(it.id, date).first().size }
        try {
            withWindows {
                launchActivity<DeveloperActivity>().use { scenario ->
                    scenario.onActivity { activity ->
                        decor = activity.window.decorView
                        activity.setContent { FoodYouTheme { FoodUsAppShell({}) } }
                    }
                    // No ellipsis menu is opened: this must be the direct fifth card action.
                    click(Res.string.action_load_meal)
                    capture("direct-load-meal")
                    driver.clickText(name)
                    driver.await("Saved Meal appends one constituent") {
                        runBlocking { slots.sumOf { manual.observeAll(it.id, date).first().size - before.getValue(it.id) } == 1 }
                    }
                    val changed = runBlocking { slots.count { manual.observeAll(it.id, date).first().size != before.getValue(it.id) } }
                    assertEquals(1, changed)
                }
            }
        } finally { meals.delete(meal) }
    }

    @Test fun rememberedSortSurvivesProcessRestart() = runBlocking {
        emulatorOnly()
        assumeTrue(InstrumentationRegistry.getArguments().getString("phoneSortRestartVerify") == "true")
        val settings: UserPreferencesRepository<Settings> = GlobalContext.get().get(named(Settings::class.qualifiedName!!))
        assertEquals(LibrarySortOrder.ReverseAlphabetical, settings.observe().first().librarySortOrder)
    }

    @Test fun textOnlySubmitAndDuplicateGuardUseTheRealScannerViewModel() {
        emulatorOnly()
        val application = GlobalContext.get()
        val configured = MutableStateFlow(AiSettings("synthetic-test-key", null, null, null))
        val settings = object : UserPreferencesRepository<AiSettings> {
            override fun observe() = configured
            override suspend fun update(transform: AiSettings.() -> AiSettings) { configured.value = configured.value.transform() }
        }
        val received = CompletableDeferred<AiFoodScanRequest>()
        val release = CompletableDeferred<Unit>()
        var requests = 0
        val scanner = object : AiFoodScanner {
            override suspend fun scan(jpeg: ByteArray, hint: String?): AiScanResult = error("Text fixture must use the request boundary")
            override suspend fun scan(request: AiFoodScanRequest): AiScanResult {
                requests++; received.complete(request); release.await()
                return AiScanResult.Success(AiFoodEstimate("Synthetic soup result", calories = 120.0, protein = 4.0, fat = 3.0, carbohydrates = 15.0))
            }
        }
        val model = AiScanViewModel(scanner, application.get<NutritionalItemSearchUseCase>(), ObserveAiConfigured(settings, application.get<AppConfig>()))
        val isolated = koinApplication { modules(module { viewModel { model } }) }
        try {
            withWindows {
                launchActivity<DeveloperActivity>().use { scenario ->
                    scenario.onActivity { activity ->
                        decor = activity.window.decorView
                        activity.setContent { KoinIsolatedContext(context = isolated) { MaterialTheme {
                            AiScanScreen(onBack = {}, onSaveToQuickAdd = { _, _ -> }, onAlignPickFood = {}, onCreateCustomFood = {})
                        } } }
                    }
                    driver.findText(label(Res.string.label_ai_scan_hint))
                    assertFalse(driver.control(label(Res.string.action_submit)).isEnabled)
                    driver.setField(Res.string.label_ai_scan_hint, "Synthetic soup")
                    driver.await("Configured text-only Submit enabled") { driver.control(label(Res.string.action_submit)).isEnabled }
                    click(Res.string.action_submit)
                    val request = runBlocking { withTimeout(10_000) { received.await() } }
                    assertNull(request.jpeg)
                    assertEquals("Synthetic soup", request.description)
                    instrumentation.runOnMainSync { model.submit(); model.submit() }
                    assertEquals(1, requests)
                    release.complete(Unit)
                    driver.findText("Synthetic soup result")
                    capture("text-only-result")
                    click(Res.string.action_ai_edit_input)
                    driver.setField(Res.string.label_ai_scan_hint, "Synthetic revised soup")
                    click(Res.string.action_submit)
                    driver.await("Revised text-only lookup submitted") { requests == 2 }
                    driver.findText("Synthetic soup result")
                }
            }
        } finally { instrumentation.runOnMainSync { model.viewModelScope.cancel() }; isolated.close() }
    }
}
