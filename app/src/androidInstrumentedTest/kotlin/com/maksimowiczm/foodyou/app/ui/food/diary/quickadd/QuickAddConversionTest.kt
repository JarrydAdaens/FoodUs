package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.PixelCopy
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.Room
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimate
import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimateParser
import com.maksimowiczm.foodyou.ai.domain.toNutritionFacts
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.navigation.FoodDiaryCreateQuickAdd
import com.maksimowiczm.foodyou.app.navigation.PromoteToProduct
import com.maksimowiczm.foodyou.app.navigation.PromoteToRecipe
import com.maksimowiczm.foodyou.app.ui.food.product.CreateProductScreen
import com.maksimowiczm.foodyou.app.ui.food.product.create.CreateProductViewModel
import com.maksimowiczm.foodyou.app.ui.food.product.download.DownloadProductHolder
import com.maksimowiczm.foodyou.app.ui.food.recipe.CreateRecipeScreen
import com.maksimowiczm.foodyou.app.ui.food.recipe.CreateRecipeViewModel
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.NutrientValue.Companion.toNutrientValue
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.NutritionFactsField
import com.maksimowiczm.foodyou.common.domain.food.get
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.log.Logger
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import com.maksimowiczm.foodyou.food.domain.usecase.CreateProductUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.CreateRecipeUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.DeleteNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.infrastructure.repository.RoomNutritionalItemHistoryRepository
import com.maksimowiczm.foodyou.food.infrastructure.repository.RoomProductRepository
import com.maksimowiczm.foodyou.food.infrastructure.repository.RoomRecipeRepository
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.RoomManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import foodyou.app.generated.resources.*
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.junit.Assert.*
import org.junit.Test
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Production create/update, typed promotion routes, product/recipe editors and their real
 * ViewModels/use cases/repositories over PRIVATE in-memory Room. Global Koin supplies only the
 * clock; no global diary, settings, food, network or photo sources are resolved.
 */
