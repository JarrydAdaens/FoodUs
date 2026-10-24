package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import android.app.Instrumentation
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.View
import android.view.WindowInsets
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import foodyou.app.generated.resources.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.Assert.*

/** Shared synthetic-host driver. Never reads owner values; diagnostics omit UI text and secrets. */
internal class ScanTestDriver(
    private val instrumentation: Instrumentation,
    private val decorProvider: () -> View?,
    private val activityPackageProvider: () -> String,
    private val fixtureCrashProvider: () -> String?,
) {
    private val decor: View get() = requireNotNull(decorProvider())
    private val activityPackage: String get() = activityPackageProvider()
    private val fixtureCrash: String? get() = fixtureCrashProvider()

    fun awaitField(resource: StringResource, expected: String) {
        await("next synthetic form value") { field(label(resource)).text?.toString().orEmpty() == expected }
        assertField(resource, expected)
    }

    fun control(text: String): AccessibilityNodeInfo {
        var node: AccessibilityNodeInfo? = findText(text)
        while (node != null) {
            if (node.className?.toString() == "android.widget.Button" || node.isClickable) return node
            node = node.parent
        }
        error("Missing synthetic control; ${diagnosticGeometry()}")
    }

    fun assertField(resource: StringResource, expected: String) {
        val field = field(label(resource))
        val actual = field.text?.toString().orEmpty()
        assertEquals("Editable value for ${label(resource)}", expected, actual)
    }

    fun setField(resource: StringResource, value: String) {
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

    fun findText(text: String): AccessibilityNodeInfo = find("text $text") {
        it.text?.toString()?.lines()?.contains(text) == true || it.contentDescription?.toString() == text
    }

    fun clickText(text: String) {
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
        if (decorProvider() == null) return null
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

    fun descendants(node: AccessibilityNodeInfo): Sequence<AccessibilityNodeInfo> = sequence {
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

    fun <T> onMain(block: () -> T): T {
        var result: T? = null
        instrumentation.runOnMainSync { result = block() }
        return requireNotNull(result)
    }

    /** Bounded window/geometry/action metadata only. Package and resource identifiers identify
     * system overlays; labels, values, descriptions, exception messages and settings are omitted.
     */
    fun diagnosticGeometry(): String = buildString {
        val targetPackage = instrumentation.targetContext.packageName
        append("targetPackage=$targetPackage activityPackage=$activityPackage samePackage=${activityPackage == targetPackage}; ")
        if (decorProvider() != null) {
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

    fun await(description: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 15_000
        while (!condition()) {
            check(SystemClock.uptimeMillis() < deadline) { "Timed out waiting for $description" }
            SystemClock.sleep(100)
        }
    }

    fun label(resource: StringResource) = runBlocking { getString(resource) }

}
