package com.maksimowiczm.foodyou.profile.domain

import kotlinx.coroutines.flow.Flow

/**
 * Storage for the device's single [Profile].
 *
 * There is deliberately no method that rewrites a profile's id: [updateUsername] is the only edit
 * path, which is what keeps the GUID immutable for the whole social feature set.
 */
interface ProfileRepository {
    fun observe(): Flow<Profile?>

    suspend fun get(): Profile?

    suspend fun insert(profile: Profile)

    suspend fun updateUsername(id: String, username: String, lastEditedEpochSeconds: Long)
}
