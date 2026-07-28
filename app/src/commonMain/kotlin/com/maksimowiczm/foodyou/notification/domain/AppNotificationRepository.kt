package com.maksimowiczm.foodyou.notification.domain

import kotlinx.coroutines.flow.Flow

/**
 * Persistent store behind the Notification Center.
 *
 * Feature code never calls this directly — it publishes a [NotificationEvent] and the recording
 * handler writes the row. The interface exists for the tab UI and for the recording path itself.
 */
interface AppNotificationRepository {

    /**
     * Notifications newest first. [includeDismissed] `false` is the tab's default view; `true`
     * backs the history control.
     */
    fun observe(includeDismissed: Boolean): Flow<List<AppNotification>>

    suspend fun insert(notification: AppNotification)

    suspend fun dismiss(id: Long)

    suspend fun dismissAll()

    /** Retention sweep: drops dismissed rows older than [cutoffEpochSeconds]. Never touches
     * undismissed rows, so nothing the user has not seen can expire. */
    suspend fun deleteDismissedOlderThan(cutoffEpochSeconds: Long)
}
