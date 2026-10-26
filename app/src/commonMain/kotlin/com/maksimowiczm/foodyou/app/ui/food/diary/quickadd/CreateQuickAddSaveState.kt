package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

/** Guards one live form insertion, including the interval before navigation consumes Saved. */
internal data class CreateQuickAddSaveState(
    val saving: Boolean = false,
    val saved: Boolean = false,
    val failed: Boolean = false,
) {
    val busy: Boolean get() = saving || saved
    fun start(): CreateQuickAddSaveState? = if (busy) null else copy(saving = true, failed = false)
    fun success() = copy(saving = false, saved = true, failed = false)
    fun failure() = copy(saving = false, saved = false, failed = true)
}
