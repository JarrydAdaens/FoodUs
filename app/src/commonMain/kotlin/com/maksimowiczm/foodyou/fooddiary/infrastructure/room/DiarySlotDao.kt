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
abstract class DiarySlotDao {
    @Query("SELECT EXISTS(SELECT 1 FROM meal WHERE id = :id)")
    abstract suspend fun exists(id: Long): Boolean


    @Query("SELECT * FROM meal WHERE id = :diarySlotId")
    abstract fun observeDiarySlotById(diarySlotId: Long): Flow<DiarySlotEntity?>

    @Query("SELECT * FROM meal ORDER BY rank ASC")
    abstract fun observeDiarySlots(): Flow<List<DiarySlotEntity>>

    @Delete protected abstract suspend fun deleteDiarySlot(diarySlot: DiarySlotEntity)

    @Transaction
    open suspend fun delete(diarySlotId: Long) {
        val diarySlot = observeDiarySlotById(diarySlotId).first() ?: return
        deleteDiarySlot(diarySlot)
    }

    @Update protected abstract suspend fun updateDiarySlot(diarySlot: DiarySlotEntity)

    @Query(
        """
        UPDATE meal
        SET name = :name,
            fromHour = :fromHour,
            fromMinute = :fromMinute,
            toHour = :toHour,
            toMinute = :toMinute
        WHERE id = :id
        """
    )
    abstract suspend fun updateDiarySlotIgnoreRank(
        id: Long,
        name: String,
        fromHour: Int,
        fromMinute: Int,
        toHour: Int,
        toMinute: Int,
    )

    @Transaction
    open suspend fun updateDiarySlotsRanks(map: Map<Long, Int>) {
        val diarySlots = observeDiarySlots().first()

        diarySlots.forEach {
            val updated = it.copy(rank = map[it.id] ?: it.rank)

            updateDiarySlot(updated)
        }
    }

    @Insert protected abstract fun insertDiarySlot(diarySlot: DiarySlotEntity)

    @Query(
        """
        SELECT COALESCE(MAX(rank), -1) + 1 
        FROM meal
        ORDER BY rank DESC
        LIMIT 1
        """
    )
    protected abstract suspend fun getLastDiarySlotRank(): Int

    @Transaction
    open suspend fun insertWithLastRank(diarySlot: DiarySlotEntity) {
        val lastRank = getLastDiarySlotRank()
        val diarySlotWithRank = diarySlot.copy(rank = lastRank + 1)
        insertDiarySlot(diarySlotWithRank)
    }
}
