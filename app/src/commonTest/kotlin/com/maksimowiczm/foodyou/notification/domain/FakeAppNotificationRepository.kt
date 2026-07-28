package com.maksimowiczm.foodyou.notification.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory stand-in for the Room store. Mirrors the DAO's ordering and its two filtering rules so
 * tests exercise the same observable behavior the tab relies on.
 */
internal class FakeAppNotificationRepository : AppNotificationRepository {
    private val rows = MutableStateFlow<List<AppNotification>>(emptyList())
    private var nextId = 1L

    val stored: List<AppNotification>
        get() = rows.value

    override fun observe(includeDismissed: Boolean): Flow<List<AppNotification>> =
        rows.map { all ->
            all.filter { includeDismissed || !it.isDismissed }
                .sortedWith(compareByDescending<AppNotification> { it.occurredAtEpochSeconds }
                    .thenByDescending { it.id })
        }

    override suspend fun insert(notification: AppNotification) {
        rows.value = rows.value + notification.copy(id = nextId++)
    }

    override suspend fun dismiss(id: Long) {
        rows.value = rows.value.map { if (it.id == id) it.copy(isDismissed = true) else it }
    }

    override suspend fun dismissAll() {
        rows.value = rows.value.map { it.copy(isDismissed = true) }
    }

    override suspend fun deleteDismissedOlderThan(cutoffEpochSeconds: Long) {
        rows.value =
            rows.value.filterNot {
                it.isDismissed && it.occurredAtEpochSeconds < cutoffEpochSeconds
            }
    }
}
