package com.maksimowiczm.foodyou.app.ui.onboarding

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.maksimowiczm.foodyou.app.navigation.forwardBackwardComposable
import com.maksimowiczm.foodyou.app.ui.database.australianfoodcompositiondatabase.AustralianFoodCompositionDatabaseScreen
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Onboarding(onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()
    val state: OnboardingState = rememberOnboardingState()
    val viewModel: OnboardingViewModel = koinViewModel()

    val latestOnFinish by rememberUpdatedState(onFinish)
    LaunchedCollectWithLifecycle(viewModel.events) {
        when (it) {
            is OnboardingEvent.Finished -> latestOnFinish()
        }
    }

    NavHost(navController = navController, startDestination = BeforeYouStart, modifier = modifier) {
        forwardBackwardComposable<BeforeYouStart> {
            UiSourceScope(uiInspectionSource.element("Onboarding.BeforeYouStartScreen.1", "com.maksimowiczm.foodyou.app.ui.onboarding.BeforeYouStartScreen", "Onboarding")) { BeforeYouStartScreen(
                onContinue = { navController.navigate(FoodDatabase) { launchSingleTop = true } }
            ) }
        }
        forwardBackwardComposable<FoodDatabase> {
            UiSourceScope(uiInspectionSource.element("Onboarding.FoodDatabaseScreen.1", "com.maksimowiczm.foodyou.app.ui.onboarding.FoodDatabaseScreen", "Onboarding")) { FoodDatabaseScreen(
                onBack = { navController.popBackStack<FoodDatabase>(true) },
                onSkip = {
                    viewModel.finish(state)
                    navController.navigate(AlmostDone) {
                        launchSingleTop = true
                        popUpTo(BeforeYouStart) { inclusive = true }
                    }
                },
                onAgree = {
                    viewModel.finish(state)
                    navController.navigate(AlmostDone) {
                        launchSingleTop = true
                        popUpTo(BeforeYouStart) { inclusive = true }
                    }
                },
                onAustralianFoodCompositionDatabase = {
                    navController.navigate(AustralianFoodCompositionDatabase) {
                        launchSingleTop = true
                    }
                },
                state = state,
            ) }
        }
        forwardBackwardComposable<AustralianFoodCompositionDatabase> {
            UiSourceScope(uiInspectionSource.element("Onboarding.AustralianFoodCompositionDatabaseScreen.1", "com.maksimowiczm.foodyou.app.ui.database.australianfoodcompositiondatabase.AustralianFoodCompositionDatabaseScreen", "Onboarding")) { AustralianFoodCompositionDatabaseScreen(onBack = { navController.popBackStack() }) }
        }
        forwardBackwardComposable<AlmostDone> { UiSourceScope(uiInspectionSource.element("Onboarding.AlmostDoneScreen.1", "com.maksimowiczm.foodyou.app.ui.onboarding.AlmostDoneScreen", "Onboarding")) { AlmostDoneScreen() } }
    }
}

@Serializable private data object BeforeYouStart

@Serializable private data object FoodDatabase

@Serializable private data object AustralianFoodCompositionDatabase

@Serializable private data object AlmostDone


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.onboarding",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/onboarding/Onboarding.kt",
)
