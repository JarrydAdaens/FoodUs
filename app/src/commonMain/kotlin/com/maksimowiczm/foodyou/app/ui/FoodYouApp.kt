package com.maksimowiczm.foodyou.app.ui

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
                        PreviewReleaseDialog()
                        TranslationWarningStartupDialog()

                        if (onboardingFinished) {
                            Surface {
                                FoodUsAppShell(onDatabaseBackup)
                                AppUpdateChangelogModalBottomSheet()
                            }
                        } else {
                            Onboarding(onFinish = viewModel::finishOnboarding)
                        }
                    }
                }
            }
        }
    }
}
