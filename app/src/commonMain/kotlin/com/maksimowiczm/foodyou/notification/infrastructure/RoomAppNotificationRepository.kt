package com.maksimowiczm.foodyou.notification.infrastructure

import com.maksimowiczm.foodyou.notification.domain.AppNotification
import com.maksimowiczm.foodyou.notification.domain.AppNotificationRepository
import com.maksimowiczm.foodyou.notification.infrastructure.room.NotificationDao
import com.maksimowiczm.foodyou.notification.infrastructure.room.NotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal class RoomAppNotificationRepository(private val dao: NotificationDao) :
    AppNotificationRepository {

    override fun observe(includeDismissed: Boolean): Flow<List<AppNotification>> =
        (if (includeDismissed) dao.observeAll() else dao.observeActive()).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insert(notification: AppNotification) {
        dao.insert(notification.toEntity())
    }

    override suspend fun dismiss(id: Long) {
        dao.dismiss(id)
    }

    override suspend fun dismissAll() {
        dao.dismissAll()
    }

    override suspend fun deleteDismissedOlderThan(cutoffEpochSeconds: Long) {
        dao.deleteDismissedOlderThan(cutoffEpochSeconds)
    }
}

private val json = Json

private fun NotificationEntity.toDomain(): AppNotification =
    AppNotification(
        id = id,
        typeKey = type,
        occurredAtEpochSeconds = occurredAtEpochSeconds,
        isDismissed = isDismissed,
        arguments = decodeArguments(argumentsJson),
    )

private fun AppNotification.toEntity(): NotificationEntity =
    NotificationEntity(
        id = id,
        type = typeKey,
        occurredAtEpochSeconds = occurredAtEpochSeconds,
        isDismissed = isDismissed,
        argumentsJson = json.encodeToString(arguments),
    )

/**
 * A row whose arguments cannot be read is still worth showing — the type alone carries most of the
 * meaning — so a malformed payload degrades to "no arguments" instead of failing the whole list.
 */
private fun decodeArguments(argumentsJson: String): List<String> =
    try {
        json.decodeFromString<List<String>>(argumentsJson)
    } catch (_: SerializationException) {
        emptyList()
    }
