package com.maksimowiczm.foodyou.profile.infrastructure.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The device's profile row (Milestone 3, Story 2). At most one row ever exists.
 *
 * It lives in the app's single Room database so it rides the existing database backup/restore by
 * construction. [id] is the randomly generated GUID and is never rewritten after insert.
 *
 * [publicKey] and [keyAlgorithm] (Milestone 3, Story 3) hold the *public* half of the messaging key
 * pair and nothing else — the private half lives only in the platform key vault and is forbidden
 * from this table by `context/laws.md` §2. They are nullable because profiles created before Story
 * 3 predate the key pair; the reconcile path fills them in.
 */
@Entity(tableName = "Profile")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val username: String,
    val createdEpochSeconds: Long,
    val lastEditedEpochSeconds: Long,
    val publicKey: String? = null,
    val keyAlgorithm: String? = null,
)
