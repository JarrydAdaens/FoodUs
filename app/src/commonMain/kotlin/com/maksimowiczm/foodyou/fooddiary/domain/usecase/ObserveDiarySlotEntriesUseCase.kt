package com.maksimowiczm.foodyou.fooddiary.domain.usecase

import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlotEntries
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlotsPreferences
import com.maksimowiczm.foodyou.fooddiary.domain.repository.DiarySlotRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.NutritionalItemDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import com.maksimowiczm.foodyou.fooddiary.domain.entity.containsTime
import com.maksimowiczm.foodyou.fooddiary.domain.entity.orderedDiaryEntries

class ObserveDiarySlotEntriesUseCase(
    private val diarySlotRepository: DiarySlotRepository,
    private val diarySlotsPreferencesRepository: UserPreferencesRepository<DiarySlotsPreferences>,
    private val foodEntryRepository: NutritionalItemDiaryEntryRepository,
    private val manualEntryRepository: ManualDiaryEntryRepository,
    private val dateProvider: DateProvider,
) {
    fun observe(date: LocalDate): Flow<List<DiarySlotEntries>> {
        return combine(
                diarySlotRepository.observeDiarySlots(),
                diarySlotsPreferencesRepository.observe(),
                dateProvider.observeTime(1.seconds),
            ) { diarySlots, prefs, time ->
                val timeBased = prefs.useTimeBasedSorting
                val ignoreAllDayDiarySlots = prefs.ignoreAllDayDiarySlots

                diarySlots.sortedBy { diarySlot ->
                    if (timeBased) {
                        if (shouldShowDiarySlot(diarySlot, time, ignoreAllDayDiarySlots)) {
                            diarySlot.rank
                        } else {
                            1_000_000 + diarySlot.rank
                        }
                    } else {
                        diarySlot.rank
                    }
                }
            }
            .flatMapLatest { diarySlots ->
                val diaryEntries =
                    diarySlots.map { diarySlot ->
                        combine(
                            manualEntryRepository.observeAll(diarySlotId = diarySlot.id, date = date),
                            foodEntryRepository.observeAll(diarySlotId = diarySlot.id, date = date),
                        ) { manualEntries, foodEntries ->
                            (manualEntries + foodEntries).orderedDiaryEntries()
                        }
                    }

                combine(diaryEntries) { entries ->
                    entries.zip(diarySlots) { entries, diarySlot ->
                        DiarySlotEntries(diarySlot = diarySlot, entries = entries)
                    }
                }
            }
    }
}

private fun shouldShowDiarySlot(diarySlot: DiarySlot, time: LocalTime, ignoreAllDayDiarySlots: Boolean): Boolean =
    !(ignoreAllDayDiarySlots && diarySlot.from == diarySlot.to) && diarySlot.containsTime(time)
