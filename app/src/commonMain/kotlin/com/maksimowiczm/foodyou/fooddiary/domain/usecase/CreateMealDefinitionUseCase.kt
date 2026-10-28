package com.maksimowiczm.foodyou.fooddiary.domain.usecase

import com.maksimowiczm.foodyou.common.result.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import kotlinx.coroutines.CancellationException

/** Saves a definition draft without reading or writing a diary destination. Repository save is atomic. */
class CreateMealDefinitionUseCase(private val repository: MealRepository) {
    suspend fun create(name: String, items: List<MealItem>): Result<MealId, UpdateMealError> {
        validateMeal(name, items)?.let { return Err(UpdateMealError.Invalid(it)) }
        return try {
            Ok(repository.save(name.trim(), items.map { it.copy(id = null) }))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Err(UpdateMealError.PersistenceFailure)
        }
    }
}
