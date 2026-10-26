package com.maksimowiczm.foodyou.fooddiary.infrastructure.repository

import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlot
import com.maksimowiczm.foodyou.fooddiary.domain.repository.DiarySlotRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotDao
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime

internal class RoomDiarySlotRepository(private val diarySlotDao: DiarySlotDao) : DiarySlotRepository {
    override fun observeDiarySlot(diarySlotId: Long): Flow<DiarySlot?> =
        diarySlotDao.observeDiarySlotById(diarySlotId).map { it?.toModel() }

    override fun observeDiarySlots(): Flow<List<DiarySlot>> =
        diarySlotDao.observeDiarySlots().map { diarySlots -> diarySlots.map { it.toModel() } }

    override suspend fun insertDiarySlotWithLastRank(name: String, from: LocalTime, to: LocalTime) {
        val diarySlot = DiarySlot(id = 0, name = name, from = from, to = to, rank = 0)
        diarySlotDao.insertWithLastRank(diarySlot.toEntity())
    }

    override suspend fun updateDiarySlot(id: Long, name: String, from: LocalTime, to: LocalTime) {
        diarySlotDao.updateDiarySlotIgnoreRank(
            id = id,
            name = name,
            fromHour = from.hour,
            fromMinute = from.minute,
            toHour = to.hour,
            toMinute = to.minute,
        )
    }

    override suspend fun deleteDiarySlot(diarySlotId: Long) {
        diarySlotDao.delete(diarySlotId)
    }

    override suspend fun reorderDiarySlots(order: List<Long>) {
        val rankMap = order.withIndex().associate { (index, id) -> id to index }
        diarySlotDao.updateDiarySlotsRanks(rankMap)
    }
}

private fun DiarySlotEntity.toModel(): DiarySlot {
    val from = LocalTime(fromHour, fromMinute)
    val to = LocalTime(toHour, toMinute)

    return DiarySlot(id = id, name = name, from = from, to = to, rank = rank)
}

private fun DiarySlot.toEntity(): DiarySlotEntity =
    DiarySlotEntity(
        id = id,
        name = name,
        fromHour = from.hour,
        fromMinute = from.minute,
        toHour = to.hour,
        toMinute = to.minute,
        rank = rank,
    )
