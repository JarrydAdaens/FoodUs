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
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.repository.DiarySlotRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.NutritionalItemDiaryEntryRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

sealed interface CreateNutritionalItemDiaryEntryError {
    data object DiarySlotNotFound : CreateNutritionalItemDiaryEntryError

    data object InvalidMeasurement : CreateNutritionalItemDiaryEntryError
}

class CreateNutritionalItemDiaryEntryUseCase(
    private val diarySlotRepository: DiarySlotRepository,
    private val entryRepository: NutritionalItemDiaryEntryRepository,
    private val transactionProvider: TransactionProvider,
    private val dateProvider: DateProvider,
    private val logger: Logger,
) {
    suspend fun createDiaryEntry(
        measurement: Measurement,
        diarySlotId: Long,
        date: LocalDate,
        food: NutritionalItemSnapshot,
    ): Result<NutritionalItemDiaryEntryId, CreateNutritionalItemDiaryEntryError> {
        if (food.nutritionFor(measurement) == null) {
            return logger.logAndReturnFailure(
                tag = TAG,
                error = CreateNutritionalItemDiaryEntryError.InvalidMeasurement,
                message = { "Portion must be positive, finite and resolvable." },
            )
        }

        val now = dateProvider.now()
        return transactionProvider.withTransaction {
            val diarySlot = diarySlotRepository.observeDiarySlot(diarySlotId).first()

            if (diarySlot == null) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    throwable = null,
                    error = CreateNutritionalItemDiaryEntryError.DiarySlotNotFound,
                    message = { "Meal with id $diarySlotId not found" },
                )
            }

            val entryId =
                entryRepository.insert(
                    measurement = measurement,
                    diarySlotId = diarySlotId,
                    date = date,
                    food = food,
                    createdAt = now,
                )

            Ok(entryId)
        }
    }

    private companion object {
        const val TAG = "CreateFoodDiaryEntryUseCase"
    }
}
