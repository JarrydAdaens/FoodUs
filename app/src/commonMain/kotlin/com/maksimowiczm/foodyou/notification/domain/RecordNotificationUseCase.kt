package com.maksimowiczm.foodyou.notification.domain

import com.maksimowiczm.foodyou.common.domain.date.DateProvider

/**
 * Turns a [NotificationEvent] into a stored notification and keeps the history from growing
 * forever.
 *
 * Retention runs here rather than on a schedule: this app has no background worker, and an insert
 * is the only moment the history can grow, so it is also the cheapest moment to trim it.
 */
class RecordNotificationUseCase(
    private val repository: AppNotificationRepository,
    private val dateProvider: DateProvider,
) {
    suspend operator fun invoke(event: NotificationEvent) {
        val now = dateProvider.nowInstant().epochSeconds

        repository.insert(
            AppNotification(
                id = AppNotification.UNASSIGNED_ID,
                typeKey = event.type.key,
                occurredAtEpochSeconds = now,
                isDismissed = false,
                arguments = event.arguments,
            )
        )

        repository.deleteDismissedOlderThan(now - DISMISSED_RETENTION_SECONDS)
    }

    private companion object {
        /** Dismissed notifications are kept for 90 days; undismissed ones are kept indefinitely. */
        const val DISMISSED_RETENTION_SECONDS = 90L * 24 * 60 * 60
    }
}
