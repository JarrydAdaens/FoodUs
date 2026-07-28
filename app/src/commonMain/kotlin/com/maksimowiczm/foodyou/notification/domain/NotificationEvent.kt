package com.maksimowiczm.foodyou.notification.domain

import com.maksimowiczm.foodyou.common.domain.event.IntegrationEvent

/**
 * The one and only way to raise a notification: publish this on the `EventBus` and the Notification
 * Center records it.
 *
 * There is a single event class rather than one per kind on purpose. The recording handler
 * subscribes to this type once, so an emitter story (Milestone 3, Stories 8-12) never has to touch
 * the notification slice, the handler registration, or the database — it adds a [NotificationType]
 * entry, a text template, and publishes.
 *
 * @param arguments Display values for the type's text template, in the order documented on the
 *   [NotificationType] entry. Callers resolve them before publishing, because the recording path has
 *   no access to Compose resources.
 */
data class NotificationEvent(val type: NotificationType, val arguments: List<String> = emptyList()) :
    IntegrationEvent
