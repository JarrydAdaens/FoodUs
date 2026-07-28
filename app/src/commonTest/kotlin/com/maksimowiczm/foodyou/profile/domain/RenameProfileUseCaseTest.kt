package com.maksimowiczm.foodyou.profile.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class RenameProfileUseCaseTest {

    @Test
    fun `rename changes username and last-edited but never the GUID`() = runBlocking {
        val repository = FakeProfileRepository()
        val dateProvider = FixedDateProvider(1_000L)
        val created = assertNotNull(CreateProfileUseCase(repository, dateProvider)("Jarryd"))

        dateProvider.epochSeconds = 5_000L
        RenameProfileUseCase(repository, dateProvider)("Renamed")

        val renamed = assertNotNull(repository.get())
        assertEquals(created.id, renamed.id)
        assertEquals(created.createdEpochSeconds, renamed.createdEpochSeconds)
        assertEquals("Renamed", renamed.username)
        assertEquals(5_000L, renamed.lastEditedEpochSeconds)
    }

    @Test
    fun `rename does nothing when no profile exists`() = runBlocking {
        val repository = FakeProfileRepository()

        RenameProfileUseCase(repository, FixedDateProvider(1_000L))("Renamed")

        assertNull(repository.get())
    }
}
