package com.maksimowiczm.foodyou.app.ui.developer.inspection

import android.os.SystemClock
import android.os.ParcelFileDescriptor
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.common.compose.utility.ClipboardManager
import com.maksimowiczm.foodyou.common.compose.utility.ClipboardManagerProvider
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import org.junit.After

/** Real FoodYouHomeCard gesture pilot in an isolated, synthetic ActivityScenario content root. */
@SdkSuppress(minSdkVersion = 31)
class UiNameInspectorTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var originalServiceFlags = 0

    @Before fun enableWindowInspection() {
        originalServiceFlags = instrumentation.uiAutomation.serviceInfo.flags
        instrumentation.uiAutomation.serviceInfo = instrumentation.uiAutomation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
    }

    @After fun restoreWindowInspection() {
        instrumentation.uiAutomation.serviceInfo = instrumentation.uiAutomation.serviceInfo.apply {
            flags = originalServiceFlags
        }
    }

    @Test
    fun deepHoldSelectsNestedTextSuppressesLongClickAndRestoresNativeBehaviorOnDisable() {
        val inspector = AtomicReference<UiInspectorState?>()
        val bounds = AtomicReference<Rect?>()
        val clicks = AtomicInteger()
        val longClicks = AtomicInteger()
        var enabled by mutableStateOf(true)
        launchActivity<DeveloperActivity>().use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    UiInspectionContent(enabled, false, {}) {
                        val currentInspector = LocalUiInspector.current
                        SideEffect { inspector.set(currentInspector) }
                        Column(Modifier.fillMaxSize().padding(24.dp).uiName(descriptor("pilot.container"))) {
                            FoodYouHomeCard(
                                onClick = { clicks.incrementAndGet() },
                                onLongClick = { longClicks.incrementAndGet() },
                            ) {
                                Text(
                                    text = "Synthetic inspection target",
                                    modifier = Modifier.padding(24.dp)
                                        .onGloballyPositioned { bounds.set(it.boundsInWindow()) }
                                        .uiName(
                                            "pilot.card.label",
                                            "androidx.compose.material3.Text",
                                            "com.maksimowiczm.foodyou.app.ui.developer.inspection.UiNameInspectorTest",
                                            "app/src/androidInstrumentedTest/kotlin/com/maksimowiczm/foodyou/app/ui/developer/inspection/UiNameInspectorTest.kt",
                                        ),
                                )
                            }
                        }
                    }
                }
            }
            await { inspector.get() != null && bounds.get() != null }
            fun event(action: Int, down: Long) {
                scenario.onActivity { activity ->
                    val point = bounds.get()!!.center
                    MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, point.x, point.y, 0).let {
                        activity.dispatchTouchEvent(it)
                        it.recycle()
                    }
                }
            }
            val down = SystemClock.uptimeMillis()
            event(MotionEvent.ACTION_DOWN, down)
            SystemClock.sleep(800)
            assertEquals("Native long press must not navigate while inspection is enabled", 0, longClicks.get())
            assertNull(onMain { inspector.get()!!.snapshot })
            await { onMain { inspector.get()?.snapshot != null } }
            assertEquals("pilot.card.label", onMain { inspector.get()!!.snapshot!!.copyText(false) })
            assertTrue(onMain { inspector.get()!!.snapshot!!.ancestors.any { it.descriptor.name == "pilot.container" } })
            event(MotionEvent.ACTION_UP, down)
            assertEquals("Inspected hold must not click on release", 0, clicks.get())
            assertEquals(0, longClicks.get())
            await { onMain { inspector.get()!!.selectedTarget == null } }
            assertEquals("Outline expires while the copied snapshot stays available", "pilot.card.label",
                onMain { inspector.get()!!.snapshot!!.copyText(false) })

            val tap = SystemClock.uptimeMillis()
            event(MotionEvent.ACTION_DOWN, tap)
            SystemClock.sleep(60)
            event(MotionEvent.ACTION_UP, tap)
            await { clicks.get() == 1 }

            val abandoned = SystemClock.uptimeMillis()
            event(MotionEvent.ACTION_DOWN, abandoned)
            SystemClock.sleep(1_000)
            event(MotionEvent.ACTION_UP, abandoned)
            assertEquals("An abandoned inspection hold must not activate the card", 1, clicks.get())

            scenario.onActivity { enabled = false }
            await { inspector.get() == null }
            val normal = SystemClock.uptimeMillis()
            event(MotionEvent.ACTION_DOWN, normal)
            await { longClicks.get() == 1 }
            event(MotionEvent.ACTION_UP, normal)
            assertEquals(1, clicks.get())
        }
    }

    @Test
    fun repeatedRowsUseNamedParentOccurrenceAndModalCopySurvivesDismissal() {
        val inspector = AtomicReference<UiInspectorState?>()
        val bounds = AtomicReference<Rect?>()
        val touchRoot = AtomicReference<View?>()
        val copied = AtomicReference<String?>()
        var modal by mutableStateOf(false)
        var backgroundVersion by mutableStateOf(0)
        val clipboard = object : ClipboardManager {
            override fun copy(label: String, text: String) { copied.set(text) }
            override fun paste(): String? = null
        }
        launchActivity<DeveloperActivity>().use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    ClipboardManagerProvider(clipboard) {
                        UiInspectionContent(true, true, {}) {
                            val currentInspector = LocalUiInspector.current
                            SideEffect { inspector.set(currentInspector) }
                            UiSourceScope(descriptor("pilot.screen", "com.example.PilotScreen").copy(owner = "com.example.Host")) {
                                LazyColumn(Modifier.fillMaxSize().uiName(descriptor("pilot.list", "androidx.compose.foundation.lazy.LazyColumn"))) {
                                    item {
                                        if (backgroundVersion == 0) Text("Unrelated old target",
                                            Modifier.uiName(descriptor("pilot.background.old", "androidx.compose.material3.Text")))
                                        else Text("Unrelated new target",
                                            Modifier.uiName(descriptor("pilot.background.new", "androidx.compose.material3.Text")))
                                    }
                                    items(3) { index ->
                                        // Deliberate unnamed layout wrapper: occurrence must use named ancestry.
                                        Box {
                                            Column(Modifier.fillMaxWidth().padding(12.dp).uiName(descriptor("pilot.row"))) {
                                                val view = LocalView.current
                                                Text("Synthetic row $index", Modifier.padding(12.dp)
                                                    .onGloballyPositioned {
                                                        if (index == 1 && !modal) {
                                                            bounds.set(it.boundsInWindow())
                                                            touchRoot.set(view.rootView)
                                                        }
                                                    }.uiName(descriptor("pilot.row.label", "androidx.compose.material3.Text")))
                                            }
                                        }
                                    }
                                }
                                if (modal) UiSourceScope(descriptor("pilot.dialog", "androidx.compose.ui.window.Dialog")) {
                                    Dialog(onDismissRequest = { modal = false }) {
                                        val view = LocalView.current
                                        Text("Synthetic modal target", Modifier.padding(24.dp)
                                            .onGloballyPositioned {
                                                bounds.set(it.boundsInWindow())
                                                touchRoot.set(view.rootView)
                                            }.uiName(descriptor("pilot.dialog.label", "androidx.compose.material3.Text")))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            await { bounds.get() != null && touchRoot.get() != null }
            hold(touchRoot.get()!!, bounds.get()!!)
            val row = onMain { inspector.get()!!.snapshot!! }
            assertEquals("pilot.row.label", row.copyText(false))
            assertEquals(2, row.ancestors.single { it.descriptor.name == "pilot.row" }.occurrence)
            assertEquals("com.example.PilotScreen", row.screen.composable)
            assertEquals("com.example.Host", row.screen.owner)
            var decor: View? = null
            scenario.onActivity { decor = it.window.decorView }
            val driver = ScanTestDriver(instrumentation, { decor }, { instrumentation.targetContext.packageName }, { null })
            driver.clickText("Copy UI locator")
            assertEquals(row.copyText(true), copied.get())
            writeEvidence("repeated", requireNotNull(copied.get()))

            val activityRoot = itRoot(scenario)
            scenario.onActivity { modal = true }
            await { onMain { touchRoot.get() !== activityRoot && touchRoot.get()?.hasWindowFocus() == true } }
            instrumentation.waitForIdleSync()
            hold(touchRoot.get()!!, bounds.get()!!) {
                scenario.onActivity { backgroundVersion = 1 }
            }
            val dialog = onMain { inspector.get()!!.snapshot!! }
            assertEquals("pilot.dialog.label", dialog.copyText(false))
            assertEquals("androidx.compose.ui.window.Dialog", dialog.surface.composable)
            assertEquals("com.example.PilotScreen", dialog.screen.composable)
            scenario.onActivity { modal = false }
            instrumentation.waitForIdleSync()
            assertEquals(dialog.copyText(true), onMain { inspector.get()!!.snapshot!!.copyText(true) })
            driver.clickText("Copy UI locator")
            assertEquals(dialog.copyText(true), copied.get())
            writeEvidence("modal", requireNotNull(copied.get()))
        }
    }

    @Test
    fun earlyReleaseMovementCancellationAndDisableDoNotCapture() {
        val inspector = AtomicReference<UiInspectorState?>()
        val bounds = AtomicReference<Rect?>()
        var enabled by mutableStateOf(true)
        launchActivity<DeveloperActivity>().use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    UiInspectionContent(enabled, false, {}) {
                        val state = LocalUiInspector.current
                        SideEffect { inspector.set(state) }
                        Text("Synthetic cancellation target", Modifier.fillMaxWidth().height(200.dp)
                            .onGloballyPositioned { bounds.set(it.boundsInWindow()) }
                            .uiName(descriptor("pilot.cancel", "androidx.compose.material3.Text")))
                    }
                }
            }
            await { inspector.get() != null && bounds.get() != null }
            fun event(action: Int, down: Long, move: Float = 0f) {
                scenario.onActivity { activity ->
                    val point = bounds.get()!!.center
                    MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, point.x + move, point.y, 0).let {
                        activity.dispatchTouchEvent(it)
                        it.recycle()
                    }
                }
            }
            var down = SystemClock.uptimeMillis()
            event(MotionEvent.ACTION_DOWN, down)
            SystemClock.sleep(100)
            event(MotionEvent.ACTION_UP, down)
            SystemClock.sleep(3_100)
            assertNull(onMain { inspector.get()!!.snapshot })

            down = SystemClock.uptimeMillis()
            event(MotionEvent.ACTION_DOWN, down)
            event(MotionEvent.ACTION_MOVE, down, 120f)
            SystemClock.sleep(3_100)
            assertNull(onMain { inspector.get()!!.snapshot })
            event(MotionEvent.ACTION_UP, down, 120f)

            down = SystemClock.uptimeMillis()
            event(MotionEvent.ACTION_DOWN, down)
            event(MotionEvent.ACTION_CANCEL, down)
            SystemClock.sleep(3_100)
            assertNull(onMain { inspector.get()!!.snapshot })

            down = SystemClock.uptimeMillis()
            event(MotionEvent.ACTION_DOWN, down)
            scenario.onActivity { enabled = false }
            await { inspector.get() == null }
            event(MotionEvent.ACTION_UP, down)
            scenario.onActivity { enabled = true }
            await { inspector.get() != null }
            SystemClock.sleep(3_100)
            assertNull(onMain { inspector.get()!!.snapshot })
        }
    }

    private fun itRoot(scenario: androidx.test.core.app.ActivityScenario<DeveloperActivity>): View? {
        var root: View? = null
        scenario.onActivity { root = it.window.decorView }
        return root
    }

    private fun writeEvidence(name: String, xml: String) {
        require(name in setOf("repeated", "modal"))
        val pipes = instrumentation.uiAutomation.executeShellCommandRw("tee /data/local/tmp/foodus-story29-$name.xml")
        ParcelFileDescriptor.AutoCloseOutputStream(pipes[1]).use { it.write(xml.toByteArray(Charsets.UTF_8)) }
        val persisted = ParcelFileDescriptor.AutoCloseInputStream(pipes[0]).bufferedReader().use { it.readText() }
        assertEquals("Persist actual structural clipboard evidence", xml, persisted)
    }

    private fun hold(root: View, bounds: Rect, duringHold: () -> Unit = {}) {
        val down = SystemClock.uptimeMillis()
        fun event(action: Int) = onMain {
            MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, bounds.center.x, bounds.center.y, 0).let {
                root.dispatchTouchEvent(it)
                it.recycle()
            }
        }
        event(MotionEvent.ACTION_DOWN)
        SystemClock.sleep(500)
        duringHold()
        SystemClock.sleep(2_700)
        event(MotionEvent.ACTION_UP)
        instrumentation.waitForIdleSync()
    }

    private fun await(condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 6_000
        while (!condition() && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(50)
        assertTrue("Synthetic inspector condition within timeout", condition())
    }

    private fun <T> onMain(block: () -> T): T {
        val result = AtomicReference<T>()
        instrumentation.runOnMainSync { result.set(block()) }
        return result.get()
    }

    private fun descriptor(name: String, composable: String = "androidx.compose.foundation.layout.Column") =
        UiSourceDescriptor(name, composable,
            "com.maksimowiczm.foodyou.app.ui.developer.inspection.UiNameInspectorTest",
            "app/src/androidInstrumentedTest/kotlin/com/maksimowiczm/foodyou/app/ui/developer/inspection/UiNameInspectorTest.kt")
}
