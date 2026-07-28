package com.maksimowiczm.foodyou.notification.infrastructure

import com.maksimowiczm.foodyou.common.domain.event.IntegrationEventHandler
import com.maksimowiczm.foodyou.notification.domain.NotificationEvent
import com.maksimowiczm.foodyou.notification.domain.RecordNotificationUseCase

/**
 * Persists every published [NotificationEvent]. Subscribing to exactly one event type is what keeps
 * this handler invisible to the rest of the `EventBus` traffic.
 */
internal class NotificationRecordingEventHandler(
    private val recordNotification: RecordNotificationUseCase
) : IntegrationEventHandler<NotificationEvent> {
    override suspend fun handle(event: NotificationEvent) {
        recordNotification(event)
    }
}
