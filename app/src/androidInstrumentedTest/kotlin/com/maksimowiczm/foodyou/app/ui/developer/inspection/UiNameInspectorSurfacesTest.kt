package com.maksimowiczm.foodyou.app.ui.developer.inspection

import android.accessibilityservice.AccessibilityServiceInfo
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.app.ui.settings.SettingsScreen
import com.maksimowiczm.foodyou.app.ui.theme.ThemeSettingsViewModel
import com.maksimowiczm.foodyou.barcodescanner.ui.CameraBarcodeScannerScreen
import com.maksimowiczm.foodyou.common.compose.utility.ClipboardManager
import com.maksimowiczm.foodyou.common.compose.utility.ClipboardManagerProvider
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.settings.domain.entity.*
import com.maksimowiczm.foodyou.settings.domain.repository.TranslationRepository
import foodyou.app.generated.resources.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Synthetic values and isolated preferences exercise production inspection and Settings UI.
 * These fixtures do not open the owner's diary, change application preferences, or use a network.
 */
@SdkSuppress(minSdkVersion = 31)
class UiNameInspectorSurfacesTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var originalServiceFlags = 0
    private var decor: View? = null
    private val driver = ScanTestDriver(instrumentation, { decor },
        { instrumentation.targetContext.packageName }, { null })

    @Before fun enableWindowInspection() {
        originalServiceFlags = instrumentation.uiAutomation.serviceInfo.flags
        instrumentation.uiAutomation.serviceInfo = instrumentation.uiAutomation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
    }

    @After fun restoreWindowInspection() {
        decor = null
        instrumentation.uiAutomation.serviceInfo = instrumentation.uiAutomation.serviceInfo.apply {
            flags = originalServiceFlags
        }
    }

    @Test
    fun settingsTogglePreservesBackStackDraftAndUnrelatedPreferences() {
        val initial = settings().copy(uiNameBarIncludeHierarchy = true)
        val preferences = MutableStateFlow(initial)
        val copied = AtomicReference<String?>()
        val clipboard = object : ClipboardManager {
            override fun copy(label: String, text: String) { copied.set(text) }
            override fun paste(): String? = null
        }
        val repository = object : UserPreferencesRepository<Settings> {
            override fun observe() = preferences
            override suspend fun update(transform: Settings.() -> Settings) { preferences.update(transform) }
        }
        val applicationKoin = GlobalContext.get()
        val isolated = koinApplication {
            modules(module {
                single<UserPreferencesRepository<Settings>>(named(Settings::class.qualifiedName!!)) { repository }
                // Theme is read-only in this fixture; keep its ViewModel available in isolated Koin.
                viewModel { applicationKoin.get<ThemeSettingsViewModel>() }
                // The language summary only observes the current translation; callbacks stay inert.
                single<TranslationRepository> { applicationKoin.get<TranslationRepository>() }
            })
        }
        val inspector = AtomicReference<UiInspectorState?>()
        try {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.setContent {
                        KoinIsolatedContext(context = isolated) {
                          ClipboardManagerProvider(clipboard) {
                            UiInspectionHost(repository) {
                                val state = LocalUiInspector.current
                                SideEffect { inspector.set(state) }
                                val nav = rememberNavController()
                                NavHost(navController = nav, startDestination = "editor") {
                                    composable("editor") {
                                        var draft by rememberSaveable { mutableStateOf("") }
                                        Column(Modifier.fillMaxSize().padding(24.dp)) {
                                            OutlinedTextField(value = draft, onValueChange = { draft = it },
                                                label = { Text("Synthetic draft") })
                                            Button(onClick = { nav.navigate("settings") }) { Text("Open synthetic settings") }
                                        }
                                    }
                                    composable("settings") {
                                        // Match the source scope supplied by the production navigation caller.
                                        UiSourceScope(descriptor("fixture.settings", SETTINGS_SCREEN)) {
                                            SettingsScreen(onBack = { nav.popBackStack() }, onAbout = {},
                                                onDiarySlots = {}, onLanguage = {}, onGoals = {},
                                                onPersonalization = {}, onDatabase = {}, onExternalDatabases = {},
                                                onAiSettings = {}, onRelaySettings = {})
                                        }
                                    }
                                }
                            }
                          }
                        }
                    }
                }
                setEditable("Synthetic draft", "Synthetic pending edit")
                repeat(2) { index ->
                    driver.clickText("Open synthetic settings")
                    driver.findText(driver.label(Res.string.headline_settings))
                    driver.clickText(driver.label(Res.string.ui_name_bar_setting))
                    val enabled = index == 0
                    await { preferences.value.uiNameBarEnabled == enabled && (onMain { inspector.get() } != null) == enabled }
                    assertEquals("Only the inspection setting changes", initial.copy(uiNameBarEnabled = enabled), preferences.value)
                    if (enabled) {
                        hold(accessibilityTarget(driver.findText(driver.label(Res.string.action_go_back))))
                        val snapshot = onMain { inspector.get()!!.snapshot!! }
                        assertEquals("ArrowBackIconButton.Icon.1", snapshot.copyText(false))
                        assertEquals(SETTINGS_SCREEN, snapshot.screen.composable)
                        val caller = snapshot.ancestors.single { it.descriptor.name == "SettingsScreen.ArrowBackIconButton.1" }
                        assertEquals(SETTINGS_SOURCE, caller.descriptor.source)
                        assertEquals(SETTINGS_SCREEN, caller.descriptor.owner)
                        val component = snapshot.ancestors.single { it.descriptor.name == "ArrowBackIconButton.IconButton.1" }
                        assertEquals(ARROW_BACK_SOURCE, component.descriptor.source)
                        assertEquals(ARROW_BACK_SOURCE, snapshot.selected.descriptor.source)
                        assertEquals(SETTINGS_SOURCE, snapshot.selected.descriptor.callSiteSource)
                        assertEquals(SETTINGS_SCREEN, snapshot.selected.descriptor.callSiteOwner)
                        driver.clickText(driver.label(Res.string.ui_name_bar_copy))
                        assertEquals(snapshot.copyText(true), copied.get())
                        assertTrue(copied.get()!!.contains("SettingsScreen.ArrowBackIconButton.1"))
                        assertTrue(copied.get()!!.contains("ArrowBackIconButton.IconButton.1"))
                        assertTrue(copied.get()!!.contains("ArrowBackIconButton.Icon.1"))
                        instrumentation.sendStatus(0, Bundle().apply {
                            putString("stream", "Synthetic Settings ArrowBack structural locator:\n${copied.get()}\n")
                        })
                        writeSyntheticEvidence("foodus-story29-settings-shared.xml", requireNotNull(copied.get()))
                        // A held Back icon must leave this destination intact until the short click below.
                        driver.findText(driver.label(Res.string.headline_settings))
                    }
                    driver.clickText(driver.label(Res.string.action_go_back))
                    driver.findText("Open synthetic settings")
                    assertEquals("Navigation restores the same editor draft", "Synthetic pending edit",
                        editable("Synthetic draft").text?.toString())
                }
                assertEquals(initial, preferences.value)
            }
        } finally {
            isolated.close()
            assertSame("Application Koin survives isolated settings fixture", applicationKoin, GlobalContext.get())
        }
    }

    @Test
    fun deniedCameraPermissionUiUsesSharedModuleSourceAdapterWithoutOpeningCamera() {
        // Device setup owns denial/restoration outside the instrumented process: revoking this
        // permission here can kill that process. Never enter the granted-camera rendering branch.
        assertEquals("Denied-camera fixture requires CAMERA denied before instrumentation starts",
            PackageManager.PERMISSION_DENIED,
            instrumentation.targetContext.checkSelfPermission(Manifest.permission.CAMERA))
        val inspector = AtomicReference<UiInspectorState?>()
        val scans = AtomicInteger()
        val closes = AtomicInteger()
        val copied = AtomicReference<String?>()
        val clipboard = object : ClipboardManager {
            override fun copy(label: String, text: String) { copied.set(text) }
            override fun paste(): String? = null
        }
        launchActivity<DeveloperActivity>().use { scenario ->
            scenario.onActivity { activity ->
                decor = activity.window.decorView
                activity.setContent {
                    ClipboardManagerProvider(clipboard) {
                        UiInspectionContent(true, true, {}) {
                            val state = LocalUiInspector.current
                            SideEffect { inspector.set(state) }
                            BarcodeUiInspectionScope {
                                UiSourceScope(descriptor("fixture.scanner", "com.maksimowiczm.foodyou.barcodescanner.ui.CameraBarcodeScannerScreen")) {
                                    CameraBarcodeScannerScreen(onBarcodeScan = { scans.incrementAndGet() },
                                        onClose = { closes.incrementAndGet() })
                                }
                            }
                        }
                    }
                }
            }
            val permissionLabel = driver.findText(driver.label(Res.string.action_tap_to_allow_access))
            await { inspector.get() != null }
            hold(accessibilityTarget(permissionLabel))
            val snapshot = onMain { inspector.get()!!.snapshot!! }
            assertEquals("RequestCameraPermissionScreen.Text.2", snapshot.copyText(false))
            assertEquals("com.maksimowiczm.foodyou.barcodescanner.ui.RequestCameraPermissionScreen",
                snapshot.selected.descriptor.owner)
            assertEquals("shared/barcodescanner/src/androidMain/kotlin/com/maksimowiczm/foodyou/barcodescanner/ui/CameraBarcodeScannerScreen.android.kt",
                snapshot.selected.descriptor.source)
            assertTrue(snapshot.ancestors.any { it.descriptor.name == "CameraBarcodeScannerScreen.Box.1" })
            assertTrue(snapshot.ancestors.any { it.descriptor.name == "RequestCameraPermissionScreen.Surface.1" })
            driver.clickText(driver.label(Res.string.ui_name_bar_copy))
            assertEquals(snapshot.copyText(true), copied.get())
            assertEquals(PackageManager.PERMISSION_DENIED,
                instrumentation.targetContext.checkSelfPermission(Manifest.permission.CAMERA))
            assertEquals(0, scans.get())
            assertEquals(0, closes.get())
        }
    }

    @Test
    fun sliderHoldCapturesSourceAndReportsAnyPressTimeValueChange() {
        val inspector = AtomicReference<UiInspectorState?>()
        val target = AtomicReference<TouchTarget?>()
        val callbacks = AtomicInteger()
        var value by mutableStateOf(.2f)
        launchActivity<DeveloperActivity>().use { scenario ->
            scenario.onActivity { activity ->
                decor = activity.window.decorView
                activity.setContent {
                    UiInspectionContent(true, false, {}) {
                        val state = LocalUiInspector.current
                        SideEffect { inspector.set(state) }
                        Column(Modifier.fillMaxSize().padding(32.dp)) {
                            Slider(value = value, onValueChange = { value = it; callbacks.incrementAndGet() },
                                modifier = Modifier.fillMaxWidth().capture(target)
                                    .uiName(descriptor("surface.slider", "androidx.compose.material3.Slider")))
                        }
                    }
                }
            }
            await { inspector.get() != null && target.get() != null }
            val slider = target.get()!!
            val x = slider.bounds.left + slider.bounds.width * .8f
            val down = SystemClock.uptimeMillis()
            val initial = onMain { value }
            event(slider.root, down, MotionEvent.ACTION_DOWN, x, slider.bounds.center.y)
            SystemClock.sleep(250)
            val afterDown = onMain { value }
            val callbacksAfterDown = callbacks.get()
            SystemClock.sleep(3_050)
            val afterCapture = onMain { value }
            val callbacksAfterCapture = callbacks.get()
            event(slider.root, down, MotionEvent.ACTION_UP, x, slider.bounds.center.y)
            instrumentation.waitForIdleSync()
            assertEquals("surface.slider", onMain { inspector.get()!!.snapshot!!.copyText(false) })
            val observation = "Synthetic Slider inspection: initial=$initial afterDown=$afterDown " +
                "afterCapture=$afterCapture afterRelease=${onMain { value }} " +
                "callbacksAfterDown=$callbacksAfterDown callbacksAfterCapture=$callbacksAfterCapture " +
                "callbacksAfterRelease=${callbacks.get()}\n"
            instrumentation.sendStatus(0, Bundle().apply { putString("stream", observation) })
            writeSyntheticEvidence("foodus-story29-slider.txt", observation)
            // Observe the native press mutation explicitly; this test does not assume a suppression
            // policy for controls that change on DOWN rather than through a long-press action.
            assertEquals("A stationary hold must not keep changing the Slider", afterDown, afterCapture, 0f)
            assertEquals(callbacksAfterDown, callbacksAfterCapture)

            val tap = SystemClock.uptimeMillis()
            val tapX = slider.bounds.left + slider.bounds.width * .3f
            event(slider.root, tap, MotionEvent.ACTION_DOWN, tapX, slider.bounds.center.y)
            SystemClock.sleep(60)
            event(slider.root, tap, MotionEvent.ACTION_UP, tapX, slider.bounds.center.y)
            await { onMain { value != afterCapture } }
        }
    }

    @Test
    fun editableFieldDeepHoldDoesNotSelectTextAndDisabledButtonRemainsIdentifiable() {
        val inspector = AtomicReference<UiInspectorState?>()
        val field = AtomicReference<TouchTarget?>()
        val disabled = AtomicReference<TouchTarget?>()
        val clicks = AtomicInteger()
        launchActivity<DeveloperActivity>().use { scenario ->
            scenario.onActivity { activity ->
                decor = activity.window.decorView
                activity.setContent {
                    UiInspectionContent(true, false, {}) {
                        val state = LocalUiInspector.current
                        SideEffect { inspector.set(state) }
                        Column(Modifier.fillMaxSize().padding(24.dp)) {
                            var draft by remember { mutableStateOf("") }
                            OutlinedTextField(value = draft, onValueChange = { draft = it },
                                label = { Text("Synthetic editable") },
                                modifier = Modifier.fillMaxWidth().capture(field)
                                    .uiName(descriptor("surface.editable", "androidx.compose.material3.OutlinedTextField")))
                            Button(onClick = { clicks.incrementAndGet() }, enabled = false,
                                modifier = Modifier.padding(top = 24.dp).capture(disabled)
                                    .uiName(descriptor("surface.disabled", "androidx.compose.material3.Button"), enabled = false)) {
                                Text("Synthetic disabled action")
                            }
                        }
                    }
                }
            }
            setEditable("Synthetic editable", "Synthetic editable content")
            await { field.get() != null && inspector.get() != null }
            // ACTION_SET_TEXT need not focus a Compose field, and ACTION_SET_SELECTION may then
            // return false. Use the real short-tap path to establish a focused, collapsed caret.
            val input = field.get()!!
            val tap = SystemClock.uptimeMillis()
            event(input.root, tap, MotionEvent.ACTION_DOWN, input.bounds.center.x, input.bounds.center.y)
            SystemClock.sleep(60)
            event(input.root, tap, MotionEvent.ACTION_UP, input.bounds.center.x, input.bounds.center.y)
            await {
                val before = editable("Synthetic editable")
                before.isFocused && before.textSelectionStart >= 0 && before.textSelectionStart == before.textSelectionEnd
            }
            hideKeyboard()
            hold(field.get()!!)
            assertEquals("surface.editable", onMain { inspector.get()!!.snapshot!!.copyText(false) })
            val after = editable("Synthetic editable")
            assertEquals("Hold must not select a word", after.textSelectionStart, after.textSelectionEnd)
            assertEquals("Synthetic editable content", after.text?.toString())
            assertFalse("Structural locator excludes entered text", onMain {
                inspector.get()!!.snapshot!!.copyText(true).contains("Synthetic editable content")
            })
            // Editing remains available after a deep hold has been consumed.
            setEditable("Synthetic editable", "Synthetic subsequent edit")
            hold(disabled.get()!!)
            val snapshot = onMain { inspector.get()!!.snapshot!! }
            assertEquals("surface.disabled", snapshot.copyText(false))
            assertEquals(false, snapshot.selected.enabled)
            assertFalse("The actual Button semantics remain disabled",
                driver.control("Synthetic disabled action").isEnabled)
            assertEquals(0, clicks.get())
        }
    }

    @Test
    fun narrowLargeFontBarReservesContentAndKeepsCopyAndHierarchyUsable() {
        val inspector = AtomicReference<UiInspectorState?>()
        val host = AtomicReference<TouchTarget?>()
        val target = AtomicReference<TouchTarget?>()
        val copied = AtomicReference<String?>()
        var includeHierarchy by mutableStateOf(false)
        val clipboard = object : ClipboardManager {
            override fun copy(label: String, text: String) { copied.set(text) }
            override fun paste(): String? = null
        }
        launchActivity<DeveloperActivity>().use { scenario ->
            scenario.onActivity { activity ->
                decor = activity.window.decorView
                activity.setContent {
                    val density = LocalDensity.current
                    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1.8f)) {
                        Box(Modifier.width(320.dp).fillMaxHeight().capture(host)) {
                            ClipboardManagerProvider(clipboard) {
                                UiInspectionContent(true, includeHierarchy, { includeHierarchy = it }) {
                                    val state = LocalUiInspector.current
                                    SideEffect { inspector.set(state) }
                                    Text("Synthetic narrow target", Modifier.fillMaxWidth().heightIn(min = 80.dp)
                                        .padding(16.dp).capture(target)
                                        .uiName(descriptor("surface.narrow.target", "androidx.compose.material3.Text")))
                                }
                            }
                        }
                    }
                }
            }
            await { inspector.get() != null && host.get() != null && target.get() != null }
            hold(target.get()!!)
            assertEquals("surface.narrow.target", onMain { inspector.get()!!.snapshot!!.copyText(false) })

            val field = accessibilityTarget(nodes().single { it.isVisibleToUser && it.className?.toString() == "android.widget.EditText" })
            val copy = accessibilityTarget(driver.control(driver.label(Res.string.ui_name_bar_copy)))
            val checkboxNode = nodes().single { it.isVisibleToUser && it.className?.toString() == "android.widget.CheckBox" }
            val checkbox = accessibilityTarget(checkboxNode)
            val hierarchyLabel = accessibilityTarget(driver.findText(driver.label(Res.string.ui_name_bar_hierarchy)))
            val content = target.get()!!
            val viewport = host.get()!!.bounds
            listOf(field, copy, checkbox, hierarchyLabel, content).forEach { measured ->
                assertFalse("A narrow large-font control has visible bounds", measured.bounds.isEmpty)
                assertTrue("Control fits the local 320dp viewport horizontally", measured.bounds.left >= viewport.left - 1f &&
                    measured.bounds.right <= viewport.right + 1f)
                assertTrue("Control fits the viewport vertically", measured.bounds.top >= viewport.top - 1f &&
                    measured.bounds.bottom <= viewport.bottom + 1f)
            }
            assertFalse("Name field leaves room for Copy", field.bounds.overlaps(copy.bounds))
            assertTrue("Chrome reserves space above selectable app content", listOf(field, copy, checkbox, hierarchyLabel)
                .all { it.bounds.bottom <= content.bounds.top + 1f })
            driver.clickText(driver.label(Res.string.ui_name_bar_copy))
            assertEquals("surface.narrow.target", copied.get())
            assertTrue("Hierarchy remains actionable at large font scale",
                checkboxNode.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            await { onMain { includeHierarchy } }
            driver.clickText(driver.label(Res.string.ui_name_bar_copy))
            assertEquals(onMain { inspector.get()!!.snapshot!!.copyText(true) }, copied.get())
        }
    }

    @Test
    fun overlappingZOrderAndClippedChildUseActualVisibleHitPath() {
        val inspector = AtomicReference<UiInspectorState?>()
        val front = AtomicReference<TouchTarget?>()
        val back = AtomicReference<TouchTarget?>()
        val clippedParent = AtomicReference<TouchTarget?>()
        launchActivity<DeveloperActivity>().use { scenario ->
            scenario.onActivity { activity ->
                decor = activity.window.decorView
                activity.setContent {
                    UiInspectionContent(true, false, {}) {
                        val state = LocalUiInspector.current
                        SideEffect { inspector.set(state) }
                        Box(Modifier.fillMaxSize().padding(24.dp).uiName(descriptor("surface.hit.background"))) {
                            // Declare front first so registration order disagrees with draw order.
                            Box(Modifier.offset(20.dp, 20.dp).size(120.dp).zIndex(2f).capture(front)
                                .uiName(descriptor("surface.hit.front")))
                            Box(Modifier.offset(60.dp, 60.dp).size(120.dp).zIndex(1f).capture(back)
                                .uiName(descriptor("surface.hit.back")))
                            Box(Modifier.offset(20.dp, 220.dp).size(100.dp).clipToBounds().capture(clippedParent)
                                .uiName(descriptor("surface.hit.clipParent"))) {
                                Box(Modifier.offset(x = 60.dp).size(100.dp)
                                    .uiName(descriptor("surface.hit.clippedChild")))
                            }
                        }
                    }
                }
            }
            await { inspector.get() != null && front.get() != null && back.get() != null && clippedParent.get() != null }
            val top = front.get()!!
            val bottom = back.get()!!
            val overlap = Rect(maxOf(top.bounds.left, bottom.bounds.left), maxOf(top.bounds.top, bottom.bounds.top),
                minOf(top.bounds.right, bottom.bounds.right), minOf(top.bounds.bottom, bottom.bounds.bottom))
            assertFalse("Fixture siblings visibly overlap", overlap.isEmpty)
            hold(TouchTarget(top.root, overlap))
            assertEquals("surface.hit.front", onMain { inspector.get()!!.snapshot!!.copyText(false) })

            val clip = clippedParent.get()!!
            val y = clip.bounds.center.y
            fun holdAt(x: Float) = hold(TouchTarget(clip.root, Rect(x - 1f, y - 1f, x + 1f, y + 1f)))
            holdAt(clip.bounds.right - clip.bounds.width * .2f)
            assertEquals("surface.hit.clippedChild", onMain { inspector.get()!!.snapshot!!.copyText(false) })
            holdAt(clip.bounds.right + clip.bounds.width * .2f)
            assertEquals("surface.hit.background", onMain { inspector.get()!!.snapshot!!.copyText(false) })
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun popupAndBottomSheetKeepSourceAncestryAndCopyableSnapshotAfterDismissal() {
        val inspector = AtomicReference<UiInspectorState?>()
        val target = AtomicReference<TouchTarget?>()
        val tooltipText = AtomicReference<TouchTarget?>()
        val copied = AtomicReference<String?>()
        var surface by mutableStateOf<String?>(null)
        val clipboard = object : ClipboardManager {
            override fun copy(label: String, text: String) { copied.set(text) }
            override fun paste(): String? = null
        }
        launchActivity<DeveloperActivity>().use { scenario ->
            scenario.onActivity { activity ->
                decor = activity.window.decorView
                activity.setContent {
                    ClipboardManagerProvider(clipboard) {
                        UiInspectionContent(true, true, {}) {
                            val state = LocalUiInspector.current
                            SideEffect { inspector.set(state) }
                            UiSourceScope(descriptor("surface.screen", OWNER_SCREEN)) {
                                Box(Modifier.fillMaxSize().uiName(descriptor("surface.background"))) {
                                    Text("Synthetic background")
                                }
                                if (surface == "popup") UiSourceScope(descriptor("surface.popup", "androidx.compose.ui.window.Popup")) {
                                    Popup(alignment = Alignment.Center, onDismissRequest = { surface = null },
                                        properties = PopupProperties(focusable = true)) {
                                        Surface {
                                            Text("Synthetic popup target", Modifier.padding(40.dp).capture(target)
                                                .uiName(descriptor("surface.popup.target", "androidx.compose.material3.Text")))
                                        }
                                    }
                                }
                                if (surface == "sheet") UiSourceScope(descriptor("surface.sheet", "androidx.compose.material3.ModalBottomSheet")) {
                                    ModalBottomSheet(onDismissRequest = { surface = null },
                                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                                        Text("Synthetic sheet target", Modifier.fillMaxWidth().padding(40.dp).capture(target)
                                            .uiName(descriptor("surface.sheet.target", "androidx.compose.material3.Text")))
                                        Spacer(Modifier.height(100.dp))
                                    }
                                }
                                if (surface == "tooltip") UiSourceScope(descriptor("surface.tooltip", "androidx.compose.material3.TooltipBox")) {
                                    val tooltip = rememberTooltipState(isPersistent = true)
                                    LaunchedEffect(tooltip) { tooltip.show() }
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        TooltipBox(positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                                            state = tooltip,
                                            tooltip = {
                                                PlainTooltip(modifier = Modifier.capture(target)
                                                    .uiName(descriptor("surface.tooltip.target", "androidx.compose.material3.PlainTooltip"))) {
                                                    Box(Modifier.padding(24.dp)) {
                                                        Text("Synthetic tooltip target", Modifier.capture(tooltipText))
                                                    }
                                                }
                                            },
                                            modifier = Modifier.uiName(descriptor("surface.tooltip.anchor", "androidx.compose.material3.TooltipBox"))) {
                                            Box(Modifier.size(48.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            await { inspector.get() != null }
            listOf("popup", "sheet", "tooltip").forEach { kind ->
                target.set(null)
                scenario.onActivity { surface = kind }
                await("Attach and measure synthetic $kind surface") {
                    onMain {
                        target.get()?.let { measured ->
                            measured.root.isAttachedToWindow && measured.root.isShown && !measured.bounds.isEmpty &&
                                (kind == "tooltip" || measured.root.hasWindowFocus())
                        } == true
                    }
                }
                // Wait for the bottom-sheet opening animation before measuring its stationary target.
                instrumentation.waitForIdleSync()
                SystemClock.sleep(600)
                if (kind == "tooltip") {
                    val tooltip = target.get()!!
                    val inset = onMain { requireNotNull(decor).resources.displayMetrics.density * 12f }
                    val point = Offset(tooltip.bounds.left + inset, tooltip.bounds.center.y)
                    assertFalse("Select PlainTooltip padding rather than its Text", tooltipText.get()!!.bounds.contains(point))
                    hold(TouchTarget(tooltip.root, Rect(point.x - 1f, point.y - 1f, point.x + 1f, point.y + 1f)))
                } else hold(target.get()!!)
                val snapshot = onMain { inspector.get()!!.snapshot!! }
                assertEquals("surface.$kind.target", snapshot.copyText(false))
                assertEquals(OWNER_SCREEN, snapshot.screen.owner)
                assertEquals(when (kind) {
                    "popup" -> "androidx.compose.ui.window.Popup"
                    "sheet" -> "androidx.compose.material3.ModalBottomSheet"
                    else -> "androidx.compose.material3.TooltipBox"
                }, snapshot.surface.composable)
                if (kind == "tooltip") assertEquals("androidx.compose.material3.PlainTooltip", snapshot.selected.descriptor.composable)
                assertTrue(snapshot.ancestors.any { it.descriptor.name == "surface.screen" })
                assertTrue(snapshot.ancestors.any { it.descriptor.name == "surface.$kind" })
                scenario.onActivity { surface = null }
                await("Dismiss synthetic $kind surface") {
                    onMain { decor?.hasWindowFocus() == true && target.get()?.root?.isAttachedToWindow == false }
                }
                driver.clickText(driver.label(Res.string.ui_name_bar_copy))
                assertEquals(snapshot.copyText(true), copied.get())
            }
        }
    }

    @Test
    fun distinctSiblingPointersCannotRestartInspectionUntilAllFingersLift() {
        val inspector = AtomicReference<UiInspectorState?>()
        val first = AtomicReference<TouchTarget?>()
        val second = AtomicReference<TouchTarget?>()
        launchActivity<DeveloperActivity>().use { scenario ->
            scenario.onActivity { activity ->
                decor = activity.window.decorView
                activity.setContent {
                    UiInspectionContent(true, false, {}) {
                        val state = LocalUiInspector.current
                        SideEffect { inspector.set(state) }
                        Column(Modifier.fillMaxSize().padding(24.dp)
                            .uiName(descriptor("surface.siblings.parent", "androidx.compose.foundation.layout.Column"))) {
                            Box(Modifier.fillMaxWidth().height(100.dp).capture(first)
                                .uiName(descriptor("surface.siblings.first")))
                            Spacer(Modifier.height(32.dp))
                            Box(Modifier.fillMaxWidth().height(100.dp).capture(second)
                                .uiName(descriptor("surface.siblings.second")))
                        }
                    }
                }
            }
            await { inspector.get() != null && first.get() != null && second.get() != null }
            val a = first.get()!!
            val b = second.get()!!
            assertFalse("Fixture siblings must have disjoint named hit paths", a.bounds.overlaps(b.bounds))
            val positions = listOf(0 to a.bounds.center, 1 to b.bounds.center)
            val down = SystemClock.uptimeMillis()
            dispatchPointers(a.root, down, MotionEvent.ACTION_DOWN, positions.take(1))
            SystemClock.sleep(200)
            dispatchPointers(a.root, down,
                MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), positions)
            SystemClock.sleep(3_200)
            assertNull("A sibling's new hit path must not restart a two-finger hold", onMain { inspector.get()!!.snapshot })

            // Lift the original finger first. The second finger must remain blocked even though
            // it is now the sole pressed pointer and belongs to a different named child.
            dispatchPointers(a.root, down, MotionEvent.ACTION_POINTER_UP, positions)
            dispatchPointers(a.root, down, MotionEvent.ACTION_MOVE, positions.drop(1))
            SystemClock.sleep(3_200)
            assertNull("The remaining sibling finger stays blocked until all-up", onMain { inspector.get()!!.snapshot })
            dispatchPointers(a.root, down, MotionEvent.ACTION_UP, positions.drop(1))
            instrumentation.waitForIdleSync()

            hold(first.get()!!)
            assertEquals("surface.siblings.first", onMain { inspector.get()!!.snapshot!!.copyText(false) })
        }
    }

    @Test
    fun actualListScrollingAndAdditionalPointerCancelPendingInspection() {
        val inspector = AtomicReference<UiInspectorState?>()
        val target = AtomicReference<TouchTarget?>()
        val listState = AtomicReference<LazyListState?>()
        launchActivity<DeveloperActivity>().use { scenario ->
            scenario.onActivity { activity ->
                decor = activity.window.decorView
                activity.setContent {
                    UiInspectionContent(true, false, {}) {
                        val state = LocalUiInspector.current
                        val scroll = rememberLazyListState()
                        SideEffect { inspector.set(state); listState.set(scroll) }
                        LazyColumn(state = scroll, modifier = Modifier.fillMaxSize().capture(target)
                            .uiName(descriptor("surface.scroll", "androidx.compose.foundation.lazy.LazyColumn"))) {
                            items(60) { index ->
                                Text("Synthetic scroll row $index", Modifier.fillMaxWidth().height(80.dp).padding(16.dp))
                            }
                        }
                    }
                }
            }
            await { inspector.get() != null && target.get() != null && listState.get() != null }
            val list = target.get()!!
            val x = list.bounds.center.x
            val from = list.bounds.top + list.bounds.height * .8f
            val to = list.bounds.top + list.bounds.height * .25f
            val down = SystemClock.uptimeMillis()
            event(list.root, down, MotionEvent.ACTION_DOWN, x, from)
            repeat(12) { index ->
                SystemClock.sleep(20)
                event(list.root, down, MotionEvent.ACTION_MOVE, x, from + (to - from) * (index + 1) / 12)
            }
            event(list.root, down, MotionEvent.ACTION_UP, x, to)
            await { onMain { listState.get()!!.firstVisibleItemIndex > 0 } }
            SystemClock.sleep(3_100)
            assertNull("Actual scrolling must not select the list", onMain { inspector.get()!!.snapshot })

            val current = target.get()!!
            val start = SystemClock.uptimeMillis()
            event(current.root, start, MotionEvent.ACTION_DOWN, current.bounds.center.x, current.bounds.center.y)
            SystemClock.sleep(200)
            pointerEvent(current, start, MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT))
            SystemClock.sleep(3_100)
            assertNull("A second pointer cancels inspection even while both stay still", onMain { inspector.get()!!.snapshot })
            pointerEvent(current, start, MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT))
            event(current.root, start, MotionEvent.ACTION_UP, current.bounds.center.x, current.bounds.center.y)
            SystemClock.sleep(200)
            assertNull(onMain { inspector.get()!!.snapshot })

            // Repeat on this same named hit path with the opposite lift order. The original
            // target's pointer loop must observe the remaining pointer's final UP as well.
            val firstUp = target.get()!!
            val positions = listOf(0 to firstUp.bounds.center, 1 to firstUp.bounds.center + Offset(40f, 0f))
            val nextDown = SystemClock.uptimeMillis()
            dispatchPointers(firstUp.root, nextDown, MotionEvent.ACTION_DOWN, positions.take(1))
            SystemClock.sleep(200)
            dispatchPointers(firstUp.root, nextDown,
                MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), positions)
            SystemClock.sleep(200)
            dispatchPointers(firstUp.root, nextDown, MotionEvent.ACTION_POINTER_UP, positions)
            dispatchPointers(firstUp.root, nextDown, MotionEvent.ACTION_MOVE, positions.drop(1))
            SystemClock.sleep(3_200)
            assertNull("Original finger lifting first must leave the other finger blocked", onMain { inspector.get()!!.snapshot })
            dispatchPointers(firstUp.root, nextDown, MotionEvent.ACTION_UP, positions.drop(1))
            instrumentation.waitForIdleSync()
            hold(target.get()!!)
            assertEquals("All-up must unblock a subsequent single-finger hold", "surface.scroll",
                onMain { inspector.get()!!.snapshot!!.copyText(false) })
        }
    }

    private data class TouchTarget(val root: View, val bounds: Rect)

    private fun accessibilityTarget(node: AccessibilityNodeInfo): TouchTarget = onMain {
        val bounds = android.graphics.Rect().also(node::getBoundsInScreen)
        val root = requireNotNull(decor)
        val origin = IntArray(2).also(root::getLocationOnScreen)
        TouchTarget(root, Rect((bounds.left - origin[0]).toFloat(), (bounds.top - origin[1]).toFloat(),
            (bounds.right - origin[0]).toFloat(), (bounds.bottom - origin[1]).toFloat()))
    }

    @Composable
    private fun Modifier.capture(target: AtomicReference<TouchTarget?>): Modifier {
        val view = LocalView.current
        return onGloballyPositioned { target.set(TouchTarget(view.rootView, it.boundsInWindow())) }
    }

    private fun hold(target: TouchTarget) {
        val down = SystemClock.uptimeMillis()
        event(target.root, down, MotionEvent.ACTION_DOWN, target.bounds.center.x, target.bounds.center.y)
        SystemClock.sleep(3_250)
        event(target.root, down, MotionEvent.ACTION_UP, target.bounds.center.x, target.bounds.center.y)
        instrumentation.waitForIdleSync()
    }

    private fun event(root: View, down: Long, action: Int, x: Float, y: Float) = onMain {
        val properties = arrayOf(MotionEvent.PointerProperties().apply {
            id = 0
            toolType = MotionEvent.TOOL_TYPE_FINGER
        })
        val coordinates = arrayOf(MotionEvent.PointerCoords().apply {
            this.x = x
            this.y = y
            pressure = 1f
            size = 1f
        })
        MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, 1, properties, coordinates,
            0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0).let {
            root.dispatchTouchEvent(it)
            it.recycle()
        }
    }

    private fun writeSyntheticEvidence(fileName: String, payload: String) {
        require(fileName in setOf("foodus-story29-settings-shared.xml", "foodus-story29-slider.txt"))
        // Only typed structural XML or synthetic numeric observations reach this boundary. Tee
        // receives data on stdin; neither payload nor shell quoting can alter the static command.
        val pipes = instrumentation.uiAutomation.executeShellCommandRw("tee /data/local/tmp/$fileName")
        ParcelFileDescriptor.AutoCloseOutputStream(pipes[1]).use {
            it.write(payload.toByteArray(Charsets.UTF_8))
        }
        val output = ParcelFileDescriptor.AutoCloseInputStream(pipes[0])
            .bufferedReader().use { it.readText() }
        assertEquals("Synthetic structural evidence echoes exactly", payload, output)
    }

    private fun pointerEvent(target: TouchTarget, down: Long, action: Int) = onMain {
        val properties = Array(2) { index -> MotionEvent.PointerProperties().apply {
            id = index
            toolType = MotionEvent.TOOL_TYPE_FINGER
        } }
        val coordinates = Array(2) { index -> MotionEvent.PointerCoords().apply {
            x = target.bounds.center.x + index * 40f
            y = target.bounds.center.y
            pressure = 1f
            size = 1f
        } }
        MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, 2, properties, coordinates,
            0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0).let {
            target.root.dispatchTouchEvent(it)
            it.recycle()
        }
    }

    private fun dispatchPointers(root: View, down: Long, action: Int, positions: List<Pair<Int, Offset>>) = onMain {
        val properties = positions.map { (pointerId, _) -> MotionEvent.PointerProperties().apply {
            id = pointerId
            toolType = MotionEvent.TOOL_TYPE_FINGER
        } }.toTypedArray()
        val coordinates = positions.map { (_, position) -> MotionEvent.PointerCoords().apply {
            x = position.x
            y = position.y
            pressure = 1f
            size = 1f
        } }.toTypedArray()
        MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, positions.size, properties, coordinates,
            0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0).let {
            root.dispatchTouchEvent(it)
            it.recycle()
        }
    }

    private fun setEditable(label: String, text: String) {
        val node = editable(label)
        assertTrue("Edit synthetic field", node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,
            Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }))
        await { editable(label).text?.toString() == text }
        hideKeyboard()
    }

    private fun editable(label: String): AccessibilityNodeInfo {
        var field: AccessibilityNodeInfo? = null
        await {
            field = nodes().firstOrNull { node -> node.isEditable && node.isVisibleToUser &&
                (node.hintText?.toString()?.lines()?.contains(label) == true ||
                    driver.descendants(node).any { it.text?.toString()?.lines()?.contains(label) == true }) }
            field != null
        }
        return requireNotNull(field)
    }

    private fun nodes(): Sequence<AccessibilityNodeInfo> {
        if (Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.clearCache()
        val display = onMain { decor?.display?.displayId ?: -1 }
        return instrumentation.uiAutomation.windowsOnAllDisplays.get(display).orEmpty().asSequence()
            .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
            .mapNotNull { it.root }
            .filter { it.packageName?.toString() == instrumentation.targetContext.packageName }
            .flatMap { driver.descendants(it) }
    }

    private fun hideKeyboard() {
        onMain { decor?.windowInsetsController?.hide(WindowInsets.Type.ime()) }
        instrumentation.waitForIdleSync()
    }

    private fun await(description: String = "Synthetic inspector surface condition", condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 8_000
        while (!condition() && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(50)
        assertTrue("$description within timeout", condition())
    }

    private fun <T> onMain(block: () -> T): T {
        val result = AtomicReference<T>()
        instrumentation.runOnMainSync { result.set(block()) }
        return result.get()
    }

    private fun descriptor(name: String, composable: String = "androidx.compose.foundation.layout.Box") =
        UiSourceDescriptor(name, composable, OWNER_SCREEN, SOURCE)

    private fun settings() = Settings(lastRememberedVersion = "synthetic", hidePreviewDialog = true,
        showTranslationWarning = false, nutrientsOrder = NutrientsOrder.defaultOrder, secureScreen = false,
        homeCardOrder = HomeCard.defaultOrder, expandGoalCard = false, onboardingFinished = true,
        energyFormat = EnergyFormat.DEFAULT, graphStyle = GraphStyle.DEFAULT, weekLayout = WeekLayout.DEFAULT,
        appLaunchInfo = AppLaunchInfo(null, null, 0))

    private companion object {
        const val OWNER_SCREEN = "com.maksimowiczm.foodyou.app.ui.developer.inspection.UiNameInspectorSurfacesTest"
        const val SOURCE = "app/src/androidInstrumentedTest/kotlin/com/maksimowiczm/foodyou/app/ui/developer/inspection/UiNameInspectorSurfacesTest.kt"
        const val SETTINGS_SCREEN = "com.maksimowiczm.foodyou.app.ui.settings.SettingsScreen"
        const val SETTINGS_SOURCE = "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/SettingsScreen.kt"
        const val ARROW_BACK_SOURCE = "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/ArrowBackIconButton.kt"
    }
}
