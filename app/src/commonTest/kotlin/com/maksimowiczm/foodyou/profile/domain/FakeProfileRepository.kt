package com.maksimowiczm.foodyou.profile.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory stand-in for the Room repository, holding the single-profile row. */
internal class FakeProfileRepository : ProfileRepository {
    private val state = MutableStateFlow<Profile?>(null)

    override fun observe(): Flow<Profile?> = state

    override suspend fun get(): Profile? = state.value

    override suspend fun insert(profile: Profile) {
        state.value = profile
    }

    override suspend fun updateUsername(
        id: String,
        username: String,
        lastEditedEpochSeconds: Long,
    ) {
        val current = state.value ?: return
        if (current.id != id) return
        state.value =
            current.copy(username = username, lastEditedEpochSeconds = lastEditedEpochSeconds)
    }

    override suspend fun updatePublicKey(
        id: String,
        publicKey: String,
        keyAlgorithm: String,
        lastEditedEpochSeconds: Long,
    ) {
        val current = state.value ?: return
        if (current.id != id) return
        state.value =
            current.copy(
                publicKey = publicKey,
                keyAlgorithm = keyAlgorithm,
                lastEditedEpochSeconds = lastEditedEpochSeconds,
            )
    }
}
