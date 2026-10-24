package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
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
                    onSaveToQuickAdd = { estimate ->
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
    private fun saveControl(): AccessibilityNodeInfo {
        var node: AccessibilityNodeInfo? = findText(label(Res.string.action_save))
        while (node != null) {
            if (node.className?.toString() == "android.widget.Button" || node.isClickable ||
                node.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK }) return node
            node = node.parent
        }
        error("Missing Save action control; ${diagnosticGeometry()}")
    }

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

    private fun assertField(resource: StringResource, expected: String) {
        val field = field(label(resource))
        val actual = field.text?.toString().orEmpty()
        assertEquals("Editable value for ${label(resource)}", expected, actual)
    }

    private fun setField(resource: StringResource, value: String) {
        val field = field(label(resource))
        assertTrue("Set synthetic field", field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,
            Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value) }))
        await("edited field ${label(resource)}") { field(label(resource)).text?.toString() == value }
        SystemClock.sleep(250)
    }

    private fun field(text: String): AccessibilityNodeInfo {
        // These data tests do not need an open IME; FOODUS-003 owns keyboard behavior. Allow the
        // screen's delayed autofocus to run, then hide the IME without dispatching Back navigation.
        SystemClock.sleep(200)
        onMain { decor.windowInsetsController?.hide(WindowInsets.Type.ime()); Unit }
        await("Quick Add keyboard hidden for data inspection") {
            onMain { decor.rootWindowInsets?.isVisible(WindowInsets.Type.ime()) != true }
        }
        return find("editable $text") { node ->
            node.isEditable && (
                node.hintText?.toString()?.lines()?.contains(text) == true ||
                    descendants(node).any { it.text?.toString()?.lines()?.contains(text) == true }
                )
        }
    }

    private fun findText(text: String): AccessibilityNodeInfo = find("text $text") {
        it.text?.toString()?.lines()?.contains(text) == true || it.contentDescription?.toString() == text
    }

    private fun clickText(text: String) {
        var node: AccessibilityNodeInfo? = findText(text)
        while (node != null && !node.isClickable) node = node.parent
        assertNotNull("Clickable node for $text", node)
        assertTrue("Click $text", node!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        instrumentation.waitForIdleSync()
    }

    private fun find(description: String, predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo {
        var match: AccessibilityNodeInfo? = null
        repeat(2) { direction ->
            repeat(18) {
                val root = awaitApplicationRoot()
                match = descendants(root).firstOrNull { node -> node.isVisibleToUser && predicate(node) }
                if (match != null) return requireNotNull(match)
                swipe(forward = direction == 0)
            }
        }
        error("Missing synthetic UI $description; ${diagnosticGeometry()}")
    }

    private fun root(): AccessibilityNodeInfo? {
        if (Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.clearCache()
        val packageName = instrumentation.targetContext.packageName
        if (!::decor.isInitialized) return null
        val displayId = onMain { decor.display?.displayId ?: -1 }
        // windows() is scoped to UiAutomation's context display, which may differ from the
        // ActivityScenario host. Use the actual synthetic host display on API 30+.
        return instrumentation.uiAutomation.windowsOnAllDisplays.get(displayId).orEmpty().asSequence()
            .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
            .sortedByDescending { it.isFocused }
            .mapNotNull { it.root }
            .firstOrNull { it.packageName?.toString() == packageName }
    }

    private fun awaitApplicationRoot(): AccessibilityNodeInfo {
        var found: AccessibilityNodeInfo? = null
        val deadline = SystemClock.uptimeMillis() + 5_000
        do {
            found = root()
            if (found != null) return found
            SystemClock.sleep(100)
        } while (SystemClock.uptimeMillis() < deadline)
        error("Synthetic application window unavailable during transition; ${diagnosticGeometry()}")
    }

    private fun descendants(node: AccessibilityNodeInfo): Sequence<AccessibilityNodeInfo> = sequence {
        yield(node)
        for (index in 0 until node.childCount) node.getChild(index)?.let { yieldAll(descendants(it)) }
    }

    /** Swipe the actual content viewport above the IME. Whole-window coordinates can start inside
     * the keyboard once Quick Add autofocus opens it and never move the LazyColumn.
     */
    private fun swipe(forward: Boolean) {
        // Autofocus can animate the IME between searches. Wait for two equal snapshots rather
        // than comparing coordinates measured at different animation frames.
        val geometry = settledSwipeGeometry() ?: return
        val applicationRoot = awaitApplicationRoot()
        val viewport = descendants(applicationRoot).firstOrNull { node ->
            !node.isEditable && node.isVisibleToUser && (
                node.className?.toString() in setOf("android.widget.ScrollView", "androidx.recyclerview.widget.RecyclerView") ||
                    node.isScrollable && node.actionList.any {
                        it.id == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD ||
                            it.id == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    }
                )
        }
        if (viewport == null) {
            // A destination can render its heading before its scroll semantics are attached.
            SystemClock.sleep(200)
            return
        }
        val accessibilityAction = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        if (viewport.performAction(accessibilityAction)) {
            SystemClock.sleep(200)
            instrumentation.waitForIdleSync()
            return
        }
        val bounds = Rect().also(viewport::getBoundsInScreen)
        val applicationBounds = Rect().also(applicationRoot::getBoundsInScreen)
        bounds.intersect(applicationBounds)
        bounds.bottom = minOf(bounds.bottom, geometry.imeTop)
        val margin = geometry.margin
        val safeContentTop = geometry.safeContentTop
        // LazyColumn fills the Scaffold and uses contentPadding, so its accessibility bounds
        // can start at y0. A downward swipe there opens the notification shade instead of scrolling.
        bounds.top = maxOf(bounds.top, safeContentTop)
        check(bounds.width() > 0 && bounds.height() > margin * 2) {
            "Unusable synthetic scroll viewport; ${diagnosticGeometry()}"
        }
        val low = bounds.top + (bounds.height() * 0.65f).toInt()
        val high = bounds.top + margin
        check(high > safeContentTop && low < geometry.imeTop) {
            "Synthetic swipe must stay between toolbar and IME; ${diagnosticGeometry()}"
        }
        val from = if (forward) low else high
        val to = if (forward) high else low
        // If insets changed after settling, let the bounded outer search retry with fresh bounds.
        if (swipeGeometry() != geometry) return
        val displayId = geometry.displayId
        val output = ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
            "input touchscreen -d $displayId swipe ${bounds.centerX()} $from ${bounds.centerX()} $to 360"
        )).bufferedReader().use { it.readText() }
        check(output.isBlank()) { "Android native swipe reported an error" }
        SystemClock.sleep(200)
        instrumentation.waitForIdleSync()
    }

    private data class SwipeGeometry(val margin: Int, val safeContentTop: Int, val imeTop: Int, val displayId: Int)

    private fun swipeGeometry(): SwipeGeometry = onMain {
        val density = decor.resources.displayMetrics.density
        val location = IntArray(2).also(decor::getLocationOnScreen)
        val insets = decor.rootWindowInsets
        SwipeGeometry(
            margin = (16 * density).toInt(),
            safeContentTop = location[1] + (insets?.getInsets(WindowInsets.Type.systemBars())?.top ?: 0) + (64 * density).toInt(),
            imeTop = location[1] + decor.height - (insets?.getInsets(WindowInsets.Type.ime())?.bottom ?: 0),
            displayId = requireNotNull(decor.display).displayId,
        )
    }

    private fun settledSwipeGeometry(): SwipeGeometry? {
        var previous = swipeGeometry()
        repeat(5) {
            SystemClock.sleep(100)
            val current = swipeGeometry()
            if (current == previous) return current
            previous = current
        }
        return null
    }

    private fun keyboardTop(): Int = onMain {
        val location = IntArray(2).also(decor::getLocationOnScreen)
        location[1] + decor.height - (decor.rootWindowInsets?.getInsets(WindowInsets.Type.ime())?.bottom ?: 0)
    }

    private fun <T> onMain(block: () -> T): T {
        var result: T? = null
        instrumentation.runOnMainSync { result = block() }
        return requireNotNull(result)
    }

    /** Bounded window/geometry/action metadata only. Package and resource identifiers identify
     * system overlays; labels, values, descriptions, exception messages and settings are omitted.
     */
    private fun diagnosticGeometry(): String = buildString {
        val targetPackage = instrumentation.targetContext.packageName
        append("targetPackage=$targetPackage activityPackage=$activityPackage samePackage=${activityPackage == targetPackage}; ")
        if (::decor.isInitialized) {
            append(onMain {
                val location = IntArray(2).also(decor::getLocationOnScreen)
                val bounds = Rect(location[0], location[1], location[0] + decor.width, location[1] + decor.height)
                "decorShown=${decor.isShown} focused=${decor.hasWindowFocus()} attached=${decor.isAttachedToWindow} " +
                    "displayId=${decor.display?.displayId} bounds=$bounds; "
            })
            append("IME top=${keyboardTop()}; ")
        }
        val automation = instrumentation.uiAutomation
        append("serviceFlags=${automation.serviceInfo.flags}; ")
        val windows = automation.windows
        append("windowsCount=${windows.size}")
        windows.take(12).forEach { window -> appendWindowMetadata(window, targetPackage) }
        // UiAutomation.windows is restricted to its default display. Inspect all displays to
        // distinguish a missing node from an ActivityScenario virtual-display mismatch.
        val displays = automation.windowsOnAllDisplays
        append("; allDisplayCount=${displays.size()}")
        repeat(minOf(displays.size(), 6)) { index ->
            val displayId = displays.keyAt(index)
            val displayWindows = displays.valueAt(index)
            append("[display=$displayId count=${displayWindows.size}")
            displayWindows.take(12).forEach { window -> appendWindowMetadata(window, targetPackage) }
            append("]")
        }
        val active = automation.rootInActiveWindow
        append("; activeRootPresent=${active != null} activeClass=${active?.className} " +
            "activePackage=${active?.packageName?.toString()?.take(160)} " +
            "activePackageMatch=${active?.packageName?.toString() == targetPackage}; ")
        if (active != null && active.packageName?.toString() != targetPackage) {
            append("activeSystemIdentifiers=")
            descendants(active).take(25).forEach { node ->
                append("[class=${node.className},resourceId=${node.viewIdResourceName?.take(180)}]")
            }
        }
        append("fixtureCrash=${fixtureCrash ?: "none-recorded"}; ")
        val appRoot = root()
        if (appRoot == null) {
            append("no target application root")
        } else {
            append("syntheticEditableLabels=")
            val knownLabels = mapOf(
                "name" to label(Res.string.product_name), "description" to label(Res.string.label_description),
                "servings" to label(Res.string.label_number_of_servings), "weight" to label(Res.string.weight),
                "protein" to label(Res.string.nutriment_proteins), "fat" to label(Res.string.nutriment_fats),
                "carbohydrates" to label(Res.string.nutriment_carbohydrates), "fiber" to label(Res.string.nutriment_fiber),
                "sugar" to label(Res.string.nutriment_sugars), "energy" to label(Res.string.unit_energy),
            )
            descendants(appRoot).filter { it.isEditable }.take(12).forEach { node ->
                val hints = node.hintText?.toString()?.lines().orEmpty()
                val texts = descendants(node).flatMap { it.text?.toString()?.lines().orEmpty().asSequence() }.toSet()
                val bounds = Rect().also(node::getBoundsInScreen)
                append("[class=${node.className},bounds=$bounds,hintMatches=${knownLabels.filterValues { it in hints }.keys}," +
                    "descendantMatches=${knownLabels.filterValues { it in texts }.keys}]")
            }
            descendants(appRoot).take(40).forEach { node ->
                val bounds = Rect().also(node::getBoundsInScreen)
                append("[${node.className},editable=${node.isEditable},visible=${node.isVisibleToUser},bounds=$bounds,actions=${node.actionList.map { it.id }}]")
            }
        }
    }

    private fun StringBuilder.appendWindowMetadata(window: AccessibilityWindowInfo, targetPackage: String) {
        val windowRoot = window.root
        val bounds = Rect().also(window::getBoundsInScreen)
        append("[type=${window.type} id=${window.id} layer=${window.layer} displayId=${window.displayId} " +
            "bounds=$bounds active=${window.isActive} focused=${window.isFocused} " +
            "rootPresent=${windowRoot != null} rootPackage=${windowRoot?.packageName?.toString()?.take(160)} " +
            "rootPackageMatch=${windowRoot?.packageName?.toString() == targetPackage}]")
    }

    private fun await(description: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 15_000
        while (!condition()) {
            check(SystemClock.uptimeMillis() < deadline) { "Timed out waiting for $description" }
            SystemClock.sleep(100)
        }
    }

    private fun label(resource: StringResource) = runBlocking { getString(resource) }

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