@SdkSuppress(minSdkVersion = 30)
class QuickAddConversionTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private var window: Window? = null
    private var activityPackage = "not-created"
    @Volatile private var fixtureCrash: String? = null
    private val driver = ScanTestDriver(instrumentation, { decor }, { activityPackage }, { fixtureCrash })
    private lateinit var database: FoodYouDatabase
    private lateinit var diary: RoomManualDiaryEntryRepository
    private lateinit var products: RoomProductRepository
    private lateinit var recipes: RoomRecipeRepository
    private lateinit var date: LocalDate
    private var existingId: ManualDiaryEntryId? = null
    private val createdRecipes = mutableStateListOf<NutritionalItemId.Recipe>()
    private var saved by mutableStateOf(false)

    @Test fun scanProductKeepsExplicitEnergyWhenUserCompletesRequiredMissingMacro() {
        withFlow(REPORTED) {
            assertActions("scan-create")
            field(Res.string.unit_energy, "780")
            click(Res.string.action_promote_to_product)
            heading(Res.string.headline_create_product)
            field(Res.string.unit_energy, "780")
            field(Res.string.nutriment_carbohydrates, "")
            assertFalse("Product still requires an explicit carbohydrate value", saveControl().isEnabled)
            set(Res.string.nutriment_carbohydrates, "0")
            field(Res.string.unit_energy, "780")
            assertTrue(saveControl().isEnabled)
            click(Res.string.action_save)
            heading(Res.string.headline_quick_add)
            awaitProducts(1)
            val product = productList().single()
            val expected = estimate(REPORTED).toNutritionFacts().copy(carbohydrates = 0.0.toNutrientValue())
            assertFacts(expected, product.nutritionFacts)
            assertNull(product.packageWeight)
            field(Res.string.nutriment_carbohydrates, "")
            field(Res.string.unit_energy, "780")
            assertTrue("Promotion alone never inserts a diary row", diaryEntries().isEmpty())
            // Repeat and cancel the real editor; no lock or second product is introduced.
            click(Res.string.action_promote_to_product)
            heading(Res.string.headline_create_product)
            click(Res.string.action_go_back)
            heading(Res.string.headline_quick_add)
            awaitProducts(1)
            saveSource()
            assertFacts(estimate(REPORTED).toNutritionFacts(), diaryEntries().single().nutritionFacts)
        }
        // Unknown energy remains required in Product until a deliberate calculation choice.
        withFlow(UNKNOWN_ENERGY) {
            click(Res.string.action_promote_to_product)
            heading(Res.string.headline_create_product)
            field(Res.string.unit_energy, "")
            set(Res.string.nutriment_carbohydrates, "0")
            field(Res.string.unit_energy, "")
            assertFalse(saveControl().isEnabled)
            click(Res.string.headline_manual_energy_input)
            driver.awaitField(Res.string.unit_energy, "482")
            click(Res.string.action_save)
            heading(Res.string.headline_quick_add)
            awaitProducts(1)
            assertEquals(482.0, productList().single().nutritionFacts.energy.value)
            field(Res.string.unit_energy, "")
            assertTrue(diaryEntries().isEmpty())
        }
    }

    @Test fun weightedScanRecipeCancelCleansPlaceholderAndRepeatSavePreservesTotals() = withFlow(WEIGHTED) {
        assertActions("weighted-scan-create")
        val source = estimate(WEIGHTED)
        repeat(2) {
            click(Res.string.action_promote_to_recipe)
            heading(Res.string.headline_create_recipe)
            awaitProducts(1)
            assertFacts(source.toNutritionFacts() / 250.0 * 100.0, productList().single().nutritionFacts)
            click(Res.string.action_go_back)
            heading(Res.string.headline_quick_add)
            awaitProducts(0)
            assertTrue(diaryEntries().isEmpty())
        }
        click(Res.string.action_promote_to_recipe)
        heading(Res.string.headline_create_recipe)
        click(Res.string.action_save)
        heading(Res.string.headline_quick_add)
        awaitProducts(1)
        val recipe = savedRecipe()
        assertEquals(3, recipe.servings)
        assertEquals("Synthetic description", recipe.note)
        assertEquals(250.0, recipe.totalWeight!!, 0.000001)
        assertRecipeTotals(source.toNutritionFacts(), recipe)
        field(Res.string.label_number_of_servings, "2.5")
        field(Res.string.weight, "250")
        assertTrue(diaryEntries().isEmpty())
        saveSource()
        assertFacts(source.toNutritionFacts(), diaryEntries().single().nutritionFacts)
        assertEquals(2.5, diaryEntries().single().servingCount)
    }

    @Test fun directCreateShowsAccessibleActionsKeepsSaveAndNormalProductCalculation() {
        withFlow(null) {
            assertActions("direct-create")
            set(Res.string.product_name, "")
            assertFalse(driver.control(label(Res.string.action_promote_to_product)).isEnabled)
            assertFalse(driver.control(label(Res.string.action_promote_to_recipe)).isEnabled)
            assertFalse(saveControl().isEnabled)
            set(Res.string.product_name, "Synthetic direct food with a longer name for a compact display")
            field(Res.string.unit_energy, "482")
            assertActions("direct-valid-create")
            saveSource()
            val entry = diaryEntries().single()
            assertEquals(482.0, entry.nutritionFacts.energy.value)
            assertEquals(0.0, entry.nutritionFacts.carbohydrates.value)
            assertTrue(productList().isEmpty())
        }
        withFlow(null, blankProduct = true) {
            heading(Res.string.headline_create_product)
            set(Res.string.product_name, "Synthetic ordinary product")
            set(Res.string.nutriment_proteins, "35")
            set(Res.string.nutriment_carbohydrates, "0")
            set(Res.string.nutriment_fats, "38")
            driver.awaitField(Res.string.unit_energy, "482")
            // A deliberate mode switch still permits explicit energy on ordinary Product forms.
            click(Res.string.headline_auto_calculate_energy)
            set(Res.string.unit_energy, "780")
            click(Res.string.action_save)
            awaitProducts(1)
            assertEquals(780.0, productList().single().nutritionFacts.energy.value)
        }
    }

    @Test fun currentEditPromotionsPreserveSnapshotAndUnsavedEditsAllowingRepeats() = checkExisting(false)

    @Test fun historicalEditPromotionsPreserveSnapshotAndUnsavedEditsAllowingRepeats() = checkExisting(true)

    @Test fun sourceActionLayoutAcrossDirectScanWeightedCurrentAndHistoricalForms() {
        withFlow(null) { assertActions("direct-create", "layout-") }
        withFlow(REPORTED) { assertActions("scan-create", "layout-") }
        withFlow(WEIGHTED) { assertActions("weighted-scan-create", "layout-") }
        withFlow(WEIGHTED, existing = true) { assertActions("current-edit", "layout-") }
        withFlow(WEIGHTED, existing = true, historical = true) { assertActions("historical-edit", "layout-") }
    }

    private fun checkExisting(historical: Boolean) = withFlow(WEIGHTED, existing = true, historical = historical) {
        val original = diaryEntries().single()
        assertActions(if (historical) "historical-edit" else "current-edit")
        val changedName = "Synthetic unsaved promotion name"
        set(Res.string.product_name, changedName)
        click(Res.string.action_promote_to_product)
        heading(Res.string.headline_create_product)
        // Edit the draft then confirm discard; source form edits survive editor cancellation.
        set(Res.string.product_name, "Synthetic discarded product draft")
        click(Res.string.action_go_back)
        click(Res.string.action_discard)
        heading(Res.string.headline_quick_add)
        field(Res.string.product_name, changedName)
        awaitProducts(0)
        assertEquals(original, diaryEntries().single())
        click(Res.string.action_promote_to_recipe)
        heading(Res.string.headline_create_recipe)
        awaitProducts(1)
        click(Res.string.action_go_back)
        heading(Res.string.headline_quick_add)
        awaitProducts(0)
        field(Res.string.product_name, changedName)
        assertEquals(original, diaryEntries().single())
        repeat(2) { index ->
            click(Res.string.action_promote_to_product)
            heading(Res.string.headline_create_product)
            click(Res.string.action_save)
            heading(Res.string.headline_quick_add)
            awaitProducts(index + 1)
            assertEquals(original, diaryEntries().single())
        }
        productList().forEach { product ->
            assertEquals(changedName, product.name)
            assertFacts(original.nutritionFacts / 250.0 * 100.0, product.nutritionFacts)
            assertEquals(250.0, product.packageWeight)
        }
        click(Res.string.action_promote_to_recipe)
        heading(Res.string.headline_create_recipe)
        click(Res.string.action_save)
        heading(Res.string.headline_quick_add)
        awaitProducts(3)
        assertEquals(original, diaryEntries().single())
        val recipe = savedRecipe()
        assertEquals(changedName, recipe.name)
        assertRecipeTotals(original.nutritionFacts, recipe)
        // Only an explicit source Save updates the diary; its nutrition remains the same snapshot.
        saveSource()
        val edited = diaryEntries().single()
        assertEquals(original.id, edited.id)
        assertEquals(changedName, edited.name)
        assertEquals(original.nutritionFacts, edited.nutritionFacts)
        assertEquals(original.weightGrams, edited.weightGrams)
        assertEquals(original.servingCount, edited.servingCount)
    }

    private fun withFlow(
        json: String?, existing: Boolean = false, historical: Boolean = false,
        blankProduct: Boolean = false, checkFlow: () -> Unit,
    ) {
        val applicationKoin = GlobalContext.get()
        val clock = applicationKoin.get<DateProvider>()
        date = if (historical) LocalDate(2020, 1, 2) else clock.now().date
        database = Room.inMemoryDatabaseBuilder(instrumentation.targetContext, FoodYouDatabase::class.java).build()
        diary = RoomManualDiaryEntryRepository(database.manualDiaryEntryDao)
        products = RoomProductRepository(database.productDao)
        recipes = RoomRecipeRepository(database.recipeDao, products)
        val history = RoomNutritionalItemHistoryRepository(database.foodEventDao)
        val createProduct = CreateProductUseCase(products, history, database, SilentLogger)
        val createRecipe = CreateRecipeUseCase(recipes, products, history, database, SilentLogger)
        val observeFood = ObserveNutritionalItemUseCase(products, recipes)
        val deleteFood = DeleteNutritionalItemUseCase(products, recipes, database, SilentLogger)
        val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        existingId = runBlocking {
            database.diarySlotDao.insertWithLastRank(DiarySlotEntity(1, "Synthetic meal", 0, 0, 23, 59))
            if (existing) {
                val source = estimate(requireNotNull(json))
                diary.insert(source.name, 1, date, source.toNutritionFacts(), clock.now(), source.description,
                    servingCount = source.servingCount, weightGrams = source.weightGrams)
            } else null
        }
        createdRecipes.clear()
        saved = false
        val isolated = koinApplication {
            modules(module {
                single { observeFood }
                viewModel { (entryDate: LocalDate, diarySlot: Long) -> CreateQuickAddViewModel(diarySlot, entryDate, diary, clock) }
                viewModel { (id: ManualDiaryEntryId) -> UpdateQuickAddViewModel(id, diary, clock) }
                viewModel { DownloadProductHolder() }
                viewModel { CreateProductViewModel(createProduct, clock) }
                viewModel { CreateRecipeViewModel(observeFood, createRecipe, clock) }
                viewModel { (seed: QuickAddPromotionSeed) ->
                    PromoteToRecipePlaceholderViewModel(seed, createProduct, deleteFood, clock, cleanupScope, get<SavedStateHandle>())
                }
            })
        }
        val automation = instrumentation.uiAutomation
        val originalInfo = automation.serviceInfo
        val originalFlags = originalInfo.flags
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        fixtureCrash = null
        try {
            Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                fixtureCrash = exception.javaClass.name + exception.stackTrace.take(12).joinToString {
                    "[${it.className}.${it.methodName}:${it.lineNumber}]"
                }
                originalHandler?.uncaughtException(thread, exception)
            }
            automation.serviceInfo = automation.serviceInfo.apply {
                flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            }
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    window = activity.window
                    decor = activity.window.decorView
                    activityPackage = activity.packageName
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    activity.setContent {
                        KoinIsolatedContext(context = isolated) {
                            MaterialTheme { Fixture(json, blankProduct) }
                        }
                    }
                }
                instrumentation.waitForIdleSync()
                checkFlow()
            }
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(originalHandler)
            isolated.close()
            cleanupScope.cancel()
            database.close()
            decor = null
            window = null
            automation.serviceInfo = originalInfo.apply { flags = originalFlags }
            assertSame("Global application Koin survives", applicationKoin, GlobalContext.get())
        }
    }

    @Composable private fun Fixture(json: String?, blankProduct: Boolean) {
        val nav = rememberNavController()
        NavHost(nav, startDestination = "synthetic-start") {
            composable("synthetic-start") {
                LaunchedEffect(Unit) {
                    val removeStart: androidx.navigation.NavOptionsBuilder.() -> Unit = {
                        popUpTo("synthetic-start") { inclusive = true }
                    }
                    when {
                        blankProduct -> nav.navigate("synthetic-blank-product", removeStart)
                        existingId != null -> nav.navigate("synthetic-existing", removeStart)
                        json != null -> nav.navigate(FoodDiaryCreateQuickAdd(date.toEpochDays().toLong(), 1, estimate(json)), removeStart)
                        else -> nav.navigate("synthetic-direct", removeStart)
                    }
                }
            }
            composable<FoodDiaryCreateQuickAdd> { entry ->
                val route = entry.toRoute<FoodDiaryCreateQuickAdd>()
                CreateQuickAddScreen(
                    onBack = { error("No owner navigation") }, onSave = { saved = true; nav.navigate("synthetic-saved") },
                    diarySlotId = route.diarySlotId, date = LocalDate.fromEpochDays(route.epochDay),
                    scanEstimate = estimate(requireNotNull(route.scanEstimateJson)),
                    onPromoteToProduct = { nav.navigate(PromoteToProduct(it)) },
                    onPromoteToRecipe = { nav.navigate(PromoteToRecipe(it)) },
                )
            }
            composable("synthetic-direct") {
                CreateQuickAddScreen(
                    onBack = { error("No owner navigation") }, onSave = { saved = true; nav.navigate("synthetic-saved") },
                    diarySlotId = 1, date = date, prefillName = "Synthetic direct food", prefillProteins = 35.0, prefillFats = 38.0,
                    onPromoteToProduct = { nav.navigate(PromoteToProduct(it)) },
                    onPromoteToRecipe = { nav.navigate(PromoteToRecipe(it)) },
                )
            }
            composable("synthetic-existing") {
                UpdateQuickAddScreen(
                    onBack = { error("No owner navigation") }, onSave = { saved = true; nav.navigate("synthetic-saved") },
                    id = requireNotNull(existingId).value,
                    onPromoteToProduct = { nav.navigate(PromoteToProduct(it)) },
                    onPromoteToRecipe = { nav.navigate(PromoteToRecipe(it)) },
                )
            }
            composable<PromoteToProduct> { entry ->
                CreateProductScreen(
                    onBack = { nav.popBackStack() }, onCreate = { nav.popBackStack() },
                    onUpdateUsdaApiKey = { error("No configuration") },
                    onUpdateOpenFoodFactsCredentials = { error("No configuration") },
                    prefillProduct = entry.toRoute<PromoteToProduct>().toSeed().toProductPrefill(),
                )
            }
            composable<PromoteToRecipe> { entry ->
                val seed = entry.toRoute<PromoteToRecipe>().toSeed()
                val placeholder = koinViewModel<PromoteToRecipePlaceholderViewModel> { parametersOf(seed) }
                val id by placeholder.productId.collectAsStateWithLifecycle()
                id?.let { productId ->
                    CreateRecipeScreen(
                        onBack = { placeholder.discardPlaceholderIfUncommitted(); nav.popBackStack() },
                        onCreate = { placeholder.markCommitted(); createdRecipes.add(it); nav.popBackStack() },
                        onEditFood = { error("No ingredient edits in this regression") },
                        onUpdateUsdaApiKey = { error("No configuration") },
                        onUpdateOpenFoodFactsCredentials = { error("No configuration") },
                        initialName = seed.name, initialServings = seed.recipeServings, initialNote = seed.description,
                        initialIngredient = NutritionalItemId.Product(productId) to Measurement.Gram(seed.recipeIngredientWeightGrams),
                    )
                }
            }
            composable("synthetic-blank-product") {
                CreateProductScreen(
                    onBack = { error("No owner navigation") }, onCreate = { nav.navigate("synthetic-saved") },
                    onUpdateUsdaApiKey = { error("No configuration") },
                    onUpdateOpenFoodFactsCredentials = { error("No configuration") },
                )
            }
            composable("synthetic-saved") {
                Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                    androidx.compose.material3.Text("Synthetic save complete")
                }
            }
        }
    }

    /** All three controls must coexist visibly, have full bounds and separate touch targets.
     * Reaching the section uses the shared driver; assertions use one fresh final tree snapshot.
     */
    private fun assertActions(caseName: String, screenshotPrefix: String = "") {
        // Hides delayed autofocus IME using the shared data-driver policy, without Back navigation.
        field(Res.string.product_name, if (existingId != null) estimate(WEIGHTED).name else if (caseName.startsWith("direct")) {
            if (caseName == "direct-create") "Synthetic direct food" else "Synthetic direct food with a longer name for a compact display"
        } else if (caseName.startsWith("weighted")) estimate(WEIGHTED).name else estimate(REPORTED).name)
        // A field lookup can leave the first conversion partially behind the pinned toolbar.
        // Reach the start explicitly, then inspect all controls in ONE fresh tree without searches
        // that can scroll individual labels back under that toolbar.
        scrollSourceToStart()
        val labels = listOf(Res.string.action_promote_to_product, Res.string.action_promote_to_recipe, Res.string.action_save).map(::label)
        val root = sourceRoot()
        val nodes = driver.descendants(root).toList()
        val controls = labels.map { text ->
            val matches = nodes.filter { it.isVisibleToUser && (it.text?.toString() == text || it.contentDescription?.toString() == text) }
            assertEquals("Exactly one visible action label", 1, matches.size)
            var node = matches.single()
            // Compose can expose the actionable View and Button-role node separately. Measure
            // the actual click target rather than coupling touchability to the role's class name.
            fun canClick(target: AccessibilityNodeInfo) = target.isClickable &&
                target.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK }
            while (!canClick(node) && node.parent != null) node = node.parent
            assertTrue("Action exposes an actionable click target", canClick(node))
            assertTrue("Valid source action is enabled", node.isEnabled)
            node
        }
        val visible = driver.onMain {
            val view = requireNotNull(decor)
            val location = IntArray(2).also(view::getLocationOnScreen)
            Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
        }
        val bounds = controls.map { Rect().also(it::getBoundsInScreen) }
        val toolbarBottom = driver.onMain {
            val view = requireNotNull(decor)
            val location = IntArray(2).also(view::getLocationOnScreen)
            location[1] + (view.rootWindowInsets?.getInsets(WindowInsets.Type.systemBars())?.top ?: 0) +
                (64 * view.resources.displayMetrics.density).toInt()
        }
        captureSyntheticWindow(screenshotPrefix + caseName)
        bounds.forEach { assertTrue("Full action bounds inside synthetic window", !it.isEmpty && visible.contains(it)) }
        bounds.take(2).forEach {
            assertTrue("Conversion target below pinned toolbar: bounds=$it toolbarBottom=$toolbarBottom", it.top >= toolbarBottom)
        }
        for (left in bounds.indices) for (right in left + 1 until bounds.size) {
            assertFalse("Separate action touch targets: ${bounds[left]} and ${bounds[right]}", Rect.intersects(bounds[left], bounds[right]))
        }
        assertFalse("No redundant overflow", nodes.any { it.contentDescription?.toString() == label(Res.string.action_show_more) })
    }

    private fun sourceRoot(): AccessibilityNodeInfo {
        // The pinned heading is always reachable, so this shared-driver lookup does not scroll.
        var root = driver.findText(label(Res.string.headline_quick_add))
        while (root.parent != null) root = root.parent
        return root
    }

    private fun scrollSourceToStart() {
        instrumentation.waitForIdleSync()
        SystemClock.sleep(250)
        repeat(12) {
            val viewport = driver.descendants(sourceRoot()).firstOrNull { node ->
                !node.isEditable && node.isVisibleToUser && (
                    node.className?.toString() in setOf("android.widget.ScrollView", "androidx.recyclerview.widget.RecyclerView") ||
                        node.isScrollable && node.actionList.any {
                            it.id == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD || it.id == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                        }
                    )
            }
            assertNotNull("Synthetic source has a scrolling content viewport", viewport)
            val moved = requireNotNull(viewport).performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
            instrumentation.waitForIdleSync()
            SystemClock.sleep(250)
            if (!moved) return
        }
        error("Synthetic source did not reach its start within twelve accessibility scrolls")
    }

    /** PixelCopy copies only the isolated host window, including on a non-default display. */
    private fun captureSyntheticWindow(caseName: String) {
        val bitmap = driver.onMain { Bitmap.createBitmap(requireNotNull(decor).width, requireNotNull(decor).height, Bitmap.Config.ARGB_8888) }
        val latch = CountDownLatch(1)
        var result = -1
        try {
            driver.onMain {
                PixelCopy.request(requireNotNull(window), bitmap, { result = it; latch.countDown() }, Handler(Looper.getMainLooper()))
            }
            assertTrue("Synthetic window capture completed", latch.await(5, TimeUnit.SECONDS))
            assertEquals("Synthetic window capture succeeded", PixelCopy.SUCCESS, result)
            val directory = File(requireNotNull(instrumentation.targetContext.getExternalFilesDir(null)), "foodus-006")
            check(directory.isDirectory || directory.mkdirs())
            File(directory, "$caseName-${bitmap.width}x${bitmap.height}.png").outputStream().use {
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
        } finally { bitmap.recycle() }
    }

    private fun saveSource() {
        click(Res.string.action_save)
        driver.findText("Synthetic save complete")
        driver.await("source persistence acknowledged") { saved && diaryEntries().size == 1 }
    }

    private fun savedRecipe(): Recipe {
        driver.await("one committed recipe") { driver.onMain { createdRecipes.size == 1 } }
        val id = driver.onMain { createdRecipes.single() }
        return runBlocking { withTimeout(10_000) { requireNotNull(recipes.observeRecipe(id).first()) } }
    }

    private fun productList() = runBlocking { withTimeout(10_000) { products.observeProducts(100, 0).first() } }
    private fun awaitProducts(count: Int) = driver.await("expected synthetic product count $count") { productList().size == count }
    private fun diaryEntries() = runBlocking { withTimeout(10_000) { diary.observeAll(1, date).first() } }
    private fun saveControl() = driver.control(label(Res.string.action_save))
    private fun estimate(json: String): AiFoodEstimate = requireNotNull(AiFoodEstimateParser.parse(json))
    private fun label(resource: StringResource) = driver.label(resource)
    private fun heading(resource: StringResource) = driver.findText(label(resource))
    private fun click(resource: StringResource) = driver.clickText(label(resource))
    private fun field(resource: StringResource, expected: String) = driver.assertField(resource, expected)
    private fun set(resource: StringResource, value: String) = driver.setField(resource, value)

    private fun assertFacts(expected: NutritionFacts, actual: NutritionFacts) {
        NutritionFactsField.entries.forEach { field ->
            val value = expected[field].value
            if (value == null) assertNull("Unknown $field stays unknown", actual[field].value)
            else assertEquals("Supported $field survives promotion", value, requireNotNull(actual[field].value), 0.000001)
        }
    }

    private fun assertRecipeTotals(expected: NutritionFacts, recipe: Recipe) {
        // Current Recipe totals preserve unknowns exactly, including the backing Product facts.
        val ingredient = recipe.ingredients.single()
        assertFacts(expected / requireNotNull(recipe.totalWeight) * 100.0, ingredient.food.nutritionFacts)
        val totals = recipe.totalNutrition
        NutritionFactsField.entries.forEach { field ->
            val value = expected[field].value
            val actual = totals[field]
            if (value == null) {
                assertTrue("Unknown $field remains an incomplete recipe aggregate", actual.isIncomplete)
                assertNull("Unknown $field must not acquire a zero partial total", actual.value)
            } else {
                assertTrue("Supplied $field remains a complete recipe aggregate", actual.isComplete)
                assertEquals("Supplied $field survives recipe derivation", value, requireNotNull(actual.value), 0.000001)
            }
        }
    }

    private object SilentLogger : Logger {
        override fun d(tag: String, throwable: Throwable?, message: () -> String) = Unit
        override fun w(tag: String, throwable: Throwable?, message: () -> String) = Unit
        override fun e(tag: String, throwable: Throwable?, message: () -> String) = Unit
        override fun i(tag: String, throwable: Throwable?, message: () -> String) = Unit
    }

    private companion object {
        const val REPORTED = """{"name":"Synthetic reported food","calories":780,"protein":35,"fat":38,"fibre":6,"sugar":12}"""
        const val UNKNOWN_ENERGY = """{"name":"Synthetic unknown energy","protein":35,"fat":38,"sugar":0}"""
        const val WEIGHTED = """{"name":"Synthetic weighted food","description":"Synthetic description","calories":780,"protein":35,"fat":38,"carbohydrates":20,"fibre":6,"sugar":0,"servingCount":2.5,"weightGrams":250,"sodium":0.0012,"vitaminC":0.012,"saturatedFats":0}"""
    }
}
