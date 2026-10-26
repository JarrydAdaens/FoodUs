package com.maksimowiczm.foodyou.fooddiary.infrastructure.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.maksimowiczm.foodyou.common.infrastructure.datastore.AbstractDataStoreUserPreferencesRepository
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlotsCardsLayout
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlotsPreferences

internal class DataStoreDiarySlotsPreferencesRepository(dataStore: DataStore<Preferences>) :
    AbstractDataStoreUserPreferencesRepository<DiarySlotsPreferences>(dataStore) {
    override fun Preferences.toUserPreferences(): DiarySlotsPreferences =
        DiarySlotsPreferences(
            layout = getLayout(),
            useTimeBasedSorting = this[DiarySlotsPreferencesDataStoreKeys.useTimeBasedSorting] ?: false,
            ignoreAllDayDiarySlots = this[DiarySlotsPreferencesDataStoreKeys.ignoreAllDayDiarySlots] ?: false,
        )

    override fun MutablePreferences.applyUserPreferences(updated: DiarySlotsPreferences) {
        this[DiarySlotsPreferencesDataStoreKeys.layout] = updated.layout.ordinal
        this[DiarySlotsPreferencesDataStoreKeys.useTimeBasedSorting] = updated.useTimeBasedSorting
        this[DiarySlotsPreferencesDataStoreKeys.ignoreAllDayDiarySlots] = updated.ignoreAllDayDiarySlots
    }
}

private fun Preferences.getLayout(): DiarySlotsCardsLayout =
    runCatching { this[DiarySlotsPreferencesDataStoreKeys.layout]?.let { DiarySlotsCardsLayout.entries[it] } }
        .getOrNull() ?: DiarySlotsCardsLayout.default

private object DiarySlotsPreferencesDataStoreKeys {
    val layout = intPreferencesKey("fooddiary:meals_preferences:layout")
    val useTimeBasedSorting =
        booleanPreferencesKey("fooddiary:meals_preferences:use_time_based_sorting")
    val ignoreAllDayDiarySlots =
        booleanPreferencesKey("fooddiary:meals_preferences:ignore_all_day_meals")
}
