package com.maksimowiczm.foodyou.app.ui.shell

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Add
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.maksimowiczm.foodyou.app.navigation.isInSettingsFlow
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import org.koin.compose.koinInject
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
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val settingsOpen = remember(entry) { navController.isInSettingsFlow() }
    val clock: DateProvider = koinInject()
    var quickAddRequest by rememberSaveable { mutableStateOf<String?>(null) }

    // Android bottom-navigation convention: back from a secondary tab returns to the primary tab
    // instead of leaving the app. On Log, back belongs to the upstream navigation graph.
    if (selectedTab != ShellTab.Log) {
        NavigationEventHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = { selectedTabOrdinal = ShellTab.Log.ordinal },
        )
    }

    Scaffold(
        modifier = (modifier).uiName("FoodUsAppShell.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "FoodUsAppShell"),
        // The tab content owns its own Scaffolds and window insets; the shell only reserves room
        // for the bottom bar and lets the inner Scaffolds handle the rest.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (!settingsOpen || selectedTab != ShellTab.Log) NavigationBar (modifier = Modifier.uiName("FoodUsAppShell.NavigationBar.1", "androidx.compose.material3.NavigationBar", uiInspectionSource, "FoodUsAppShell")) {
                ShellTab.entries.forEach { tab ->
                    val label = tab.label()

                    NavigationBarItem(
                        selected = tab == selectedTab,
                        onClick = { selectedTabOrdinal = tab.ordinal },
                        icon = { Icon(imageVector = tab.icon, contentDescription = label, modifier = Modifier.uiName("FoodUsAppShell.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "FoodUsAppShell")) },
                        label = { Text(text = label, modifier = Modifier.uiName("FoodUsAppShell.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "FoodUsAppShell")) }, modifier = Modifier.uiName("FoodUsAppShell.NavigationBarItem.1", "androidx.compose.material3.NavigationBarItem", uiInspectionSource, "FoodUsAppShell")
                    )
                }
                NavigationBarItem(selected = false, onClick = {
                    quickAddRequest = clock.now().toString()
                    selectedTabOrdinal = ShellTab.Log.ordinal
                }, icon = { Icon(Icons.Filled.Add, stringResource(Res.string.food_entry_add), modifier = Modifier.uiName("FoodUsAppShell.Icon.phone2", "androidx.compose.material3.Icon", uiInspectionSource, "FoodUsAppShell")) },
                    label = { Text(stringResource(Res.string.food_entry_add), modifier = Modifier.uiName("FoodUsAppShell.Text.phone2", "androidx.compose.material3.Text", uiInspectionSource, "FoodUsAppShell")) }, modifier = Modifier.uiName("FoodUsAppShell.NavigationBarItem.phone2", "androidx.compose.material3.NavigationBarItem", uiInspectionSource, "FoodUsAppShell"))

            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                (Modifier.fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .consumeWindowInsets(paddingValues)).uiName("FoodUsAppShell.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "FoodUsAppShell")
        ) {
            stateHolder.SaveableStateProvider(selectedTab.name) {
                when (selectedTab) {
                    ShellTab.Groups -> UiSourceScope(uiInspectionSource.element("FoodUsAppShell.GroupsScreen.1", "com.maksimowiczm.foodyou.app.ui.groups.GroupsScreen", "FoodUsAppShell")) { GroupsScreen() }
                    ShellTab.Log -> FoodYouAppNavHost(onDatabaseBackup, navController = navController, quickAddRequest = quickAddRequest, onQuickAddHandled = { quickAddRequest = null }, modifier = Modifier.uiName("FoodUsAppShell.FoodYouAppNavHost.1", "com.maksimowiczm.foodyou.app.navigation.FoodYouAppNavHost", uiInspectionSource, "FoodUsAppShell"))
                    ShellTab.Notifications -> UiSourceScope(uiInspectionSource.element("FoodUsAppShell.NotificationsScreen.1", "com.maksimowiczm.foodyou.app.ui.notifications.NotificationsScreen", "FoodUsAppShell")) { NotificationsScreen() }
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


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.shell",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/shell/FoodUsAppShell.kt",
)
