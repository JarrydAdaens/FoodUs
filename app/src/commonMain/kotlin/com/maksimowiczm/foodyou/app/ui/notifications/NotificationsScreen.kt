package com.maksimowiczm.foodyou.app.ui.notifications

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.notification.domain.AppNotification
import foodyou.app.generated.resources.*
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Notification Center (Milestone 3, Story 13): the app's only broadcast surface.
 *
 * Undismissed notifications show by default; the history action reveals dismissed ones. Rows are
 * deliberately inert — a notification reports that something happened, it is not a shortcut into
 * another screen, so the tab never steals navigation from the user.
 */
@Composable
internal fun NotificationsScreen(modifier: Modifier = Modifier) {
    val viewModel: NotificationsViewModel = koinViewModel()
    val notifications = viewModel.notifications.collectAsStateWithLifecycle().value
    val showHistory = viewModel.showHistory.collectAsStateWithLifecycle().value

    Scaffold(
        modifier = (modifier).uiName("NotificationsScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "NotificationsScreen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.tab_notifications), modifier = Modifier.uiName("NotificationsScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "NotificationsScreen")) },
                actions = {
                    if (!showHistory && notifications.isNotEmpty()) {
                        val dismissAll = stringResource(Res.string.action_dismiss_all_notifications)

                        IconButton(onClick = viewModel::onDismissAll, modifier = Modifier.uiName("NotificationsScreen.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "NotificationsScreen")) {
                            Icon(
                                imageVector = Icons.Filled.DoneAll,
                                contentDescription = dismissAll, modifier = Modifier.uiName("NotificationsScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "NotificationsScreen")
                            )
                        }
                    }

                    val history =
                        stringResource(
                            if (showHistory) Res.string.action_hide_notification_history
                            else Res.string.action_show_notification_history
                        )

                    IconButton(onClick = viewModel::onToggleHistory, modifier = Modifier.uiName("NotificationsScreen.IconButton.2", "androidx.compose.material3.IconButton", uiInspectionSource, "NotificationsScreen")) {
                        Icon(imageVector = Icons.Filled.History, contentDescription = history, modifier = Modifier.uiName("NotificationsScreen.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "NotificationsScreen"))
                    }
                }, modifier = Modifier.uiName("NotificationsScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "NotificationsScreen")
            )
        },
    ) { paddingValues ->
        Box(modifier = (Modifier.fillMaxSize().padding(paddingValues)).uiName("NotificationsScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "NotificationsScreen")) {
            if (notifications.isEmpty()) {
                EmptyNotifications(showHistory = showHistory)
            } else {
                LazyColumn(modifier = (Modifier.fillMaxSize()).uiName("NotificationsScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "NotificationsScreen")) {
                    items(items = notifications, key = { it.id }) { notification ->
                        NotificationListItem(
                            notification = notification,
                            onDismiss = { viewModel.onDismiss(notification.id) },
                        )
                        HorizontalDivider( modifier = Modifier.uiName("NotificationsScreen.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "NotificationsScreen"))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotifications(showHistory: Boolean) {
    Box(modifier = (Modifier.fillMaxSize().padding(32.dp)).uiName("EmptyNotifications.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "EmptyNotifications"), contentAlignment = Alignment.Center) {
        Text(
            text =
                stringResource(
                    if (showHistory) Res.string.description_notification_history_empty
                    else Res.string.description_notifications_empty
                ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center, modifier = Modifier.uiName("EmptyNotifications.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "EmptyNotifications")
        )
    }
}

@Composable
private fun NotificationListItem(notification: AppNotification, onDismiss: () -> Unit) {
    val dateTime =
        Instant.fromEpochSeconds(notification.occurredAtEpochSeconds)
            .toLocalDateTime(TimeZone.currentSystemDefault())

    ListItem(
        headlineContent = { Text(text = notification.displayText(), modifier = Modifier.uiName("NotificationListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "NotificationListItem")) },
        supportingContent = { Text(text = LocalDateFormatter.current.formatDateTime(dateTime), modifier = Modifier.uiName("NotificationListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "NotificationListItem")) },
        trailingContent = {
            // Dismissed rows are only ever visible in the history view, where there is nothing left
            // to dismiss.
            if (!notification.isDismissed) {
                val dismiss = stringResource(Res.string.action_dismiss_notification)

                IconButton(onClick = onDismiss, modifier = Modifier.uiName("NotificationListItem.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "NotificationListItem")) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = dismiss, modifier = Modifier.uiName("NotificationListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "NotificationListItem"))
                }
            }
        }, modifier = Modifier.uiName("NotificationListItem.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "NotificationListItem")
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.notifications",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/notifications/NotificationsScreen.kt",
)
