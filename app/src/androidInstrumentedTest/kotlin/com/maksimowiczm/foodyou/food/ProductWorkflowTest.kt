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

/** Real Product create/update/search/portion/log/edit screens over PRIVATE in-memory Room.
 * The host uses isolated Koin; no canonical diary, network, credentials or owner settings resolve.
 */
@SdkSuppress(minSdkVersion = 30)
class ProductWorkflowTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private val driver = ScanTestDriver(instrumentation, { decor }, { instrumentation.targetContext.packageName }, { null })
    private lateinit var db: FoodYouDatabase
    private lateinit var products: RoomProductRepository
    private lateinit var diary: RoomNutritionalItemDiaryEntryRepository
    private val date = LocalDate(2026, 10, 25)
    private var productId by mutableStateOf<NutritionalItemId.Product?>(null)
    private var selected: NutritionalItemId? = null
    private var selectedMeasurement: Measurement? = null
    private var liquid = false
    private var serving: Double? = 40.0
    private val productName = "Synthetic Story26 food"
    private val fixtureFacts = NutritionFacts(
        proteins = NutrientValue.Complete(10.0),
        carbohydrates = NutrientValue.Complete(20.0),
        energy = NutrientValue.Complete(250.0),
        fats = NutrientValue.Complete(4.0),
        saturatedFats = NutrientValue.Complete(0.001),
        transFats = NutrientValue.Complete(0.001),
        monounsaturatedFats = NutrientValue.Complete(0.001),
        polyunsaturatedFats = NutrientValue.Complete(0.001),
        omega3 = NutrientValue.Complete(0.001),
        omega6 = NutrientValue.Incomplete(null),
        sugars = NutrientValue.Complete(0.001),
        addedSugars = NutrientValue.Complete(0.001),
        dietaryFiber = NutrientValue.Complete(0.001),
        solubleFiber = NutrientValue.Complete(0.001),
        insolubleFiber = NutrientValue.Complete(0.001),
        salt = NutrientValue.Complete(0.001),
        cholesterol = NutrientValue.Complete(0.001),
        caffeine = NutrientValue.Complete(0.001),
        vitaminA = NutrientValue.Complete(0.001),
        vitaminB1 = NutrientValue.Complete(0.001),
        vitaminB2 = NutrientValue.Complete(0.001),
        vitaminB3 = NutrientValue.Complete(0.001),
        vitaminB5 = NutrientValue.Complete(0.001),
        vitaminB6 = NutrientValue.Complete(0.001),
        vitaminB7 = NutrientValue.Complete(0.001),
        vitaminB9 = NutrientValue.Complete(0.001),
        vitaminB12 = NutrientValue.Complete(0.001),
        vitaminC = NutrientValue.Complete(0.001),
        vitaminD = NutrientValue.Complete(0.001),
        vitaminE = NutrientValue.Complete(0.001),
        vitaminK = NutrientValue.Incomplete(null),
        manganese = NutrientValue.Complete(0.001),
        magnesium = NutrientValue.Complete(0.001),
        potassium = NutrientValue.Complete(0.001),
        calcium = NutrientValue.Complete(0.001),
        copper = NutrientValue.Complete(0.001),
        zinc = NutrientValue.Complete(0.001),
        sodium = NutrientValue.Complete(0.001),
        iron = NutrientValue.Complete(0.001),
        phosphorus = NutrientValue.Complete(0.001),
        selenium = NutrientValue.Incomplete(null),
        iodine = NutrientValue.Complete(0.001),
        chromium = NutrientValue.Complete(0.001),
    )

    @Test fun createEditSearchSelectLogEditAgainAndCancelPreserveCapturedNutrition() = withFlow {
        saveCreated()
        driver.clickText("Edit definition")
        driver.setField(Res.string.product_brand, "Edited brand")
        driver.setField(Res.string.product_reference_serving, "40")
        click(Res.string.action_save)
        driver.findText("Synthetic Product hub")
        assertEquals("Edited brand", product().brand)
        assertEquals(250.0, product().nutritionFacts.energy.value)
        assertFacts(fixtureFacts, product().nutritionFacts)
        driver.clickText("Search food")
        driver.clickText(product().headline)
        driver.findText(label(Res.string.portion_amount_to_log))
        setAmount("0")
        assertTrue(entries().isEmpty())
        setAmount("2")
        driver.findText("2 x " + label(Res.string.product_serving) + " (80 " + label(Res.string.unit_gram_short) + ")")
        assertEquals(productId, selected)
        click(Res.string.action_save)
        driver.findText("Synthetic Product hub")
        val captured = entries().single()
        assertEquals(80.0, captured.weight!!, 0.000001)
        assertEquals(200.0, captured.nutritionFacts.energy.value)
        driver.clickText("Edit definition")
        driver.setField(Res.string.product_reference_serving, "50")
        driver.setField(Res.string.unit_energy, "500")
        click(Res.string.action_save)
        driver.findText("Synthetic Product hub")
        assertEquals(50.0, product().servingWeight)
        assertEquals(500.0, product().nutritionFacts.energy.value)
        assertEquals(captured, entries().single())
        driver.clickText("Edit definition")
        driver.setField(Res.string.product_name, "Synthetic cancelled definition")
        click(Res.string.action_go_back)
        click(Res.string.action_discard)
        driver.findText("Synthetic Product hub")
        assertEquals(productName, product().name)
        assertEquals(captured, entries().single())
        driver.clickText("Edit logged portion")
        setAmount("0")
        assertEquals(captured, entries().single())
        setAmount("0.5")
        click(Res.string.action_go_back)
        driver.findText("Synthetic Product hub")
        assertEquals(captured, entries().single())
        driver.clickText("Edit logged portion")
        setAmount("0.5")
        click(Res.string.action_save)
        driver.findText("Synthetic Product hub")
        assertEquals(20.0, entries().single().weight!!, 0.000001)
        assertEquals(50.0, entries().single().nutritionFacts.energy.value)
    }

    @Test fun liquidFractionLogsMillilitersAndUnknownServingFailsWithoutGramFallback() {
        withFlow(isLiquid = true) {
            saveCreated()
            driver.clickText("Search food")
            driver.clickText(product().headline)
            setAmount("0.5")
            driver.findText("0.5 x " + label(Res.string.product_serving) + " (20 " + label(Res.string.unit_milliliter_short) + ")")
            click(Res.string.action_save)
            driver.findText("Synthetic Product hub")
            assertEquals(20.0, entries().single().weight!!, 0.000001)
            assertEquals(50.0, entries().single().nutritionFacts.energy.value)
        }
        withFlow(referenceServing = null) {
            driver.findText(label(Res.string.headline_create_product))
            driver.clickText("100 " + label(Res.string.unit_gram_short))
            driver.clickText("1 x " + label(Res.string.product_serving))
            driver.findText(label(Res.string.error_product_nutrition_basis))
            driver.findText("100 " + label(Res.string.unit_gram_short))
            driver.assertField(Res.string.unit_energy, "250")
            saveCreated()
            driver.clickText("Unsupported serving")
            driver.findText(label(Res.string.error_portion_save))
            click(Res.string.action_save)
            driver.findText(label(Res.string.error_portion_save))
            assertTrue(entries().isEmpty())
            assertNull(product().servingWeight)
            click(Res.string.action_cancel)
            click(Res.string.action_go_back)
            driver.findText("Synthetic Product hub")
            assertTrue(entries().isEmpty())
        }
    }

    private fun saveCreated() {
        driver.findText(label(Res.string.headline_create_product))
        driver.setField(Res.string.product_name, productName)
        if (serving != null) {
            val metricUnit = if (liquid) Res.string.unit_milliliter_short else Res.string.unit_gram_short
            driver.clickText("100 " + label(metricUnit))
            driver.clickText("1 x " + label(Res.string.product_serving))
            driver.awaitField(Res.string.unit_energy, "100")
            driver.awaitField(Res.string.nutriment_proteins, "4")
        }
        click(Res.string.action_save)
        driver.findText("Synthetic Product hub")
        driver.await("synthetic Product persisted") { runBlocking { products.observeProducts(10, 0).first().size == 1 } }
        assertFacts(fixtureFacts, product().nutritionFacts)
    }
    private fun assertFacts(expected: NutritionFacts, actual: NutritionFacts) {
        NutritionFactsField.entries.forEach { field ->
            val value = expected[field].value
            if (value == null) assertNull("Unknown nutrient preserved: " + field.name, actual[field].value)
            else assertEquals("Canonical nutrient preserved: " + field.name, value, actual[field].value!!, 0.000001)
        }
    }
    private fun product() = runBlocking { products.observeProduct(requireNotNull(productId)).first()!! }
    private fun entries() = runBlocking { diary.observeAll(2601, date).first() }
    private fun label(resource: StringResource) = driver.label(resource)
    private fun click(resource: StringResource) = driver.clickText(label(resource))
    private fun amountField(): AccessibilityNodeInfo {
        var node: AccessibilityNodeInfo? = driver.findText(label(Res.string.measurement_amount))
        while (node != null && !node.isEditable) node = node.parent
        assertNotNull("Accessible portion amount", node)
        return requireNotNull(node).also { assertTrue("Editable portion amount", it.isEditable) }
    }
    private fun setAmount(value: String) {
        assertTrue(amountField().performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value)
        }))
        instrumentation.waitForIdleSync()
        driver.await("portion input updated") {
            amountField().text?.toString() == value
        }
    }

    private fun withFlow(isLiquid: Boolean = false, referenceServing: Double? = 40.0, checkFlow: () -> Unit) {
        liquid = isLiquid
        serving = referenceServing
        productId = null
        selected = null
        selectedMeasurement = null
        db = Room.inMemoryDatabaseBuilder(instrumentation.targetContext, FoodYouDatabase::class.java).build()
        products = RoomProductRepository(db.productDao)
        val recipes = RoomRecipeRepository(db.recipeDao, products)
        val history = RoomNutritionalItemHistoryRepository(db.foodEventDao)
        diary = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
        val slots = RoomDiarySlotRepository(db.diarySlotDao)
        val observe = ObserveNutritionalItemUseCase(products, recipes)
        val create = CreateProductUseCase(products, history, db, ProductTestLogger)
        val update = UpdateProductUseCase(products, db, ProductTestLogger)
        val delete = DeleteNutritionalItemUseCase(products, recipes, db, ProductTestLogger)
        val suggestions = ObserveMeasurementSuggestionsUseCase(observe, RoomNutritionalItemMeasurementSuggestionRepository(db.measurementSuggestionDao))
        val log = CreateNutritionalItemDiaryEntryUseCase(slots, diary, db, ProductTestClock, ProductTestLogger)
        val edit = UpdateNutritionalItemDiaryEntryUseCase(slots, diary, ProductTestClock, db, ProductTestLogger)
        val unpack = UnpackNutritionalItemDiaryEntryUseCase(diary, slots, db, ProductTestClock, ProductTestLogger)
        val search = RoomNutritionalItemSearchRepository(db.foodSearchDao)
        val prefs = object : UserPreferencesRepository<FoodSearchPreferences> {
            private val value = MutableStateFlow(FoodSearchPreferences(FoodSearchPreferences.OpenFoodFacts(false),
                FoodSearchPreferences.Usda(false, null), FoodSearchPreferences.AustralianFoodCompositionDatabase(false)))
            override fun observe() = value
            override suspend fun update(transform: FoodSearchPreferences.() -> FoodSearchPreferences) { value.value = transform(value.value) }
        }
        val events = object : EventBus {
            override val events: Flow<IntegrationEvent> = emptyFlow()
            override fun publish(integrationEvent: IntegrationEvent) = Unit
        }
        val remote = object : FoodRemoteMediatorFactoryAggregate {
            override val openFoodFactsRemoteMediatorFactory: ProductRemoteMediatorFactory get() = error("Synthetic offline fixture")
            override val usdaRemoteMediatorFactory: ProductRemoteMediatorFactory get() = error("Synthetic offline fixture")
        }
        val searchUseCase = NutritionalItemSearchUseCase(search, prefs, remote, events, ProductTestClock)
        runBlocking { db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2601, "Synthetic Product slot", 0, 0, 23, 59)) }
        val isolated = koinApplication { modules(module {
            single { observe }
            viewModel { DownloadProductHolder() }
            viewModel { CreateProductViewModel(create, ProductTestClock) }
            viewModel { (id: NutritionalItemId.Product) -> UpdateProductViewModel(observe, update, id) }
            viewModel { (id: NutritionalItemId) -> AddEntryViewModel(unpack, log, observe, history, delete, suggestions, slots, ProductTestClock, events, id) }
            viewModel { (id: com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntryId) ->
                UpdateNutritionalItemDiaryEntryViewModel(id, edit, unpack, diary, slots, ProductTestClock) }
            viewModel { FoodSearchViewModel(null, prefs, RoomNutritionalItemSearchHistoryRepository(db.foodSearchDao), search, searchUseCase, ProductTestClock)
                .apply { changeSource(FoodFilter.Source.YourFood) } }
        }) }
        val automation = instrumentation.uiAutomation
        val originalInfo = automation.serviceInfo
        val originalFlags = originalInfo.flags
        try {
            automation.serviceInfo = automation.serviceInfo.apply { flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS }
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.setContent { KoinIsolatedContext(context = isolated) { MaterialTheme { Fixture() } } }
                }
                instrumentation.waitForIdleSync()
                checkFlow()
            }
        } finally {
            isolated.close()
            db.close()
            decor = null
            automation.serviceInfo = originalInfo.apply { flags = originalFlags }
        }
    }

    @Composable private fun Fixture() {
        val nav = rememberNavController()
        NavHost(nav, startDestination = "create") {
            composable("create") {
                CreateProductScreen(onBack = { error("Synthetic initial screen") },
                    onCreate = { productId = it; nav.navigate("hub") { popUpTo("create") { inclusive = true } } },
                    onUpdateUsdaApiKey = { error("Offline") }, onUpdateOpenFoodFactsCredentials = { error("Offline") },
                    prefillProduct = Product(NutritionalItemId.Product(0), productName, "Synthetic brand", "2600001", "Synthetic note",
                        liquid, 200.0, serving, ProductSource(ProductSource.Type.User),
                        fixtureFacts))
            }
            composable("hub") {
                val entries by diary.observeAll(2601, date).collectAsState(emptyList())
                Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                    Text("Synthetic Product hub")
                    Button(onClick = { nav.navigate("edit") }) { Text("Edit definition") }
                    Button(onClick = { nav.navigate("search") }) { Text("Search food") }
                    Button(onClick = { selected = productId; selectedMeasurement = Measurement.Serving(1.0); nav.navigate("portion") }) { Text("Unsupported serving") }
                    if (entries.isNotEmpty()) {
                        Text("Captured nutrition: " + entries.first().nutritionFacts.energy.value)
                        Button(onClick = { nav.navigate("edit-entry") }) { Text("Edit logged portion") }
                    }
                }
            }
            composable("edit") {
                UpdateProductScreen(onBack = { nav.popBackStack() }, onUpdate = { nav.popBackStack() }, productId = requireNotNull(productId))
            }
            composable("search") {
                FoodSearchApp(onFoodClick = { item, measurement ->
                    selected = item.id; selectedMeasurement = measurement; nav.navigate("portion")
                }, onUpdateUsdaApiKey = { error("Offline") }, onUpdateOpenFoodFactsCredentials = { error("Offline") }, initialQuery = "Synthetic Story26")
            }
            composable("portion") {
                AddEntryScreen(onBack = { nav.popBackStack("hub", false) }, onEditFood = { nav.navigate("edit") },
                    onEntryAdded = { nav.popBackStack("hub", false) }, onFoodDeleted = { error("No delete") },
                    onIngredient = { _, _ -> error("Product has no ingredients") }, foodId = requireNotNull(selected),
                    diarySlotId = 2601, date = date, measurement = selectedMeasurement, animatedVisibilityScope = this)
            }
            composable("edit-entry") {
                val entries by diary.observeAll(2601, date).collectAsState(emptyList())
                entries.firstOrNull()?.let { entry ->
                    UpdateEntryScreen(entryId = entry.id.value, onBack = { nav.popBackStack() },
                        onSave = { nav.popBackStack() }, animatedVisibilityScope = this)
                }
            }
        }
    }
}
