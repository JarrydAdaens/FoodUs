package com.maksimowiczm.foodyou.notification.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotificationTypeTest {

    @Test
    fun every_type_round_trips_through_its_persisted_key() {
        NotificationType.entries.forEach { type ->
            assertEquals(type, NotificationType.fromKey(type.key))
        }
    }

    @Test
    fun keys_are_unique_so_no_type_can_shadow_another_on_disk() {
        assertEquals(
            NotificationType.entries.size,
            NotificationType.entries.map { it.key }.toSet().size,
        )
    }

    @Test
    fun a_key_written_by_a_newer_build_resolves_to_null_instead_of_throwing() {
        assertNull(NotificationType.fromKey("some_future_notification_kind"))
    }
}
