package com.maksimowiczm.foodyou.food

import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.compose.*
import androidx.room.Room
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.ui.food.diary.add.*
import com.maksimowiczm.foodyou.app.ui.food.diary.update.*
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.app.ui.food.product.CreateProductScreen
import com.maksimowiczm.foodyou.app.ui.food.product.UpdateProductScreen
import com.maksimowiczm.foodyou.app.ui.food.product.create.CreateProductViewModel
import com.maksimowiczm.foodyou.app.ui.food.product.update.UpdateProductViewModel
import com.maksimowiczm.foodyou.app.ui.food.product.download.DownloadProductHolder
import com.maksimowiczm.foodyou.app.ui.food.search.*
import com.maksimowiczm.foodyou.common.domain.event.*
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.*
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.usecase.*
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.food.search.domain.*
import com.maksimowiczm.foodyou.food.search.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
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

import com.maksimowiczm.foodyou.app.ui.food.recipe.*
import com.maksimowiczm.foodyou.app.ui.home.meals.card.*
import kotlin.math.roundToInt
import com.maksimowiczm.foodyou.food.domain.repository.RecipeRepository

/** Actual Recipe editor/search/ingredient/portion screens using isolated Koin and private Room. */
@SdkSuppress(minSdkVersion = 30)
class RecipeWorkflowTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private val driver = ScanTestDriver(instrumentation, { decor }, { instrumentation.targetContext.packageName }, { null })
    private lateinit var db: FoodYouDatabase
    private lateinit var recipes: RoomRecipeRepository
    private lateinit var diary: RoomNutritionalItemDiaryEntryRepository
    private var recipeId by mutableStateOf<NutritionalItemId.Recipe?>(null)
    private var selected: NutritionalItemId? = null
    private var selectedMeasurement: Measurement? = null
    private val searchSelectionCount = java.util.concurrent.atomic.AtomicInteger(0)
    private val observedSearchSelection = java.util.concurrent.atomic.AtomicReference<NutritionalItemId?>(null)
    private val searchContext = "Synthetic Recipe search context"
    private var failNextSave = false
    private var stageNumber = 0
    private var stagePath = "setup"
    private val stageEvents = java.util.ArrayDeque<org.json.JSONObject>()
    private val date = LocalDate(2026, 10, 25)
    private val name = "Synthetic Story27 Recipe"
    private val a = "Synthetic Recipe ingredient A"
    private val b = "Synthetic Recipe ingredient B"

    @Test fun createComposeYieldSaveLogEditCancelAndExplicitUnpackAreSeparate() = withFlow {
        driver.setField(Res.string.recipe_name,name)
        add(a,"100")
        add(b,"200")
        driver.clickText(label(Res.string.action_edit) + ": " + a)
        setAmount("50"); saveIngredient()
        driver.clickText(label(Res.string.action_edit) + ": " + a)
        setAmount("100"); saveIngredient()
        driver.clickText(label(Res.string.action_delete) + ": " + b)
        click(Res.string.action_delete)
        add(b,"200")
        driver.setField(Res.string.recipe_servings,"0")
        driver.findText(label(Res.string.error_value_must_be_positive))
        assertNull(recipeId)
        driver.setField(Res.string.recipe_servings,"3")
        driver.findText(label(Res.string.recipe_per_serving))
        driver.findText("200 " + label(Res.string.unit_kcal))
        failNextSave = true
        click(Res.string.action_save)
        driver.findText(label(Res.string.error_recipe_save))
        assertNull(recipeId)
        click(Res.string.action_cancel)
        driver.assertField(Res.string.recipe_name,name)
        driver.assertField(Res.string.recipe_servings,"3")
        click(Res.string.action_save)
        driver.findText("Synthetic Recipe hub")
        assertEquals(3,recipe().servings)
        assertEquals(600.0,recipe().totalNutrition.energy.value)
        searchRecipe()
        setAmount("0")
        driver.findText(label(Res.string.error_portion_amount))
        assertTrue(entries().isEmpty())
        setAmount("2")
        driver.findText("2 x " + label(Res.string.product_serving) + " (200 " + label(Res.string.unit_gram_short) + ")")
        driver.findText("400 " + label(Res.string.unit_kcal))
        click(Res.string.action_save)
        driver.findText("Synthetic Recipe hub")
        val captured = entries().single()
        assertEquals(400.0,captured.nutritionFacts.energy.value!!,0.000001)
        driver.clickText("Edit Recipe")
        driver.setField(Res.string.recipe_servings,"6")
        driver.findText(label(Res.string.recipe_per_serving))
        driver.findText("100 " + label(Res.string.unit_kcal))
        click(Res.string.action_save)
        driver.findText("Synthetic Recipe hub")
        assertEquals(6,recipe().servings)
        assertEquals(captured,entries().single())
        driver.clickText("Edit Recipe")
        driver.setField(Res.string.recipe_name,"Synthetic discarded Recipe")
        driver.setField(Res.string.recipe_servings,"7")
        click(Res.string.action_go_back); click(Res.string.action_discard)
        driver.findText("Synthetic Recipe hub")
        assertEquals(name,recipe().name)
        assertEquals(6,recipe().servings)
        assertEquals(captured,entries().single())
        searchRecipe()
        setAmount("2")
        click(Res.string.action_unpack)
        driver.findText("Synthetic Recipe hub")
        val after = entries()
        assertEquals(3,after.size)
        assertEquals(captured,after.first { it.id == captured.id })
        assertEquals(200.0,after.filter { it.id != captured.id }.sumOf { it.nutritionFacts.energy.value!! },0.000001)
    }

    @Test fun nestedMixedRecipeUsesServingWithoutInventingMetricBasis() = withFlow(mixed = true) {
        driver.setField(Res.string.recipe_name,name)
        add("Synthetic nested Recipe","1")
        add("Synthetic liquid ingredient","100")
        driver.setField(Res.string.recipe_servings,"3")
        click(Res.string.action_save)
        driver.findText("Synthetic Recipe hub")
        assertNull(recipe().totalWeight)
        assertEquals(400.0,recipe().totalNutrition.energy.value)
        searchRecipe()
        setAmount("2")
        driver.findText("2 x " + label(Res.string.product_serving))
        click(Res.string.action_save)
        driver.findText("Synthetic Recipe hub")
        driver.findText("2 x " + label(Res.string.product_serving))
        driver.findText("267 " + label(Res.string.unit_kcal))
        val captured = entries().single()
        assertNull(captured.weight)
        assertEquals(400.0 * 2 / 3,captured.nutritionFacts.energy.value!!,0.000001)
        val snapshot = captured.food as com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeSnapshot
        assertTrue(snapshot.ingredients.first().food is com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeSnapshot)
        assertEquals(1,snapshot.calculationVersion)
    }

    private fun add(item: String, amount: String) = stage("ingredient-add") {
        stage("open-search") { click(Res.string.action_add_ingredient) }
        stage("select-item") { driver.clickText(item) }
        setAmount("0")
        stage("invalid-feedback") { driver.findText(label(Res.string.error_portion_amount)) }
        stage("invalid-save-unavailable") {
            driver.await("invalid ingredient amount has no active Save action") { visibleIngredientSave() == null }
        }
        assertNull("Ingredient drafts must not persist a Recipe", recipeId)
        setAmount(amount)
        saveIngredient()
        stage("returned-to-draft") { driver.findText(label(Res.string.action_add_ingredient)) }
    }
    private fun ancestors(node: AccessibilityNodeInfo): Sequence<AccessibilityNodeInfo> = sequence {
        var current: AccessibilityNodeInfo? = node
        val seen = mutableSetOf<AccessibilityNodeInfo>()
        repeat(32) {
            val next = current ?: return@sequence
            assertTrue("Synthetic accessibility parent chain must not cycle", seen.add(next))
            yield(next)
            current = next.parent
        }
        assertNull("Synthetic accessibility parent chain must terminate within 32 nodes", current)
    }
    private fun visibleIngredientSave(): AccessibilityNodeInfo? {
        val root = ancestors(amountField()).last()
        val action = label(Res.string.action_save)
        return inspectedNodes(root).firstOrNull {
            it.isVisibleToUser && (it.contentDescription?.toString() == action || it.text?.toString()?.lines()?.contains(action) == true)
        }
    }
    private fun inspectedNodes(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val nodes = driver.descendants(root).take(4097).toList()
        assertTrue("Synthetic accessibility tree must be fully inspected within 4096 nodes", nodes.size <= 4096)
        return nodes
    }
    private fun saveIngredient() = stage("ingredient-save") {
        stage("accessible-control") {
            driver.await("valid ingredient amount exposes accessible Save") { visibleIngredientSave() != null }
        }
        val control = requireNotNull(ancestors(requireNotNull(visibleIngredientSave())).firstOrNull { it.isClickable }) {
            "Save must resolve to its real clickable control"
        }
        assertTrue("Valid ingredient Save is enabled", control.isEnabled)
        assertTrue("Valid ingredient Save has a click action", control.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK })
        stage("click-control") {
            assertTrue("Save actual ingredient draft", control.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        }
        stage("main-idle") { instrumentation.waitForIdleSync() }
    }
    private fun searchRecipe() = stage("recipe-search") {
        val previousSelections = searchSelectionCount.get()
        stage("open-context") { driver.clickText("Search Recipe") }
        stage("wait-context") { driver.findText(searchContext) }
        var result: AccessibilityNodeInfo? = null
        stage("wait-query-and-result") {
            driver.await("current Search query and real enabled Recipe result") {
                val context = driver.findText(searchContext)
                val nodes = inspectedNodes(context)
                if (nodes.none { it.isVisibleToUser && it.isEditable && it.text?.toString() == name }) return@await false
                result = nodes.asSequence()
                    .filter { it.isVisibleToUser && it.text?.toString()?.lines()?.contains(name) == true }
                    .filter { node -> ancestors(node).takeWhile { it != context }.none { it.isEditable } }
                    .mapNotNull { node -> ancestors(node).takeWhile { it != context }.firstOrNull { it.isClickable } }
                    .firstOrNull { control -> control.isVisibleToUser && control.isEnabled && control.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK } }
                result != null
            }
        }
        val control = requireNotNull(result)
        assertTrue("Search result must be enabled", control.isEnabled)
        assertTrue("Search result must be clickable", control.isClickable)
        assertTrue("Search result exposes the real click action", control.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK })
        stage("click-result") {
            assertTrue("Select the actual Recipe result", control.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        }
        stage("portion-screen") { driver.findText(label(Res.string.portion_amount_to_log)) }
        assertEquals("Exactly one fresh Search selection callback", previousSelections + 1, searchSelectionCount.get())
        assertEquals("Search resolves the saved Recipe identity", recipeId, observedSearchSelection.get())
    }
    private fun recipe() = stage("read-recipe") {
        runBlocking { kotlinx.coroutines.withTimeout(15_000) { recipes.observeRecipe(requireNotNull(recipeId)).first()!! } }
    }
    private fun entries() = stage("read-entries") {
        runBlocking { kotlinx.coroutines.withTimeout(15_000) { diary.observeAll(2701,date).first() } }
    }
    private fun label(resource: StringResource) = driver.label(resource)
    private fun click(resource: StringResource) = driver.clickText(label(resource))
    private fun amountField(): AccessibilityNodeInfo {
        val labelNode = driver.findText(label(Res.string.measurement_amount))
        return requireNotNull(ancestors(labelNode).firstOrNull { it.isEditable }) {
            "Accessible portion amount must resolve to its editable ancestor"
        }
    }
    private fun setAmount(value: String) = stage("set-amount") {
        stage("write-editable") {
            assertTrue(amountField().performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value)
            }))
        }
        stage("main-idle") { instrumentation.waitForIdleSync() }
        stage("verify-editable") {
            driver.await("portion input updated") { amountField().text?.toString() == value }
        }
    }

    /** Static stage identifiers only: never copy UI values, credentials, or a diary into diagnostics. */
    private fun recordStage(state: String) {
        val event = org.json.JSONObject().put("number", ++stageNumber).put("stage", stagePath)
            .put("state", state).put("uptimeMillis", android.os.SystemClock.uptimeMillis())
        stageEvents.addLast(event)
        while (stageEvents.size > 96) stageEvents.removeFirst()
        val history = org.json.JSONArray()
        stageEvents.forEach { history.put(it) }
        java.io.File(instrumentation.targetContext.cacheDir,"story27-workflow-progress.json")
            .writeText(org.json.JSONObject().put("latest",event).put("events",history).toString())
        android.util.Log.i("FoodUsRecipeWorkflow", state + " " + stagePath + " #" + stageNumber)
        instrumentation.sendStatus(2, Bundle().apply { putString("stream", "STAGE " + state + " " + stagePath + "\n") })
    }
    private inline fun <T> stage(id: String, block: () -> T): T {
        val previous = stagePath
        stagePath = previous + "/" + id
        recordStage("BEGIN")
        try {
            return block().also { recordStage("END") }
        } catch (failure: Throwable) {
            recordStage("FAIL:" + failure.javaClass.simpleName)
            throw failure
        } finally {
            stagePath = previous
        }
    }

    private fun withFlow(mixed: Boolean = false, checkFlow: () -> Unit) {
        stageNumber = 0; stageEvents.clear(); stagePath = if (mixed) "mixed" else "standard"
        recordStage("BEGIN")
        recipeId = null; selected = null; selectedMeasurement = null; searchSelectionCount.set(0); observedSearchSelection.set(null); failNextSave = false
        db = Room.inMemoryDatabaseBuilder(instrumentation.targetContext,FoodYouDatabase::class.java).build()
        val products = RoomProductRepository(db.productDao)
        recipes = RoomRecipeRepository(db.recipeDao,products)
        val history = RoomNutritionalItemHistoryRepository(db.foodEventDao)
        diary = RoomNutritionalItemDiaryEntryRepository(db,db.measurementDao)
        val slots = RoomDiarySlotRepository(db.diarySlotDao)
        val observe = ObserveNutritionalItemUseCase(products,recipes)
        val saving = object : RecipeRepository by recipes {
            override suspend fun insertRecipe(name: String,servings: Int,note: String?,isLiquid: Boolean,ingredients: List<RecipeIngredient>): NutritionalItemId.Recipe {
                val id = recipes.insertRecipe(name,servings,note,isLiquid,ingredients)
                if (failNextSave) { failNextSave = false; error("Synthetic save failure after insert") }
                return id
            }
        }
        val create = CreateRecipeUseCase(saving,products,history,db,ProductTestLogger)
        val update = UpdateRecipeUseCase(recipes,products,db,ProductTestLogger)
        val delete = DeleteNutritionalItemUseCase(products,recipes,db,ProductTestLogger)
        val suggestions = ObserveMeasurementSuggestionsUseCase(observe,RoomNutritionalItemMeasurementSuggestionRepository(db.measurementSuggestionDao))
        val log = CreateNutritionalItemDiaryEntryUseCase(slots,diary,db,ProductTestClock,ProductTestLogger)
        val unpack = UnpackNutritionalItemDiaryEntryUseCase(diary,slots,db,ProductTestClock,ProductTestLogger)
        val search = RoomNutritionalItemSearchRepository(db.foodSearchDao)
        val prefs = object : UserPreferencesRepository<FoodSearchPreferences> {
            private val value = MutableStateFlow(FoodSearchPreferences(FoodSearchPreferences.OpenFoodFacts(false),FoodSearchPreferences.Usda(false,null),FoodSearchPreferences.AustralianFoodCompositionDatabase(false)))
            override fun observe() = value
            override suspend fun update(transform: FoodSearchPreferences.() -> FoodSearchPreferences) { value.value = transform(value.value) }
        }
        val events = object : EventBus {
            override val events: Flow<IntegrationEvent> = emptyFlow()
            override fun publish(integrationEvent: IntegrationEvent) = Unit
        }
        val remote = object : FoodRemoteMediatorFactoryAggregate {
            override val openFoodFactsRemoteMediatorFactory: ProductRemoteMediatorFactory get() = error("Offline")
            override val usdaRemoteMediatorFactory: ProductRemoteMediatorFactory get() = error("Offline")
        }
        val searchUseCase = NutritionalItemSearchUseCase(search,prefs,remote,events,ProductTestClock)
        runBlocking {
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2701,"Synthetic Recipe slot",0,0,23,59))
            val facts = NutritionFacts(energy = NutrientValue.Complete(200.0),proteins = NutrientValue.Complete(10.0),carbohydrates = NutrientValue.Complete(20.0),fats = NutrientValue.Complete(4.0))
            val id = products.insertProduct(a,null,null,"Synthetic A note",false,null,null,ProductSource(ProductSource.Type.User),facts)
            products.insertProduct(b,null,null,"Synthetic B note",false,null,null,ProductSource(ProductSource.Type.User),facts)
            if (mixed) {
                products.insertProduct("Synthetic liquid ingredient",null,null,null,true,null,null,ProductSource(ProductSource.Type.User),facts)
                recipes.insertRecipe("Synthetic nested Recipe",1,"Nested note",false,listOf(RecipeIngredient(products.observeProduct(id).first()!!,Measurement.Gram(100.0))))
            }
        }
        val isolated = koinApplication { modules(module {
            single { observe }
            viewModel { CreateRecipeViewModel(observe,create,ProductTestClock) }
            viewModel { (id: NutritionalItemId.Recipe) -> UpdateRecipeViewModel(id,observe,update) }
            viewModel { (id: NutritionalItemId) -> MeasureIngredientViewModel(id,observe,suggestions) }
            viewModel { (id: NutritionalItemId) -> AddEntryViewModel(unpack,log,observe,history,delete,suggestions,slots,ProductTestClock,events,id) }
            viewModel { FoodSearchViewModel(null,prefs,RoomNutritionalItemSearchHistoryRepository(db.foodSearchDao),search,searchUseCase,ProductTestClock).apply { changeSource(FoodFilter.Source.YourFood) } }
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
                stage("initial-main-idle") { instrumentation.waitForIdleSync() }
                try { stage("workflow") { checkFlow() } } finally { recordStage("BEGIN:activity-close") }
            }
            recordStage("END:activity-close")
        } finally {
            stage("close-koin") { isolated.close() }
            stage("close-room") { db.close() }
            decor = null
            automation.serviceInfo = originalInfo.apply { this.flags = flags }
        }
    }

    @Composable private fun Fixture() {
        val nav = rememberNavController()
        NavHost(nav,startDestination = "create") {
            composable("create") {
                CreateRecipeScreen(onBack = { error("Synthetic initial screen") },onCreate = { recipeId = it; nav.navigate("hub") { popUpTo("create") { inclusive = true } } },
                    onEditFood = { error("Not a separate definition edit") },onUpdateUsdaApiKey = { error("Offline") },onUpdateOpenFoodFactsCredentials = { error("Offline") })
            }
            composable("hub") {
                val entries by diary.observeAll(2701,date).collectAsState(emptyList())
                Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                    Text("Synthetic Recipe hub")
                    Button(onClick = { nav.navigate("edit") }) { Text("Edit Recipe") }
                    Button(onClick = { nav.navigate("search") }) { Text("Search Recipe") }
                    entries.forEach { entry ->
                        val facts = entry.nutritionFacts
                        DiarySlotFoodListItem(
                            entry = FoodDiarySlotEntryModel(entry.id,entry.name,facts.energy.value?.roundToInt(),facts.proteins.value,
                                facts.carbohydrates.value,facts.fats.value,entry.measurement,entry.weight,entry.food.isLiquid,
                                entry.food is com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeSnapshot,entry.food.servingWeight,entry.food.totalWeight),
                            color = MaterialTheme.colorScheme.surface,contentColor = MaterialTheme.colorScheme.onSurface,shape = MaterialTheme.shapes.medium,
                        )
                    }
                }
            }
            composable("edit") {
                UpdateRecipeScreen(onBack = { nav.popBackStack() },onUpdate = { nav.popBackStack() },recipeId = requireNotNull(recipeId),
                    onEditFood = { error("Offline fixture") },onUpdateUsdaApiKey = { error("Offline") },onUpdateOpenFoodFactsCredentials = { error("Offline") })
            }
            composable("search") {
                FoodSearchApp(onFoodClick = { item,measurement -> observedSearchSelection.set(item.id); searchSelectionCount.incrementAndGet(); selected = item.id; selectedMeasurement = measurement; nav.navigate("portion") },
                    onUpdateUsdaApiKey = { error("Offline") },onUpdateOpenFoodFactsCredentials = { error("Offline") },initialQuery = name,
                    modifier = Modifier.semantics { contentDescription = searchContext })
            }
            composable("portion") {
                AddEntryScreen(onBack = { nav.popBackStack("hub",false) },onEditFood = { nav.navigate("edit") },onEntryAdded = { nav.popBackStack("hub",false) },
                    onFoodDeleted = { error("No delete") },onIngredient = { _,_ -> error("No ingredient drill-in") },foodId = requireNotNull(selected),
                    diarySlotId = 2701,date = date,measurement = selectedMeasurement,animatedVisibilityScope = this)
            }
        }
    }
}
