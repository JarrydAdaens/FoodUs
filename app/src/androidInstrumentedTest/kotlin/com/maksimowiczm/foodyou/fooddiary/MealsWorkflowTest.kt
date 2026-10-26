package com.maksimowiczm.foodyou.fooddiary

import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.compose.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import com.maksimowiczm.foodyou.app.ui.home.goals.GoalsViewModel as HomeSummaryViewModel
import com.maksimowiczm.foodyou.goals.domain.entity.*
import com.maksimowiczm.foodyou.goals.domain.repository.GoalsRepository
import androidx.room.Room
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.ui.food.diary.add.toDiaryFood
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.*
import com.maksimowiczm.foodyou.app.ui.food.meals.*
import com.maksimowiczm.foodyou.app.ui.food.recipe.MeasureIngredientViewModel
import com.maksimowiczm.foodyou.app.ui.food.search.*
import com.maksimowiczm.foodyou.app.ui.home.master.*
import com.maksimowiczm.foodyou.app.ui.home.meals.card.*
import com.maksimowiczm.foodyou.app.ui.home.poll.PollsViewModel
import com.maksimowiczm.foodyou.common.domain.event.*
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.*
import com.maksimowiczm.foodyou.common.domain.userpreferences.*
import com.maksimowiczm.foodyou.common.extension.now
import com.maksimowiczm.foodyou.food.ProductTestClock
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.usecase.*
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.food.search.domain.*
import com.maksimowiczm.foodyou.food.search.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.*
import com.maksimowiczm.foodyou.poll.domain.entity.*
import com.maksimowiczm.foodyou.poll.domain.repository.PollRepository
import com.maksimowiczm.foodyou.poll.domain.usecase.ObserveActivePollUseCase
import com.maksimowiczm.foodyou.settings.domain.entity.*
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.junit.Assert.*
import org.junit.Test
import org.koin.compose.KoinIsolatedContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Real Home/card/library/editor/search/portion/manual screens on a private synthetic Room DB. */
@SdkSuppress(minSdkVersion = 30)
class MealsWorkflowTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private var fixtureNav: NavHostController? = null
    private val homeContext = "Synthetic Meals Home context"
    private val driver = ScanTestDriver(instrumentation, { decor }, { instrumentation.targetContext.packageName }, { null })
    private lateinit var db: FoodYouDatabase
    private lateinit var meals: RoomMealRepository
    private lateinit var manual: RoomManualDiaryEntryRepository
    private lateinit var foods: RoomNutritionalItemDiaryEntryRepository
    private lateinit var date: LocalDate
    private var editingManual: ManualDiaryEntryId? = null
    private var homeSummary: HomeSummaryViewModel? = null
    private var failSave = false
    private var failLoad = false
    private var loadInserts = 0
    private val foodName = "Synthetic Meals Food"
    private val recipeName = "Synthetic Meals Recipe"
    private val quickName = "Synthetic Meals Quick Add"
    private val slotName = "Synthetic Meals destination"
    private val originalName = "Synthetic saved Meal"
    private val updatedName = "Synthetic edited Meal"

    @Test fun visibleSaveLibraryEditCancelFailureLoadAndDeleteUseOneCollection() = withFixture(populated = true) {
        val sourceFoods = runBlocking { foods.observeAll(2401, date).first() }
        val sourceManual = entries()
        click(Res.string.action_saved_meal_menu)
        click(Res.string.action_save_as_meal)
        driver.setField(Res.string.label_meal_name, originalName)
        clickButton(label(Res.string.action_save))
        driver.await("saved Meal committed") { saved().size == 1 }
        click(Res.string.meals_library)
        driver.findText(originalName)
        driver.findText(recipeName)
        clickButton(label(Res.string.meals_edit))
        driver.setField(Res.string.label_meal_name, "Synthetic discarded")
        clickButton(label(Res.string.action_cancel)); clickButton(label(Res.string.positive_ok))
        assertEquals(originalName, saved().single().name)

        clickButton(label(Res.string.meals_edit))
        driver.setField(Res.string.label_meal_name, " ")
        clickButton(label(Res.string.action_save))
        driver.findText(label(Res.string.meals_invalid)); clickButton(label(Res.string.positive_ok))
        assertEquals(originalName, saved().single().name)
        driver.setField(Res.string.label_meal_name, updatedName)
        rowAction(foodName, label(Res.string.action_edit_entry))
        driver.setField(Res.string.meals_captured_amount, "1.5")
        setEditable(label(Res.string.unit_energy) + " (" + label(Res.string.unit_kcal) + ")", "310")
        clickButton(label(Res.string.meals_apply_changes))
        rowAction(recipeName, label(Res.string.meals_remove_item))
        add(foodName, "1")
        add(recipeName, "2")
        failSave = true
        clickButton(label(Res.string.action_save))
        driver.findText(label(Res.string.meals_save_failed))
        assertEquals(originalName, saved().single().name)
        assertEquals(3, saved().single().itemCount)
        clickButton(label(Res.string.positive_ok))
        clickButton(label(Res.string.action_save))
        driver.await("validated editor committed once") { saved().singleOrNull()?.name == updatedName }
        driver.findText(updatedName)
        val edited = saved().single()
        assertEquals(4, edited.itemCount)
        assertEquals(1.5, (edited.items.first { it.nutritionFacts.energy.value == 310.0 }.capturedPortion as Measurement.Serving).quantity, 1e-9)
        assertEquals(sourceFoods, runBlocking { foods.observeAll(2401, date).first() })
        assertEquals(sourceManual, entries())

        failLoad = true; loadInserts = 0
        clickButton(label(Res.string.action_load_meal))
        driver.clickText(slotName)
        clickButton(label(Res.string.action_load_meal))
        driver.findText(label(Res.string.meals_load_failed))
        assertEquals("Second-insert failure leaves no partial bundle", sourceManual, entries())
        clickButton(label(Res.string.positive_ok))
        clickButton(label(Res.string.action_load_meal))
        driver.await("one explicit load appends four entries") { entries().size == sourceManual.size + 4 }
        val loaded = entries().filter { it.id !in sourceManual.map { old -> old.id } }
        assertEquals(4, loaded.size)
        assertEquals(1, loaded.count { it.name == recipeName })
        assertEquals(listOf(80.0, 310.0, 400.0, 780.0), loaded.map { it.nutritionFacts.energy.value!! }.sorted())
        assertTrue(loaded.all { it.diarySlotId == 2401L && it.date == date })
        assertEquals(Measurement.Serving(2.0), loaded.single { it.name == recipeName }.capturedPortion)
        clickButton(label(Res.string.meals_delete)); clickButton(label(Res.string.positive_ok))
        driver.findText(label(Res.string.meals_empty))
        assertTrue(saved().isEmpty())
        assertEquals(sourceManual.size + 4, entries().size)
        assertEquals(sourceFoods, runBlocking { foods.observeAll(2401, date).first() })
    }

    @Test fun emptyDiaryStillReachesMealsAndUnavailableCollectionCanRetry() = withFixture(populated = false) {
        assertTrue(entries().isEmpty())
        click(Res.string.meals_library)
        driver.findText(label(Res.string.meals_empty))
        clickButton(label(Res.string.meals_go_diary))
        driver.findText(slotName)
        val id = runBlocking { meals.save("Synthetic malformed Meal", listOf(MealItem("Known amount", NutritionFacts(), capturedPortion = Measurement.Milliliter(125.0)))) }
        val good = runBlocking { db.mealDao.getById(id.value)!!.items.single() }
        runBlocking { db.mealDao.updateItems(listOf(good.copy(capturedPortionValue = null))) }
        click(Res.string.meals_library)
        driver.findText(label(Res.string.meals_unavailable))
        clickButton(label(Res.string.positive_ok))
        driver.findText(label(Res.string.meals_retry))
        runBlocking { db.mealDao.updateItems(listOf(good)) }
        clickButton(label(Res.string.meals_retry))
        driver.findText("Synthetic malformed Meal")
        assertEquals(Measurement.Milliliter(125.0), saved().single().items.single().capturedPortion)
    }

    @Test fun preciseLoadedQuantitySurvivesManualNameEditAndNoOpThenDeliberateContextEditClearsIt() = withFixture(populated = false, precise = true) {
        val original = entries().single()
        openManualEditor(original.name)
        driver.assertField(Res.string.label_number_of_servings, "1.234567")
        driver.assertField(Res.string.weight, "123.456789")
        driver.setField(Res.string.product_name, "Synthetic precision rename")
        clickButton(label(Res.string.action_save))
        homeDiaryRow("Synthetic precision rename")
        var changed = entries().single()
        assertEquals(original.capturedPortion, changed.capturedPortion)
        assertEquals(original.servingCount, changed.servingCount)
        assertEquals(original.weightGrams, changed.weightGrams)
        assertEquals(original.nutritionFacts, changed.nutritionFacts)
        openManualEditor(changed.name)
        driver.setField(Res.string.label_number_of_servings, "1.2345670")
        clickButton(label(Res.string.action_save))
        homeDiaryRow(changed.name)
        assertEquals(original.capturedPortion, entries().single().capturedPortion)
        openManualEditor(changed.name)
        driver.findText(label(Res.string.meals_quantity_help))
        driver.setField(Res.string.label_number_of_servings, "2.5")
        clickButton(label(Res.string.action_save))
        homeDiaryRow(changed.name)
        changed = entries().single()
        assertNull(changed.capturedPortion)
        assertEquals(2.5, changed.servingCount)
        assertEquals(original.weightGrams, changed.weightGrams)
        assertEquals(original.nutritionFacts, changed.nutritionFacts)
    }

    @Test fun malformedManualShowsSummaryUnavailableAndRetryRestoresRealTotals() =
        withFixture(populated = false, precise = true, includeSummary = true) {
            val original = entries().single()
            val good = runBlocking { db.manualDiaryEntryDao.observe(original.id.value).first()!! }
            val malformed = good.copy(capturedPortionValue = null)
            runBlocking { db.manualDiaryEntryDao.update(malformed) }
            driver.findText(label(Res.string.meals_diary_unavailable))
            assertTrue(requireNotNull(homeSummary).readFailed.value)
            assertEquals(malformed, runBlocking { db.manualDiaryEntryDao.observe(original.id.value).first() })
            runBlocking { db.manualDiaryEntryDao.update(good) }
            rowAction(label(Res.string.meals_diary_unavailable), label(Res.string.meals_retry))
            driver.await("real summary recovers after explicit retry") {
                requireNotNull(homeSummary).let { !it.readFailed.value && it.model.value?.energy == 780 }
            }
            driver.findText("780 / 2000 " + label(Res.string.unit_kcal))
            assertEquals(good, runBlocking { db.manualDiaryEntryDao.observe(original.id.value).first() })
        }

    private fun click(resource: StringResource) = driver.clickText(label(resource))
    private fun label(resource: StringResource) = runBlocking { org.jetbrains.compose.resources.getString(resource) }
    private fun saved() = runBlocking { meals.observeAll().first() }
    private fun entries() = runBlocking { manual.observeAll(2401, date).first() }

    private fun ancestors(node: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        var cursor: AccessibilityNodeInfo? = node
        repeat(32) {
            val current = cursor ?: return result
            assertFalse("Accessibility ancestry cannot cycle", result.contains(current))
            result += current; cursor = current.parent
        }
        assertNull("Accessibility ancestry must terminate within bound", cursor)
        return result
    }
    private fun nodes(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> =
        driver.descendants(root).take(4097).toList().also { assertTrue("Inspect complete bounded context", it.size <= 4096) }

    private fun clickButton(text: String) {
        var control: AccessibilityNodeInfo? = null
        driver.await("enabled real action in current synthetic screen") {
            val context = ancestors(driver.findText(text)).last()
            control = nodes(context).asSequence()
                .filter { it.isVisibleToUser && (it.text?.toString()?.lines()?.contains(text) == true || it.contentDescription?.toString() == text) }
                .filter { node -> ancestors(node).takeWhile { it != context }.none { it.isEditable } }
                .mapNotNull { node -> ancestors(node).takeWhile { it != context }.firstOrNull { it.isClickable } }
                .firstOrNull { candidate -> candidate.isVisibleToUser && candidate.isEnabled &&
                    candidate.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK } }
            control != null
        }
        val action = requireNotNull(control) { "Enabled real action must resolve" }
        assertTrue("Action remains visible", action.isVisibleToUser)
        assertTrue("Action remains enabled", action.isEnabled)
        assertTrue("Action is clickable", action.isClickable)
        assertTrue(action.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK })
        assertTrue(action.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        instrumentation.waitForIdleSync()
    }

    private fun awaitFixtureRoute(route: String) {
        driver.await("completed synthetic navigation to " + route) {
            driver.onMain {
                val entry = fixtureNav?.currentBackStackEntry
                entry != null && entry.destination.route == route && entry.lifecycle.currentState == Lifecycle.State.RESUMED
            }
        }
    }

    private fun homeDiaryRow(name: String): AccessibilityNodeInfo {
        awaitFixtureRoute("home")
        var row: AccessibilityNodeInfo? = null
        driver.await("enabled noneditable diary row in current Home context") {
            val context = driver.findText(homeContext)
            row = nodes(context).asSequence()
                .filter { it.isVisibleToUser && it.text?.toString()?.lines()?.contains(name) == true }
                .filter { node -> ancestors(node).takeWhile { it != context }.none { it.isEditable } }
                .mapNotNull { node -> ancestors(node).takeWhile { it != context }.firstOrNull { it.isClickable } }
                .firstOrNull { candidate -> candidate.isVisibleToUser && candidate.isEnabled &&
                    candidate.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK } }
            row != null
        }
        return requireNotNull(row) { "Actual Home diary row must resolve" }
    }

    private fun openManualEditor(name: String) {
        val row = homeDiaryRow(name)
        assertTrue("Home row is enabled", row.isEnabled)
        assertTrue("Home row is clickable", row.isClickable)
        assertTrue(row.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK })
        assertTrue(row.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        instrumentation.waitForIdleSync()
        click(Res.string.action_edit_entry)
        awaitFixtureRoute("manual")
    }

    private fun rowAction(name: String, action: String) {
        val rowText = driver.findText(name)
        for (ancestor in ancestors(rowText)) {
            val match = nodes(ancestor).firstOrNull { it.isVisibleToUser && it.text?.toString()?.lines()?.contains(action) == true } ?: continue
            val control = ancestors(match).firstOrNull { it.isClickable && it.isEnabled } ?: continue
            assertTrue(control.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            instrumentation.waitForIdleSync()
            return
        }
        fail("Missing actual row action")
    }

    private fun setEditable(label: String, value: String) {
        val context = ancestors(driver.findText(label)).last()
        val field = nodes(context).firstOrNull { it.isVisibleToUser && it.isEditable &&
            (it.hintText?.toString()?.lines()?.contains(label) == true || driver.descendants(it).any { child -> child.text?.toString()?.lines()?.contains(label) == true }) }
        assertNotNull("Editable field in current screen", field)
        assertTrue(field!!.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,
            Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value) }))
        instrumentation.waitForIdleSync()
    }

    private fun portionAmountField(): AccessibilityNodeInfo {
        val labelNode = driver.findText(label(Res.string.measurement_amount))
        val field = requireNotNull(ancestors(labelNode).firstOrNull { it.isEditable }) {
            "Accessible portion amount must resolve to its actual editable ancestor"
        }
        assertTrue("Actual portion field is visible", field.isVisibleToUser)
        assertTrue("Actual portion field is enabled", field.isEnabled)
        assertTrue("Actual portion field exposes text editing", field.actionList.any { it.id == AccessibilityNodeInfo.ACTION_SET_TEXT })
        return field
    }

    private fun add(name: String, amount: String) {
        clickButton(label(Res.string.meals_add_item))
        var result: AccessibilityNodeInfo? = null
        driver.await("enabled actual search result in current Meals search") {
            val currentSearchRoot = ancestors(driver.findText(label(Res.string.meals_add_item))).last()
            result = nodes(currentSearchRoot).asSequence()
                .filter { it.isVisibleToUser && it.text?.toString()?.lines()?.contains(name) == true }
                .filter { node -> ancestors(node).none { it.isEditable } }
                .mapNotNull { node -> ancestors(node).firstOrNull { it.isClickable && it.isEnabled } }.firstOrNull()
            result != null
        }
        assertTrue(result!!.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK })
        assertTrue(result!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        instrumentation.waitForIdleSync()
        val field = portionAmountField()
        assertTrue(field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,
            Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, amount) }))
        driver.await("portion amount updated") {
            portionAmountField().text?.toString() == amount
        }
        clickButton(label(Res.string.action_save))
        driver.findText(updatedName)
    }

    private class Prefs<T : UserPreferences>(initial: T) : UserPreferencesRepository<T> {
        private val state = MutableStateFlow(initial)
        override fun observe() = state
        override suspend fun update(transform: T.() -> T) { state.value = transform(state.value) }
    }

    private fun withFixture(populated: Boolean, precise: Boolean = false, includeSummary: Boolean = false, checkFlow: () -> Unit) {
        date = LocalDate.now()
        failSave = false; failLoad = false; loadInserts = 0; editingManual = null; fixtureNav = null
        db = Room.inMemoryDatabaseBuilder(instrumentation.targetContext, FoodYouDatabase::class.java).build()
        meals = RoomMealRepository(db, db.mealDao)
        manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
        foods = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
        val slots = RoomDiarySlotRepository(db.diarySlotDao)
        val products = RoomProductRepository(db.productDao)
        val recipes = RoomRecipeRepository(db.recipeDao, products)
        val observe = ObserveNutritionalItemUseCase(products, recipes)
        val suggestions = ObserveMeasurementSuggestionsUseCase(observe, RoomNutritionalItemMeasurementSuggestionRepository(db.measurementSuggestionDao))
        val prefs = Prefs(FoodSearchPreferences(FoodSearchPreferences.OpenFoodFacts(false), FoodSearchPreferences.Usda(false, null), FoodSearchPreferences.AustralianFoodCompositionDatabase(false)))
        val events = object : EventBus {
            override val events: Flow<IntegrationEvent> = emptyFlow()
            override fun publish(integrationEvent: IntegrationEvent) = Unit
        }
        val remote = object : FoodRemoteMediatorFactoryAggregate {
            override val openFoodFactsRemoteMediatorFactory: ProductRemoteMediatorFactory get() = error("Offline fixture")
            override val usdaRemoteMediatorFactory: ProductRemoteMediatorFactory get() = error("Offline fixture")
        }
        val search = RoomNutritionalItemSearchRepository(db.foodSearchDao)
        val searchUseCase = NutritionalItemSearchUseCase(search, prefs, remote, events, ProductTestClock)
        val slotPrefs = Prefs(DiarySlotsPreferences(DiarySlotsCardsLayout.Vertical, false, false))
        val settings = Prefs(Settings(null, false, false, NutrientsOrder.entries, false, if (includeSummary) listOf(HomeCard.Goals, HomeCard.DiarySlots) else listOf(HomeCard.DiarySlots),
            false, true, EnergyFormat.entries.first(), GraphStyle.entries.first(), WeekLayout.entries.first(), AppLaunchInfo(null, null, 0)))
        val pollPrefs = Prefs(PollPreferences(emptySet()))
        val pollRepo = object : PollRepository { override fun observeActivePolls(): Flow<List<Poll>> = flowOf(emptyList()) }
        val saving = RoomMealRepository(db, object : MealDao by db.mealDao {
            override suspend fun updateMeal(meal: MealEntity) {
                db.mealDao.updateMeal(meal)
                if (failSave) { failSave = false; error("Synthetic editor failure after header") }
            }
        })
        val loadRepository = RoomManualDiaryEntryRepository(object : ManualDiaryEntryDao by db.manualDiaryEntryDao {
            override suspend fun insert(entry: ManualDiaryEntryEntity): Long {
                val inserted = db.manualDiaryEntryDao.insert(entry)
                if (failLoad && ++loadInserts == 2) { failLoad = false; error("Synthetic UI load failure") }
                return inserted
            }
        })
        val save = SaveMealUseCase(foods, manual, meals, db)
        val apply = ApplyMealUseCase(meals, loadRepository, slots, db, ProductTestClock)
        val observation = ObserveDiarySlotEntriesUseCase(slots, slotPrefs, foods, manual, ProductTestClock)
        runBlocking {
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2401, slotName, 0, 0, 23, 59))
            val facts = NutritionFacts(energy = NutrientValue.Complete(200.0), proteins = NutrientValue.Complete(10.0),
                carbohydrates = NutrientValue.Complete(20.0), fats = NutrientValue.Complete(4.0))
            val foodId = products.insertProduct(foodName, null, null, null, false, 100.0, 40.0, ProductSource(ProductSource.Type.User), facts)
            val product = products.observeProduct(foodId).first()!!
            val recipeId = recipes.insertRecipe(recipeName, 3, null, false,
                listOf(RecipeIngredient(product, Measurement.Gram(300.0))))
            if (populated) {
                foods.insert(Measurement.Serving(2.0), 2401, date, product.toDiaryFood(), ProductTestClock.now())
                foods.insert(Measurement.Serving(2.0), 2401, date, recipes.observeRecipe(recipeId).first()!!.toDiaryFood(), ProductTestClock.now())
                manual.insert(quickName, 2401, date, NutritionFacts(energy = NutrientValue.Complete(780.0)),
                    ProductTestClock.now(), servingCount = 1.234567, weightGrams = 123.456789)
            }
            if (precise) manual.insert("Synthetic precise captured", 2401, date,
                NutritionFacts(energy = NutrientValue.Complete(780.0)), ProductTestClock.now(),
                servingCount = 1.234567, weightGrams = 123.456789, capturedPortion = Measurement.Serving(1.234567))
        }
        val goals = object : GoalsRepository {
            override suspend fun updateWeeklyGoals(weeklyGoals: WeeklyGoals) = error("Read-only fixture")
            override fun observeWeeklyGoals() = flowOf(WeeklyGoals.defaultGoals)
            override fun observeDailyGoals(date: LocalDate) = flowOf(DailyGoal.defaultGoals)
        }
        homeSummary = if (includeSummary) HomeSummaryViewModel(settings, observation, goals) else null
        val isolated = koinApplication { modules(module {
            single { observe }
            viewModel { requireNotNull(homeSummary) }
            viewModel { HomeViewModel(settings) }
            viewModel { PollsViewModel(ObserveActivePollUseCase(settings, pollPrefs, pollRepo, ProductTestClock), pollPrefs) }
            viewModel { DiarySlotsCardsViewModel(observation, foods, manual, save, apply, meals, slotPrefs, RoomDiaryBulkRepository(db, db.measurementDao, db.manualDiaryEntryDao, db.diarySlotDao)) }
            viewModel { MealsViewModel(meals, UpdateMealUseCase(saving), apply, slots, observe) }
            viewModel { (id: ManualDiaryEntryId) -> UpdateQuickAddViewModel(id, manual, ProductTestClock) }
            viewModel { (id: NutritionalItemId) -> MeasureIngredientViewModel(id, observe, suggestions) }
            viewModel { FoodSearchViewModel(null, prefs, RoomNutritionalItemSearchHistoryRepository(db.foodSearchDao), search, searchUseCase, ProductTestClock)
                .apply { changeSource(FoodFilter.Source.YourFood) } }
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
                    activity.setContent { KoinIsolatedContext(context = isolated) { MaterialTheme { Fixture() } } }
                }
                instrumentation.waitForIdleSync()
                driver.findText(slotName)
                checkFlow()
            }
        } finally {
            isolated.close(); homeSummary?.viewModelScope?.cancel(); db.close(); decor = null; fixtureNav = null
            automation.serviceInfo = originalInfo.apply { this.flags = flags }
        }
    }

    @Composable private fun Fixture() {
        val nav = rememberNavController()
        SideEffect { fixtureNav = nav }
        NavHost(nav, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    modifier = Modifier.semantics { contentDescription = homeContext },
                    onSettings = {}, onTitle = {}, onMeals = { nav.navigate("meals") },
                    onDiarySlotCardLongClick = {}, onDiarySlotCardAddClick = { _, _ -> },
                    onDiarySlotCardQuickAddClick = { _, _ -> }, onDiarySlotCardAiScanClick = { _, _ -> },
                    onDiarySlotCardFastTextClick = { _, _ -> }, onGoalsCardLongClick = {}, onGoalsCardClick = {},
                    onEditDiaryEntryClick = { _, manualId, _ ->
                        if (manualId != null) { editingManual = ManualDiaryEntryId(manualId); nav.navigate("manual") }
                    },
                )
            }
            composable("meals") {
                MealsApp(viewModel = org.koin.compose.viewmodel.koinViewModel(), onBack = { nav.popBackStack() }, initialDate = date)
            }
            composable("manual") {
                UpdateQuickAddScreen(onBack = { nav.popBackStack() }, onSave = { nav.popBackStack() }, id = requireNotNull(editingManual).value)
            }
        }
    }
}
