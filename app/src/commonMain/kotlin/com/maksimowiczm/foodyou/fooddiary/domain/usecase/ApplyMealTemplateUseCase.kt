package com.maksimowiczm.foodyou.fooddiary.domain.usecase

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.result.Err
import com.maksimowiczm.foodyou.common.result.Ok
import com.maksimowiczm.foodyou.common.result.Result
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplateId
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealTemplateRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

sealed interface ApplyMealTemplateError {
    data object TemplateNotFound : ApplyMealTemplateError

    data object MealNotFound : ApplyMealTemplateError
}

/**
 * Applies a saved template to a meal on a given day (Milestone 2, Story 13). Every template item is
 * recreated as its own manual diary entry carrying the snapshotted name and total nutrition, so the
 * result matches the user logging the same items again that day. Inserts run in a single
 * transaction so a partially applied template can never be left behind.
 */
class ApplyMealTemplateUseCase(
    private val templateRepository: MealTemplateRepository,
    private val manualEntryRepository: ManualDiaryEntryRepository,
    private val mealRepository: MealRepository,
    private val transactionProvider: TransactionProvider,
    private val dateProvider: DateProvider,
) {
    suspend fun apply(
        templateId: MealTemplateId,
        mealId: Long,
        date: LocalDate,
    ): Result<Unit, ApplyMealTemplateError> {
        val template =
            templateRepository.getTemplate(templateId)
                ?: return Err(ApplyMealTemplateError.TemplateNotFound)

        val now = dateProvider.now()
        return transactionProvider.withTransaction {
            val meal = mealRepository.observeMeal(mealId).first()
            if (meal == null) {
                return@withTransaction Err(ApplyMealTemplateError.MealNotFound)
            }

            template.items.forEach { item ->
                manualEntryRepository.insert(
                    name = item.name,
                    mealId = mealId,
                    date = date,
                    nutritionFacts = item.nutritionFacts,
                    createdAt = now,
                )
            }

            Ok()
        }
    }
}
