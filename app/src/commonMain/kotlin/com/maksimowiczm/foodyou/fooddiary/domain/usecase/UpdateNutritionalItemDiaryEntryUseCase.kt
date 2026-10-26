package com.maksimowiczm.foodyou.fooddiary.domain.usecase

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.hasValidValues
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementResolver
import com.maksimowiczm.foodyou.common.log.Logger
import com.maksimowiczm.foodyou.common.log.logAndReturnFailure
import com.maksimowiczm.foodyou.common.result.Ok
import com.maksimowiczm.foodyou.common.result.Result
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.repository.DiarySlotRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.NutritionalItemDiaryEntryRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDate

sealed interface UpdateNutritionalItemDiaryEntryError {
    data object EntryNotFoundFood : UpdateNutritionalItemDiaryEntryError

    data object InvalidMeasurement : UpdateNutritionalItemDiaryEntryError

    data object DiarySlotNotFound : UpdateNutritionalItemDiaryEntryError
}

class UpdateNutritionalItemDiaryEntryUseCase(
    private val diarySlotRepository: DiarySlotRepository,
    private val entryRepository: NutritionalItemDiaryEntryRepository,
    private val dateProvider: DateProvider,
    private val transactionProvider: TransactionProvider,
    private val logger: Logger,
) {
    suspend fun update(
        id: NutritionalItemDiaryEntryId,
        measurement: Measurement,
        diarySlotId: Long,
        date: LocalDate,
    ): Result<Unit, UpdateNutritionalItemDiaryEntryError> {
        return transactionProvider.withTransaction {
            val entry = entryRepository.observe(id).firstOrNull()

            if (entry == null) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    error = UpdateNutritionalItemDiaryEntryError.EntryNotFoundFood,
                    message = { "Diary entry with id $id not found" },
                )
            }

            if (entry.food.nutritionFor(measurement) == null) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    error = UpdateNutritionalItemDiaryEntryError.InvalidMeasurement,
                    message = { "Portion must be positive, finite and resolvable." },
                )
            }

            val diarySlot = diarySlotRepository.observeDiarySlot(diarySlotId).firstOrNull()

            if (diarySlot == null) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    error = UpdateNutritionalItemDiaryEntryError.DiarySlotNotFound,
                    message = { "Meal with ID $diarySlotId not found" },
                )
            }

            val updated =
                entry.copy(
                    measurement = measurement,
                    diarySlotId = diarySlotId,
                    date = date,
                    updatedAt = dateProvider.now(),
                )

            entryRepository.update(updated)
            Ok(Unit)
        }
    }

    private companion object {
        const val TAG = "UpdateFoodDiaryEntryUseCase"
    }
}
