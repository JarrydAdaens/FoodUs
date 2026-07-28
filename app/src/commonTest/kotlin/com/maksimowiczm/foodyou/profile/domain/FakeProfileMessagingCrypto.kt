package com.maksimowiczm.foodyou.profile.domain

import com.maksimowiczm.foodyou.common.crypto.ProfileMessagingCrypto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * In-memory stand-in for the key vault. Real Keystore behaviour is covered by the instrumented
 * tests; this only models the part the domain depends on — that the vault owns the current key and
 * that regenerating replaces it.
 */
internal class FakeProfileMessagingCrypto(initialKey: ByteArray = byteArrayOf(1, 2, 3)) :
    ProfileMessagingCrypto {
    private var current = initialKey
    private var generation = 0

    override val isSupported: Flow<Boolean> = flowOf(true)
    override val algorithm: String = "FAKE/OAEP"
    override val publicKey: ByteArray
        get() = current

    override suspend fun decrypt(sealed: ByteArray): ByteArray = sealed

    override suspend fun regenerate(): ByteArray {
        generation++
        current = byteArrayOf(generation.toByte())
        return current
    }
}
