package com.maksimowiczm.foodyou.fooddiary.domain.usecase

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.log.Logger
import com.maksimowiczm.foodyou.common.result.Err
import com.maksimowiczm.foodyou.common.result.Ok
import com.maksimowiczm.foodyou.common.result.Result
import com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.repository.DiarySlotRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.NutritionalItemDiaryEntryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDate

sealed interface UnpackNutritionalItemDiaryEntryError {
    data object EntryNotFoundFood : UnpackNutritionalItemDiaryEntryError
    data object DiarySlotNotFound : UnpackNutritionalItemDiaryEntryError
    data object EntryCannotBeUnpackedFood : UnpackNutritionalItemDiaryEntryError
    data object InvalidMeasurement : UnpackNutritionalItemDiaryEntryError
    data object SaveFailed : UnpackNutritionalItemDiaryEntryError
}

/** Explicitly appends or replaces direct constituents in a single rollback boundary. */
class UnpackNutritionalItemDiaryEntryUseCase(
    private val entryRepository: NutritionalItemDiaryEntryRepository,
    private val diarySlotRepository: DiarySlotRepository,
    private val transactionProvider: TransactionProvider,
    private val dateProvider: DateProvider,
    @Suppress("unused") private val logger: Logger,
) {
    suspend fun append(food: RecipeSnapshot, measurement: Measurement, diarySlotId: Long, date: LocalDate): Result<Unit, UnpackNutritionalItemDiaryEntryError> =
        perform(null, food, measurement, diarySlotId, date)

    suspend fun unpack(id: NutritionalItemDiaryEntryId, measurement: Measurement, diarySlotId: Long, date: LocalDate): Result<Unit, UnpackNutritionalItemDiaryEntryError> =
        perform(id, null, measurement, diarySlotId, date)

    private suspend fun perform(id: NutritionalItemDiaryEntryId?, selected: RecipeSnapshot?, measurement: Measurement, diarySlotId: Long, date: LocalDate): Result<Unit, UnpackNutritionalItemDiaryEntryError> = try {
        transactionProvider.withTransaction {
            val original = id?.let { entryRepository.observe(it).firstOrNull() }
            if (id != null && original == null) return@withTransaction Err(UnpackNutritionalItemDiaryEntryError.EntryNotFoundFood)
            val recipe = (original?.food ?: selected) as? RecipeSnapshot
                ?: return@withTransaction Err(UnpackNutritionalItemDiaryEntryError.EntryCannotBeUnpackedFood)
            if (recipe.isReported) return@withTransaction Err(UnpackNutritionalItemDiaryEntryError.EntryCannotBeUnpackedFood)
            val constituents = recipe.unpack(measurement)
                ?: return@withTransaction Err(UnpackNutritionalItemDiaryEntryError.InvalidMeasurement)
            if (diarySlotRepository.observeDiarySlot(diarySlotId).firstOrNull() == null)
                return@withTransaction Err(UnpackNutritionalItemDiaryEntryError.DiarySlotNotFound)
            // No expected validation failures remain after the first write. Any exception/cancellation rolls back all writes.
            val createdAt = original?.createdAt ?: dateProvider.now()
            if (id != null) entryRepository.delete(id)
            constituents.forEach {
                entryRepository.insert(it.measurement, diarySlotId, date, it.food, createdAt)
            }
            Ok(Unit)
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        Err(UnpackNutritionalItemDiaryEntryError.SaveFailed)
    }
}
