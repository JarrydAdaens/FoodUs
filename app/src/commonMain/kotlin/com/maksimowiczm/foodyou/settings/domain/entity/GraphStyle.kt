package com.maksimowiczm.foodyou.settings.domain.entity

/** Visual style used for the calories and macronutrient breakdown on the daily goals card. */
enum class GraphStyle {
    Bar,
    Pie;

    companion object {
        val DEFAULT = Bar
    }
}
