package com.maksimowiczm.foodyou.fooddiary.domain.usecase

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.result.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

sealed interface ApplyMealError {
    data object MealNotFound : ApplyMealError
    data object DiarySlotNotFound : ApplyMealError
    data object InvalidMeal : ApplyMealError
    data object PersistenceFailure : ApplyMealError
}

/** One committed version, one atomic append, and no further scaling of captured totals. */
class ApplyMealUseCase(
    private val mealRepository: MealRepository,
    private val manualEntryRepository: ManualDiaryEntryRepository,
    private val diarySlotRepository: DiarySlotRepository,
    private val transactionProvider: TransactionProvider,
    private val dateProvider: DateProvider,
) {
    suspend fun apply(mealId: MealId, diarySlotId: Long, date: LocalDate): Result<Unit, ApplyMealError> =
        try {
            transactionProvider.withTransaction {
                val meal = mealRepository.getMeal(mealId)
                    ?: return@withTransaction Err(ApplyMealError.MealNotFound)
                if (diarySlotRepository.observeDiarySlot(diarySlotId).first() == null) {
                    return@withTransaction Err(ApplyMealError.DiarySlotNotFound)
                }
                if (validateMeal(meal.name, meal.items) != null) return@withTransaction Err(ApplyMealError.InvalidMeal)
                val now = dateProvider.now()
                meal.items.forEach { item ->
                    manualEntryRepository.insert(ManualDiaryEntry(
                        id = ManualDiaryEntryId(0), updatedAt = now,
                        name = item.name, diarySlotId = diarySlotId, date = date,
                        nutritionFacts = item.nutritionFacts, createdAt = now,
                        servingCount = item.servingCount, weightGrams = item.weightGrams,
                        capturedPortion = item.capturedPortion,
                        portionDescription = item.portionDescription, nutritionMetadata = item.metadata,
                    ))
                }
                Ok()
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Err(ApplyMealError.PersistenceFailure)
        }
}
