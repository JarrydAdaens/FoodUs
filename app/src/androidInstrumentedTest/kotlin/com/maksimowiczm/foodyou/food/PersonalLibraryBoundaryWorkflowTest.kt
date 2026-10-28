package com.maksimowiczm.foodyou.food

import android.accessibilityservice.AccessibilityServiceInfo
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.filters.SdkSuppress
import androidx.test.core.app.launchActivity
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.app.ui.food.library.*
import com.maksimowiczm.foodyou.app.ui.food.meals.*
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.extension.now
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.repository.ProductRepository
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import foodyou.app.generated.resources.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.koin.compose.KoinIsolatedContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@SdkSuppress(minSdkVersion = 30)
class PersonalLibraryBoundaryWorkflowTest {
    @Before fun prepare() = prepareSyntheticLibraryHost()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private val driver = ScanTestDriver(instrumentation, { decor }, { instrumentation.targetContext.packageName }, { null })
    private fun click(id: StringResource) = driver.clickText(driver.label(id))
    private fun root(node: AccessibilityNodeInfo): AccessibilityNodeInfo {
        var current = node
        repeat(32) { current.parent?.let { current = it } }
        return current
    }

    @Test fun emptyCollectionsCreateCancelAndReadRetryRemainUsable() = runBlocking<Unit> {
        val db = Room.inMemoryDatabaseBuilder(instrumentation.targetContext, FoodYouDatabase::class.java).build()
        val products = RoomProductRepository(db.productDao)
        val recipes = RoomRecipeRepository(db.recipeDao, products)
        val meals = RoomMealRepository(db, db.mealDao)
        val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
        val slots = RoomDiarySlotRepository(db.diarySlotDao)
        var failRead = false
        val failing = object : ProductRepository by products {
            override fun observeLibraryNames() = flow {
                if(failRead) error("Synthetic library read failure")
                emitAll(products.observeLibraryNames())
            }
        }
        val observe = ObserveNutritionalItemUseCase(products, recipes)
        val library = withContext(Dispatchers.Main) { LibraryViewModel(failing, recipes, observe) }
        val mealModel = withContext(Dispatchers.Main) { MealsViewModel(meals, UpdateMealUseCase(meals),
            ApplyMealUseCase(meals, manual, slots, db, ProductTestClock), slots, observe) }
        val isolated = koinApplication { modules(module { viewModel { library }; viewModel { mealModel } }) }
        var productCreates = 0
        var recipeCreates = 0
        val info = instrumentation.uiAutomation.serviceInfo
        val flags = info.flags
        instrumentation.uiAutomation.serviceInfo = info.apply { this.flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS }
        try {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.setContent { KoinIsolatedContext(isolated) { MaterialTheme {
                        PersonalLibraryScreen({}, { productCreates++ }, { recipeCreates++ }, {}, {}, {})
                    } } }
                }
                driver.findText(driver.label(Res.string.library_empty))
                click(Res.string.library_create_recipe)
                assertEquals(1, recipeCreates)
                click(Res.string.library_products)
                driver.findText(driver.label(Res.string.library_empty))
                click(Res.string.library_create_product)
                assertEquals(1, productCreates)
                failRead = true
                withContext(Dispatchers.Main) { library.retry() }
                driver.findText(driver.label(Res.string.meals_unavailable))
                failRead = false
                click(Res.string.meals_retry)
                driver.findText(driver.label(Res.string.library_empty))
                click(Res.string.library_meals)
                click(Res.string.library_create_meal)
                driver.setField(Res.string.label_meal_name, "Synthetic cancelled empty Meal")
                click(Res.string.action_cancel)
                click(Res.string.positive_ok)
                driver.findText(driver.label(Res.string.library_empty))
                assertTrue(meals.observeAll().first().isEmpty())
                assertTrue(products.observeLibraryNames().first().isEmpty())
                assertTrue(recipes.observeLibraryNames().first().isEmpty())
                recipes.insertRecipe(Recipe(NutritionalItemId.Recipe(0), "Synthetic reported unknown yield", null, emptyList(), null, false,
                    ReportedNutrition(NutritionFacts(energy = NutrientValue.Complete(321.0)), ReportedBasis.WHOLE_ITEM)))
                click(Res.string.library_recipes)
                driver.findText(driver.label(Res.string.library_serving_unavailable))
                driver.findText(driver.label(Res.string.captured_unknown_value) + " " + driver.label(Res.string.unit_kcal))
            }
        } finally {
            withContext(Dispatchers.Main) { library.viewModelScope.cancel(); mealModel.viewModelScope.cancel() }
            isolated.close(); db.close(); decor = null
            instrumentation.uiAutomation.serviceInfo = info.apply { this.flags = flags }
        }
    }

    @Test fun explicitChooserRequiresBothSelectionsAndLoadsOnlyChosenDaySlot() = runBlocking<Unit> {
        val db = Room.inMemoryDatabaseBuilder(instrumentation.targetContext, FoodYouDatabase::class.java).build()
        val products = RoomProductRepository(db.productDao)
        val recipes = RoomRecipeRepository(db.recipeDao, products)
        val meals = RoomMealRepository(db, db.mealDao)
        val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
        val slots = RoomDiarySlotRepository(db.diarySlotDao)
        val selected = LocalDate.now().let { LocalDate(it.year, it.month, 15) }
        val otherDay = LocalDate(2025, 1, 1)
        val facts = NutritionFacts(energy = NutrientValue.Complete(120.0))
        db.diarySlotDao.insertWithLastRank(DiarySlotEntity(83811, "Synthetic chosen slot", 0, 0, 23, 59))
        db.diarySlotDao.insertWithLastRank(DiarySlotEntity(83812, "Synthetic other slot", 0, 0, 23, 59))
        manual.insert("Synthetic existing destination", 83811, selected, facts, ProductTestClock.now())
        manual.insert("Synthetic other history", 83812, otherDay, facts, ProductTestClock.now())
        val history = manual.observeAll(83812, otherDay).first()
        val id = meals.save("Synthetic selected Meal", listOf(MealItem("Synthetic A", facts), MealItem("Synthetic B", facts)))
        val meal = meals.getMeal(id)!!
        val destinations = slots.observeDiarySlots().first()
        val model = withContext(Dispatchers.Main) { MealsViewModel(meals, UpdateMealUseCase(meals),
            ApplyMealUseCase(meals, manual, slots, db, ProductTestClock), slots, ObserveNutritionalItemUseCase(products, recipes)) }
        var loaded by mutableStateOf(false)
        fun loadButton(): AccessibilityNodeInfo {
            val parent = root(driver.findText(driver.label(Res.string.action_cancel)))
            val label = driver.label(Res.string.action_load_meal)
            val controls = driver.descendants(parent).filter { it.text?.toString()?.lines()?.contains(label) == true }.mapNotNull { text ->
                var current: AccessibilityNodeInfo? = text
                repeat(32) {
                    if(current?.isClickable == true) return@mapNotNull current
                    current = current?.parent
                }
                null
            }.toList()
            return controls.first()
        }
        val info = instrumentation.uiAutomation.serviceInfo
        val flags = info.flags
        instrumentation.uiAutomation.serviceInfo = info.apply { this.flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS }
        try {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.setContent { MaterialTheme {
                        val busy by model.busy.collectAsStateWithLifecycle()
                        if(loaded) Text("Synthetic load completed") else LibraryMealDestination(meal, destinations, busy, {}) { day, slot ->
                            model.load(id, slot, day) { loaded = true }
                        }
                    } }
                }
                assertFalse(loadButton().isEnabled)
                driver.clickText("Synthetic chosen slot")
                assertFalse(loadButton().isEnabled)
                click(Res.string.library_choose_day)
                var day: AccessibilityNodeInfo? = null
                try { driver.await("Calendar fifteenth day is rendered") {
                    val calendar = root(driver.findText(driver.label(Res.string.positive_ok)))
                    day = driver.descendants(calendar).firstOrNull { node ->
                        node.isVisibleToUser && node.isClickable && Regex("\\b15\\b").containsMatchIn(
                            node.text?.toString().orEmpty() + " " + node.contentDescription?.toString().orEmpty())
                    }
                    day != null
                } } catch (failure: Throwable) {
                    // This guarded fixture contains synthetic data only; preserve native calendar semantics.
                    val diagnostics = buildString {
                        val windows = instrumentation.uiAutomation.windowsOnAllDisplays
                        repeat(windows.size()) { index -> windows.valueAt(index).forEach { window ->
                            appendLine("window focused=" + window.isFocused + " type=" + window.type)
                            window.root?.let { windowRoot -> driver.descendants(windowRoot).forEach { node ->
                                appendLine("text=" + node.text + " description=" + node.contentDescription +
                                    " visible=" + node.isVisibleToUser + " clickable=" + node.isClickable)
                            } }
                        } }
                    }
                    java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "story38-calendar.txt").writeText(diagnostics)
                    instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                        java.io.FileOutputStream(java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "story38-calendar.png")).use {
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                        }
                        bitmap.recycle()
                    }
                    throw failure
                }
                var control = requireNotNull(day)
                repeat(20) { if(!control.isClickable) control.parent?.let { control = it } }
                assertTrue(control.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                instrumentation.waitForIdleSync()
                driver.await("Chosen date enables calendar confirmation") { driver.control(driver.label(Res.string.positive_ok)).isEnabled }
                click(Res.string.positive_ok)
                assertTrue(loadButton().isEnabled)
                val submit = loadButton()
                assertTrue(submit.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                submit.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                driver.await("Explicit Meal load completes") { loaded }
                val result = manual.observeAll(83811, selected).first()
                assertEquals(listOf("Synthetic existing destination", "Synthetic A", "Synthetic B"), result.map { it.name })
                assertEquals(history, manual.observeAll(83812, otherDay).first())
                assertTrue(manual.observeAll(83812, selected).first().isEmpty())
                meals.delete(id)
                assertEquals(result, manual.observeAll(83811, selected).first())
            }
        } finally {
            withContext(Dispatchers.Main) { model.viewModelScope.cancel() }
            db.close(); decor = null
            instrumentation.uiAutomation.serviceInfo = info.apply { this.flags = flags }
        }
    }
}
