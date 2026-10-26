package com.maksimowiczm.foodyou.fooddiary.domain.repository

import com.maksimowiczm.foodyou.fooddiary.domain.entity.Meal
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealItem
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for reusable diarySlot Meals (Milestone 2, Story 13). */
interface MealRepository {
    /** Observes every saved Meal, each with its snapshotted items, newest first. */
    fun observeAll(): Flow<List<Meal>>

    /** Reads a single Meal with its items, or `null` if it no longer exists. */
    suspend fun getMeal(id: MealId): Meal?

    /** Saves a new named Meal from the given item snapshots and returns its id. */
    suspend fun save(name: String, items: List<MealItem>): MealId

    suspend fun save(meal: Meal): MealId {
        check(meal.reportedNutrition == null && meal.metadata == null) { "Reported Meal insertion unavailable" }
        return save(meal.name, meal.items)
    }

    /** Atomically updates an existing aggregate, retaining owned item IDs; false if stale. */
    suspend fun update(id: MealId, name: String, items: List<MealItem>): Boolean

    /** Permanently deletes a Meal. Already-applied diary entries are unaffected. */
    suspend fun delete(id: MealId)
}
