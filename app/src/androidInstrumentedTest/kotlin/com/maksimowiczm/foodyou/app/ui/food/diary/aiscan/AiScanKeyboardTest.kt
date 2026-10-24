package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import androidx.compose.material3.MaterialTheme
import androidx.test.core.app.launchActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.SdkSuppress
import com.maksimowiczm.foodyou.ai.domain.AiFoodScanner
import com.maksimowiczm.foodyou.ai.domain.AiScanResult
import com.maksimowiczm.foodyou.ai.domain.AiSettings
import com.maksimowiczm.foodyou.ai.domain.ObserveAiConfigured
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.common.config.AppConfig
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.food.search.domain.FoodSearchUseCase
import foodyou.app.generated.resources.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.Assert.*
import org.junit.Test
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Runs the production screen and Android IME with only synthetic photo/hint/configuration.
 * Run unchanged at the physical display size and a compact display override. The host mirrors
 * MainActivity's adjustResize mode and existing edge-to-edge setup. No camera, picker, network,
 * credentials, settings writes, or diary operations are used.
 */
@SdkSuppress(minSdkVersion = 30)
class AiScanKeyboardTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var decor: View
    private var scrollAttempts = 0
    private var successfulScrollActions = 0
    private var activeCase = "unknown"

    @Test
    fun shortHintStaysVisibleWhileTypingAndControlsRemainReachable() =
        checkHintLayout("rice", caseName = "short")

    @Test
    fun multilineHintStaysVisibleAndDismissalRestoresLayout() =
        checkHintLayout("Synthetic rice\nSynthetic chicken\nSynthetic vegetables\nSauce on the side\nOne plate", caseName = "multiline")

    private fun checkHintLayout(text: String, caseName: String) {
        activeCase = caseName
        val automation = instrumentation.uiAutomation
        val originalServiceInfo = automation.serviceInfo
        val originalServiceFlags = originalServiceInfo.flags
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        val applicationKoin = GlobalContext.get()
        val settings = object : UserPreferencesRepository<AiSettings> {
            override fun observe() = MutableStateFlow(AiSettings("synthetic-test-key", null, null, null))
            override suspend fun update(transform: AiSettings.() -> AiSettings) =
                error("Keyboard fixture must not persist settings")
        }
        val config = object : AppConfig by applicationKoin.get<AppConfig>() {
            override val aiApiKey = ""
            override val aiEndpoint = ""
            override val aiModel = ""
        }
        val scanner = object : AiFoodScanner {
            override suspend fun scan(jpeg: ByteArray, hint: String?): AiScanResult =
                error("Keyboard fixture must not submit a scan")
        }
        val model = AiScanViewModel(
            scanner,
            // Align is outside this test; no search operation is called.
            applicationKoin.get<FoodSearchUseCase>(),
            ObserveAiConfigured(settings, config),
        ).apply { onPhotoCaptured(syntheticJpeg()) }
        val isolated = koinApplication { modules(module { viewModel { model } }) }
        var backCount = 0
        try {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    activity.setContent {
                        KoinIsolatedContext(context = isolated) {
                            MaterialTheme {
                                AiScanScreen(
                                    onBack = { backCount++ },
                                    onSaveToQuickAdd = { _, _, _, _ -> error("Not used") },
                                    onAlignPickFood = { error("Not used") },
                                    onCreateCustomFood = { error("Not used") },
                                )
                            }
                        }
                    }
                }
                instrumentation.waitForIdleSync()
                val initialWindow = windowBounds()
                check(initialWindow.width() > 0 && initialWindow.height() > initialWindow.width()) {
                    "Device prerequisite: keyboard regression requires a settled portrait window; measured=$initialWindow"
                }
                val minimumHeight = onMain { (56 * decor.resources.displayMetrics.density).toInt() }
                val beforeKeyboard = inputContainer(findEditable(scroll = true, minimumHeight = minimumHeight)).bounds()
                assertTrue("Input border must be at least 56 dp before focus", beforeKeyboard.height() >= minimumHeight)
                click(findEditable())
                await("real Android keyboard shown") { keyboardVisible() }
                captureSyntheticHint("focus-$caseName")
                assertHintAboveKeyboard("focus", minimumHeight = beforeKeyboard.height())

                text.lines().forEachIndexed { index, line ->
                    if (index > 0) instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER)
                    instrumentation.sendStringSync(line)
                }
                await("synthetic hint reaches production ViewModel") { model.hint.text.toString() == text }
                assertHintAboveKeyboard("typed text", minimumHeight = beforeKeyboard.height())
                captureSyntheticHint(caseName)

                var expectedText = text
                if (caseName == "multiline") {
                    // Edit the first line through the editor's actual selection path.
                    assertTrue("Editor must support selecting the first hint character", findEditable().performAction(
                        AccessibilityNodeInfo.ACTION_SET_SELECTION,
                        Bundle().apply {
                            putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0)
                            putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, 0)
                        },
                    ))
                    await("focused editor cursor at the first hint character with keyboard open") {
                        model.hint.selection.start == 0 && model.hint.selection.end == 0 &&
                            editableNode()?.isFocused == true && keyboardVisible()
                    }
                    instrumentation.sendStringSync("Edited ")
                    expectedText = "Edited $text"
                    await("earlier-line edit preserved") { model.hint.text.toString() == expectedText }
                    assertHintAboveKeyboard("earlier-line edit", minimumHeight = beforeKeyboard.height())
                    captureSyntheticHint("multiline-earlier-edit")
                }

                val submit = findLabel(label(Res.string.action_submit), scroll = true)
                assertTrue("Submit stays enabled for synthetic captured/configured state", clickableAncestor(submit).isEnabled)
                assertAboveKeyboard(submit.bounds(), "Submit after scrolling")
                assertTrue("Scrolling must not dismiss keyboard", keyboardVisible())

                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                await("keyboard dismissed") { !keyboardVisible() }
                findEditable(scroll = true)
                assertEquals("Keyboard dismissal preserves entered hint", expectedText, model.hint.text.toString())
                // Return to the photo, then down to Submit. Both ends must remain reachable after
                // IME padding disappears, without a stale keyboard-sized blank tail.
                scrollToStart()
                findLabel(label(Res.string.neutral_tap_photo_to_retake), scroll = true)
                val restoredSubmit = findLabel(label(Res.string.action_submit), scroll = true)
                assertVisibleInWindow(restoredSubmit.bounds(), "Submit after dismissal")
                assertNoKeyboardSpaceAfterDismissal()
                scrollToStart()
                click(findLabel(label(Res.string.action_go_back)))
                assertEquals("Back remains usable", 1, backCount)
            }
        } finally {
            isolated.close()
            automation.serviceInfo = originalServiceInfo.apply { flags = originalServiceFlags }
            assertSame("Preserve the app global Koin container", applicationKoin, GlobalContext.get())
        }
    }

    private fun assertHintAboveKeyboard(stage: String, minimumHeight: Int) {
        await("hint input border and text fully visible above IME after $stage") {
            val field = editableNode() ?: return@await false
            val input = inputContainer(field)
            val bounds = input.bounds()
            val innerText = inputText(field).bounds()
            field.isVisibleToUser && input.isVisibleToUser && bounds.height() >= minimumHeight &&
                bounds.top >= windowBounds().top && bounds.bottom <= keyboardTop() &&
                !innerText.isEmpty && innerText.top >= bounds.top && innerText.bottom <= bounds.bottom
        }
        assertAboveKeyboard(inputContainer(findEditable()).bounds(), "Hint input border after $stage")
    }

    /** The merged EditText includes the label/support prose. Samsung exposes the outlined input
     * as its first non-text View child, followed by the actual inner editable text View.
     * Inspecting synthetic screenshots confirmed this border, rather than merged helper bounds.
     */
    private fun inputContainer(field: AccessibilityNodeInfo): AccessibilityNodeInfo =
        (0 until field.childCount).asSequence().mapNotNull(field::getChild)
            .firstOrNull { it.className?.toString() == "android.view.View" && !it.isTextInput() }
            ?: error("Missing outlined input container in Samsung accessibility tree")

    private fun inputText(field: AccessibilityNodeInfo): AccessibilityNodeInfo {
        val container = inputContainer(field).bounds()
        return (0 until field.childCount).asSequence().mapNotNull(field::getChild)
            .firstOrNull { node -> node.className?.toString() == "android.view.View" &&
                !node.isTextInput() && node.bounds().width() < container.width() && container.contains(node.bounds()) }
            ?: error("Missing inner editable text geometry")
    }

    private fun assertAboveKeyboard(bounds: Rect, description: String) {
        assertTrue("$description bounds=$bounds overlap IME top=${keyboardTop()}",
            !bounds.isEmpty && bounds.bottom <= keyboardTop())
    }

    /** Samsung's accessibility provider does not expose individual character bounds. Save only
     * this isolated synthetic fixture so the final short/multiline line can be visually checked.
     */
    private fun captureSyntheticHint(caseName: String) {
        // IME visibility can become true before Compose has finished remeasuring and drawing.
        SystemClock.sleep(1_000)
        instrumentation.waitForIdleSync()
        val heading = label(Res.string.headline_ai_scanning)
        check(root()?.let { findNode(it) { node -> node.text?.toString() == heading } } != null) {
            "Capture requires the synthetic scanner heading in the target app window"
        }
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot()) {
            "Could not capture synthetic scanner keyboard evidence"
        }
        try {
            val directory = File(
                requireNotNull(instrumentation.targetContext.getExternalFilesDir(null)),
                "foodus-003-keyboard",
            )
            check(directory.isDirectory || directory.mkdirs()) { "Could not create synthetic evidence directory" }
            val dimensions = onMain { "${decor.width}x${decor.height}" }
            File(directory, "$caseName-$dimensions.png").outputStream().use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) { "Could not encode synthetic screenshot" }
            }
        } finally { bitmap.recycle() }
    }

    private fun assertNoKeyboardSpaceAfterDismissal() {
        var settled = false
        for (attempt in 0 until 12) {
            if (!scroll(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                settled = true
                break
            }
            SystemClock.sleep(200)
        }
        assertTrue("Scanner scroll must settle at the bottom after dismissal", settled)
        val viewport = scrollViewport().bounds()
        val submit = clickableAncestor(findLabel(label(Res.string.action_submit))).bounds()
        val maximumGap = onMain { (64 * decor.resources.displayMetrics.density).toInt() }
        val gap = viewport.bottom - submit.bottom
        assertTrue("Stale keyboard space after dismissal: viewport=$viewport Submit=$submit gap=$gap allowed=$maximumGap",
            gap in 0..maximumGap)
    }

    private fun assertVisibleInWindow(bounds: Rect, description: String) {
        assertTrue("$description bounds=$bounds outside window=${windowBounds()}",
            !bounds.isEmpty && windowBounds().contains(bounds))
    }

    private fun keyboardVisible(): Boolean = onMain { decor.rootWindowInsets?.isVisible(WindowInsets.Type.ime()) == true }

    private fun keyboardTop(): Int = onMain {
        val location = IntArray(2).also(decor::getLocationOnScreen)
        location[1] + decor.height - (decor.rootWindowInsets?.getInsets(WindowInsets.Type.ime())?.bottom ?: 0)
    }

    private fun windowBounds(): Rect = onMain {
        val location = IntArray(2).also(decor::getLocationOnScreen)
        Rect(location[0], location[1], location[0] + decor.width, location[1] + decor.height)
    }

    private fun <T> onMain(block: () -> T): T {
        var result: T? = null
        instrumentation.runOnMainSync { result = block() }
        return requireNotNull(result)
    }

    private fun findEditable(scroll: Boolean = false, minimumHeight: Int = 0): AccessibilityNodeInfo =
        find("editable hint (minimum height $minimumHeight px)", scroll) { it.isTextInput() && it.bounds().height() >= minimumHeight }

    private fun AccessibilityNodeInfo.isTextInput(): Boolean =
        isEditable || className?.toString() == "android.widget.EditText" ||
            actionList.any { it.id == AccessibilityNodeInfo.ACTION_SET_TEXT }

    private fun editableNode(): AccessibilityNodeInfo? = root()?.let { findNode(it) { node -> node.isTextInput() } }

    private fun findLabel(text: String, scroll: Boolean = false): AccessibilityNodeInfo =
        find(text, scroll) { it.text?.toString()?.lines()?.contains(text) == true || it.contentDescription?.toString() == text }

    private fun find(description: String, scroll: Boolean, matches: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo {
        var found: AccessibilityNodeInfo? = null
        await("visible $description") {
            found = root()?.let { findNode(it) { node -> matches(node) && node.isVisibleToUser && !node.bounds().isEmpty } }
            if (found == null && scroll) scroll(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            found != null
        }
        return requireNotNull(found)
    }

    private fun root(): AccessibilityNodeInfo? {
        if (Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.clearCache()
        val packageName = instrumentation.targetContext.packageName
        val windows = instrumentation.uiAutomation.windows
        return windows.asSequence()
            .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
            .sortedByDescending { it.isFocused }
            .mapNotNull { it.root }
            .firstOrNull { it.packageName?.toString() == packageName }
            ?: instrumentation.uiAutomation.rootInActiveWindow?.takeIf {
                it.packageName?.toString() == packageName
            }
    }

    private fun findNode(node: AccessibilityNodeInfo, matches: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        if (matches(node)) return node
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            findNode(child, matches)?.let { return it }
        }
        return null
    }

    private fun AccessibilityNodeInfo.bounds(): Rect = Rect().also(::getBoundsInScreen)

    private fun clickableAncestor(node: AccessibilityNodeInfo): AccessibilityNodeInfo {
        var current = node
        while (!current.isClickable) current = requireNotNull(current.parent) { "Missing clickable ancestor" }
        return current
    }

    private fun click(node: AccessibilityNodeInfo) {
        assertTrue(clickableAncestor(node).performAction(AccessibilityNodeInfo.ACTION_CLICK))
        instrumentation.waitForIdleSync()
    }

    private fun scroll(action: Int): Boolean {
        scrollAttempts++
        val forward = action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        val viewport = scrollViewport().bounds()
        val margin = onMain { (16 * decor.resources.displayMetrics.density).toFloat() }
        val x = viewport.exactCenterX()
        val top = viewport.top + margin
        // Start in real content (normally the photo), away from system edge gestures and
        // the focused text field near the viewport bottom.
        val bottom = viewport.top + viewport.height() * 0.65f
        check(bottom - top > 100) { "Scanner viewport too small for a touch swipe: $viewport" }
        val event = try {
            instrumentation.uiAutomation.executeAndWaitForEvent(
                { injectSwipe(x, if (forward) bottom else top, if (forward) top else bottom) },
                { event -> event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED &&
                    event.packageName?.toString() == instrumentation.targetContext.packageName },
                1_500,
            )
        } catch (_: TimeoutException) { null }
        instrumentation.waitForIdleSync()
        if (event != null) {
            successfulScrollActions++
            try {
                check(event.scrollY >= 0 && event.maxScrollY >= 0) { "Scroll event omitted position/range" }
                return if (forward) event.scrollY < event.maxScrollY else event.scrollY > 0
            } finally { event.recycle() }
        }
        // A missing scroll event is accepted only with independent end-content geometry.
        // The measured bottom gap is also asserted after dismissal by the caller.
        val currentViewport = scrollViewport()
        val bounds = currentViewport.bounds()
        if (forward) {
            val submitLabel = label(Res.string.action_submit)
            val submit = root()?.let { findNode(it) { node ->
                node.text?.toString() == submitLabel && node.isVisibleToUser
            } }?.let(::clickableAncestor)?.bounds()
            val maximumGap = onMain { (64 * decor.resources.displayMetrics.density).toInt() }
            check(submit != null && !submit.isEmpty && bounds.bottom - submit.bottom in 0..maximumGap) {
                "Touch swipe produced no scroll event and bottom is not visible: viewport=$bounds Submit=$submit"
            }
        } else {
            val first = currentViewport.getChild(0)
            check(first != null && first.isVisibleToUser && first.bounds().top in bounds.top..(bounds.top + margin.toInt() * 2)) {
                "Touch swipe produced no scroll event and first scanner content is not at the top"
            }
        }
        return false
    }

    private fun scrollViewport(): AccessibilityNodeInfo = requireNotNull(root()?.let { findNode(it) { node ->
        node.className?.toString() == "android.widget.ScrollView" || (!node.isTextInput() && node.isScrollable)
    } }) { "Missing scanner scroll viewport" }

    private fun injectSwipe(x: Float, startY: Float, endY: Float) {
        val displayId = onMain { requireNotNull(decor.display).displayId }
        // Use Android's native input driver after synthetic MotionEvents were accepted but
        // produced no observable scroll. Coordinates and display are measured fixture values.
        val command = "input touchscreen -d $displayId swipe ${x.toInt()} ${startY.toInt()} ${x.toInt()} ${endY.toInt()} 360"
        val descriptor = instrumentation.uiAutomation.executeShellCommand(command)
        val output = ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText() }
        check(output.isBlank()) { "Android native swipe reported an error" }
    }

    private fun scrollToStart() {
        repeat(12) {
            if (!scroll(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) return
            SystemClock.sleep(150)
        }
    }

    private fun await(description: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 8_000
        while (!condition()) {
            if (SystemClock.uptimeMillis() >= deadline) {
                captureSyntheticHint("timeout-$activeCase")
                error("Timed out waiting for $description; hint=${editableNode()?.bounds()}, IME top=${keyboardTop()}, window=${windowBounds()}, scroll actions=$successfulScrollActions/$scrollAttempts; tree=${diagnosticTree()}")
            }
            SystemClock.sleep(150)
        }
        instrumentation.waitForIdleSync()
    }

    private fun label(resource: StringResource): String = runBlocking { getString(resource) }

    /** Geometry/action metadata only: never include text, descriptions, or app settings in logs. */
    private fun diagnosticTree(): String = buildString {
        var count = 0
        fun visit(node: AccessibilityNodeInfo, depth: Int) {
            if (count++ >= 40) return
            append("[$depth:${node.className},editable=${node.isEditable},visible=${node.isVisibleToUser},bounds=${node.bounds()},actions=${node.actionList.map { it.id }}]")
            for (index in 0 until node.childCount) node.getChild(index)?.let { visit(it, depth + 1) }
        }
        root()?.let { visit(it, 0) } ?: append("no target application window")
    }

    private fun syntheticJpeg(): ByteArray {
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        return try {
            ByteArrayOutputStream().use { stream ->
                bitmap.eraseColor(android.graphics.Color.GREEN)
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream))
                stream.toByteArray()
            }
        } finally { bitmap.recycle() }
    }
}
