package com.maksimowiczm.foodyou.profile.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class RenameProfileUseCaseTest {

    @Test
    fun rename_changes_username_and_last_edited_but_never_guid() = runBlocking {
        val repository = FakeProfileRepository()
        val dateProvider = FixedDateProvider(1_000L)
        val created =
            assertNotNull(
                CreateProfileUseCase(repository, FakeProfileMessagingCrypto(), dateProvider)(
                    "Jarryd"
                )
            )

        dateProvider.epochSeconds = 5_000L
        RenameProfileUseCase(repository, dateProvider)("Renamed")

        val renamed = assertNotNull(repository.get())
        assertEquals(created.id, renamed.id)
        assertEquals(created.createdEpochSeconds, renamed.createdEpochSeconds)
        assertEquals("Renamed", renamed.username)
        assertEquals(5_000L, renamed.lastEditedEpochSeconds)
    }

    @Test
    fun rename_does_nothing_when_no_profile_exists() = runBlocking {
        val repository = FakeProfileRepository()

        RenameProfileUseCase(repository, FixedDateProvider(1_000L))("Renamed")

        assertNull(repository.get())
    }
}
