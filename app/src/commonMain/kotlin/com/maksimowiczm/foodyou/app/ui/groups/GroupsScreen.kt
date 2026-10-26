package com.maksimowiczm.foodyou.app.ui.groups

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.groups.profile.MyProfileCard
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Groups tab. Hosts the My Profile card; Story 7 adds the Friends card and Story 9 the group cards
 * below it.
 */
@Composable
internal fun GroupsScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = (modifier).uiName("GroupsScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "GroupsScreen"),
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.tab_groups), modifier = Modifier.uiName("GroupsScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "GroupsScreen")) }, modifier = Modifier.uiName("GroupsScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "GroupsScreen")) },
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.fillMaxSize().padding(paddingValues)).uiName("GroupsScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "GroupsScreen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { MyProfileCard( modifier = Modifier.uiName("GroupsScreen.MyProfileCard.1", "com.maksimowiczm.foodyou.app.ui.groups.profile.MyProfileCard", uiInspectionSource, "GroupsScreen")) }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.groups",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/groups/GroupsScreen.kt",
)
