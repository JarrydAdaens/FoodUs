package com.maksimowiczm.foodyou.app.ui.settings.ai

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.PixelCopy
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.Lifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.ai.domain.AiConnectionValidator
import com.maksimowiczm.foodyou.ai.domain.AiRuntimeConfig
import com.maksimowiczm.foodyou.ai.domain.AiSettings
import com.maksimowiczm.foodyou.ai.domain.AiValidationResult
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import foodyou.app.generated.resources.*
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Assert.*
import org.junit.Test
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Real screen/VM with synthetic preferences and validator. No owner AI settings, clipboard,
 * diary, photos or network are touched. The host inherits the accepted secureScreen boolean read.
 * Diagnostics deliberately omit credential values and accessibility-tree text.
 */
@SdkSuppress(minSdkVersion = 30)
class AiSettingsKeyPrivacyTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private var window: Window? = null
    private var activityPackage = ""
    private lateinit var fixture: Fixture
    private var captureCase = ""
    private var captureRun = 0L
    private var captureState = 0
    private val driver = ScanTestDriver(instrumentation, { decor }, { activityPackage }, { null })

    @Test fun masksByDefaultRevealsDeliberatelyAndMasksRetainedAndFreshReentry() = withHost("navigation") { fixture, _ ->
        openAi()
        assertMasked(DUMMY_KEY)
        val originalModel = driver.onMain { fixture.model }
        toggleReveal()
        assertVisible(DUMMY_KEY)
        toggleHide()
        assertMasked(DUMMY_KEY)
        toggleReveal()

        driver.clickText("Cover AI")
        driver.findText("Synthetic covered destination")
        driver.clickText("Return to AI")
        driver.findText(driver.label(Res.string.headline_ai_settings))
        assertSame("Retained destination keeps its real ViewModel", originalModel,
            driver.onMain { fixture.model })
        assertMasked(DUMMY_KEY)

        toggleReveal()
        driver.clickText(driver.label(Res.string.action_go_back))
        driver.findText("Synthetic settings home")
        openAi()
        assertMasked(DUMMY_KEY)
        assertNotSame("Fresh entry has a new real ViewModel", originalModel,
            driver.onMain { fixture.model })
        assertEquals("Visibility actions never write settings", 0, fixture.writes)
        assertEquals("Visibility actions never validate", 0, fixture.probes.size)
    }

    @Test fun recreationRestoresNavigationAndDraftButNeverRevealsTheKey() = withHost("recreation") { fixture, scenario ->
        openAi()
        toggleReveal()
        replaceKey(DUMMY_EDITED)
        driver.clickText("Mark restoration")
        driver.findText("Synthetic restored sentinel 1")
        val originalModel = driver.onMain { fixture.model }
        val originalMounts = fixture.mounts
        scenario.recreate()
        driver.await("recreated synthetic activity content") { fixture.mounts > originalMounts }
        // The sentinel and restored AI route prove saved-state restoration is actually mounted.
        driver.findText("Synthetic restored sentinel 1")
        driver.findText(driver.label(Res.string.headline_ai_settings))
        assertSame("Activity recreation retains the draft ViewModel", originalModel,
            driver.onMain { fixture.model })
        assertMasked(DUMMY_EDITED)
        toggleReveal()
        assertVisible(DUMMY_EDITED)
        assertEquals("Recreation does not save the draft", 0, fixture.writes)
    }

    @Test fun backgroundReturnMasksAndEditingPrivateClipboardValidationAndSaveKeepRealDrafts() =
        withHost("editing-background") { fixture, scenario ->
            openAi()
            toggleReveal()
            replaceKey(DUMMY_EDITED)
            val key = keyField()
            assertTrue("Native secure editor supports selection", key.performAction(
                AccessibilityNodeInfo.ACTION_SET_SELECTION,
                Bundle().apply {
                    putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0)
                    putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, 3)
                },
            ))
            driver.await("real synthetic draft selection") {
                driver.onMain { fixture.model.apiKey.selection.start == 0 &&
                    fixture.model.apiKey.selection.end == 3 }
            }

            replaceKey("")
            driver.onMain { fixture.clipboard.entry = ClipEntry(ClipData.newPlainText("synthetic", DUMMY_PASTED)) }
            assertTrue("Native secure editor accepts paste", keyField().performAction(
                AccessibilityNodeInfo.ACTION_PASTE))
            driver.await("private clipboard pasted into actual draft") {
                driver.onMain { fixture.model.apiKey.text.toString() == DUMMY_PASTED }
            }
            assertTrue("Paste used the isolated Compose clipboard", fixture.clipboard.reads > 0)
            assertVisible(DUMMY_PASTED)
            hideKeyboard()

            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            driver.findText(driver.label(Res.string.headline_ai_settings))
            assertMasked(DUMMY_PASTED)
            toggleReveal()
            assertVisible(DUMMY_PASTED)
            toggleHide()

            driver.clickText(driver.label(Res.string.action_validate))
            driver.findText(driver.label(Res.string.ai_validation_success))
            assertEquals("Actual validation uses the edited synthetic draft", listOf(
                Probe(DUMMY_PASTED, AiRuntimeConfig.DEFAULT_ENDPOINT, AiRuntimeConfig.DEFAULT_MODEL)),
                fixture.probes.toList())
            driver.clickText(driver.label(Res.string.action_save))
            driver.await("actual save updates isolated settings") { fixture.writes == 1 }
            assertEquals(fixture.initial.copy(apiKey = DUMMY_PASTED), fixture.preferences.value)
            assertMasked(DUMMY_PASTED)
        }

    private fun openAi() {
        driver.clickText("Open AI")
        driver.findText(driver.label(Res.string.headline_ai_settings))
        driver.await("synthetic key loaded") { keyField().text?.isNotEmpty() == true }
    }

    private fun toggleReveal() = driver.clickText(driver.label(Res.string.action_show_password))
    private fun toggleHide() = driver.clickText(driver.label(Res.string.action_hide_password))

    private fun assertMasked(dummy: String) {
        driver.findText(driver.label(Res.string.action_show_password))
        val node = keyField()
        assertTrue("Native key field retains password semantics", node.isPassword)
        assertTrue("Masking preserves the real synthetic draft",
            driver.onMain { fixture.model.apiKey.text.toString() == dummy })
        // Resolved Compose 1.11.0-alpha05 exposes output text to privileged accessibility;
        // Hidden changes visual codepoints only. Inspect every captured field image for bullets.
        captureSyntheticKeyField("masked")
    }

    private fun assertVisible(dummy: String) {
        driver.findText(driver.label(Res.string.action_hide_password))
        driver.await("explicit reveal of synthetic draft") { keyField().text?.toString() == dummy }
        assertTrue("Reveal preserves secure password semantics", keyField().isPassword)
        assertTrue("Reveal preserves the real synthetic draft",
            driver.onMain { fixture.model.apiKey.text.toString() == dummy })
        captureSyntheticKeyField("revealed")
    }

    private fun replaceKey(value: String) {
        assertTrue("Native secure editor accepts editing", keyField().performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value) },
        ))
        driver.await("synthetic editor changed") { keyField().text?.toString().orEmpty() == value }
    }

    private fun keyField(): AccessibilityNodeInfo {
        val label = driver.label(Res.string.headline_api_key)
        driver.findText(label)
        if (Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.clearCache()
        val displayId = driver.onMain { requireNotNull(decor?.display).displayId }
        return instrumentation.uiAutomation.windowsOnAllDisplays.get(displayId).orEmpty()
            .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
            .mapNotNull { it.root }
            .filter { it.packageName?.toString() == instrumentation.targetContext.packageName }
            .flatMap { driver.descendants(it).toList() }
            .firstOrNull { node -> node.isVisibleToUser && node.isEditable &&
                (node.hintText?.toString()?.lines()?.contains(label) == true ||
                    driver.descendants(node).any { it.text?.toString()?.lines()?.contains(label) == true }) }
            ?: error("Synthetic secure key field unavailable; ${driver.diagnosticGeometry()}")
    }

    private fun hideKeyboard() {
        driver.onMain { decor?.windowInsetsController?.hide(WindowInsets.Type.ime()); Unit }
        driver.await("synthetic key keyboard hidden") {
            driver.onMain { decor?.rootWindowInsets?.isVisible(WindowInsets.Type.ime()) != true }
        }
    }

    /** Copy only the synthetic key-field region from this Activity's actual rendered window.
     * This excludes other apps, system UI, and the rest of the synthetic settings surface.
     * Unique case/run/state/viewport names preserve every required visual acceptance frame.
     */
    private fun captureSyntheticKeyField(state: String) {
        hideKeyboard()
        instrumentation.waitForIdleSync()
        SystemClock.sleep(200)
        val screenBounds = Rect().also(keyField()::getBoundsInScreen)
        val geometry = driver.onMain {
            val view = requireNotNull(decor)
            val onScreen = IntArray(2).also(view::getLocationOnScreen)
            val inWindow = IntArray(2).also(view::getLocationInWindow)
            val bounds = Rect(screenBounds).apply {
                offset(inWindow[0] - onScreen[0], inWindow[1] - onScreen[1])
            }
            assertTrue("Synthetic key capture stays inside host window",
                Rect(inWindow[0], inWindow[1], inWindow[0] + view.width, inWindow[1] + view.height).contains(bounds))
            Triple(bounds, view.width, view.height)
        }
        val bounds = geometry.first
        assertTrue("Synthetic key field has visible capture area", bounds.width() > 0 && bounds.height() > 0)
        val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
        val latch = CountDownLatch(1)
        var result = -1
        try {
            driver.onMain {
                PixelCopy.request(requireNotNull(window), bounds, bitmap,
                    { result = it; latch.countDown() }, Handler(Looper.getMainLooper()))
            }
            assertTrue("Synthetic field capture completed", latch.await(5, TimeUnit.SECONDS))
            assertEquals("Synthetic field capture succeeded", PixelCopy.SUCCESS, result)
            val directory = File(requireNotNull(instrumentation.targetContext.getExternalFilesDir(null)),
                "foodus-010-synthetic")
            check(directory.isDirectory || directory.mkdirs())
            captureState++
            val name = "$captureCase-${geometry.second}x${geometry.third}-$captureRun-$captureState-$state.png"
            File(directory, name).outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        } finally { bitmap.recycle() }
    }

    private fun withHost(caseName: String, checkFlow: (Fixture, ActivityScenario<DeveloperActivity>) -> Unit) {
        fixture = Fixture()
        captureCase = caseName
        captureRun = SystemClock.elapsedRealtime()
        captureState = 0
        val applicationKoin = GlobalContext.get()
        val isolated = koinApplication {
            modules(module {
                viewModel {
                    AiSettingsViewModel(fixture.repository, fixture.validator).also { fixture.model = it }
                }
            })
        }
        val application = instrumentation.targetContext.applicationContext as Application
        val callback = object : Application.ActivityLifecycleCallbacks {
            // Mount before first composition, including the recreated Activity. setContent called
            // after RESUMED would consume a new owner and silently miss Compose saved-state restore.
            override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity !is DeveloperActivity) return
                window = activity.window
                decor = activity.window.decorView
                activityPackage = activity.packageName
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                activity.setContent {
                    KoinIsolatedContext(context = isolated) {
                        CompositionLocalProvider(LocalClipboard provides fixture.clipboard,
                            LocalClipboardManager provides fixture.clipboard) {
                            MaterialTheme { SyntheticNavigation(fixture) }
                        }
                    }
                }
                fixture.mounts++
            }
            override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        }
        val automation = instrumentation.uiAutomation
        val originalInfo = automation.serviceInfo
        val originalFlags = originalInfo.flags
        application.registerActivityLifecycleCallbacks(callback)
        try {
            automation.serviceInfo = automation.serviceInfo.apply {
                flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            }
            launchActivity<DeveloperActivity>().use { scenario ->
                instrumentation.waitForIdleSync()
                checkFlow(fixture, scenario)
            }
        } finally {
            application.unregisterActivityLifecycleCallbacks(callback)
            decor = null
            window = null
            isolated.close()
            automation.serviceInfo = originalInfo.apply { flags = originalFlags }
            assertSame("Global application Koin survives", applicationKoin, GlobalContext.get())
        }
    }

    @Composable
    private fun SyntheticNavigation(fixture: Fixture) {
        val navController = rememberNavController()
        var sentinel by rememberSaveable { mutableIntStateOf(0) }
        Column(Modifier.safeDrawingPadding()) {
            TextButton(onClick = { sentinel++ }) { Text("Mark restoration") }
            Text("Synthetic restored sentinel $sentinel")
            NavHost(navController, startDestination = "home", modifier = Modifier.weight(1f)) {
                composable("home") {
                    Column {
                        Text("Synthetic settings home")
                        TextButton(onClick = { navController.navigate("ai") }) { Text("Open AI") }
                    }
                }
                composable("ai") {
                    Column {
                        TextButton(onClick = { navController.navigate("cover") }) { Text("Cover AI") }
                        AiSettingsScreen(onBack = { navController.popBackStack() })
                    }
                }
                composable("cover") {
                    Column {
                        Text("Synthetic covered destination")
                        TextButton(onClick = { navController.popBackStack() }) { Text("Return to AI") }
                    }
                }
            }
        }
    }

    private data class Probe(val apiKey: String, val endpoint: String, val model: String)

    private class Fixture {
        val initial = AiSettings(DUMMY_KEY, null, null, "Synthetic personal prompt")
        val preferences = MutableStateFlow(initial)
        @Volatile var writes = 0
        @Volatile var mounts = 0
        lateinit var model: AiSettingsViewModel
        val probes = java.util.concurrent.CopyOnWriteArrayList<Probe>()
        val clipboard = PrivateClipboard()
        val repository = object : UserPreferencesRepository<AiSettings> {
            override fun observe() = preferences
            override suspend fun update(transform: AiSettings.() -> AiSettings) {
                preferences.update(transform)
                writes++
            }
        }
        val validator = object : AiConnectionValidator {
            override suspend fun validate(apiKey: String, endpoint: String, model: String): AiValidationResult {
                probes.add(Probe(apiKey, endpoint, model))
                return AiValidationResult.Success
            }
        }
    }

    /** Both Compose clipboard locals are replaced. Native clipboard access fails closed. */
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    private class PrivateClipboard : Clipboard, ClipboardManager {
        var entry: ClipEntry? = null
        @Volatile var reads = 0
        override val nativeClipboard: android.content.ClipboardManager
            get() = error("System clipboard is unavailable in this synthetic fixture")
        override suspend fun getClipEntry(): ClipEntry? { reads++; return entry }
        override suspend fun setClipEntry(clipEntry: ClipEntry?) { entry = clipEntry }
        override fun getClip(): ClipEntry? { reads++; return entry }
        override fun setClip(clipEntry: ClipEntry?) { entry = clipEntry }
        override fun hasText() = entry != null
        override fun getText(): AnnotatedString? {
            reads++
            return entry?.clipData?.getItemAt(0)?.text?.toString()?.let(::AnnotatedString)
        }
        override fun setText(annotatedString: AnnotatedString) {
            entry = ClipEntry(ClipData.newPlainText("synthetic", annotatedString.text))
        }
    }

    private companion object {
        const val DUMMY_KEY = "synthetic-original-key"
        const val DUMMY_EDITED = "synthetic-edited-key"
        const val DUMMY_PASTED = "synthetic-pasted-key"
    }
}
