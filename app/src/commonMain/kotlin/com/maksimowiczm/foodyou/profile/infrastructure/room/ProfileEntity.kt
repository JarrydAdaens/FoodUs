package com.maksimowiczm.foodyou.profile.infrastructure.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The device's profile row (Milestone 3, Story 2). At most one row ever exists.
 *
 * It lives in the app's single Room database so it rides the existing database backup/restore by
 * construction. [id] is the randomly generated GUID and is never rewritten after insert.
 */
@Entity(tableName = "Profile")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val username: String,
    val createdEpochSeconds: Long,
    val lastEditedEpochSeconds: Long,
)
