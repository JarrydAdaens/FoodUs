package com.maksimowiczm.foodyou.profile.domain

import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * The single entry point for creating this device's profile.
 *
 * Everything that must happen exactly once per identity belongs here: Story 3 attaches key-pair
 * generation to this call so the key pair is always born alongside the GUID, and no caller has to
 * change.
 */
class CreateProfileUseCase(
    private val repository: ProfileRepository,
    private val dateProvider: DateProvider,
) {
    /**
     * Creates the profile with a freshly generated GUID and returns it, or returns `null` when a
     * profile already exists — a device holds exactly one profile, so creation is not repeatable.
     *
     * @param username Cosmetic display name; must not be blank once trimmed.
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(username: String): Profile? {
        val trimmed = username.trim()
        require(trimmed.isNotEmpty()) { "Username must not be blank" }

        if (repository.get() != null) return null

        val now = dateProvider.nowInstant().epochSeconds
        val profile =
            Profile(
                id = Uuid.random().toString(),
                username = trimmed,
                createdEpochSeconds = now,
                lastEditedEpochSeconds = now,
            )

        repository.insert(profile)
        return profile
    }
}
