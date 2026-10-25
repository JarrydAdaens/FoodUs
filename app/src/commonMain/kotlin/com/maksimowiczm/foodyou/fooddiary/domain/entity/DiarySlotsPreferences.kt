package com.maksimowiczm.foodyou.fooddiary.domain.entity

import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferences

data class DiarySlotsPreferences(
    val layout: DiarySlotsCardsLayout,
    val useTimeBasedSorting: Boolean,
    val ignoreAllDayDiarySlots: Boolean,
) : UserPreferences
