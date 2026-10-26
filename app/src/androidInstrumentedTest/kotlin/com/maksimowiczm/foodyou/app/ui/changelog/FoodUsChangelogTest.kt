package com.maksimowiczm.foodyou.app.ui.changelog

import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.os.ParcelFileDescriptor
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.ui.about.AboutScreen
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalAppConfig
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.changelog.infrastructure.ChangelogRepositoryImpl
import com.maksimowiczm.foodyou.common.config.AppConfig
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.settings.domain.entity.*
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Assert.*
import org.junit.Test
import org.koin.compose.KoinIsolatedContext
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Production About/history/automatic sheet with synthetic config and in-memory settings.
 * The inherited host reads the general secureScreen preference; this fixture never writes owner
 * settings or accesses AI credentials, diary records, photos, or external browser activities.
 */
@SdkSuppress(minSdkVersion = 30)
class FoodUsChangelogTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private var decor: View? = null
    private var activityPackage = ""
    private val driver = ScanTestDriver(instrumentation, { decor }, { activityPackage }, { null })

    @Test
    fun aboutShowsVerifiedHistoryKeepsUpstreamCreditAndSupportsBackAndReentry() {
        val fixture = Fixture("3.5.0")
        withHost(fixture) {
            driver.findText(runBlocking { getString(Res.string.description_derived_from_food_you, "3.4.9") })
            driver.clickText(driver.label(Res.string.action_original_creator_on_github))
            assertEquals(listOf("https://github.com/maksimowiczm"), fixture.openedUris)
            openHistory()
            driver.findText("2.23.0")
            assertNoInstalledBadge()
            driver.findText("2.19.1")
            driver.findText("1.0 announcement")
            dismissWithBack()
            driver.clickText(driver.label(Res.string.headline_changelog))
            driver.findText("2.23.0")
            dismissWithBack()
            driver.clickText(driver.label(Res.string.action_go_back))
            driver.findText("Synthetic About back destination")
            assertEquals(fixture.initialSettings, fixture.preferences.value)
        }
    }

    @Test
    fun publishedForkBuildGetsInstalledBadgeAndDismissalRemembersForkInsteadOfUpstream() {
        val fixture = Fixture("2.23.0", automatic = true)
        withHost(fixture) {
            driver.findText(driver.label(Res.string.headline_whats_new))
            driver.findText("2.23.0")
            driver.findText(driver.label(Res.string.headline_installed))
            dismissWithBack()
            driver.await("synthetic published fork version remembered") {
                fixture.preferences.value.lastRememberedVersion == "2.23.0"
            }
            assertEquals(fixture.initialSettings.copy(lastRememberedVersion = "2.23.0"),
                fixture.preferences.value)
            reenterAutomaticSheet(fixture)
            assertFalse(visibleText(driver.label(Res.string.headline_whats_new)))
        }
    }

    @Test
    fun unknownLegacyAndFutureFourPartBuildsShowHistoryWithoutInventingInstalledEntries() {
        listOf("3.5.0", "1.3.9.0").forEach { forkVersion ->
            val fixture = Fixture(forkVersion, automatic = true)
            withHost(fixture) {
                driver.findText(driver.label(Res.string.headline_whats_new))
                driver.findText("2.23.0")
                assertNoInstalledBadge()
                assertFalse(visibleText(forkVersion))
                dismissWithBack()
                driver.await("synthetic exact fork identity remembered") {
                    fixture.preferences.value.lastRememberedVersion == forkVersion
                }
                assertEquals(fixture.initialSettings.copy(lastRememberedVersion = forkVersion),
                    fixture.preferences.value)
                reenterAutomaticSheet(fixture)
                assertFalse(visibleText(driver.label(Res.string.headline_whats_new)))
            }
        }
    }

    private fun openHistory() {
        driver.clickText(driver.label(Res.string.headline_changelog))
        driver.findText(driver.label(Res.string.headline_whats_new))
    }

    private fun assertNoInstalledBadge() {
        assertFalse(visibleText(driver.label(Res.string.headline_installed)))
    }

    private fun reenterAutomaticSheet(fixture: Fixture) {
        driver.onMain { fixture.showAutomatic.value = false }
        instrumentation.waitForIdleSync()
        driver.onMain { fixture.showAutomatic.value = true }
        instrumentation.waitForIdleSync()
        driver.findText("Synthetic automatic history host")
    }

    private fun dismissWithBack() {
        val displayId = driver.onMain { requireNotNull(decor?.display).displayId }
        val output = ParcelFileDescriptor.AutoCloseInputStream(
            instrumentation.uiAutomation.executeShellCommand("input -d $displayId keyevent KEYCODE_BACK")
        ).bufferedReader().use { it.readText() }
        check(output.isBlank()) { "Synthetic Back action failed" }
        driver.await("synthetic history dismissed") {
            !visibleText(driver.label(Res.string.headline_whats_new))
        }
    }

    private fun visibleText(text: String): Boolean {
        if (Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.clearCache()
        val displayId = driver.onMain { requireNotNull(decor?.display).displayId }
        return instrumentation.uiAutomation.windowsOnAllDisplays.get(displayId).orEmpty()
            .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
            .mapNotNull { it.root }
            .filter { it.packageName?.toString() == instrumentation.targetContext.packageName }
            .flatMap { driver.descendants(it).toList() }
            .any { node: AccessibilityNodeInfo -> node.isVisibleToUser &&
                (node.text?.toString()?.lines()?.contains(text) == true ||
                    node.contentDescription?.toString() == text) }
    }

    private fun withHost(fixture: Fixture, checkFlow: () -> Unit) {
        val applicationKoin = GlobalContext.get()
        val isolated = koinApplication {
            modules(module {
                single<UserPreferencesRepository<Settings>>(named(Settings::class.qualifiedName!!)) {
                    fixture.settingsRepository
                }
                viewModel {
                    ChangelogViewModel(ChangelogRepositoryImpl(fixture.config),
                        fixture.settingsRepository, SavedStateHandle())
                }
            })
        }
        val automation = instrumentation.uiAutomation
        val originalInfo = automation.serviceInfo
        val originalFlags = originalInfo.flags
        try {
            automation.serviceInfo = automation.serviceInfo.apply {
                flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            }
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor = activity.window.decorView
                    activityPackage = activity.packageName
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.setContent {
                        KoinIsolatedContext(context = isolated) {
                            CompositionLocalProvider(LocalAppConfig provides fixture.config,
                                LocalUriHandler provides fixture.uriHandler) {
                                MaterialTheme {
                                    Box(Modifier.safeDrawingPadding()) {
                                        if (fixture.automatic) {
                                            Text("Synthetic automatic history host")
                                            if (fixture.showAutomatic.value) AppUpdateChangelogModalBottomSheet()
                                        } else if (fixture.backedOut.value) {
                                            Text("Synthetic About back destination")
                                        } else {
                                            AboutScreen(onBack = { fixture.backedOut.value = true }, onSponsor = {})
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                instrumentation.waitForIdleSync()
                checkFlow()
            }
        } finally {
            decor = null
            isolated.close()
            automation.serviceInfo = originalInfo.apply { flags = originalFlags }
            assertSame("Global application Koin survives", applicationKoin, GlobalContext.get())
        }
    }

    private class Fixture(forkVersion: String, val automatic: Boolean = false) {
        val initialSettings = Settings(
            lastRememberedVersion = "3.4.9", hidePreviewDialog = true,
            showTranslationWarning = false, nutrientsOrder = NutrientsOrder.defaultOrder,
            secureScreen = false, homeCardOrder = HomeCard.defaultOrder,
            expandGoalCard = false, onboardingFinished = true, energyFormat = EnergyFormat.DEFAULT,
            graphStyle = GraphStyle.DEFAULT, weekLayout = WeekLayout.DEFAULT,
            appLaunchInfo = AppLaunchInfo(null, null, 0),
        )
        val preferences = MutableStateFlow(initialSettings)
        val settingsRepository = object : UserPreferencesRepository<Settings> {
            override fun observe() = preferences
            override suspend fun update(transform: Settings.() -> Settings) {
                preferences.update(transform)
            }
        }
        val backedOut = mutableStateOf(false)
        val showAutomatic = mutableStateOf(true)
        val openedUris = mutableListOf<String>()
        val uriHandler = object : UriHandler {
            override fun openUri(uri: String) { openedUris.add(uri) }
        }
        val config = object : AppConfig {
            override val versionName = "3.4.9"
            override val forkVersionName = forkVersion
            override val contactEmailUri = "mailto:synthetic@example.invalid"
            override val translationUri = "https://example.invalid/translation"
            override val sourceCodeUri = "https://example.invalid/source"
            override val issueTrackerUri = "https://example.invalid/issues"
            override val upstreamAuthorUri = "https://github.com/maksimowiczm"
            override val privacyPolicyUri = "https://example.invalid/privacy"
            override val openFoodFactsTermsOfUseUri = ""
            override val openFoodFactsPrivacyPolicyUri = ""
            override val foodDataCentralPrivacyPolicyUri = ""
            override val openFoodFactsWebsiteUri = ""
            override val foodDataCentralWebsiteUri = ""
            override val swissFoodCompositionDatabaseWebsiteUri = ""
            override val australianFoodCompositionDatabaseWebsiteUri = ""
            override val aiApiKey = ""
            override val aiEndpoint = ""
            override val aiModel = ""
        }
    }
}
