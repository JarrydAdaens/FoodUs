package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    @Transaction
    @Query("SELECT * FROM MealTemplate ORDER BY createdEpochSeconds DESC")
    fun observeAll(): Flow<List<MealWithItems>>

    @Transaction
    @Query("SELECT * FROM MealTemplate WHERE id = :id")
    suspend fun getById(id: Long): MealWithItems?

    @Insert suspend fun insertMeal(meal: MealEntity): Long

    @Insert suspend fun insertItems(items: List<MealItemEntity>)

    @Update suspend fun updateMeal(meal: MealEntity)

    @Update suspend fun updateItems(items: List<MealItemEntity>)

    @Query("DELETE FROM MealTemplateItem WHERE id IN (:ids)")
    suspend fun deleteItems(ids: List<Long>)

    @Query("DELETE FROM MealTemplate WHERE id = :id") suspend fun delete(id: Long)
}
