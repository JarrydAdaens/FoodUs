package com.maksimowiczm.foodyou.app.infrastructure.android

import android.view.WindowManager
import android.os.SystemClock
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.launchActivity
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.settings.domain.entity.Settings
import com.maksimowiczm.foodyou.app.BuildConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.getString

class SecureActivityTest {
    private inline fun <reified A : FoodYouAbstractActivity> testSecureFlag() {
        val repository: UserPreferencesRepository<Settings> =
            GlobalContext.get().get(named(Settings::class.qualifiedName!!))
        val original = runBlocking { repository.observe().first() }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val barLabel = runBlocking { getString(Res.string.ui_name_bar_title) }
        try {
            launchActivity<A>().use { scenario ->
                if (A::class.java == MainActivity::class.java) {
                    // Observe only known public startup labels; never dump arbitrary screen values.
                    val startupLabels = runBlocking { mapOf(
                        "preview" to getString(Res.string.headline_preview_release),
                        "translation" to getString(Res.string.description_translation_warning),
                        "changelog" to getString(Res.string.headline_whats_new),
                    ) }
                    var startup: String? = null
                    val startupDeadline = SystemClock.uptimeMillis() + 3_000
                    do {
                        if (Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.clearCache()
                        val root = instrumentation.uiAutomation.rootInActiveWindow
                        startup = startupLabels.entries.firstOrNull { (_, label) ->
                            root != null && containsBar(root, label)
                        }?.key
                        if (startup == null) SystemClock.sleep(50)
                    } while (startup == null && SystemClock.uptimeMillis() < startupDeadline)
                    Log.i("FoodUsInspectionSecureProbe", "MainActivity activeStartup=${startup ?: "none"}")
                    // Startup dialogs have their own active accessibility window. Establish the
                    // underlying Activity as the active root before checking its rendered chrome.
                    runBlocking { repository.update { copy(hidePreviewDialog = true,
                        showTranslationWarning = false, lastRememberedVersion = BuildConfig.FORK_VERSION_NAME) } }
                }
                for (inspectionEnabled in listOf(false, true)) {
                    for (secure in listOf(true, false)) {
                        runBlocking {
                            repository.update { copy(uiNameBarEnabled = inspectionEnabled, secureScreen = secure) }
                        }
                        val expected = if (secure) WindowManager.LayoutParams.FLAG_SECURE else 0
                        var actual = -1
                        var barVisible: Boolean? = null
                        val deadline = SystemClock.uptimeMillis() + 8_000
                        do {
                            scenario.onActivity { activity ->
                                actual = activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE
                            }
                            if (Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.clearCache()
                            barVisible = instrumentation.uiAutomation.rootInActiveWindow?.let { root ->
                                root.takeIf { it.packageName?.toString() == instrumentation.targetContext.packageName }
                                    ?.let { containsBar(it, barLabel) }
                            }
                            if (actual != expected || barVisible != inspectionEnabled) SystemClock.sleep(25)
                        } while ((actual != expected || barVisible != inspectionEnabled) && SystemClock.uptimeMillis() < deadline)
                        assertEquals("FLAG_SECURE with inspection=$inspectionEnabled, secure=$secure", expected, actual)
                        assertEquals("Rendered inspector mode before assessing secure-window boundary", inspectionEnabled, barVisible)
                    }
                }
            }
        } finally {
            runBlocking {
                repository.update { copy(secureScreen = original.secureScreen, uiNameBarEnabled = original.uiNameBarEnabled,
                    hidePreviewDialog = original.hidePreviewDialog, showTranslationWarning = original.showTranslationWarning,
                    lastRememberedVersion = original.lastRememberedVersion) }
            }
        }
    }

    private fun containsBar(node: AccessibilityNodeInfo, label: String): Boolean {
        if (node.isVisibleToUser && (node.text?.toString()?.lines()?.contains(label) == true ||
            node.hintText?.toString() == label)) return true
        return (0 until node.childCount).any { index -> node.getChild(index)?.let { containsBar(it, label) } == true }
    }

    @Test fun testSecureMainActivity() = testSecureFlag<MainActivity>()

    @Test fun testSecureShareProductActivity() = testSecureFlag<ShareProductActivity>()

    @Test fun testSecureCrashReportActivity() = testSecureFlag<CrashReportActivity>()
}
