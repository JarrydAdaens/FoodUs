package com.maksimowiczm.foodyou.profile.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class CreateProfileUseCaseTest {

    @Test
    fun `creates profile with a GUID and matching created and last-edited stamps`() = runBlocking {
        val repository = FakeProfileRepository()
        val createProfile = CreateProfileUseCase(repository, FixedDateProvider(1_000L))

        val profile = assertNotNull(createProfile("Jarryd"))

        assertEquals("Jarryd", profile.username)
        assertEquals(1_000L, profile.createdEpochSeconds)
        assertEquals(1_000L, profile.lastEditedEpochSeconds)
        assertEquals(36, profile.id.length, "id should be a canonical GUID string")
        assertEquals(profile, repository.get())
    }

    @Test
    fun `trims the username`() = runBlocking {
        val repository = FakeProfileRepository()
        val createProfile = CreateProfileUseCase(repository, FixedDateProvider(1_000L))

        assertEquals("Jarryd", assertNotNull(createProfile("  Jarryd  ")).username)
    }

    @Test
    fun `rejects a blank username`() = runBlocking {
        val repository = FakeProfileRepository()
        val createProfile = CreateProfileUseCase(repository, FixedDateProvider(1_000L))

        assertFailsWith<IllegalArgumentException> { createProfile("   ") }
        assertNull(repository.get())
    }

    @Test
    fun `a second create leaves the existing profile untouched`() = runBlocking {
        val repository = FakeProfileRepository()
        val createProfile = CreateProfileUseCase(repository, FixedDateProvider(1_000L))
        val first = assertNotNull(createProfile("Jarryd"))

        assertNull(createProfile("Someone else"))
        assertEquals(first, repository.get())
    }
}
