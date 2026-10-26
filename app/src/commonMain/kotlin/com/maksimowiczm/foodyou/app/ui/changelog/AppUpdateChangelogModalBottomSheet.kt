package com.maksimowiczm.foodyou.app.ui.changelog

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalAppConfig
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.settings.domain.entity.Settings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

/** Modal bottom sheet that will show the changelog of the app if the user has not seen it yet. */
@Composable
fun AppUpdateChangelogModalBottomSheet(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val settingsRepository: UserPreferencesRepository<Settings> =
        koinInject(named(Settings::class.qualifiedName!!))
    val appConfig = LocalAppConfig.current

    val currentVersion = remember(appConfig) { appConfig.forkVersionName }
    val settings = settingsRepository.observe().collectAsStateWithLifecycle(null).value

    if (settings != null && currentVersion != settings.lastRememberedVersion) {
        ChangelogModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    settingsRepository.update { copy(lastRememberedVersion = currentVersion) }
                }
            },
            modifier = (modifier).uiName("AppUpdateChangelogModalBottomSheet.ChangelogModalBottomSheet.1", "com.maksimowiczm.foodyou.app.ui.changelog.ChangelogModalBottomSheet", uiInspectionSource, "AppUpdateChangelogModalBottomSheet"),
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.changelog",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/changelog/AppUpdateChangelogModalBottomSheet.kt",
)
