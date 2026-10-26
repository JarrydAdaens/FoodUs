package com.maksimowiczm.foodyou.app.ui.developer.inspection

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.settings.infrastructure.DataStoreSettingsRepository
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import org.junit.Assert.*
import org.junit.Test

/** Real DataStore mapping/reopen with a private synthetic file, never the app's preferences. */
class UiNameInspectorPreferencesTest {
    @Test fun defaultOffAtomicUpdateAndReopenPreserveUnrelatedPreferences() {
        runBlocking {
            val file = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,
                "inspection-${UUID.randomUUID()}.preferences_pb")
            val firstJob = SupervisorJob()
            val firstStore = PreferenceDataStoreFactory.createWithPath(
                scope = CoroutineScope(Dispatchers.IO + firstJob), produceFile = { file.absolutePath.toPath() })
            val unrelated = stringPreferencesKey("synthetic:unrelated")
            val original = try {
                firstStore.edit { it[unrelated] = "synthetic preserved value" }
                val repository = DataStoreSettingsRepository(firstStore)
                val defaults = repository.observe().first()
                assertFalse(defaults.uiNameBarEnabled)
                assertFalse(defaults.uiNameBarIncludeHierarchy)
                repository.update { copy(uiNameBarEnabled = true, uiNameBarIncludeHierarchy = true) }
                assertEquals(defaults.copy(uiNameBarEnabled = true, uiNameBarIncludeHierarchy = true),
                    repository.observe().first())
                assertEquals("synthetic preserved value", firstStore.data.first()[unrelated])
                defaults
            } finally { firstJob.cancelAndJoin() }
            val reopenedJob = SupervisorJob()
            val reopenedStore = PreferenceDataStoreFactory.createWithPath(
                scope = CoroutineScope(Dispatchers.IO + reopenedJob), produceFile = { file.absolutePath.toPath() })
            try {
                val reopened = DataStoreSettingsRepository(reopenedStore)
                assertEquals(original.copy(uiNameBarEnabled = true, uiNameBarIncludeHierarchy = true),
                    reopened.observe().first())
                reopened.update { copy(uiNameBarEnabled = false) }
                assertFalse(reopened.observe().first().uiNameBarEnabled)
                assertTrue(reopened.observe().first().uiNameBarIncludeHierarchy)
                assertEquals("synthetic preserved value", reopenedStore.data.first()[unrelated])
            } finally {
                reopenedJob.cancelAndJoin()
                file.delete()
            }
        }
    }
}
