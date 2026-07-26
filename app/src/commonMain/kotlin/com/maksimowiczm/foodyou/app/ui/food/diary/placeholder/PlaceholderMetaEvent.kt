package com.maksimowiczm.foodyou.app.ui.food.diary.placeholder

/** One-off UI events emitted by [PlaceholderMetaViewModel]. */
internal sealed interface PlaceholderMetaEvent {
    /**
     * The AI produced a food-search query; the caller should open search pre-populated with it for
     * the given day and meal.
     */
    data class QueryGenerated(val epochDay: Long, val mealId: Long, val query: String) :
        PlaceholderMetaEvent

    /** The placeholder was removed; the caller should navigate back. */
    data object Deleted : PlaceholderMetaEvent
}
