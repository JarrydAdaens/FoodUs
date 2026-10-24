package com.maksimowiczm.foodyou.app.ui.onboarding

import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.test.core.app.launchActivity
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.ui.database.australianfoodcompositiondatabase.AustralianFoodCompositionDatabaseViewModel
import com.maksimowiczm.foodyou.common.domain.food.FoodSource
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.food.domain.repository.OpenFoodFactsCredentialsRepository
import com.maksimowiczm.foodyou.food.search.domain.FoodSearchPreferences
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.*
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.ProviderMetadata
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.ProviderMetadataRepository
import com.maksimowiczm.foodyou.importexport.swissfoodcompositiondatabase.domain.ImportSwissFoodCompositionDatabaseUseCase
import com.maksimowiczm.foodyou.importexport.swissfoodcompositiondatabase.domain.SwissFoodCompositionDatabaseRepository.Language
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.*
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

/** Exercises production onboarding and its AFCD destination with isolated, synthetic repositories.
 * DeveloperActivity supplies the existing app UI environment; only its content is replaced. No app
 * data is cleared and no source configuration, credentials, imports, or diary entries are persisted.
 */
class OnboardingAfcdTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun afcdSetupPreservesOtherSelectionsAndRemainsEnabledAfterAgree() {
        val fixture = Fixture()
        withOnboarding(fixture) {
            click(label(Res.string.action_agree_and_continue))
            click(label(Res.string.headline_open_food_facts))
            click(label(Res.string.headline_food_data_central_usda))
            click("English")
            click(label(Res.string.headline_australian_food_composition_database))
            click(label(Res.string.action_import))
            await("AFCD enabled after successful import") {
                fixture.preferences.value.isAustralianFoodCompositionDatabaseEnabled
            }
            assertEquals("test-release", fixture.metadata.value?.installedVersion)
            find(label(Res.string.action_update_dataset))

            click(label(Res.string.action_go_back), scroll = false)
            find(label(Res.string.headline_food_database), scroll = false)
            click(label(Res.string.action_agree_and_continue), scroll = false)
            find(FINISHED, scroll = false)

            assertTrue(fixture.preferences.value.isOpenFoodFactsEnabled)
            assertTrue(fixture.preferences.value.isUsdaEnabled)
            assertTrue(fixture.preferences.value.isAustralianFoodCompositionDatabaseEnabled)
            assertEquals(setOf(Language.ENGLISH), fixture.importedLanguages)
            assertEquals(1, fixture.importAttempts)
        }
    }

    @Test
    fun afcdFailureCanRetryAndSkipPreservesSuccessfulSetup() {
        val fixture = Fixture(failFirstImport = true)
        withOnboarding(fixture) {
            click(label(Res.string.action_agree_and_continue))
            click(label(Res.string.headline_australian_food_composition_database))
            click(label(Res.string.action_import))
            find(IMPORT_ERROR)
            assertFalse(fixture.preferences.value.isAustralianFoodCompositionDatabaseEnabled)
            assertNull(fixture.metadata.value)

            click(label(Res.string.action_import))
            await("AFCD enabled after retry") {
                fixture.preferences.value.isAustralianFoodCompositionDatabaseEnabled
            }
            click(label(Res.string.action_go_back), scroll = false)
            find(label(Res.string.headline_food_database), scroll = false)
            click(label(Res.string.action_skip), scroll = false)
            find(FINISHED, scroll = false)

            assertTrue(fixture.preferences.value.isAustralianFoodCompositionDatabaseEnabled)
            assertFalse(fixture.preferences.value.isOpenFoodFactsEnabled)
            assertFalse(fixture.preferences.value.isUsdaEnabled)
            assertTrue(fixture.importedLanguages.isEmpty())
            assertEquals(2, fixture.importAttempts)
        }
    }

    private fun withOnboarding(fixture: Fixture, test: () -> Unit) {
        val applicationKoin = GlobalContext.get()
        val isolated = koinApplication { modules(fixture.bindings) }
        try {
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.setContent {
                        KoinIsolatedContext(context = isolated) {
                            MaterialTheme {
                                var finished by remember { mutableStateOf(false) }
                                if (finished) Text(FINISHED)
                                else Onboarding(onFinish = { finished = true })
                            }
                        }
                    }
                }
                test()
            }
        } finally {
            // Close only the test container after the host and composition have been destroyed.
            isolated.close()
            assertSame("Test must preserve the app's global Koin instance", applicationKoin, GlobalContext.get())
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
            val root = instrumentation.uiAutomation.rootInActiveWindow
            found = root?.let { findNode(it) { node ->
                node.text?.toString()?.lines()?.contains(text) == true ||
                    node.contentDescription?.toString() == text
            } }
            if (found == null && scroll && SystemClock.uptimeMillis() - lastScroll > 400) {
                root?.let { findNode(it) { node -> node.isScrollable } }
                    ?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                lastScroll = SystemClock.uptimeMillis()
            }
            found != null
        }
        return requireNotNull(found) { "Missing UI text '$text'" }
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

    private class Fixture(failFirstImport: Boolean = false) {
        val preferences = MutableStateFlow(
            FoodSearchPreferences(
                openFoodFacts = FoodSearchPreferences.OpenFoodFacts(false),
                usda = FoodSearchPreferences.Usda(false, null),
                australianFoodCompositionDatabase = FoodSearchPreferences.AustralianFoodCompositionDatabase(false),
            )
        )
        val metadata = MutableStateFlow<ProviderMetadata?>(null)
        var importedLanguages: Set<Language> = emptySet()
        var importAttempts = 0

        private val preferencesRepository = object : UserPreferencesRepository<FoodSearchPreferences> {
            override fun observe() = preferences
            override suspend fun update(transform: FoodSearchPreferences.() -> FoodSearchPreferences) {
                preferences.update(transform)
            }
        }
        private val metadataRepository = object : ProviderMetadataRepository {
            override fun observe(source: FoodSource.Type) = metadata
            override suspend fun put(metadata: ProviderMetadata) {
                this@Fixture.metadata.value = metadata
            }
        }
        private val importAfcd = ImportAustralianFoodCompositionDatabaseUseCase {
            flow {
                importAttempts++
                emit(AfcdImportProgress.Downloading)
                if (failFirstImport && importAttempts == 1) error(IMPORT_ERROR)
                emit(AfcdImportProgress.Importing(1f))
                metadataRepository.put(
                    ProviderMetadata(
                        source = FoodSource.Type.AustralianFoodCompositionDatabase,
                        installedVersion = "test-release",
                        publicationDate = null,
                        checksum = null,
                        importedAtEpochSeconds = 1_700_000_000,
                        lastSuccessfulCheckEpochSeconds = null,
                        lastError = null,
                    )
                )
                emit(AfcdImportProgress.Finished)
            }
        }
        private val importSwiss = ImportSwissFoodCompositionDatabaseUseCase { languages ->
            importedLanguages = languages
            flowOf(1)
        }

        val bindings = module {
            viewModel { OnboardingViewModel(importSwiss, preferencesRepository) }
            viewModel {
                AustralianFoodCompositionDatabaseViewModel(
                    importUseCase = importAfcd,
                    checkUpdateUseCase = CheckAustralianFoodCompositionDatabaseUpdateUseCase {
                        AfcdUpdateCheckOutcome.UpToDate
                    },
                    preferencesRepository = preferencesRepository,
                    metadataRepository = metadataRepository,
                )
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

    private companion object {
        const val FINISHED = "Onboarding test finished"
        const val IMPORT_ERROR = "Synthetic AFCD download failure"
    }
}
