package com.maksimowiczm.foodyou.food

import android.os.Build
import com.maksimowiczm.foodyou.app.BuildConfig
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.settings.domain.entity.Settings
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named

/** Prepares UI-only preferences on the disposable emulator; never runs on the owner phone. */
internal fun prepareSyntheticLibraryHost() {
    assumeTrue(Build.FINGERPRINT.contains("generic") || Build.MODEL.contains("sdk_gphone"))
    val preferences: UserPreferencesRepository<Settings> = GlobalContext.get().get(named(Settings::class.qualifiedName!!))
    runBlocking { preferences.update { copy(uiNameBarEnabled = false, hidePreviewDialog = true,
        showTranslationWarning = false, onboardingFinished = true, lastRememberedVersion = BuildConfig.FORK_VERSION_NAME) } }
}
