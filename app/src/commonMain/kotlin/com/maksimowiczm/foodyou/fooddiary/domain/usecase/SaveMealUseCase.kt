package com.maksimowiczm.foodyou.fooddiary.domain.usecase

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.result.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

sealed interface SaveMealError {
    data object BlankName : SaveMealError
    data object EmptyDiarySlot : SaveMealError
    data object InvalidMeal : SaveMealError
    data object PersistenceFailure : SaveMealError
}

/** Captures one coherent diary selection, preserving source history and unknown nutrition. */
class SaveMealUseCase(
    private val foodEntryRepository: NutritionalItemDiaryEntryRepository,
    private val manualEntryRepository: ManualDiaryEntryRepository,
    private val mealRepository: MealRepository,
    private val transactionProvider: TransactionProvider,
) {
    suspend fun save(name: String, diarySlotId: Long, date: LocalDate): Result<MealId, SaveMealError> {
        if (name.isBlank()) return Err(SaveMealError.BlankName)
        return try {
            transactionProvider.withTransaction {
                val source = manualEntryRepository.observeAll(diarySlotId, date).first() +
                    foodEntryRepository.observeAll(diarySlotId, date).first()
                if (source.isEmpty()) return@withTransaction Err(SaveMealError.EmptyDiarySlot)
                if (source.any { !it.nutritionFacts.validMealNutrition() }) {
                    return@withTransaction Err(SaveMealError.InvalidMeal)
                }
                val items = source.sortedBy { it.name }.map { it.toMealItem() }
                if (validateMeal(name, items) != null) return@withTransaction Err(SaveMealError.InvalidMeal)
                Ok(mealRepository.save(name.trim(), items))
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Err(SaveMealError.PersistenceFailure)
        }
    }
}
