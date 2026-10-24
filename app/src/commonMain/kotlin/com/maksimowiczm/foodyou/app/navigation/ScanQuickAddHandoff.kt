package com.maksimowiczm.foodyou.app.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimate

/** Local back-stack result, emitted only after a successful Quick Add insertion. */
internal object ScanQuickAddHandoff {
    const val SAVED_ITEM_KEY = "scan.quickAdd.savedItem"

    fun route(epochDay: Long, mealId: Long, estimate: AiFoodEstimate, itemId: String) =
        FoodDiaryCreateQuickAdd(epochDay, mealId, estimate).copy(scanItemId = itemId)

    fun acknowledge(route: FoodDiaryCreateQuickAdd, scannerState: SavedStateHandle?) {
        route.scanItemId?.let { scannerState?.set(SAVED_ITEM_KEY, it) }
    }

    fun open(navController: NavHostController, route: FoodDiaryCreateQuickAdd) {
        // Rapid repeated review actions must share one form ViewModel/insertion guard.
        navController.navigate(route) { launchSingleTop = true }
    }

    fun saved(navController: NavHostController, route: FoodDiaryCreateQuickAdd) {
        acknowledge(route, navController.previousBackStackEntry?.savedStateHandle)
        navController.popBackStackInclusive<FoodDiaryCreateQuickAdd>()
    }

    /** Called only after the scanner accepts the final selected item's successful save. */
    fun complete(navController: NavHostController) {
        navController.popBackStack<Home>(inclusive = false)
    }
}
