package com.maksimowiczm.foodyou.app.ui.settings

import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.launchActivity
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.ui.database.externaldatabases.ExternalDatabasesScreen
import com.maksimowiczm.foodyou.app.ui.database.externaldatabases.ExternalDatabasesViewModel
import com.maksimowiczm.foodyou.app.ui.database.master.DatabaseSettingsScreen
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.food.domain.repository.OpenFoodFactsCredentialsRepository
import com.maksimowiczm.foodyou.food.search.domain.FoodSearchPreferences
import com.maksimowiczm.foodyou.settings.domain.entity.Translation
import com.maksimowiczm.foodyou.settings.domain.repository.TranslationRepository
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
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

/** Production screens in a minimal navigation host with in-memory source preferences. Actual
 * app-graph callback placement is additionally inspected in FoodYouAppNavHost. No app data is
 * cleared; import/export/backup actions are intercepted before any storage or picker operation.
 */
class ExternalDatabasesNavigationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun mainSettingsOpensSourcesAndDatabaseRetainsOnlyDataActions() {
        val fixture = Fixture()
        val applicationKoin = GlobalContext.get()
        val isolated = koinApplication { modules(fixture.bindings) }
        try {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.setContent {
                        KoinIsolatedContext(context = isolated) {
                            MaterialTheme { TestNavigation(fixture) }
                        }
                    }
                }

                click(label(Res.string.headline_external_databases))
                find(label(Res.string.headline_food_database), scroll = false)
                click(label(Res.string.headline_open_food_facts))
                await("synthetic source preference enabled") {
                    fixture.preferences.value.isOpenFoodFactsEnabled
                }
                click(label(Res.string.headline_swiss_food_composition_database))
                click(label(Res.string.headline_australian_food_composition_database))
                assertEquals(1, fixture.swissVisits)
                assertEquals(1, fixture.afcdVisits)
                assertTrue(fixture.preferences.value.isUsdaEnabled)
                assertTrue(fixture.preferences.value.isAustralianFoodCompositionDatabaseEnabled)

                click(label(Res.string.action_go_back), scroll = false)
                find(label(Res.string.headline_settings), scroll = false)
                click(label(Res.string.headline_external_databases))
                find(label(Res.string.headline_food_database), scroll = false)
                click(label(Res.string.headline_open_food_facts))
                await("source preference retained across Back and re-entry") {
                    !fixture.preferences.value.isOpenFoodFactsEnabled
                }
                click(label(Res.string.action_go_back), scroll = false)

                click(label(Res.string.headline_data_backup_and_export))
                find(label(Res.string.headline_database), scroll = false)
                click(label(Res.string.action_import_csv_food_products))
                click(label(Res.string.action_export_csv_food_products))
                click(label(Res.string.headline_database_backup))
                assertEquals(listOf("import", "export", "backup"), fixture.dataActions)
                assertNull(
                    "External databases must no longer be duplicated under Database",
                    matchingNode(label(Res.string.headline_external_databases)),
                )
                click(label(Res.string.action_go_back), scroll = false)
                find(label(Res.string.headline_settings), scroll = false)
                find(label(Res.string.headline_external_databases))
            }
        } finally {
            isolated.close()
            assertSame("App global Koin must survive this fixture", applicationKoin, GlobalContext.get())
        }
    }

    @Composable
    private fun TestNavigation(fixture: Fixture) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "settings") {
            composable("settings") {
                SettingsScreen(
                    onBack = {},
                    onAbout = {},
                    onDiarySlots = {},
                    onLanguage = {},
                    onGoals = {},
                    onPersonalization = {},
                    onDatabase = { navController.navigate("database") },
                    onExternalDatabases = { navController.navigate("sources") },
                    onAiSettings = {},
                    onRelaySettings = {},
                )
            }
            composable("sources") {
                ExternalDatabasesScreen(
                    onBack = { navController.popBackStack() },
                    onSwissFoodCompositionDatabase = { fixture.swissVisits++ },
                    onAustralianFoodCompositionDatabase = { fixture.afcdVisits++ },
                )
            }
            composable("database") {
                DatabaseSettingsScreen(
                    onBack = { navController.popBackStack() },
                    onImportCsvProducts = { fixture.dataActions.add("import") },
                    onImportLibrary = { fixture.dataActions.add("library") },
                    onExportCsvProducts = { fixture.dataActions.add("export") },
                    onDatabaseBackup = { fixture.dataActions.add("backup") },
                )
            }
        }
    }

    private fun label(resource: StringResource): String = runBlocking { getString(resource) }

    private fun click(text: String, scroll: Boolean = true) {
        var node: AccessibilityNodeInfo? = find(text, scroll)
        while (node != null && !node.isClickable) node = node.parent
        assertNotNull("No clickable ancestor for '$text'", node)
        assertTrue("Could not click '$text'", node!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        instrumentation.waitForIdleSync()
    }

    private fun find(text: String, scroll: Boolean = true): AccessibilityNodeInfo {
        var found: AccessibilityNodeInfo? = null
        var lastScroll = 0L
        await("UI label '$text' (forward scrolling: $scroll)") {
            found = matchingNode(text)
            if (found == null && scroll && SystemClock.uptimeMillis() - lastScroll > 400) {
                instrumentation.uiAutomation.rootInActiveWindow
                    ?.let { findNode(it) { node -> node.isScrollable } }
                    ?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                lastScroll = SystemClock.uptimeMillis()
            }
            found != null
        }
        return requireNotNull(found)
    }

    private fun matchingNode(text: String): AccessibilityNodeInfo? =
        instrumentation.uiAutomation.rootInActiveWindow?.let { root ->
            findNode(root) { node ->
                node.text?.toString()?.lines()?.contains(text) == true ||
                    node.contentDescription?.toString() == text
            }
        }

    private fun findNode(
        node: AccessibilityNodeInfo,
        matches: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo? {
        if (matches(node)) return node
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            findNode(child, matches)?.let { return it }
        }
        return null
    }

    private fun await(description: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 15_000
        while (!condition()) {
            check(SystemClock.uptimeMillis() < deadline) { "Timed out waiting for $description" }
            SystemClock.sleep(100)
        }
    }

    private class Fixture {
        val preferences = MutableStateFlow(
            FoodSearchPreferences(
                openFoodFacts = FoodSearchPreferences.OpenFoodFacts(false),
                usda = FoodSearchPreferences.Usda(true, null),
                australianFoodCompositionDatabase = FoodSearchPreferences.AustralianFoodCompositionDatabase(true),
            )
        )
        var swissVisits = 0
        var afcdVisits = 0
        val dataActions = mutableListOf<String>()
        private val preferencesRepository = object : UserPreferencesRepository<FoodSearchPreferences> {
            override fun observe() = preferences
            override suspend fun update(transform: FoodSearchPreferences.() -> FoodSearchPreferences) {
                preferences.update(transform)
            }
        }
        val bindings = module {
            viewModel { ExternalDatabasesViewModel(preferencesRepository) }
            single<TranslationRepository> {
                object : TranslationRepository {
                    override fun observe() = flowOf(emptyList<Translation>())
                    override fun observeCurrent() = emptyFlow<Translation>()
                    override suspend fun setTranslation(translation: Translation?) = error("Not used")
                }
            }
            single<OpenFoodFactsCredentialsRepository> {
                object : OpenFoodFactsCredentialsRepository {
                    override fun hasCredentials() = flowOf(false)
                    override suspend fun store(login: String, password: String) = error("Not used")
                    override suspend fun clear() = error("Not used")
                }
            }
        }
    }
}
