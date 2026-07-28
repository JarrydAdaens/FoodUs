package com.maksimowiczm.foodyou.profile.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class CreateProfileUseCaseTest {

    @Test
    fun creates_profile_with_guid_and_matching_stamps() = runBlocking {
        val repository = FakeProfileRepository()
        val createProfile =
            CreateProfileUseCase(
                repository,
                FakeProfileMessagingCrypto(),
                FixedDateProvider(1_000L),
            )

        val profile = assertNotNull(createProfile("Jarryd"))

        assertEquals("Jarryd", profile.username)
        assertEquals(1_000L, profile.createdEpochSeconds)
        assertEquals(1_000L, profile.lastEditedEpochSeconds)
        assertEquals(36, profile.id.length, "id should be a canonical GUID string")
        assertEquals(profile, repository.get())
    }

    @Test
    fun records_messaging_public_key_alongside_guid() = runBlocking {
        val repository = FakeProfileRepository()
        val crypto = FakeProfileMessagingCrypto()
        val createProfile = CreateProfileUseCase(repository, crypto, FixedDateProvider(1_000L))

        val profile = assertNotNull(createProfile("Jarryd"))

        assertEquals(crypto.publicKey.encodeProfilePublicKey(), profile.publicKey)
        assertEquals(crypto.algorithm, profile.keyAlgorithm)
    }

    @Test
    fun trims_the_username() = runBlocking {
        val repository = FakeProfileRepository()
        val createProfile =
            CreateProfileUseCase(
                repository,
                FakeProfileMessagingCrypto(),
                FixedDateProvider(1_000L),
            )

        assertEquals("Jarryd", assertNotNull(createProfile("  Jarryd  ")).username)
    }

    @Test
    fun rejects_a_blank_username() = runBlocking {
        val repository = FakeProfileRepository()
        val createProfile =
            CreateProfileUseCase(
                repository,
                FakeProfileMessagingCrypto(),
                FixedDateProvider(1_000L),
            )

        assertFailsWith<IllegalArgumentException> { createProfile("   ") }
        assertNull(repository.get())
    }

    @Test
    fun second_create_leaves_existing_profile_untouched() = runBlocking {
        val repository = FakeProfileRepository()
        val createProfile =
            CreateProfileUseCase(
                repository,
                FakeProfileMessagingCrypto(),
                FixedDateProvider(1_000L),
            )
        val first = assertNotNull(createProfile("Jarryd"))

        assertNull(createProfile("Someone else"))
        assertEquals(first, repository.get())
    }
}
