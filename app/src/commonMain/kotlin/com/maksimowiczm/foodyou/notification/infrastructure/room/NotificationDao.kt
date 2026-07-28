package com.maksimowiczm.foodyou.notification.infrastructure.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    /** The tab's default view. `id` breaks ties so rows recorded in the same second stay ordered. */
    @Query(
        """
        SELECT * FROM Notification
        WHERE isDismissed = 0
        ORDER BY occurredAtEpochSeconds DESC, id DESC
        """
    )
    fun observeActive(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM Notification ORDER BY occurredAtEpochSeconds DESC, id DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Insert suspend fun insert(entity: NotificationEntity)

    @Query("UPDATE Notification SET isDismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: Long)

    @Query("UPDATE Notification SET isDismissed = 1 WHERE isDismissed = 0")
    suspend fun dismissAll()

    /** Deliberately scoped to dismissed rows: nothing unread can ever be pruned. */
    @Query("DELETE FROM Notification WHERE isDismissed = 1 AND occurredAtEpochSeconds < :cutoff")
    suspend fun deleteDismissedOlderThan(cutoff: Long)
}
