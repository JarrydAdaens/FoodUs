package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.Room
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.ai.domain.*
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.navigation.FoodDiaryCreateQuickAdd
import com.maksimowiczm.foodyou.app.navigation.Home
import com.maksimowiczm.foodyou.app.navigation.ScanQuickAddHandoff
import com.maksimowiczm.foodyou.app.ui.common.utility.EnergyFormatter
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiScanReviewSession
import com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiScanScreen
import com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiScanUiState
import com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiScanViewModel
import com.maksimowiczm.foodyou.common.config.AppConfig
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.food.search.domain.NutritionalItemSearchUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.RoomManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import foodyou.app.generated.resources.*
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.Assert.*
import org.junit.Test
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Production multi-food review, actual handoff helper, editable forms and IN-MEMORY Room
 * database. Global settings and diary repositories are never resolved. The fake scanner returns
 * synthetic JSON, and the only image is a generated solid-color JPEG; no camera or endpoint runs.
 */
@SdkSuppress(minSdkVersion = 30)
class MultiFoodScanTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val date = LocalDate(2026, 1, 2)
    private var activityPackage = "not-created"
    @Volatile private var fixtureCrash: String? = null
    private lateinit var decor: View
    private lateinit var nav: NavHostController
    private lateinit var database: FoodYouDatabase
    private lateinit var repository: ManualDiaryEntryRepository
    private lateinit var model: AiScanViewModel
    private var scannerCalls = 0
    private var acceptedRoute: FoodDiaryCreateQuickAdd? = null

    @Volatile private var insertAttempts = 0
    @Volatile private var failNextChicken = false
    @Volatile private var insertGate: CompletableDeferred<Unit>? = null
    private var repeatFirstOpen = false
    private var completionCount = 0

    @Test fun coherentDishKeepsFullSnapshotAndRapidRepeatedHandoffCreatesOneEntry() {
        repeatFirstOpen = true
        withFlow(PIE) {
            findText("780 kcal")
            acceptSelected(single = true)
            assertField(Res.string.unit_energy, "780")
            assertField(Res.string.nutriment_fiber, "6")
            assertField(Res.string.nutriment_sugars, "12")
            assertField(Res.string.nutriment_carbohydrates, "")
            setField(Res.string.unit_energy, "790")
            insertGate = CompletableDeferred()
            val save = saveControl()
            assertTrue(save.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            await("one blocked insertion") { insertAttempts == 1 }
            assertFalse("Save disabled during insertion", saveControl().isEnabled)
            // A second stale accessibility click cannot trigger another insertion.
            save.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            val back = driver.control(label(Res.string.action_go_back))
            assertFalse("Back disabled during insertion", back.isEnabled)
            back.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            SystemClock.sleep(250)
            assertEquals(0, entries().size)
            assertEquals(1, insertAttempts)
            assertQuickAddRoute()
            assertEquals(0, completionCount)
            onMain { insertGate!!.complete(Unit); Unit }
            expectSavedCount(1)
            val entry = entries().single()
            assertEquals("Synthetic pie", entry.name)
            assertEquals(790.0, entry.nutritionFacts.energy.value)
            assertEquals(35.0, entry.nutritionFacts.proteins.value)
            assertEquals(38.0, entry.nutritionFacts.fats.value)
            assertEquals(6.0, entry.nutritionFacts.dietaryFiber.value)
            assertEquals(12.0, entry.nutritionFacts.sugars.value)
            assertNull(entry.nutritionFacts.carbohydrates.value)
            assertEquals(0.012, entry.nutritionFacts.vitaminC.value)
            assertEquals(2.5, entry.servingCount)
            assertEquals(250.0, entry.weightGrams)
            assertSavedCannotBeReselected("Synthetic pie")
            assertEquals(1, scannerCalls)
            assertCompletedBackStack()
        }
    }

    @Test fun inspectDeselectAndSaveDistinctFoodsWithoutMergingOrLosingValues() = withFlow(MULTIPLE) {
        findText(text(Res.string.neutral_ai_omitted_count, 1))
        // Bread is the first response item; scroll its details action into view before clicking.
        // The 100 kcal assertion below verifies the correct card expanded.
        clickText(label(Res.string.action_ai_show_details))
        findText("100 kcal")
        assertTrue(checkbox("Synthetic pasta").performAction(AccessibilityNodeInfo.ACTION_CLICK))
        findText(label(Res.string.action_save_selected_foods))
        captureQueue("ai-results")
        acceptSelected(count = 2)
        findText(runBlocking { getString(Res.string.quick_add_page, 1, 2) })
        assertField(Res.string.unit_energy, "100")
        captureQueue("queue-first")
        assertField(Res.string.product_name, "Synthetic bread")
        assertField(Res.string.nutriment_sugars, "0")
        clickSave()
        assertField(Res.string.product_name, "Synthetic chicken")
        findText(runBlocking { getString(Res.string.quick_add_page, 2, 2) })
        captureQueue("queue-second")
        assertQuickAddRoute()
        assertEquals(1, entries().size)
        assertEquals(0, completionCount)
        assertField(Res.string.unit_energy, "780")
        assertField(Res.string.nutriment_fiber, "6")
        clickSave()
        expectSavedCount(2)
        val saved = entries()
        assertEquals(setOf("Synthetic bread", "Synthetic chicken"), saved.map { it.name }.toSet())
        assertEquals(2, saved.size)
        assertEquals(2, saved.map { it.id }.distinct().size)
        assertEquals(100.0, saved.single { it.name == "Synthetic bread" }.nutritionFacts.energy.value)
        val chicken = saved.single { it.name == "Synthetic chicken" }
        assertEquals(780.0, chicken.nutritionFacts.energy.value)
        assertEquals(6.0, chicken.nutritionFacts.dietaryFiber.value)
        assertEquals(0.0012, chicken.nutritionFacts.sodium.value)
        assertSavedCannotBeReselected("Synthetic bread")
        assertSavedCannotBeReselected("Synthetic chicken")
        val review = review()
        val pasta = review.items.single { it.estimate.name == "Synthetic pasta" }
        assertFalse(pasta.id in review.savedIds)
        assertTrue(pasta.id in review.select(pasta.id, true).selectedIds)
        assertEquals(2, insertAttempts)
        assertCompletedBackStack()
    }

    @Test fun cancelUnsavedEditsThenRetryLaterFailureKeepsExactPartialCount() = withFlow(MULTIPLE) {
        acceptSelected(count = 3)
        clickSave()
        assertField(Res.string.product_name, "Synthetic pasta")
        setField(Res.string.product_name, "Canceled synthetic edits")
        clickText(label(Res.string.action_go_back))
        clickText(label(Res.string.action_discard))
        expectSavedCount(1)
        assertEquals(listOf("Synthetic bread"), entries().map { it.name })
        // Duplicate and old-generation results must not imply an empty completed queue.
        listOf(review().savedIds.single(), "old-generation:0").forEach { staleId ->
            onMain { nav.currentBackStackEntry!!.savedStateHandle[ScanQuickAddHandoff.SAVED_ITEM_KEY] = staleId }
            await("stale acknowledgement consumed") {
                onMain { nav.currentBackStackEntry?.savedStateHandle
                    ?.get<String>(ScanQuickAddHandoff.SAVED_ITEM_KEY) == null }
            }
            assertTrue(onMain { nav.currentDestination?.route == SCAN })
            assertEquals(0, completionCount)
            assertEquals(1, review().savedIds.size)
        }
        assertTrue(checkbox("Synthetic pasta").performAction(AccessibilityNodeInfo.ACTION_CLICK))
        failNextChicken = true
        acceptSelected(count = 1)
        assertField(Res.string.product_name, "Synthetic chicken")
        clickSave()
        findText(label(Res.string.error_quick_add_save))
        assertEquals(1, entries().size)
        assertField(Res.string.unit_energy, "780")
        assertQuickAddRoute()
        assertEquals(0, completionCount)
        assertTrue("Failed item remains retryable", saveControl().isEnabled)
        clickSave()
        expectSavedCount(2)
        assertEquals(setOf("Synthetic bread", "Synthetic chicken"), entries().map { it.name }.toSet())
        assertEquals(2, entries().size)
        assertEquals(3, insertAttempts)
        assertEquals(1, scannerCalls)
        assertCompletedBackStack()
    }

    @Test fun singleSaveFailureKeepsEditedValuesAndRetryCompletesOnlyAfterPersistence() = withFlow(PIE) {
        acceptSelected(single = true)
        setField(Res.string.product_name, "Synthetic chicken")
        setField(Res.string.unit_energy, "791")
        failNextChicken = true
        clickSave()
        findText(label(Res.string.error_quick_add_save))
        assertField(Res.string.product_name, "Synthetic chicken")
        assertField(Res.string.unit_energy, "791")
        assertTrue(saveControl().isEnabled)
        assertEquals(0, entries().size)
        assertEquals(0, completionCount)
        assertQuickAddRoute()
        setField(Res.string.unit_energy, "792")
        clickSave()
        expectSavedCount(1)
        val saved = entries().single()
        assertEquals("Synthetic chicken", saved.name)
        assertEquals(792.0, saved.nutritionFacts.energy.value)
        assertEquals(6.0, saved.nutritionFacts.dietaryFiber.value)
        assertEquals(2, insertAttempts)
        assertCompletedBackStack()
    }

    @Test fun directQuickAddSaveReturnsToItsOwnPredecessorWithoutCompletingScan() =
        withFlow(PIE, direct = true) {
            clickText(OPEN_DIRECT)
            assertField(Res.string.product_name, "Synthetic direct food")
            clickSave()
            findText(DIRECT_PREDECESSOR)
            await("direct form popped") { onMain { nav.currentDestination?.route == DIRECT_PREDECESSOR } }
            val saved = entries().single()
            assertEquals("Synthetic direct food", saved.name)
            assertEquals(1, insertAttempts)
            assertEquals(0, scannerCalls)
            assertEquals(0, completionCount)
            assertTrue(onMain { nav.previousBackStackEntry?.destination?.hasRoute<Home>() == true })
            assertTrue(onMain { nav.popBackStack() })
            findText(HOME)
            assertTrue(onMain { nav.currentDestination?.hasRoute<Home>() == true })
            assertNoScanOrAddEntries()
        }

    @Test fun emptyResponseShowsRetryableFailureWithoutSavingOrOpeningAForm() =
        withFlow("""{"foods":[]}""", expectResult = false) {
            findText("Could not understand the AI response.")
            assertTrue(driver.control(label(Res.string.action_submit)).isEnabled)
            assertEquals(0, entries().size)
            assertNull(acceptedRoute)
            assertEquals(1, scannerCalls)
        }

    private fun withFlow(json: String, expectResult: Boolean = true, direct: Boolean = false, checkFlow: () -> Unit) {
        val applicationKoin = GlobalContext.get()
        database = Room.inMemoryDatabaseBuilder(instrumentation.targetContext, FoodYouDatabase::class.java).build()
        val roomRepository = RoomManualDiaryEntryRepository(database.manualDiaryEntryDao)
        repository = object : ManualDiaryEntryRepository by roomRepository {
            override suspend fun insert(name: String, diarySlotId: Long, date: LocalDate,
                nutritionFacts: NutritionFacts, createdAt: LocalDateTime, description: String?,
                isPlaceholder: Boolean, servingCount: Double?, weightGrams: Double?,
                capturedPortion: com.maksimowiczm.foodyou.common.domain.measurement.Measurement?): ManualDiaryEntryId {
                insertAttempts++
                insertGate?.await()
                if (name == "Synthetic chicken" && failNextChicken) {
                    failNextChicken = false
                    throw IllegalStateException("Synthetic insert failure")
                }
                return roomRepository.insert(name, diarySlotId, date, nutritionFacts, createdAt,
                    description, isPlaceholder, servingCount, weightGrams, capturedPortion)
            }
        }
        insertAttempts = 0
        runBlocking {
            database.diarySlotDao.insertWithLastRank(DiarySlotEntity(1, "Synthetic meal", 0, 0, 23, 59))
        }
        scannerCalls = 0
        completionCount = 0
        acceptedRoute = null
        val config = object : AppConfig by applicationKoin.get<AppConfig>() {
            override val aiApiKey = ""
            override val aiEndpoint = ""
            override val aiModel = ""
        }
        val preferences = object : UserPreferencesRepository<AiSettings> {
            override fun observe() = flowOf(AiSettings("synthetic-dummy-key", null, null, null))
            override suspend fun update(transform: AiSettings.() -> AiSettings) = error("No settings writes")
        }
        val scanner = object : AiFoodScanner {
            override suspend fun scan(jpeg: ByteArray, hint: String?): AiScanResult {
                scannerCalls++
                check(jpeg.contentEquals(syntheticJpeg)) { "Only the generated image is permitted" }
                return AiFoodEstimateCollectionParser.parse(json)
                    ?: AiScanResult.Failure("Could not understand the AI response.")
            }
        }
        model = AiScanViewModel(scanner, applicationKoin.get<NutritionalItemSearchUseCase>(), ObserveAiConfigured(preferences, config))
        val clock = applicationKoin.get<DateProvider>()
        val isolated = koinApplication {
            modules(module {
                viewModel { model }
                viewModel { (entryDate: LocalDate, diarySlotId: Long) -> CreateQuickAddViewModel(diarySlotId, entryDate, repository, clock) }
            })
        }
        val automation = instrumentation.uiAutomation
        val originalServiceInfo = automation.serviceInfo
        val originalServiceFlags = originalServiceInfo.flags
        val originalExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        fixtureCrash = null
        val fixtureExceptionHandler = Thread.UncaughtExceptionHandler { thread, exception ->
            // In-memory diagnostic only: deliberately omit exception messages and arguments.
            fixtureCrash = buildString {
                append(exception.javaClass.name)
                exception.stackTrace.take(12).forEach { frame ->
                    append("[${frame.className}.${frame.methodName}:${frame.lineNumber}]")
                }
            }
            originalExceptionHandler?.uncaughtException(thread, exception)
        }
        try {
            Thread.setDefaultUncaughtExceptionHandler(fixtureExceptionHandler)
            automation.serviceInfo = automation.serviceInfo.apply {
                flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            }
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activityPackage = activity.packageName
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    activity.setContent {
                        KoinIsolatedContext(context = isolated) {
                            MaterialTheme {
                                CompositionLocalProvider(LocalEnergyFormatter provides EnergyFormatter.kilocalories) {
                                    FixtureHost(direct)
                                }
                            }
                        }
                    }
                    if (!direct) {
                        model.onPhotoCaptured(syntheticJpeg)
                    }
                }
                if (!direct) {
                    await("synthetic scanner configured") { model.aiConfigured.value }
                    onMain { model.submit() }
                    await("synthetic scan result") {
                        if (expectResult) model.state.value is AiScanUiState.Result
                        else model.state.value is AiScanUiState.Failed
                    }
                }
                instrumentation.waitForIdleSync()
                checkFlow()
            }
        } finally {
            insertGate?.complete(Unit)
            Thread.setDefaultUncaughtExceptionHandler(originalExceptionHandler)
            isolated.close()
            database.close()
            automation.serviceInfo = originalServiceInfo.apply { flags = originalServiceFlags }
            assertSame("Global application Koin must survive", applicationKoin, GlobalContext.get())
        }
    }

    @Composable private fun FixtureHost(direct: Boolean) {
        nav = rememberNavController()
        LaunchedEffect(Unit) { nav.navigate(if (direct) DIRECT_PREDECESSOR else SCAN) }
        NavHost(nav, startDestination = Home) {
            composable<Home> {
                // Canonical production destination, with synthetic readback instead of owner Home data.
                val entries by repository.observeAll(1, date).collectAsState(initial = emptyList())
                Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                    Text(HOME)
                    Text(text(Res.string.neutral_ai_saved_count, entries.size))
                    entries.forEach { Text(it.name) }
                }
            }
            composable(DIRECT_PREDECESSOR) {
                Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                    Text(DIRECT_PREDECESSOR)
                    Button(onClick = { ScanQuickAddHandoff.open(nav,
                        FoodDiaryCreateQuickAdd(date.toEpochDays().toLong(), 1,
                            prefillName = "Synthetic direct food", prefillEnergyKcal = 100.0)) }) {
                        Text(OPEN_DIRECT)
                    }
                }
            }
            composable(SCAN) { entry ->
                val savedId by entry.savedStateHandle.getStateFlow<String?>(ScanQuickAddHandoff.SAVED_ITEM_KEY, null)
                    .collectAsStateWithLifecycle()
                AiScanScreen(
                    onBack = { error("Not used") },
                    savedScanItemId = savedId,
                    onSavedScanItemConsumed = { entry.savedStateHandle[ScanQuickAddHandoff.SAVED_ITEM_KEY] = null },
                    onScanCompleted = {
                        // Prove persistence exists before navigation success is signalled.
                        assertEquals(review().savedIds.size, entries().size)
                        completionCount++
                        ScanQuickAddHandoff.complete(nav)
                    },
                    onSaveToQuickAdd = { estimate, itemId ->
                        val route = ScanQuickAddHandoff.route(date.toEpochDays().toLong(), 1, estimate, itemId, review().pageIndex, review().queueSize)
                        acceptedRoute = route
                        ScanQuickAddHandoff.open(nav, route)
                        // Reproduce two review callbacks before navigation renders, using production helper.
                        if (repeatFirstOpen) {
                            repeatFirstOpen = false
                            ScanQuickAddHandoff.open(nav, route)
                        }
                    },
                    onAlignPickFood = { error("No global food lookup") },
                    onCreateCustomFood = { error("No global product writes") },
                )
            }
            composable<FoodDiaryCreateQuickAdd> { entry ->
                val route = entry.toRoute<FoodDiaryCreateQuickAdd>()
                val estimate = route.scanEstimateJson?.let(AiFoodEstimateParser::parse)
                CreateQuickAddScreen(onBack = { nav.popBackStack() },
                    onSave = { ScanQuickAddHandoff.saved(nav, route) },
                    diarySlotId = route.diarySlotId, date = LocalDate.fromEpochDays(route.epochDay), scanEstimate = estimate,
                    prefillName = route.prefillName, prefillEnergyKcal = route.prefillEnergyKcal,
                    scanPageIndex = route.scanPageIndex, scanPageCount = route.scanPageCount)
            }
        }
    }

    private fun captureQueue(name: String) {
        val folder = java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "story40").apply { mkdirs() }
        java.io.FileOutputStream(java.io.File(folder, "$name.png")).use {
            assertTrue(requireNotNull(instrumentation.uiAutomation.takeScreenshot()).compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }

    private fun acceptSelected(single: Boolean = false, count: Int = 1) {
        clickText(label(Res.string.action_save_selected_foods))
        findText(label(Res.string.headline_quick_add))
        assertNotNull(requireNotNull(acceptedRoute).scanEstimateJson)
        assertNotNull(requireNotNull(acceptedRoute).scanItemId)
    }

    private fun clickSave() = clickText(label(Res.string.action_save))

    private fun entries(): List<ManualDiaryEntry> = runBlocking {
        withTimeout(5_000) { repository.observeAll(1, date).first() }
    }

    private fun review(): AiScanReviewSession = (model.state.value as AiScanUiState.Result).review

    private fun assertSavedCannotBeReselected(name: String) {
        val review = review()
        val id = review.items.single { it.estimate.name == name }.id
        assertTrue(id in review.savedIds)
        assertFalse(id in review.selectedIds)
        assertEquals("Saved item cannot be reselected", review, review.select(id, true))
    }

    private fun assertQuickAddRoute() {
        assertTrue(onMain { nav.currentDestination?.hasRoute<FoodDiaryCreateQuickAdd>() == true })
    }

    private fun assertNoScanOrAddEntries() {
        assertTrue(onMain { runCatching { nav.getBackStackEntry(SCAN) }.isFailure })
        assertTrue(onMain { runCatching { nav.getBackStackEntry<FoodDiaryCreateQuickAdd>() }.isFailure })
    }

    private fun assertCompletedBackStack() {
        findText(HOME)
        assertEquals(1, completionCount)
        assertTrue(onMain { nav.currentDestination?.hasRoute<Home>() == true })
        assertTrue(onMain { nav.previousBackStackEntry == null })
        assertNoScanOrAddEntries()
        // This is the same pop operation used by Back. No completed workflow can be reopened.
        assertFalse(onMain { nav.popBackStack() })
        assertNoScanOrAddEntries()
    }

    private fun expectSavedCount(count: Int) {
        findText(text(Res.string.neutral_ai_saved_count, count))
        await("exact isolated diary count $count") { entries().size == count }
    }

    private fun text(resource: StringResource, count: Int) = runBlocking { getString(resource, count) }

    private fun checkbox(name: String): AccessibilityNodeInfo {
        var node: AccessibilityNodeInfo? = findText(name)
        while (node != null) {
            descendants(node).firstOrNull { it.isCheckable }?.let { return it }
            node = node.parent
        }
        error("Missing synthetic item checkbox")
    }

    private fun saveControl(): AccessibilityNodeInfo = driver.control(label(Res.string.action_save))

    private val driver = ScanTestDriver(instrumentation,
        { if (::decor.isInitialized) decor else null }, { activityPackage }, { fixtureCrash })
    private fun assertField(resource: StringResource, expected: String) = driver.awaitField(resource, expected)
    private fun setField(resource: StringResource, value: String) = driver.setField(resource, value)
    private fun findText(text: String) = driver.findText(text)
    private fun clickText(text: String) = driver.clickText(text)
    private fun label(resource: StringResource) = driver.label(resource)
    private fun await(description: String, condition: () -> Boolean) = driver.await(description, condition)
    private fun descendants(node: AccessibilityNodeInfo) = driver.descendants(node)
    private fun <T> onMain(block: () -> T) = driver.onMain(block)

    private val syntheticJpeg by lazy {
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.GREEN)
        ByteArrayOutputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output))
            bitmap.recycle()
            output.toByteArray()
        }
    }

    private companion object {
        const val SCAN = "synthetic-multi-scan"
        const val HOME = "Synthetic Home Log"
        const val DIRECT_PREDECESSOR = "Synthetic direct predecessor"
        const val OPEN_DIRECT = "Open synthetic direct Quick Add"
        const val PIE = """{"foods":[{"name":"Synthetic pie","calories":780,"protein":35,"fat":38,"fibre":6,"sugar":12,"vitaminC":0.012,"servingCount":2.5,"weightGrams":250}]}"""
        const val MULTIPLE = """{"foods":[{"name":"Synthetic bread","calories":100,"sugar":0},{"name":"Synthetic pasta","calories":200},{"name":"Synthetic chicken","calories":780,"protein":35,"fat":38,"fibre":6,"sugar":12,"sodium":0.0012},{}]}"""
    }
}
