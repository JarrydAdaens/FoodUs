package com.maksimowiczm.foodyou.settings.domain.entity

enum class HomeCard {
    Calendar,
    Goals,
    DiarySlots;

    companion object {
        val defaultOrder: List<HomeCard> = listOf(Calendar, Goals, DiarySlots)
    }
}
