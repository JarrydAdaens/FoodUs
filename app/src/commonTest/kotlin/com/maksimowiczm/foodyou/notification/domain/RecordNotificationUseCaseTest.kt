package com.maksimowiczm.foodyou.notification.domain

import com.maksimowiczm.foodyou.profile.domain.FixedDateProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class RecordNotificationUseCaseTest {

    private val ninetyDays = 90L * 24 * 60 * 60

    @Test
    fun records_published_event_as_an_undismissed_notification() = runBlocking {
        val repository = FakeAppNotificationRepository()
        val record = RecordNotificationUseCase(repository, FixedDateProvider(1_000L))

        record(
            NotificationEvent(
                type = NotificationType.EntryAddedToYourDiary,
                arguments = listOf("Jarryd", "Porridge"),
            )
        )

        val stored = repository.observe(includeDismissed = false).first().single()
        assertEquals(NotificationType.EntryAddedToYourDiary.key, stored.typeKey)
        assertEquals(listOf("Jarryd", "Porridge"), stored.arguments)
        assertEquals(1_000L, stored.occurredAtEpochSeconds)
        assertFalse(stored.isDismissed)
    }

    @Test
    fun dismissed_notification_leaves_the_default_view_but_stays_in_history() = runBlocking {
        val repository = FakeAppNotificationRepository()
        val record = RecordNotificationUseCase(repository, FixedDateProvider(1_000L))
        record(NotificationEvent(NotificationType.EntryFailedToAdd, listOf("Porridge")))

        repository.dismiss(repository.stored.single().id)

        assertTrue(repository.observe(includeDismissed = false).first().isEmpty())
        assertEquals(1, repository.observe(includeDismissed = true).first().size)
    }

    @Test
    fun recording_prunes_only_dismissed_notifications_past_the_retention_window() = runBlocking {
        val repository = FakeAppNotificationRepository()
        val clock = FixedDateProvider(0L)
        val record = RecordNotificationUseCase(repository, clock)

        record(NotificationEvent(NotificationType.EntryFailedToAdd, listOf("Old dismissed")))
        record(NotificationEvent(NotificationType.EntryFailedToAdd, listOf("Old kept")))
        repository.dismiss(repository.stored.first().id)

        clock.epochSeconds = ninetyDays + 1
        record(NotificationEvent(NotificationType.EntryFailedToAdd, listOf("New")))

        val remaining = repository.observe(includeDismissed = true).first().map { it.arguments[0] }
        assertEquals(listOf("New", "Old kept"), remaining)
    }
}
