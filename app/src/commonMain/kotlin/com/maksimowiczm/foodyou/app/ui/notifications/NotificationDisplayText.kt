package com.maksimowiczm.foodyou.app.ui.notifications

import androidx.compose.runtime.Composable
import com.maksimowiczm.foodyou.notification.domain.AppNotification
import com.maksimowiczm.foodyou.notification.domain.NotificationType
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Renders a stored notification as user-facing text.
 *
 * Resolution happens here, at display time, rather than when the row is written: the text follows
 * the device's current language, and a type this build does not know — a row left behind by a newer
 * build — still shows something honest instead of disappearing.
 */
@Composable
internal fun AppNotification.displayText(): String {
    val type = NotificationType.fromKey(typeKey)

    if (type == null || arguments.size < type.argumentCount) {
        return stringResource(Res.string.description_notification_unsupported)
    }

    return when (type) {
        NotificationType.EntryAddedToYourDiary ->
            stringResource(
                Res.string.notification_entry_added_to_your_diary,
                arguments[0],
                arguments[1],
            )

        NotificationType.EntryFailedToAdd ->
            stringResource(Res.string.notification_entry_failed_to_add, arguments[0])

        NotificationType.UnknownMessageVersionRefused ->
            stringResource(Res.string.notification_unknown_message_version_refused, arguments[0])
    }
}
