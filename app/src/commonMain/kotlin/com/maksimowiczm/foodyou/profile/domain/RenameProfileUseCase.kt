package com.maksimowiczm.foodyou.profile.domain

import com.maksimowiczm.foodyou.common.domain.date.DateProvider

/**
 * Renames the device's profile.
 *
 * The username is the only mutable field: this updates it and the last-edited stamp, leaving the
 * GUID and creation stamp untouched.
 */
class RenameProfileUseCase(
    private val repository: ProfileRepository,
    private val dateProvider: DateProvider,
) {
    /**
     * Renames the existing profile, or does nothing when none exists yet.
     *
     * @param username Cosmetic display name; must not be blank once trimmed.
     */
    suspend operator fun invoke(username: String) {
        val trimmed = username.trim()
        require(trimmed.isNotEmpty()) { "Username must not be blank" }

        val profile = repository.get() ?: return

        repository.updateUsername(
            id = profile.id,
            username = trimmed,
            lastEditedEpochSeconds = dateProvider.nowInstant().epochSeconds,
        )
    }
}
