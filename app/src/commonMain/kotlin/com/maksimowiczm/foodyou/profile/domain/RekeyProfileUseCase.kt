package com.maksimowiczm.foodyou.profile.domain

import com.maksimowiczm.foodyou.common.crypto.ProfileMessagingCrypto

/**
 * Replaces this device's messaging key pair, keeping the GUID (Milestone 3, Story 3).
 *
 * The recovery path after device loss: restore the database backup, keeping the GUID and social
 * graph, then re-key, because the private key deliberately did not come with the backup. Messages
 * already in flight for the old key are lost, which the design accepts for disposable diet packets.
 *
 * Ordering is vault first, record second, and the record write is delegated to
 * [ReconcileProfileKeyUseCase] so a crash in between is repaired by the same code that repairs a
 * restored backup, rather than by a second, subtly different path.
 *
 * Announcing the new key to friends is Story 8/14 scope; this only rotates the local identity.
 */
class RekeyProfileUseCase(
    private val messagingCrypto: ProfileMessagingCrypto,
    private val reconcileProfileKey: ReconcileProfileKeyUseCase,
) {
    suspend operator fun invoke() {
        messagingCrypto.regenerate()
        reconcileProfileKey()
    }
}
