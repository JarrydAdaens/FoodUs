package com.maksimowiczm.foodyou.fooddiary.domain.repository

import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplate
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplateId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplateItem
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for reusable meal templates (Milestone 2, Story 13). */
interface MealTemplateRepository {
    /** Observes every saved template, each with its snapshotted items, newest first. */
    fun observeAll(): Flow<List<MealTemplate>>

    /** Reads a single template with its items, or `null` if it no longer exists. */
    suspend fun getTemplate(id: MealTemplateId): MealTemplate?

    /** Saves a new named template from the given item snapshots and returns its id. */
    suspend fun save(name: String, items: List<MealTemplateItem>): MealTemplateId

    /** Permanently deletes a template. Already-applied diary entries are unaffected. */
    suspend fun delete(id: MealTemplateId)
}
