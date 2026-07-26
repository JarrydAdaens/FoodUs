package com.maksimowiczm.foodyou.fooddiary.domain.usecase

import com.maksimowiczm.foodyou.common.result.Err
import com.maksimowiczm.foodyou.common.result.Ok
import com.maksimowiczm.foodyou.common.result.Result
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplateId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.toMealTemplateItem
import com.maksimowiczm.foodyou.fooddiary.domain.repository.FoodDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealTemplateRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

sealed interface SaveMealTemplateError {
    /** The name was blank after trimming. */
    data object BlankName : SaveMealTemplateError

    /** The meal had no entries on that day, so there was nothing to save. */
    data object EmptyMeal : SaveMealTemplateError
}

/**
 * Saves the current entries of a meal on a given day as a reusable named template (Milestone 2,
 * Story 13). Each entry — food, recipe or manual / Quick Add — is snapshotted into a template item
 * so applying the template later recreates the same items as separate diary entries.
 */
class SaveMealTemplateUseCase(
    private val foodEntryRepository: FoodDiaryEntryRepository,
    private val manualEntryRepository: ManualDiaryEntryRepository,
    private val templateRepository: MealTemplateRepository,
) {
    suspend fun save(
        name: String,
        mealId: Long,
        date: LocalDate,
    ): Result<MealTemplateId, SaveMealTemplateError> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Err(SaveMealTemplateError.BlankName)
        }

        val foodEntries = foodEntryRepository.observeAll(mealId, date).first()
        val manualEntries = manualEntryRepository.observeAll(mealId, date).first()

        val items =
            (manualEntries + foodEntries).sortedBy { it.name }.map { it.toMealTemplateItem() }

        if (items.isEmpty()) {
            return Err(SaveMealTemplateError.EmptyMeal)
        }

        return Ok(templateRepository.save(name = trimmedName, items = items))
    }
}
