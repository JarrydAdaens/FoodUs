package com.maksimowiczm.foodyou.food.domain.usecase

import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.repository.RecipeRepository

/** Path-local detection permits a shared descendant while rejecting a back edge. */
internal suspend fun RecipeRepository.hasRecipeCycle(
    candidateId: NutritionalItemId.Recipe?,
    ingredients: List<NutritionalItemId>,
): Boolean {
    val active = mutableSetOf<NutritionalItemId.Recipe>()
    val complete = mutableSetOf<NutritionalItemId.Recipe>()
    suspend fun visit(id: NutritionalItemId.Recipe): Boolean {
        if (id == candidateId || id in active) return true
        if (id in complete) return false
        active.add(id)
        if (recipeDependencies(id).any { visit(it) }) return true
        active.remove(id)
        complete.add(id)
        return false
    }
    return ingredients.filterIsInstance<NutritionalItemId.Recipe>().any { visit(it) }
}
