package com.maksimowiczm.foodyou.app.ui
import androidx.compose.ui.Modifier

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.navigation.DownloadProductAppNavHost
import com.maksimowiczm.foodyou.app.ui.onboarding.Onboarding
import com.maksimowiczm.foodyou.app.ui.theme.FoodYouTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DownloadProductApp(onBack: () -> Unit, onCreate: () -> Unit, url: String) {
    val viewModel: AppViewModel = koinViewModel()
    val onboardingFinished by viewModel.onboardingFinished.collectAsStateWithLifecycle()

    FoodYouTheme {
        Surface (modifier = Modifier.uiName("DownloadProductApp.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "DownloadProductApp")) {
            if (!onboardingFinished) {
                UiSourceScope(uiInspectionSource.element("DownloadProductApp.Onboarding.1", "com.maksimowiczm.foodyou.app.ui.onboarding.Onboarding", "DownloadProductApp")) { Onboarding(onFinish = viewModel::finishOnboarding) }
            } else {
                DownloadProductAppNavHost(onBack = onBack, onCreate = onCreate, url = url, modifier = Modifier.uiName("DownloadProductApp.DownloadProductAppNavHost.1", "com.maksimowiczm.foodyou.app.navigation.DownloadProductAppNavHost", uiInspectionSource, "DownloadProductApp"))
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/DownloadProductApp.kt",
)
