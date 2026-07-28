package com.maksimowiczm.foodyou.profile.domain

/**
 * The device's single local profile (Milestone 3, Story 2).
 *
 * [id] is a randomly generated GUID stamped once at creation and never changed or shown in the UI —
 * it is the primary key every social feature (friends, groups, relay traffic) references. [username]
 * is purely cosmetic: free text, collisions allowed, renameable, and explicitly *not* an identifier.
 */
data class Profile(
    val id: String,
    val username: String,
    val createdEpochSeconds: Long,
    val lastEditedEpochSeconds: Long,
)
