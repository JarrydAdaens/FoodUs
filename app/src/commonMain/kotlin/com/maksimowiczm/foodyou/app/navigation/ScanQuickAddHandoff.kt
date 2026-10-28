package com.maksimowiczm.foodyou.app.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimate

/** Local back-stack result, emitted only after a successful Quick Add insertion. */
internal object ScanQuickAddHandoff {
    const val SAVED_ITEM_KEY = "scan.quickAdd.savedItem"

    const val ENTRY_DATE_KEY = "foodEntry.date"
    private const val TARGET_DATE_KEY = "scan.targetDate"
    const val PAGE_INDEX_KEY = "scan.pageIndex"
    const val PAGE_COUNT_KEY = "scan.pageCount"
    private const val TARGET_SLOT_KEY = "scan.targetSlot"

    /** Explicit restart captures the current header selection; continuations use this frozen target. */
    fun beginQueue(state: SavedStateHandle, slotId: Long, epochDay: Long? = null) {
        state[TARGET_SLOT_KEY] = slotId
        state[TARGET_DATE_KEY] = epochDay
    }
    fun targetDate(state: SavedStateHandle, fallback: Long): Long = state[TARGET_DATE_KEY] ?: fallback
    fun targetSlot(state: SavedStateHandle, fallback: Long): Long = state[TARGET_SLOT_KEY] ?: fallback
    fun finishQueue(state: SavedStateHandle) {
        state.remove<Long>(TARGET_SLOT_KEY)
        state.remove<Long>(TARGET_DATE_KEY)
        state.remove<Int>(PAGE_INDEX_KEY)
        state.remove<Int>(PAGE_COUNT_KEY)
    }

    fun route(epochDay: Long, diarySlotId: Long, estimate: AiFoodEstimate, itemId: String,
        pageIndex: Int? = null, pageCount: Int? = null) =
        FoodDiaryCreateQuickAdd(epochDay, diarySlotId, estimate).copy(scanItemId = itemId,
            scanPageIndex = pageIndex, scanPageCount = pageCount)

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
