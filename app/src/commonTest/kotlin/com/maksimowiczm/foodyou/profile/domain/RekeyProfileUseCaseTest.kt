package com.maksimowiczm.foodyou.profile.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking

class RekeyProfileUseCaseTest {

    @Test
    fun rotates_the_key_and_keeps_the_guid() = runBlocking {
        val repository = FakeProfileRepository()
        val crypto = FakeProfileMessagingCrypto()
        val dateProvider = FixedDateProvider(1_000L)
        val created =
            assertNotNull(CreateProfileUseCase(repository, crypto, dateProvider)("Jarryd"))

        RekeyProfileUseCase(crypto, ReconcileProfileKeyUseCase(repository, crypto, dateProvider))()

        val rekeyed = assertNotNull(repository.get())
        assertEquals(created.id, rekeyed.id)
        assertNotEquals(created.publicKey, rekeyed.publicKey)
        assertEquals(crypto.publicKey.encodeProfilePublicKey(), rekeyed.publicKey)
    }
}
