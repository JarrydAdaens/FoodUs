package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.maksimowiczm.foodyou.common.log.Logger
import com.maksimowiczm.foodyou.common.system.SystemDetails
import com.maksimowiczm.foodyou.fooddiary.domain.service.LocalizedDiarySlotsProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class InitializeDiarySlotsCallback(
    private val provider: LocalizedDiarySlotsProvider,
    private val systemDetails: SystemDetails,
    private val logger: Logger,
) : RoomDatabase.Callback() {

    override fun onCreate(connection: SQLiteConnection) {
        val diarySlots = runBlocking {
            val languageTag = systemDetails.languageTag.first()

            provider.getDiarySlots(languageTag).mapIndexed { i, (name, start, end) ->
                DiarySlotEntity(
                    name = name,
                    fromHour = start.hour,
                    fromMinute = start.minute,
                    toHour = end.hour,
                    toMinute = end.minute,
                    rank = i,
                )
            }
        }

        connection.execSQL("BEGIN TRANSACTION;")

        try {
            diarySlots.forEach { diarySlot ->
                val query =
                    """
                    INSERT INTO Meal (name, fromHour, fromMinute, toHour, toMinute, rank) 
                    VALUES ($1, $2, $3, $4, $5, $6)
                    """
                        .trimIndent()

                val statement = connection.prepare(query)

                statement.bindText(1, diarySlot.name)
                statement.bindInt(2, diarySlot.fromHour)
                statement.bindInt(3, diarySlot.fromMinute)
                statement.bindInt(4, diarySlot.toHour)
                statement.bindInt(5, diarySlot.toMinute)
                statement.bindInt(6, diarySlot.rank)

                statement.step()

                statement.close()
            }

            connection.execSQL("COMMIT;")
        } catch (e: Exception) {
            logger.e(TAG, e) { "Failed to insert meals" }
            connection.execSQL("ROLLBACK;")
        }
    }

    companion object {
        const val TAG = "InitializeMealsCallback"
    }
}
