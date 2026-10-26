package com.maksimowiczm.foodyou.app.ui
import androidx.compose.ui.Modifier

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.changelog.AppUpdateChangelogModalBottomSheet
import com.maksimowiczm.foodyou.app.ui.changelog.PreviewReleaseDialog
import com.maksimowiczm.foodyou.app.ui.common.utility.EnergyFormatterProvider
import com.maksimowiczm.foodyou.app.ui.common.utility.GraphStyleProvider
import com.maksimowiczm.foodyou.app.ui.common.utility.NutrientsOrderProvider
import com.maksimowiczm.foodyou.app.ui.common.utility.WeekLayoutProvider
import com.maksimowiczm.foodyou.app.ui.language.TranslationWarningStartupDialog
import com.maksimowiczm.foodyou.app.ui.onboarding.Onboarding
import com.maksimowiczm.foodyou.app.ui.shell.FoodUsAppShell
import com.maksimowiczm.foodyou.app.ui.theme.FoodYouTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FoodYouApp(onDatabaseBackup: () -> Unit) {
    val viewModel: AppViewModel = koinViewModel()
    val nutrientsOrder by viewModel.nutrientsOrder.collectAsStateWithLifecycle()
    val onboardingFinished by viewModel.onboardingFinished.collectAsStateWithLifecycle()
    val energyFormatter by viewModel.energyFormatter.collectAsStateWithLifecycle()
    val graphStyle by viewModel.graphStyle.collectAsStateWithLifecycle()
    val weekLayout by viewModel.weekLayout.collectAsStateWithLifecycle()

    NutrientsOrderProvider(nutrientsOrder) {
        EnergyFormatterProvider(energyFormatter) {
            GraphStyleProvider(graphStyle) {
                WeekLayoutProvider(weekLayout) {
                    FoodYouTheme {
                        PreviewReleaseDialog( modifier = Modifier.uiName("FoodYouApp.PreviewReleaseDialog.1", "com.maksimowiczm.foodyou.app.ui.changelog.PreviewReleaseDialog", uiInspectionSource, "FoodYouApp"))
                        TranslationWarningStartupDialog( modifier = Modifier.uiName("FoodYouApp.TranslationWarningStartupDialog.1", "com.maksimowiczm.foodyou.app.ui.language.TranslationWarningStartupDialog", uiInspectionSource, "FoodYouApp"))

                        if (onboardingFinished) {
                            Surface (modifier = Modifier.uiName("FoodYouApp.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "FoodYouApp")) {
                                UiSourceScope(uiInspectionSource.element("FoodYouApp.FoodUsAppShell.1", "com.maksimowiczm.foodyou.app.ui.shell.FoodUsAppShell", "FoodYouApp")) { FoodUsAppShell(onDatabaseBackup) }
                                AppUpdateChangelogModalBottomSheet( modifier = Modifier.uiName("FoodYouApp.AppUpdateChangelogModalBottomSheet.1", "com.maksimowiczm.foodyou.app.ui.changelog.AppUpdateChangelogModalBottomSheet", uiInspectionSource, "FoodYouApp"))
                            }
                        } else {
                            UiSourceScope(uiInspectionSource.element("FoodYouApp.Onboarding.1", "com.maksimowiczm.foodyou.app.ui.onboarding.Onboarding", "FoodYouApp")) { Onboarding(onFinish = viewModel::finishOnboarding) }
                        }
                    }
                }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/FoodYouApp.kt",
)
