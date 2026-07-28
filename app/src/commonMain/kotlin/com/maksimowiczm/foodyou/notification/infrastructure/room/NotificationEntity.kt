package com.maksimowiczm.foodyou.notification.infrastructure.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A Notification Center row (Milestone 3, Story 13).
 *
 * It lives in the app's single Room database so the history rides the existing backup/restore by
 * construction. Nothing here is secret: notification text is what the user already sees on screen.
 *
 * The table intentionally has no per-kind columns. [type] holds the
 * `NotificationType.key` string and [argumentsJson] the JSON array of display arguments, so new
 * notification kinds are additive in code only and never require a migration.
 */
@Entity(
    tableName = "Notification",
    indices = [Index("occurredAtEpochSeconds"), Index("isDismissed")],
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val occurredAtEpochSeconds: Long,
    val isDismissed: Boolean,
    val argumentsJson: String,
)
