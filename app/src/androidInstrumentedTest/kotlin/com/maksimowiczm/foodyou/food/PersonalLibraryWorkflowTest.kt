package com.maksimowiczm.foodyou.food

import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.os.ParcelFileDescriptor
import android.graphics.Bitmap
import android.graphics.Rect
import android.view.View
import android.view.WindowManager
import androidx.test.filters.SdkSuppress
import androidx.test.core.app.launchActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.Lifecycle
import androidx.room.Room
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.app.ui.food.product.ProductFormState
import com.maksimowiczm.foodyou.app.ui.food.product.rememberProductFormState
import com.maksimowiczm.foodyou.app.ui.food.product.create.CreateProductScreen
import com.maksimowiczm.foodyou.app.ui.food.product.create.CreateProductViewModel
import com.maksimowiczm.foodyou.food.domain.usecase.CreateProductUseCase
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.app.ui.shell.FoodUsAppShell
import com.maksimowiczm.foodyou.app.ui.theme.FoodYouTheme
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext
import java.util.UUID

/** Actual production shell, DI, Settings and navigation. Mutating fixture refuses physical devices. */
@SdkSuppress(minSdkVersion = 30)
class PersonalLibraryWorkflowTest {
    @Before fun prepare() = prepareSyntheticLibraryHost()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private val driver = ScanTestDriver(instrumentation, { decor }, { instrumentation.targetContext.packageName }, { null })
    private val prefix = "Synthetic S38 " + UUID.randomUUID().toString().take(6)
    private fun label(id: StringResource) = runBlocking { getString(id) }
    private fun click(id: StringResource) = driver.clickText(label(id))
    private fun setFoodQuery(value: String) {
        fun input(): android.view.accessibility.AccessibilityNodeInfo? {
            var current = driver.findText(label(Res.string.action_go_back))
            repeat(32) { current.parent?.let { current = it } }
            return driver.descendants(current).filter { it.isVisibleToUser && it.isEditable }.toList().singleOrNull()
        }
        driver.await("Food search is the sole visible editable field after navigation") { input() != null }
        assertTrue(requireNotNull(input()).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT,
            android.os.Bundle().apply { putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value) }))
        driver.await("Food search field contains synthetic query") { input()?.text?.toString() == value }
        assertTrue("Submit the existing food search", requireNotNull(input()).performAction(
            android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id))
        instrumentation.waitForIdleSync()
    }

    private fun fillRequiredProductNutrition() {
        driver.setField(Res.string.nutriment_proteins, "8")
        driver.setField(Res.string.nutriment_carbohydrates, "15")
        driver.setField(Res.string.nutriment_fats, "2")
    }

    private fun shell(command: String): String = ParcelFileDescriptor.AutoCloseInputStream(
        instrumentation.uiAutomation.executeShellCommand(command)).bufferedReader().use { it.readText().trim() }

    private fun diaryCounts(db: FoodYouDatabase): List<Long> = android.database.sqlite.SQLiteDatabase.openDatabase(
        instrumentation.targetContext.getDatabasePath(requireNotNull(db.openHelper.databaseName)).path,
        null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY).use { sql ->
        listOf("Measurement", "ManualDiaryEntry", "DiaryProduct", "DiaryRecipe", "DiaryRecipeIngredient", "DiaryReportedRecipeComponent").map { table ->
            sql.rawQuery("SELECT COUNT(*) FROM $table", null).use { cursor -> cursor.moveToFirst(); cursor.getLong(0) }
        }
    }

    @Test fun lightDarkLargeTextAndNarrowLibraryKeepLastRowAboveCreateAndRetainScroll() {
        assumeTrue(Build.FINGERPRINT.contains("generic") || Build.MODEL.contains("sdk_gphone"))
        val koin = GlobalContext.get()
        val products = koin.get<ProductRepository>()
        val recipes = koin.get<RecipeRepository>()
        val last = "$prefix Recipe 11"
        runBlocking {
            val foodId = products.insertProduct("$prefix base", null, null, null, false, null, null,
                ProductSource(ProductSource.Type.User), NutritionFacts(energy = NutrientValue.Complete(120.0)))
            val source = products.observeProduct(foodId).first()!!
            repeat(12) { recipes.insertRecipe("$prefix Recipe " + it.toString().padStart(2, '0'), 2, null, false,
                listOf(RecipeIngredient(source, Measurement.Gram(100.0)))) }
        }
        val info = instrumentation.uiAutomation.serviceInfo
        val flags = info.flags
        val oldScale = shell("settings get system font_scale").toFloatOrNull() ?: 1f
        val oldHardwareIme = shell("settings get secure show_ime_with_hard_keyboard").toIntOrNull()
        shell("settings put secure show_ime_with_hard_keyboard 1")
        val oldNight = shell("cmd uimode night").let { if(it.contains("yes")) "yes" else if(it.contains("no")) "no" else "auto" }
        instrumentation.uiAutomation.serviceInfo = info.apply { this.flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS }
        try {
            for ((night, scale) in listOf("no" to 1f, "yes" to 1.5f)) {
                val preferences: com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository<com.maksimowiczm.foodyou.settings.domain.entity.Settings> =
                    koin.get(org.koin.core.qualifier.named(com.maksimowiczm.foodyou.settings.domain.entity.Settings::class.qualifiedName!!))
                runBlocking { preferences.update { copy(librarySortOrder = com.maksimowiczm.foodyou.settings.domain.entity.LibrarySortOrder.Alphabetical) } }
                shell("cmd uimode night $night")
                shell("settings put system font_scale $scale")
                if(scale > 1f) shell("wm size 900x2000")
                launchActivity<DeveloperActivity>().use { scenario ->
                    scenario.onActivity { activity ->
                        decor = activity.window.decorView
                        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        activity.setContent { FoodYouTheme { FoodUsAppShell({}) } }
                        assertEquals(scale, activity.resources.configuration.fontScale, 0.01f)
                    }
                    click(Res.string.action_go_to_settings)
                    click(Res.string.library_title)
                    val sortLabel = runBlocking { getString(Res.string.library_sort_label, getString(Res.string.library_sort_az)) }
                    driver.clickText(sortLabel)
                    click(Res.string.library_sort_lowest)
                    driver.setField(Res.string.library_search, prefix)
                    val tabHeights = listOf(Res.string.library_recipes, Res.string.library_meals, Res.string.library_products).map {
                        Rect().also(driver.findText(label(it))::getBoundsInScreen).height()
                    }
                    assertTrue("Tab labels stay on one line at enlarged text", tabHeights.max() - tabHeights.min() <= 2)
                    val directory = java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "story38-appearance").apply { mkdirs() }
                    java.io.FileOutputStream(java.io.File(directory, "library-start-$night-$scale.png")).use {
                        assertTrue(requireNotNull(instrumentation.uiAutomation.takeScreenshot()).compress(Bitmap.CompressFormat.PNG, 100, it))
                    }
                    val lastNode = driver.findText(last)
                    val lastBounds = Rect().also(lastNode::getBoundsInScreen)
                    val createBounds = Rect().also(driver.control(label(Res.string.library_create_recipe))::getBoundsInScreen)
                    assertTrue("Last row is visible above creation action", lastBounds.bottom <= createBounds.top)
                    java.io.FileOutputStream(java.io.File(directory, "library-$night-$scale.png")).use {
                        assertTrue(requireNotNull(instrumentation.uiAutomation.takeScreenshot()).compress(Bitmap.CompressFormat.PNG, 100, it))
                    }
                    var queryRoot = driver.findText(label(Res.string.library_title))
                    repeat(32) { queryRoot.parent?.let { queryRoot = it } }
                    val input = driver.descendants(queryRoot).filter { it.isVisibleToUser && it.isEditable }.toList().single()
                    input.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_FOCUS)
                    input.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                    instrumentation.runOnMainSync { decor?.windowInsetsController?.show(android.view.WindowInsets.Type.ime()) }
                    driver.await("Library keyboard is visible") {
                        var visible = false
                        instrumentation.runOnMainSync { visible = decor?.rootWindowInsets?.isVisible(android.view.WindowInsets.Type.ime()) == true }
                        visible
                    }
                    val keyboardCreateBounds = Rect().also(driver.control(label(Res.string.library_create_recipe))::getBoundsInScreen)
                    var imeTop = 0
                    instrumentation.runOnMainSync {
                        val view = requireNotNull(decor)
                        val position = IntArray(2).also(view::getLocationOnScreen)
                        imeTop = position[1] + view.height - view.rootWindowInsets.getInsets(android.view.WindowInsets.Type.ime()).bottom
                    }
                    assertTrue("Creation action stays above the keyboard", keyboardCreateBounds.bottom <= imeTop)
                    java.io.FileOutputStream(java.io.File(directory, "library-ime-$night-$scale.png")).use {
                        assertTrue(requireNotNull(instrumentation.uiAutomation.takeScreenshot()).compress(Bitmap.CompressFormat.PNG, 100, it))
                    }
                    instrumentation.runOnMainSync { decor?.windowInsetsController?.hide(android.view.WindowInsets.Type.ime()) }
                    driver.clickText(last)
                    driver.findText(label(Res.string.recipe_name))
                    driver.setField(Res.string.headline_note, "Synthetic edited note $night")
                    Thread.sleep(5600)
                    click(Res.string.action_save)
                    driver.findText(label(Res.string.library_title))
                    driver.assertField(Res.string.library_search, prefix)
                    val header = driver.findText(label(Res.string.library_title))
                    var root = header
                    repeat(32) { root.parent?.let { root = it } }
                    assertTrue("Scrolled row survives a long editor round trip", driver.descendants(root).any {
                        it.isVisibleToUser && it.text?.toString()?.lines()?.contains(last) == true
                    })
                }
                shell("wm size reset")
            }
        } finally {
            shell("wm size reset"); shell("settings put system font_scale $oldScale"); shell("cmd uimode night $oldNight")
            if(oldHardwareIme == null) shell("settings delete secure show_ime_with_hard_keyboard") else shell("settings put secure show_ime_with_hard_keyboard $oldHardwareIme")
            decor = null
            instrumentation.uiAutomation.serviceInfo = info.apply { this.flags = flags }
        }
    }

    @Test fun delayedProductSaveCapturesOneDraftAndRejectsRepeatedSubmission() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(instrumentation.targetContext, FoodYouDatabase::class.java).build()
        val products = RoomProductRepository(db.productDao)
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        var writes = 0
        var form: ProductFormState? = null
        val delayed = object : ProductRepository by products {
            override suspend fun insertProduct(name: String, brand: String?, barcode: String?, note: String?, isLiquid: Boolean,
                packageWeight: Double?, servingWeight: Double?, source: ProductSource, nutritionFacts: NutritionFacts,
                sourceRecordId: String?): NutritionalItemId.Product {
                writes++; entered.complete(Unit); release.await()
                return products.insertProduct(name, brand, barcode, note, isLiquid, packageWeight, servingWeight, source, nutritionFacts, sourceRecordId)
            }
        }
        val model = withContext(Dispatchers.Main) { CreateProductViewModel(
            CreateProductUseCase(delayed, RoomNutritionalItemHistoryRepository(db.foodEventDao), db, ProductTestLogger), ProductTestClock) }
        val info = instrumentation.uiAutomation.serviceInfo
        val flags = info.flags
        instrumentation.uiAutomation.serviceInfo = info.apply { this.flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS }
        try {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.setContent {
                        val draft = rememberProductFormState()
                        SideEffect { form = draft }
                        MaterialTheme { CreateProductScreen(draft, {}, model::createProduct, {}) }
                    }
                }
                driver.setField(Res.string.product_name, "$prefix captured")
                fillRequiredProductNutrition()
                assertTrue("Synthetic Product form is valid before delayed Save", requireNotNull(form).isValid)
                driver.setField(Res.string.headline_source, "https://example.invalid/source")
                val event = async { model.events.first() }
                withContext(Dispatchers.Main) {
                    requireNotNull(form).apply { sourceTypeLocked = true; sourceType = ProductSource.Type.USDA }
                    model.createProduct(requireNotNull(form)); model.createProduct(requireNotNull(form))
                }
                withTimeout(5000) { entered.await() }
                assertTrue(requireNotNull(form).saving)
                driver.setField(Res.string.product_name, "$prefix later edit")
                release.complete(Unit)
                withTimeout(5000) { event.await() }
                assertEquals(1, writes)
                assertEquals("$prefix captured", products.observeLibraryNames().first().single().name)
                val saved = products.observeProducts(10, 0).first().single()
                assertEquals(ProductSource.Type.User, saved.source.type)
                assertEquals("https://example.invalid/source", saved.source.url)
            }
        } finally {
            withContext(Dispatchers.Main) { model.viewModelScope.cancel() }
            decor = null; db.close()
            instrumentation.uiAutomation.serviceInfo = info.apply { this.flags = flags }
        }
    }

    @Test fun productionSettingsSearchCreateEachDefinitionAndReturnNeverLogsImplicitly() {
        assumeTrue(Build.FINGERPRINT.contains("generic") || Build.MODEL.contains("sdk_gphone"))
        val koin = GlobalContext.get()
        val db = koin.get<FoodYouDatabase>()
        val products = koin.get<ProductRepository>()
        val recipes = koin.get<RecipeRepository>()
        val meals = koin.get<MealRepository>()
        val manual = koin.get<ManualDiaryEntryRepository>()
        val diary = koin.get<NutritionalItemDiaryEntryRepository>()
        val historyDay = LocalDate(2026, 1, 1)
        val slot = 83801L
        val foodName = "$prefix ingredient"
        val recipeName = "$prefix recipe"
        val newFood = "$prefix created Product"
        val newRecipe = "$prefix created Recipe"
        val newMeal = "$prefix created Meal"
        val facts = NutritionFacts(energy = NutrientValue.Complete(120.0), proteins = NutrientValue.Complete(8.0))
        runBlocking {
            if(koin.get<DiarySlotRepository>().observeDiarySlots().first().none { it.id == slot })
                db.diarySlotDao.insertWithLastRank(DiarySlotEntity(slot, "Synthetic library destination", 0, 0, 23, 59))
            val id = products.insertProduct(foodName, null, null, null, false, null, null, ProductSource(ProductSource.Type.User), facts)
            val source = products.observeProduct(id).first()!!
            recipes.insertRecipe(recipeName, 2, null, false, listOf(RecipeIngredient(source, Measurement.Gram(100.0))))
            manual.insert("$prefix historical", slot, historyDay, facts, ProductTestClock.now())
        }
        val beforeManual = runBlocking { manual.observeAll(slot, historyDay).first() }
        val beforeFoods = runBlocking { diary.observeAll(slot, historyDay).first() }
        val beforeCounts = diaryCounts(db)
        val info = instrumentation.uiAutomation.serviceInfo
        val oldFlags = info.flags
        instrumentation.uiAutomation.serviceInfo = info.apply { flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS }
        try {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.setContent { FoodYouTheme { FoodUsAppShell({}) } }
                }
                click(Res.string.action_go_to_settings)
                val goal = driver.findText(label(Res.string.headline_daily_goals))
                val library = driver.findText(label(Res.string.library_title))
                val goalBounds = android.graphics.Rect().also(goal::getBoundsInScreen)
                val libraryBounds = android.graphics.Rect().also(library::getBoundsInScreen)
                assertTrue("Library follows Daily goals", libraryBounds.top >= goalBounds.bottom)
                click(Res.string.library_title)
                driver.findText(label(Res.string.library_create_recipe))
                driver.setField(Res.string.library_search, prefix)
                driver.clickText(recipeName)
                driver.findText(label(Res.string.recipe_name))
                click(Res.string.action_go_back)
                driver.assertField(Res.string.library_search, prefix)
                driver.setField(Res.string.library_search, "$prefix no matches")
                driver.findText(label(Res.string.library_no_results))
                click(Res.string.library_products)
                driver.assertField(Res.string.library_search, "$prefix no matches")
                driver.setField(Res.string.library_search, prefix)
                driver.clickText(foodName)
                driver.setField(Res.string.product_name, "$prefix unsaved edit")
                scenario.moveToState(Lifecycle.State.CREATED)
                Thread.sleep(2500)
                scenario.moveToState(Lifecycle.State.RESUMED)
                driver.assertField(Res.string.product_name, "$prefix unsaved edit")
                click(Res.string.action_go_back)
                click(Res.string.action_discard)
                driver.assertField(Res.string.library_search, prefix)

                click(Res.string.library_create_product)
                driver.setField(Res.string.product_name, newFood)
                fillRequiredProductNutrition()
                click(Res.string.action_save)
                driver.findText(label(Res.string.library_title))
                driver.findText(newFood)
                assertEquals(1, runBlocking { products.observeLibraryNames().first().count { it.name == newFood } })
                driver.assertField(Res.string.library_search, prefix)
                click(Res.string.library_create_product)
                driver.findText(label(Res.string.headline_create_product))
                click(Res.string.action_go_back)
                driver.findText(label(Res.string.library_create_product))

                click(Res.string.library_recipes)
                click(Res.string.library_create_recipe)
                driver.setField(Res.string.recipe_name, newRecipe)
                driver.setField(Res.string.recipe_servings, "2")
                click(Res.string.action_add_ingredient)
                setFoodQuery(prefix)
                driver.clickText(foodName)
                click(Res.string.action_save)
                driver.findText(label(Res.string.action_add_ingredient))
                click(Res.string.action_save)
                driver.findText(label(Res.string.library_title))
                driver.findText(newRecipe)
                assertEquals(1, runBlocking { recipes.observeLibraryNames().first().count { it.name == newRecipe } })

                click(Res.string.library_meals)
                click(Res.string.library_create_meal)
                driver.setField(Res.string.label_meal_name, newMeal)
                click(Res.string.meals_add_item)
                setFoodQuery(prefix)
                driver.clickText(foodName)
                click(Res.string.action_save)
                driver.findText(label(Res.string.label_meal_name))
                click(Res.string.action_save)
                driver.findText(label(Res.string.library_title))
                driver.findText(newMeal)
                assertEquals(1, runBlocking { meals.observeAll().first().count { it.name == newMeal } })
                assertEquals(beforeManual, runBlocking { manual.observeAll(slot, historyDay).first() })
                assertEquals(beforeFoods, runBlocking { diary.observeAll(slot, historyDay).first() })
                assertEquals("Definition management never adds a diary record on any day", beforeCounts, diaryCounts(db))
                click(Res.string.action_load_meal)
                driver.findText(label(Res.string.library_choose_day))
                driver.findText(label(Res.string.library_choose_slot))
                click(Res.string.action_cancel)
                click(Res.string.action_go_back)
                driver.findText(label(Res.string.headline_daily_goals))
            }
        } finally {
            decor = null
            instrumentation.uiAutomation.serviceInfo = info.apply { flags = oldFlags }
        }
    }
}
