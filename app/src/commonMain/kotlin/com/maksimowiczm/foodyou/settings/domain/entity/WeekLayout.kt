package com.maksimowiczm.foodyou.settings.domain.entity

/** Layout used for the diary home's date navigation strip. */
enum class WeekLayout {
    /** Day-by-day pager with a calendar picker (the original behavior). */
    Scrolling,

    /** Pinned Monday-to-Sunday week strip that swipes in whole-week increments. */
    Fixed;

    companion object {
        val DEFAULT = Scrolling
    }
}
