package com.maksimowiczm.foodyou.common.infrastructure.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import com.maksimowiczm.foodyou.common.crypto.ProfileMessagingCrypto
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Android Keystore implementation of [ProfileMessagingCrypto].
 *
 * Uses its own alias, separate from upstream's `FOODYOU_IDENTITY_KEY` and `FOOD_YOU_MASTER_KEY`, so
 * the fork never repurposes an upstream key.
 *
 * RSA-OAEP is a provisional choice (see [ProfileMessagingCrypto]): it decrypts from API 28 with a
 * single code path, and it wraps a symmetric key cleanly once the envelope format exists. EC key
 * agreement would be more modern but needs API 31.
 */
class AndroidProfileMessagingCrypto : ProfileMessagingCrypto {
    private val keyStore
        get() = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private val mutex = Mutex()

    /**
     * Cached so the common case avoids a Keystore round trip. Cleared by [regenerate] rather than
     * being a `lazy`, because the alias' contents genuinely change.
     */
    @Volatile private var cachedPublicKey: ByteArray? = null

    override val isSupported: Flow<Boolean> = flow { emit(isSupported()) }
    override val algorithm: String = TRANSFORMATION
    override val publicKey: ByteArray
        get() = runBlocking { mutex.withLock { publicKeyLocked() } }.copyOf()

    private suspend fun isSupported(): Boolean {
        val key = mutex.withLock { initializeOrGetKeyLocked() }
        val factory = KeyFactory.getInstance(key.privateKey.algorithm, ANDROID_KEYSTORE)
        val keyInfo = factory.getKeySpec(key.privateKey, KeyInfo::class.java)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val securityLevel = keyInfo.securityLevel
            securityLevel == KeyProperties.SECURITY_LEVEL_STRONGBOX ||
                securityLevel == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT
        } else
            @Suppress("DEPRECATION")
            {
                keyInfo.isInsideSecureHardware
            }
    }

    override suspend fun decrypt(sealed: ByteArray): ByteArray {
        val key = mutex.withLock { initializeOrGetKeyLocked() }

        return withContext(Dispatchers.Default) {
            Cipher.getInstance(TRANSFORMATION)
                .apply { init(Cipher.DECRYPT_MODE, key.privateKey, oaepParameterSpec()) }
                .doFinal(sealed)
        }
    }

    override suspend fun regenerate(): ByteArray =
        mutex
            .withLock {
                cachedPublicKey = null
                keyStore.deleteEntry(KEY_ALIAS)
                publicKeyLocked()
            }
            .copyOf()

    private fun publicKeyLocked(): ByteArray =
        cachedPublicKey
            ?: run {
                val certificate = initializeOrGetKeyLocked().certificate
                if (certificate.publicKey.format != "X.509") {
                    error("Public key format is not X.509")
                }

                certificate.publicKey.encoded.also { cachedPublicKey = it }
            }

    /** Must be called under [mutex]; creating an alias twice concurrently would race. */
    private fun initializeOrGetKeyLocked(): KeyStore.PrivateKeyEntry {
        val store = keyStore
        val existingKey = store.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry
        if (existingKey != null) {
            return existingKey
        }

        val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)

        val spec =
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_DECRYPT)
                // SHA-1 is authorized alongside SHA-256 only because OAEP's MGF1 runs on SHA-1
                // here (see oaepParameterSpec); the OAEP digest itself stays SHA-256.
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .setKeySize(KEY_SIZE_BITS)
                // Decryption happens unattended when the app drains the relay on wake, so it must
                // not wait on a user unlock prompt.
                .setUserAuthenticationRequired(false)
                .build()

        kpg.initialize(spec)
        kpg.genKeyPair()

        return store.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "FOODUS_PROFILE_MESSAGING_KEY"
        private const val KEY_SIZE_BITS = 3072
        const val TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"

        /**
         * The Android Keystore ignores the MGF1 digest carried by the transformation string and
         * always uses SHA-1 for the mask function, so both sides must state that explicitly or the
         * round trip fails. Senders sealing for this key must use the same parameters.
         */
        fun oaepParameterSpec(): OAEPParameterSpec =
            OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)
    }
}
