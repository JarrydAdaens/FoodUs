package com.maksimowiczm.foodyou.app.ui.developer.inspection

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.os.ParcelFileDescriptor
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.app.Activity
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.BuildConfig
import com.maksimowiczm.foodyou.app.infrastructure.android.MainActivity
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.settings.domain.entity.Settings
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named

/** Actual shipping Activity/root/navigation, with only temporary inspection/onboarding preferences.
 * Intended for the isolated emulator. No diary, credential, relay, camera, or network operations.
 */
@SdkSuppress(minSdkVersion = 31)
class UiNameInspectorMainAppTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private val driver = ScanTestDriver(instrumentation, { decor },
        { instrumentation.targetContext.packageName }, { null })

    @Test
    fun actualOnboardingAndMainSettingsExportProductionSourceLocators() {
        val repository: UserPreferencesRepository<Settings> =
            GlobalContext.get().get(named(Settings::class.qualifiedName!!))
        val original = runBlocking { repository.observe().first() }
        val automation = instrumentation.uiAutomation
        val originalFlags = automation.serviceInfo.flags
        try {
            automation.serviceInfo = automation.serviceInfo.apply {
                flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            }
            runBlocking {
                repository.update { copy(
                    onboardingFinished = false,
                    hidePreviewDialog = true,
                    showTranslationWarning = false,
                    lastRememberedVersion = BuildConfig.FORK_VERSION_NAME,
                    uiNameBarEnabled = true,
                    uiNameBarIncludeHierarchy = true,
                ) }
            }
            launchActivity<MainActivity>().use { scenario ->
                scenario.onActivity {
                    decor = it.window.decorView
                    it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                hold(driver.label(Res.string.headline_before_you_start), scenario)
                val xml = copyOwnLocator()
                assertTrue(xml.contains("BeforeYouStartScreen.Text.1"))
                assertTrue(xml.contains("app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/onboarding/BeforeYouStartScreen.kt"))
                assertTrue(xml.contains("com.maksimowiczm.foodyou.app.ui.onboarding.BeforeYouStartScreen"))
                saveEvidence("main-onboarding", xml, screenshot = true)
            }
            runBlocking { repository.update { copy(onboardingFinished = true) } }
            launchActivity<MainActivity>().use { scenario ->
                scenario.onActivity {
                    decor = it.window.decorView
                    it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                driver.clickText(driver.label(Res.string.action_go_to_settings))
                driver.findText(driver.label(Res.string.headline_settings))
                hold(driver.label(Res.string.action_go_back), scenario)
                val xml = copyOwnLocator()
                assertTrue(xml.contains("ArrowBackIconButton.Icon.1"))
                assertTrue(xml.contains("SettingsScreen.ArrowBackIconButton.1"))
                assertTrue(xml.contains("call-site-owner=\"com.maksimowiczm.foodyou.app.ui.settings.SettingsScreen\""))
                saveEvidence("main-settings", xml, screenshot = true)
                listOf(
                    Triple(Res.string.headline_daily_goals, "goals/setup/DailyGoalsScreen.kt", "goals"),
                    Triple(Res.string.headline_ai_settings, "settings/ai/AiSettingsScreen.kt", "ai"),
                    Triple(Res.string.headline_relay_settings, "settings/relay/RelaySettingsScreen.kt", "relay"),
                ).forEach { (title, source, name) ->
                    driver.clickText(driver.label(title))
                    SystemClock.sleep(400)
                    hold(driver.label(title), scenario)
                    val locator = copyOwnLocator()
                    assertTrue("Actual $name title locates its source", locator.contains("/app/ui/$source"))
                    saveEvidence("route-$name", locator)
                    driver.clickText(driver.label(Res.string.action_go_back))
                    driver.findText(driver.label(Res.string.headline_settings))
                }
                driver.clickText(driver.label(Res.string.headline_data_backup_and_export))
                driver.clickText(driver.label(Res.string.library_import_title))
                SystemClock.sleep(400)
                hold(driver.label(Res.string.library_import_title), scenario)
                val importLocator = copyOwnLocator()
                assertTrue(importLocator.contains("/database/importlibrary/ImportLibraryScreen.kt"))
                saveEvidence("route-import", importLocator)
                driver.clickText(driver.label(Res.string.action_go_back))
                driver.clickText(driver.label(Res.string.action_go_back))
                driver.clickText(driver.label(Res.string.action_go_back))
                driver.findText(driver.label(Res.string.action_go_to_settings))
            }
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { decor = it.window.decorView }
                hold(driver.label(Res.string.headline_database_backup), scenario)
                val locator = copyOwnLocator()
                assertTrue(locator.contains("/infrastructure/android/DeveloperActivity.kt"))
                saveEvidence("route-backup", locator)
            }
        } finally {
            decor = null
            runBlocking { repository.update { copy(
                onboardingFinished = original.onboardingFinished,
                hidePreviewDialog = original.hidePreviewDialog,
                showTranslationWarning = original.showTranslationWarning,
                lastRememberedVersion = original.lastRememberedVersion,
                uiNameBarEnabled = original.uiNameBarEnabled,
                uiNameBarIncludeHierarchy = original.uiNameBarIncludeHierarchy,
            ) } }
            automation.serviceInfo = automation.serviceInfo.apply { flags = originalFlags }
        }
    }

    private fun <A : Activity> hold(label: String, scenario: androidx.test.core.app.ActivityScenario<A>) {
        val bounds = Rect().also(driver.findText(label)::getBoundsInScreen)
        val down = SystemClock.uptimeMillis()
        fun event(action: Int) {
            scenario.onActivity { activity ->
                val origin = IntArray(2).also(activity.window.decorView::getLocationOnScreen)
                MotionEvent.obtain(down, SystemClock.uptimeMillis(), action,
                    bounds.exactCenterX() - origin[0], bounds.exactCenterY() - origin[1], 0).let {
                    activity.dispatchTouchEvent(it)
                    it.recycle()
                }
            }
        }
        event(MotionEvent.ACTION_DOWN)
        SystemClock.sleep(3_350)
        event(MotionEvent.ACTION_UP)
        instrumentation.waitForIdleSync()
    }

    private fun copyOwnLocator(): String {
        driver.clickText(driver.label(Res.string.ui_name_bar_copy))
        var value: String? = null
        instrumentation.runOnMainSync {
            // Read only after this test has explicitly overwritten the clipboard with our locator.
            value = instrumentation.targetContext.getSystemService(ClipboardManager::class.java)
                .primaryClip?.getItemAt(0)?.text?.toString()
        }
        return requireNotNull(value).also { assertTrue(it.startsWith("<ui-selection ")) }
    }

    private fun saveEvidence(name: String, xml: String, screenshot: Boolean = false) {
        require(name in setOf("main-onboarding", "main-settings", "route-goals", "route-ai", "route-relay", "route-import", "route-backup"))
        val pipes = instrumentation.uiAutomation.executeShellCommandRw("tee /data/local/tmp/foodus-story29-$name.xml")
        ParcelFileDescriptor.AutoCloseOutputStream(pipes[1]).use { it.write(xml.toByteArray(Charsets.UTF_8)) }
        val persisted = ParcelFileDescriptor.AutoCloseInputStream(pipes[0]).bufferedReader().use { it.readText() }
        assertEquals("Persist actual structural clipboard evidence", xml, persisted)
        // Shell-owned synthetic screenshot survives Gradle uninstalling its temporary test APK.
        if (screenshot) {
            ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
                "screencap -p /data/local/tmp/foodus-story29-$name.png"
            )).use { it.readBytes() }
        }
        instrumentation.sendStatus(0, Bundle().apply {
            putString("stream", "FoodUs inspection evidence: ${instrumentation.targetContext.packageName} " +
                "${BuildConfig.FORK_VERSION_NAME} $name\n$xml\n")
        })
    }
}
