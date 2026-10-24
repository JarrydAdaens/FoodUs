package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Bitmap
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import com.maksimowiczm.foodyou.app.ui.common.utility.EnergyFormatter
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiScanScreen
import com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiScanUiState
import com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiScanViewModel
import com.maksimowiczm.foodyou.app.ui.food.shared.component.KnownNutrientList
import com.maksimowiczm.foodyou.common.config.AppConfig
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.food.search.domain.FoodSearchUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.RoomManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.MealEntity
import foodyou.app.generated.resources.*
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.Assert.*
import org.junit.Test
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Production scan review, typed route, create/edit screens and repository over an IN-MEMORY Room
 * database. Global settings and diary repositories are never resolved. The fake scanner returns
 * synthetic JSON, and the only image is a generated solid-color JPEG; no camera or endpoint runs.
 */
@SdkSuppress(minSdkVersion = 30)
class ScanDataPreservationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val date = LocalDate(2026, 1, 2)
    private var activityPackage = "not-created"
    @Volatile private var fixtureCrash: String? = null
    private lateinit var decor: View
    private lateinit var nav: NavHostController
    private lateinit var database: FoodYouDatabase
    private lateinit var repository: ManualDiaryEntryRepository
    private var scannerCalls = 0
    private var acceptedRoute: FoodDiaryCreateQuickAdd? = null

    @Test fun reported780SampleSurvivesReviewQuickAddSaveAndHistoricalReadback() =
        withFlow(REPORTED) {
            findText("780 kcal")
            findText("35 g")
            findText("38 g")
            findText("6 g")
            findText("12 g")
            acceptReview()
            assertField(Res.string.nutriment_proteins, "35")
            assertField(Res.string.nutriment_fats, "38")
            assertField(Res.string.nutriment_carbohydrates, "")
            assertField(Res.string.nutriment_fiber, "6")
            assertField(Res.string.nutriment_sugars, "12")
            assertField(Res.string.unit_energy, "780")
            save()
            val first = savedEntry()
            assertSample(first)
            clickText(EDIT)
            assertField(Res.string.unit_energy, "780")
            assertField(Res.string.nutriment_carbohydrates, "")
            assertField(Res.string.label_number_of_servings, "")
            assertField(Res.string.weight, "")
            save()
            val reopened = savedEntry()
            assertSample(reopened)
            assertEquals(first.id, reopened.id)
            assertEquals(first.nutritionFacts, reopened.nutritionFacts)
            assertEquals(1, scannerCalls)
        }

    @Test fun optionalNutrientsAndPortionSurviveKilojouleFormAndMetadataEditWithoutScaling() =
        withFlow(OPTIONALS, kilojoules = true) {
            findText("Synthetic optional food")
            findText("Synthetic description")
            findText("1.2 mg")
            acceptReview()
            assertField(Res.string.label_description, "Synthetic description")
            assertField(Res.string.label_number_of_servings, "2.5")
            assertField(Res.string.weight, "250")
            assertField(Res.string.nutriment_carbohydrates, "0")
            assertField(Res.string.nutriment_sugars, "0")
            assertField(Res.string.unit_energy, "3263.52")
            setField(Res.string.nutriment_sugars, "NaN")
            await("Save disabled for non-finite sugar") {
                !saveControl().isEnabled
            }
            assertFalse("Invalid sugar cannot be persisted", saveControl().isEnabled)
            setField(Res.string.nutriment_sugars, "0")
            await("Save restored after finite sugar") {
                saveControl().isEnabled
            }
            save()
            val first = savedEntry()
            assertEquals(780.0, first.nutritionFacts.energy.value!!, 0.00001)
            assertEquals(0.0, first.nutritionFacts.carbohydrates.value)
            assertEquals(0.0, first.nutritionFacts.sugars.value)
            assertEquals(0.0012, first.nutritionFacts.sodium.value)
            assertEquals(0.012, first.nutritionFacts.vitaminC.value)
            assertEquals(250.0, first.weightGrams)
            assertEquals(2.5, first.servingCount)
            assertNull(first.nutritionFacts.proteins.value)
            clickText(EDIT)
            setField(Res.string.weight, "500")
            setField(Res.string.label_number_of_servings, "5")
            save()
            val edited = savedEntry()
            assertEquals(first.nutritionFacts, edited.nutritionFacts)
            assertEquals(500.0, edited.weightGrams)
            assertEquals(5.0, edited.servingCount)
            assertEquals(1, scannerCalls)
        }

    @Test fun missingScanEnergyStaysUnknownUntilUserChoosesAutomaticCalculation() =
        withFlow("""{"name":"Synthetic unknown energy","protein":35,"fat":38}""") {
            acceptReview()
            assertField(Res.string.unit_energy, "")
            assertField(Res.string.nutriment_carbohydrates, "")
            save()
            val unknown = savedEntry()
            assertNull(unknown.nutritionFacts.energy.value)
            assertNull(unknown.nutritionFacts.carbohydrates.value)
            clickText(EDIT)
            assertField(Res.string.unit_energy, "")
            // The existing energy mode control explicitly opts into macro calculation.
            clickText(label(Res.string.headline_manual_energy_input))
            assertField(Res.string.unit_energy, "482")
            save()
            val entry = savedEntry()
            assertEquals(482.0, entry.nutritionFacts.energy.value)
            assertNull(entry.nutritionFacts.carbohydrates.value)
            assertNull(entry.servingCount)
        }

    @Test fun directManualQuickAddKeepsAutomaticEnergyAndBlankToZeroSaveBehavior() =
        withFlow(null) {
            assertField(Res.string.unit_energy, "482")
            assertField(Res.string.nutriment_carbohydrates, "")
            save()
            val entry = savedEntry()
            assertEquals("Synthetic manual food", entry.name)
            assertEquals(482.0, entry.nutritionFacts.energy.value)
            assertEquals(35.0, entry.nutritionFacts.proteins.value)
            assertEquals(38.0, entry.nutritionFacts.fats.value)
            assertEquals(0.0, entry.nutritionFacts.carbohydrates.value)
            assertEquals(1.0, entry.servingCount)
            assertEquals(0, scannerCalls)
        }

    private fun withFlow(json: String?, kilojoules: Boolean = false, checkFlow: () -> Unit) {
        val applicationKoin = GlobalContext.get()
        database = Room.inMemoryDatabaseBuilder(instrumentation.targetContext, FoodYouDatabase::class.java).build()
        repository = RoomManualDiaryEntryRepository(database.manualDiaryEntryDao)
        runBlocking {
            database.mealDao.insertWithLastRank(MealEntity(1, "Synthetic meal", 0, 0, 23, 59))
        }
        scannerCalls = 0
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
                return AiScanResult.Success(requireNotNull(AiFoodEstimateParser.parse(requireNotNull(json))))
            }
        }
        val model = AiScanViewModel(scanner, applicationKoin.get<FoodSearchUseCase>(), ObserveAiConfigured(preferences, config))
        val clock = applicationKoin.get<DateProvider>()
        val isolated = koinApplication {
            modules(module {
                viewModel { model }
                viewModel { (entryDate: LocalDate, mealId: Long) -> CreateQuickAddViewModel(mealId, entryDate, repository, clock) }
                viewModel { (id: com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntryId) -> UpdateQuickAddViewModel(id, repository, clock) }
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
                                CompositionLocalProvider(LocalEnergyFormatter provides
                                    if (kilojoules) EnergyFormatter.kilojoules else EnergyFormatter.kilocalories) {
                                    FixtureHost(json != null)
                                }
                            }
                        }
                    }
                    if (json != null) {
                        model.onPhotoCaptured(syntheticJpeg)
                        model.submit()
                    }
                }
                if (json != null) await("synthetic scan result") { model.state.value is AiScanUiState.Result }
                instrumentation.waitForIdleSync()
                checkFlow()
            }
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(originalExceptionHandler)
            isolated.close()
            database.close()
            automation.serviceInfo = originalServiceInfo.apply { flags = originalServiceFlags }
            assertSame("Global application Koin must survive", applicationKoin, GlobalContext.get())
        }
    }

    @Composable private fun FixtureHost(scan: Boolean) {
        nav = rememberNavController()
        val initial = remember { if (scan) SCAN else MANUAL }
        NavHost(nav, startDestination = initial) {
            composable(SCAN) {
                AiScanScreen(
                    onBack = { error("Not used") },
                    onSaveToQuickAdd = { estimate, _ ->
                        val route = FoodDiaryCreateQuickAdd(date.toEpochDays().toLong(), 1, estimate)
                        acceptedRoute = route
                        nav.navigate(route)
                    },
                    onAlignPickFood = { error("No global food lookup") },
                    onCreateCustomFood = { error("No global product writes") },
                )
            }
            composable<FoodDiaryCreateQuickAdd> { entry ->
                val route = entry.toRoute<FoodDiaryCreateQuickAdd>()
                val estimate = requireNotNull(AiFoodEstimateParser.parse(requireNotNull(route.scanEstimateJson)))
                CreateQuickAddScreen(onBack = { error("Not used") }, onSave = { nav.navigate(SAVED) },
                    mealId = route.mealId, date = LocalDate.fromEpochDays(route.epochDay), scanEstimate = estimate)
            }
            composable(MANUAL) {
                CreateQuickAddScreen(onBack = { error("Not used") }, onSave = { nav.navigate(SAVED) },
                    mealId = 1, date = date, prefillName = "Synthetic manual food", prefillProteins = 35.0, prefillFats = 38.0)
            }
            composable(SAVED) {
                val entries by repository.observeAll(1, date).collectAsState(initial = emptyList())
                Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())) {
                    Text(SAVED)
                    entries.singleOrNull()?.let { entry ->
                        Text(entry.name)
                        entry.description?.let { Text(it) }
                        KnownNutrientList(entry.nutritionFacts)
                        Button(onClick = { nav.navigate(EditFixture(entry.id.value)) }) { Text(EDIT) }
                    }
                }
            }
            composable<EditFixture> { entry ->
                UpdateQuickAddScreen(onBack = { error("Not used") }, onSave = { nav.navigate(SAVED) }, id = entry.toRoute<EditFixture>().id)
            }
        }
    }

    private fun acceptReview() {
        clickText(label(Res.string.action_save))
        findText(label(Res.string.headline_quick_add))
        val route = requireNotNull(acceptedRoute)
        assertNotNull(route.scanEstimateJson)
    }

    private fun save() {
        // Toolbar Save is distinct from the review card Save and remains reachable when scrolling.
        clickText(label(Res.string.action_save))
        findText(SAVED)
        await("one isolated persisted diary row") { runBlocking { repository.observeAll(1, date).first().size == 1 } }
    }

    /** Icon children may remain enabled while their actionable Button ancestor is disabled. */
    private fun saveControl(): AccessibilityNodeInfo = driver.control(label(Res.string.action_save))

    private fun savedEntry(): ManualDiaryEntry = runBlocking { repository.observeAll(1, date).first().single() }

    private fun assertSample(entry: ManualDiaryEntry) {
        assertEquals("Synthetic reported food", entry.name)
        assertEquals(780.0, entry.nutritionFacts.energy.value)
        assertEquals(35.0, entry.nutritionFacts.proteins.value)
        assertEquals(38.0, entry.nutritionFacts.fats.value)
        assertEquals(6.0, entry.nutritionFacts.dietaryFiber.value)
        assertEquals(12.0, entry.nutritionFacts.sugars.value)
        assertNull(entry.nutritionFacts.carbohydrates.value)
        assertNull(entry.description)
        assertNull(entry.servingCount)
        assertNull(entry.weightGrams)
    }

    private val driver = ScanTestDriver(instrumentation,
        { if (::decor.isInitialized) decor else null }, { activityPackage }, { fixtureCrash })
    private fun assertField(resource: StringResource, expected: String) = driver.assertField(resource, expected)
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

    @Serializable private data class EditFixture(val id: Long)
    private companion object {
        const val SCAN = "synthetic-scan"
        const val MANUAL = "synthetic-manual"
        const val SAVED = "Synthetic diary readback"
        const val EDIT = "Edit synthetic diary entry"
        const val REPORTED = """{"name":"Synthetic reported food","calories":780,"protein":35,"fat":38,"fibre":6,"sugar":12}"""
        const val OPTIONALS = """{"name":"Synthetic optional food","description":"Synthetic description","energyKj":3263.52,"servingCount":2.5,"weightKg":0.25,"carbohydrates":0,"sugar":0,"sodium":0.0012,"vitaminC":0.012}"""
    }
}
