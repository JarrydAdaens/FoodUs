package com.maksimowiczm.foodyou.app.ui.food.diary.placeholder

import androidx.compose.runtime.Immutable

/**
 * UI state for the placeholder resolution meta screen (Milestone 2, Story 9). Carries the saved
 * placeholder context plus the AI query-generation status.
 */
@Immutable
internal data class PlaceholderMetaUiState(
    val loaded: Boolean = false,
    val epochDay: Long = 0,
    val mealId: Long = 0,
    val name: String = "",
    val description: String? = null,
    /** Name of the meal being logged into (e.g. "Lunch"), used only as AI context. */
    val mealName: String? = null,
    val aiConfigured: Boolean = false,
    val generatingQuery: Boolean = false,
    /** Last AI failure message, safe to show to the user; null when there is nothing to report. */
    val errorMessage: String? = null,
)
