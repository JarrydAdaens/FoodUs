package com.maksimowiczm.foodyou.fooddiary.domain.service

import kotlinx.datetime.LocalTime

/**
 * Provides a list of localized diarySlots. Implementations should return diarySlots appropriate for the
 * user's locale.
 */
fun interface LocalizedDiarySlotsProvider {
    /**
     * Returns a list of diarySlots, each represented as a Triple containing the diary slot name, start time,
     * and end time.
     *
     * @param languageTag ISO 3166-1 alpha-2 country code representing the desired locale
     */
    suspend fun getDiarySlots(languageTag: String): List<LocalizedDiarySlot>

    data class LocalizedDiarySlot(val name: String, val start: LocalTime, val end: LocalTime)
}
