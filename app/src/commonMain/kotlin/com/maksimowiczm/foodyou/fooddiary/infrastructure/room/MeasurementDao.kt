package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
abstract class MeasurementDao {
    @Query("SELECT * FROM Measurement WHERE mealId = :slot AND epochDay = :day")
    abstract suspend fun entries(slot: Long, day: Long): List<MeasurementEntity>
    @Query("SELECT * FROM DiaryProduct WHERE id = :id")
    abstract suspend fun product(id: Long): DiaryProductEntity?
    @Query("SELECT * FROM DiaryRecipe WHERE id = :id")
    abstract suspend fun recipe(id: Long): DiaryRecipeEntity?
    @Query("SELECT * FROM DiaryRecipeIngredient WHERE recipeId = :id ORDER BY id")
    abstract suspend fun ingredients(id: Long): List<DiaryRecipeIngredientEntity>
    @Query("SELECT * FROM DiaryReportedRecipeComponent WHERE recipeId = :id ORDER BY position")
    abstract suspend fun components(id: Long): List<DiaryReportedRecipeComponentEntity>
    @Query("SELECT COUNT(*) FROM Measurement WHERE productId = :id")
    abstract suspend fun productEntries(id: Long): Int
    @Query("SELECT COUNT(*) FROM DiaryRecipeIngredient WHERE ingredientProductId = :id")
    abstract suspend fun productIngredients(id: Long): Int
    @Query("SELECT COUNT(*) FROM Measurement WHERE recipeId = :id")
    abstract suspend fun recipeEntries(id: Long): Int
    @Query("SELECT COUNT(*) FROM DiaryRecipeIngredient WHERE ingredientRecipeId = :id")
    abstract suspend fun recipeIngredients(id: Long): Int
    @Query("DELETE FROM Measurement WHERE id = :id")
    abstract suspend fun deleteEntry(id: Long)


    @Insert protected abstract suspend fun insertMeasurementRaw(measurement: MeasurementEntity): Long

    @Query("SELECT MAX(sortOrder) FROM (SELECT sortOrder FROM Measurement WHERE mealId = :slot AND epochDay = :day UNION ALL SELECT sortOrder FROM ManualDiaryEntry WHERE mealId = :slot AND dateEpochDay = :day)")
    protected abstract suspend fun lastOrder(slot: Long, day: Long): Long?

    suspend fun nextOrder(slot: Long, day: Long): Long? = lastOrder(slot, day)?.plus(1)

    @Transaction
    open suspend fun insertMeasurement(measurement: MeasurementEntity): Long =
        insertMeasurementRaw(if (measurement.sortOrder != null) measurement else
            measurement.copy(sortOrder = lastOrder(measurement.diarySlotId, measurement.epochDay)?.plus(1)))

    @Transaction
    open suspend fun insertMeasurements(measurements: List<MeasurementEntity>) {
        measurements.forEach { insertMeasurement(it) }
    }

    @Transaction
    open suspend fun replaceMeasurement(
        measurementId: Long,
        measurements: List<MeasurementEntity>,
    ) {
        val measurement = observeMeasurementById(measurementId).first()

        if (measurement == null) {
            return
        }

        deleteMeasurement(measurement)
        insertMeasurements(measurements)
    }

    @Update abstract suspend fun updateMeasurement(measurement: MeasurementEntity)

    @Delete protected abstract suspend fun deleteMeasurement(measurement: MeasurementEntity)

    @Transaction
    open suspend fun deleteMeasurement(id: Long) {
        val measurement = observeMeasurementById(id).first() ?: return
        deleteMeasurement(measurement)
    }

    @Query(
        """
        SELECT *
        FROM Measurement
        WHERE id = :measurementId 
        """
    )
    abstract fun observeMeasurementById(measurementId: Long): Flow<MeasurementEntity?>

    @Query(
        """
        SELECT * 
        FROM Measurement
        WHERE 
            mealId = :diarySlotId
            AND epochDay = :epochDay 
        """
    )
    abstract fun observeMeasurements(diarySlotId: Long, epochDay: Long): Flow<List<MeasurementEntity>>

    @Query(
        """
        SELECT *
        FROM DiaryProduct
        WHERE id = :id
        """
    )
    abstract fun observeDiaryProduct(id: Long): Flow<DiaryProductEntity?>

    @Insert abstract suspend fun insertDiaryProduct(diaryProduct: DiaryProductEntity): Long

    @Insert
    abstract suspend fun insertDiaryRecipeIngredient(
        diaryRecipeIngredient: DiaryRecipeIngredientEntity
    )

    @Insert abstract suspend fun insertDiaryRecipe(diaryRecipe: DiaryRecipeEntity): Long

    @Insert abstract suspend fun insertReportedComponents(components: List<DiaryReportedRecipeComponentEntity>)

    @Query("SELECT * FROM DiaryReportedRecipeComponent WHERE recipeId = :recipeId ORDER BY position")
    abstract fun observeReportedComponents(recipeId: Long): Flow<List<DiaryReportedRecipeComponentEntity>>

    @Query(
        """
        SELECT *
        FROM DiaryRecipe
        WHERE id = :id
        """
    )
    abstract fun observeDiaryRecipe(id: Long): Flow<DiaryRecipeEntity?>

    @Query(
        """
        SELECT *
        FROM DiaryRecipeIngredient
        WHERE recipeId = :id ORDER BY id
        """
    )
    abstract fun observeDiaryRecipeIngredients(id: Long): Flow<List<DiaryRecipeIngredientEntity>>

    @Delete protected abstract suspend fun deleteDiaryProduct(diaryProduct: DiaryProductEntity)

    @Transaction
    open suspend fun deleteDiaryProduct(id: Long) {
        val diaryProduct = observeDiaryProduct(id).first() ?: return
        deleteDiaryProduct(diaryProduct)
    }

    @Delete protected abstract suspend fun deleteDiaryRecipe(diaryRecipe: DiaryRecipeEntity)

    @Transaction
    open suspend fun deleteDiaryRecipe(id: Long) {
        val diaryRecipe = observeDiaryRecipe(id).first() ?: return
        deleteDiaryRecipe(diaryRecipe)
    }
}
