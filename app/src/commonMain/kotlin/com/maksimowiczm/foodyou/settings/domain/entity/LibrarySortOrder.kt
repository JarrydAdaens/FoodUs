package com.maksimowiczm.foodyou.settings.domain.entity

/** One remembered ordering for the personal library, independent of search-provider preferences. */
enum class LibrarySortOrder {
    Alphabetical, ReverseAlphabetical, OldestCreated, NewestCreated, LowestCalories, HighestCalories;
    val byCalories: Boolean get() = this == LowestCalories || this == HighestCalories
    companion object {
        fun fromStored(value: String?): LibrarySortOrder = entries.firstOrNull { it.name == value } ?: Alphabetical
    }
}
