package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ManualDiaryEntryDao {

    @Query("SELECT * FROM ManualDiaryEntry WHERE id = :id")
    fun observe(id: Long): Flow<ManualDiaryEntryEntity?>

    @Query(
        """
            SELECT * 
            FROM ManualDiaryEntry 
            WHERE mealId = :diarySlotId AND dateEpochDay = :dateEpochDay
        """
    )
    fun observeAll(diarySlotId: Long, dateEpochDay: Long): Flow<List<ManualDiaryEntryEntity>>

    @Insert suspend fun insertRaw(entry: ManualDiaryEntryEntity): Long

    @androidx.room.Transaction
    suspend fun insert(entry: ManualDiaryEntryEntity): Long = insertRaw(
        if (entry.sortOrder != null) entry else entry.copy(sortOrder = lastOrder(entry.diarySlotId, entry.dateEpochDay)?.plus(1))
    )

    @Query("SELECT MAX(sortOrder) FROM (SELECT sortOrder FROM Measurement WHERE mealId = :slot AND epochDay = :day UNION ALL SELECT sortOrder FROM ManualDiaryEntry WHERE mealId = :slot AND dateEpochDay = :day)")
    suspend fun lastOrder(slot: Long, day: Long): Long?

    @Query("SELECT * FROM ManualDiaryEntry WHERE mealId = :slot AND dateEpochDay = :day")
    suspend fun entries(slot: Long, day: Long): List<ManualDiaryEntryEntity>

    @Update suspend fun update(entry: ManualDiaryEntryEntity)

    @Query("SELECT * FROM ManualDiaryEntry WHERE id = :id")
    suspend fun get(id: Long): ManualDiaryEntryEntity?

    @androidx.room.Transaction
    suspend fun updatePreservingOrder(entry: ManualDiaryEntryEntity) {
        val previous = get(entry.id) ?: return
        val order = if (previous.diarySlotId == entry.diarySlotId && previous.dateEpochDay == entry.dateEpochDay)
            previous.sortOrder else lastOrder(entry.diarySlotId, entry.dateEpochDay)?.plus(1)
        update(entry.copy(sortOrder = order))
    }

    @Query("DELETE FROM ManualDiaryEntry WHERE id = :id") suspend fun delete(id: Long)
}
