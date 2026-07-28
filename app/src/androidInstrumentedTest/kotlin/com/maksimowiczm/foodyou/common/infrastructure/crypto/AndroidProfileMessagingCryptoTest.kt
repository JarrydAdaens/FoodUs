package com.maksimowiczm.foodyou.common.infrastructure.crypto

import java.security.KeyFactory
import java.security.KeyStore
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Exercises the real Android Keystore, mirroring the existing crypto instrumented tests. Keystore
 * behaviour cannot be faked in a unit test, and this is the security foundation of the multiplayer
 * milestone, so it is verified against the device.
 */
class AndroidProfileMessagingCryptoTest {

    @Test
    fun decrypt_round_trip() = runBlocking {
        val crypto = AndroidProfileMessagingCrypto()

        val plaintext = "Hello, household!".toByteArray()
        val sealed = seal(plaintext, crypto.publicKey)

        assertContentEquals(plaintext, crypto.decrypt(sealed))
    }

    @Test
    fun public_key_is_stable_across_instances() = runBlocking {
        val first = AndroidProfileMessagingCrypto().publicKey
        val second = AndroidProfileMessagingCrypto().publicKey

        assertContentEquals(first, second, "the alias must survive; a new instance is not a re-key")
    }

    @Test
    fun regenerate_replaces_the_pair_and_orphans_old_ciphertext() = runBlocking {
        val crypto = AndroidProfileMessagingCrypto()
        val oldKey = crypto.publicKey
        val staleCiphertext = seal("in flight".toByteArray(), oldKey)

        val newKey = crypto.regenerate()

        assertFalse(oldKey.contentEquals(newKey), "regenerate must yield a different key")
        assertContentEquals(newKey, crypto.publicKey, "the cached key must follow the vault")
        assertTrue(
            runCatching { crypto.decrypt(staleCiphertext) }.isFailure,
            "blobs sealed for the old key must become unopenable",
        )

        // The new pair must be immediately usable, not just different.
        assertContentEquals(
            "fresh".toByteArray(),
            crypto.decrypt(seal("fresh".toByteArray(), newKey)),
        )
    }

    @Test
    fun private_key_is_not_exportable() {
        // Touching publicKey is what mints the alias, so the entry is guaranteed to exist.
        AndroidProfileMessagingCrypto().publicKey

        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = store.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry

        assertTrue(entry.privateKey.encoded == null, "the private key must never leave the vault")
    }

    /**
     * Seals exactly the way a sender on another device would, using only the exported public key.
     */
    private fun seal(plaintext: ByteArray, publicKey: ByteArray): ByteArray {
        val key = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(publicKey))

        return Cipher.getInstance(AndroidProfileMessagingCrypto.TRANSFORMATION)
            .apply {
                init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    AndroidProfileMessagingCrypto.oaepParameterSpec(),
                )
            }
            .doFinal(plaintext)
    }

    private companion object {
        const val KEY_ALIAS = "FOODUS_PROFILE_MESSAGING_KEY"
    }
}
