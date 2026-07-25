package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MealTemplateDao {

    @Transaction
    @Query("SELECT * FROM MealTemplate ORDER BY createdEpochSeconds DESC")
    fun observeAll(): Flow<List<MealTemplateWithItems>>

    @Transaction
    @Query("SELECT * FROM MealTemplate WHERE id = :id")
    suspend fun getById(id: Long): MealTemplateWithItems?

    @Insert suspend fun insertTemplate(template: MealTemplateEntity): Long

    @Insert suspend fun insertItems(items: List<MealTemplateItemEntity>)

    @Query("DELETE FROM MealTemplate WHERE id = :id") suspend fun delete(id: Long)
}
