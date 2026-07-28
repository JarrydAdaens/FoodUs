package com.maksimowiczm.foodyou.app.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.maksimowiczm.foodyou.app.navigation.FoodYouAppNavHost
import com.maksimowiczm.foodyou.app.ui.groups.GroupsScreen
import com.maksimowiczm.foodyou.app.ui.notifications.NotificationsScreen
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Fork-owned root shell: a bottom navigation bar over three tabs. The [ShellTab.Log] tab hosts the
 * unmodified upstream navigation graph, so switching tabs must not disturb its back stack — hence
 * the [rememberSaveableStateHolder] wrapping each tab's content.
 */
@Composable
fun FoodUsAppShell(onDatabaseBackup: () -> Unit, modifier: Modifier = Modifier) {
    var selectedTabOrdinal by rememberSaveable { mutableIntStateOf(ShellTab.Log.ordinal) }
    val selectedTab = ShellTab.entries[selectedTabOrdinal]
    val stateHolder = rememberSaveableStateHolder()

    // Android bottom-navigation convention: back from a secondary tab returns to the primary tab
    // instead of leaving the app. On Log, back belongs to the upstream navigation graph.
    if (selectedTab != ShellTab.Log) {
        NavigationEventHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = { selectedTabOrdinal = ShellTab.Log.ordinal },
        )
    }

    Scaffold(
        modifier = modifier,
        // The tab content owns its own Scaffolds and window insets; the shell only reserves room
        // for the bottom bar and lets the inner Scaffolds handle the rest.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar {
                ShellTab.entries.forEach { tab ->
                    val label = tab.label()

                    NavigationBarItem(
                        selected = tab == selectedTab,
                        onClick = { selectedTabOrdinal = tab.ordinal },
                        icon = { Icon(imageVector = tab.icon, contentDescription = label) },
                        label = { Text(text = label) },
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .consumeWindowInsets(paddingValues)
        ) {
            stateHolder.SaveableStateProvider(selectedTab.name) {
                when (selectedTab) {
                    ShellTab.Groups -> GroupsScreen()
                    ShellTab.Log -> FoodYouAppNavHost(onDatabaseBackup)
                    ShellTab.Notifications -> NotificationsScreen()
                }
            }
        }
    }
}

private val ShellTab.icon: ImageVector
    get() =
        when (this) {
            ShellTab.Groups -> Icons.Filled.Group
            ShellTab.Log -> Icons.AutoMirrored.Filled.MenuBook
            ShellTab.Notifications -> Icons.Filled.Notifications
        }

@Composable
private fun ShellTab.label(): String =
    when (this) {
        ShellTab.Groups -> stringResource(Res.string.tab_groups)
        ShellTab.Log -> stringResource(Res.string.tab_log)
        ShellTab.Notifications -> stringResource(Res.string.tab_notifications)
    }
