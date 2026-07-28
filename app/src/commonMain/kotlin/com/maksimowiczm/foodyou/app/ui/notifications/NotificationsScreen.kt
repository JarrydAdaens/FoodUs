package com.maksimowiczm.foodyou.app.ui.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.maksimowiczm.foodyou.app.ui.shell.PlaceholderTabScreen
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** Notifications tab. A stub for now: Story 13 builds the Notification Center on top of it. */
@Composable
internal fun NotificationsScreen(modifier: Modifier = Modifier) {
    PlaceholderTabScreen(title = stringResource(Res.string.tab_notifications), modifier = modifier)
}
