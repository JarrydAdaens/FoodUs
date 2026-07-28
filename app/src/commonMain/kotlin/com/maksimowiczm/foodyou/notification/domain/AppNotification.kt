package com.maksimowiczm.foodyou.notification.domain

/**
 * One recorded entry in the Notification Center.
 *
 * [typeKey] is stored rather than a [NotificationType] so that a row written by a newer build
 * survives a downgrade and still renders; use [NotificationType.fromKey] to resolve it.
 *
 * [arguments] are the already-resolved display values for the type's text template (a username, a
 * food name, ...). Keeping them as plain strings is what lets new notification kinds land without a
 * schema change; the meaning of each position is documented on the [NotificationType] entry.
 */
data class AppNotification(
    val id: Long,
    val typeKey: String,
    val occurredAtEpochSeconds: Long,
    val isDismissed: Boolean,
    val arguments: List<String>,
) {
    companion object {
        /** [id] of a notification that has not been persisted yet; storage assigns the real one. */
        const val UNASSIGNED_ID = 0L
    }
}
