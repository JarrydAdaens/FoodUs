package com.maksimowiczm.foodyou.common.crypto

import kotlinx.coroutines.flow.Flow

/**
 * The device's multiplayer messaging identity (Milestone 3, Story 3).
 *
 * A fork-owned sibling of [IdentityCrypto], deliberately kept separate: [IdentityCrypto] is an
 * upstream sign-only key with its own alias and its own consumers, and a key should serve exactly
 * one purpose. This one only ever opens envelopes addressed to this device.
 *
 * Constitutional constraint (`context/laws.md` §2, Data Boundaries): the private key exists only
 * inside the platform key vault. It must never reach the Room database, DataStore, a file, a
 * backup, an analytics payload, or a log line. Only [publicKey] and [algorithm] are allowed to
 * cross this interface as data — which is exactly why the database backup stays portable while
 * the key deliberately does not.
 *
 * The scheme behind [algorithm] is **provisional and replaceable**. The interoperable choice is
 * owned by the foodus-relay wire contract (v1, not yet written); until the envelope pipeline ships
 * there is no interop surface, so swapping the implementation costs a [regenerate] call and a
 * record update. Callers depend on this interface, never on the scheme.
 */
interface ProfileMessagingCrypto {
    /** True when the private key is held in hardware-backed storage rather than software. */
    val isSupported: Flow<Boolean>

    /** JCA transformation the sender must use to seal a blob for [publicKey]. */
    val algorithm: String

    /** Public key in X.509 format (DER encoded). Safe to store, back up, and publish. */
    val publicKey: ByteArray

    /**
     * Opens a blob that was sealed with [publicKey]. The vault performs the operation; the private
     * key never enters app memory.
     *
     * @param sealed Ciphertext produced with [publicKey] and [algorithm].
     * @return The plaintext.
     */
    suspend fun decrypt(sealed: ByteArray): ByteArray

    /**
     * Replaces the key pair, abandoning the old one — in-flight blobs sealed for the previous key
     * become permanently unopenable, which is the accepted cost of the keys-live-and-die-with-the-
     * device policy.
     *
     * The vault is the source of truth: this call succeeds or fails on its own, and the profile
     * record is reconciled to whatever key the vault holds afterwards.
     *
     * @return The new [publicKey].
     */
    suspend fun regenerate(): ByteArray
}
