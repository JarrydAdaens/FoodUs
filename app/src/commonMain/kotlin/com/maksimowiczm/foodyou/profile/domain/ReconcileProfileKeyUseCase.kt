package com.maksimowiczm.foodyou.profile.domain

import com.maksimowiczm.foodyou.common.crypto.ProfileMessagingCrypto
import com.maksimowiczm.foodyou.common.domain.date.DateProvider

/**
 * Makes the profile record agree with the key vault (Milestone 3, Story 3).
 *
 * The vault is the source of truth and the record is a publishable copy of its public half. A
 * single fixed alias cannot hold the old and new key at once, so a re-key is not atomic across the
 * two stores: process death between regenerating and writing the record leaves a stale public key
 * that nobody could send to. The same mismatch appears when a database backup is restored onto a
 * device whose vault holds a different key, or never held one — exactly the situation the
 * keys-live-and-die-with-the-device policy creates.
 *
 * Rather than pretending the write is atomic, this repairs the mismatch: read the vault (which
 * mints a pair if the alias is gone), compare, and rewrite the record when they disagree. It is
 * idempotent and cheap, so it is safe to run whenever the profile is next observed. The invariant
 * it restores is that a usable private key always matches the stored public key.
 */
class ReconcileProfileKeyUseCase(
    private val repository: ProfileRepository,
    private val messagingCrypto: ProfileMessagingCrypto,
    private val dateProvider: DateProvider,
) {
    /** No-op when no profile exists yet — creation records the key itself. */
    suspend operator fun invoke() {
        val profile = repository.get() ?: return

        val vaultKey = messagingCrypto.publicKey.encodeProfilePublicKey()
        val vaultAlgorithm = messagingCrypto.algorithm
        if (profile.publicKey == vaultKey && profile.keyAlgorithm == vaultAlgorithm) return

        repository.updatePublicKey(
            id = profile.id,
            publicKey = vaultKey,
            keyAlgorithm = vaultAlgorithm,
            lastEditedEpochSeconds = dateProvider.nowInstant().epochSeconds,
        )
    }
}
