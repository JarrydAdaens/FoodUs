package com.maksimowiczm.foodyou.fooddiary.domain.usecase

import com.maksimowiczm.foodyou.common.result.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import kotlinx.coroutines.CancellationException

sealed interface UpdateMealError {
    data class Invalid(val reason: MealValidationError) : UpdateMealError
    data object NotFoundOrStale : UpdateMealError
    data object PersistenceFailure : UpdateMealError
}

class UpdateMealUseCase(private val repository: MealRepository) {
    suspend fun update(id: MealId, name: String, items: List<MealItem>): Result<Unit, UpdateMealError> {
        validateMeal(name, items)?.let { return Err(UpdateMealError.Invalid(it)) }
        return try {
            if (repository.update(id, name.trim(), items)) Ok() else Err(UpdateMealError.NotFoundOrStale)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Err(UpdateMealError.PersistenceFailure)
        }
    }
}
