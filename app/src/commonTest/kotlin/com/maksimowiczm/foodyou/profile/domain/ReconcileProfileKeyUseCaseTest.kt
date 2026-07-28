package com.maksimowiczm.foodyou.profile.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class ReconcileProfileKeyUseCaseTest {

    @Test
    fun repairs_record_whose_key_drifted_from_vault() = runBlocking {
        val repository = FakeProfileRepository()
        val crypto = FakeProfileMessagingCrypto()
        val dateProvider = FixedDateProvider(1_000L)
        val created =
            assertNotNull(CreateProfileUseCase(repository, crypto, dateProvider)("Jarryd"))

        // Stands in for a crash between regenerating and writing the record.
        val vaultKey = crypto.regenerate().encodeProfilePublicKey()
        ReconcileProfileKeyUseCase(repository, crypto, dateProvider)()

        val reconciled = assertNotNull(repository.get())
        assertEquals(vaultKey, reconciled.publicKey)
        assertEquals(created.id, reconciled.id, "re-keying must not disturb the GUID")
    }

    @Test
    fun fills_in_profile_that_predates_the_key_pair() = runBlocking {
        val repository = FakeProfileRepository()
        val crypto = FakeProfileMessagingCrypto()
        repository.insert(
            Profile(
                id = "guid",
                username = "Jarryd",
                createdEpochSeconds = 1L,
                lastEditedEpochSeconds = 1L,
            )
        )

        ReconcileProfileKeyUseCase(repository, crypto, FixedDateProvider(2_000L))()

        val reconciled = assertNotNull(repository.get())
        assertEquals(crypto.publicKey.encodeProfilePublicKey(), reconciled.publicKey)
        assertEquals(crypto.algorithm, reconciled.keyAlgorithm)
    }

    @Test
    fun leaves_a_matching_record_untouched() = runBlocking {
        val repository = FakeProfileRepository()
        val crypto = FakeProfileMessagingCrypto()
        val createProfile = CreateProfileUseCase(repository, crypto, FixedDateProvider(1_000L))
        val created = assertNotNull(createProfile("Jarryd"))

        ReconcileProfileKeyUseCase(repository, crypto, FixedDateProvider(9_000L))()

        assertEquals(created, repository.get(), "no edit means no last-edited bump")
    }

    @Test
    fun does_nothing_when_no_profile_exists() = runBlocking {
        val repository = FakeProfileRepository()

        ReconcileProfileKeyUseCase(
            repository,
            FakeProfileMessagingCrypto(),
            FixedDateProvider(1_000L),
        )()

        assertNull(repository.get())
    }
}
