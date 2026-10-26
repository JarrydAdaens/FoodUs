package com.maksimowiczm.foodyou.app.ui.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalAppConfig
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiNameBarSettingsListItem
import com.maksimowiczm.foodyou.common.compose.extension.add
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onDiarySlots: () -> Unit,
    onLanguage: () -> Unit,
    onGoals: () -> Unit,
    onPersonalization: () -> Unit,
    onDatabase: () -> Unit,
    onExternalDatabases: () -> Unit,
    onAiSettings: () -> Unit,
    onRelaySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val color = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface
    val shape = RectangleShape

    Scaffold(
        modifier = (modifier).uiName("SettingsScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "SettingsScreen"),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_settings), modifier = Modifier.uiName("SettingsScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SettingsScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("SettingsScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "SettingsScreen")) },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("SettingsScreen.LargeFlexibleTopAppBar.1", "androidx.compose.material3.LargeFlexibleTopAppBar", uiInspectionSource, "SettingsScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("SettingsScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "SettingsScreen"),
            contentPadding = paddingValues.add(vertical = 8.dp),
        ) {
            item {
                PersonalizationSettingsListItem(
                    onClick = onPersonalization,
                    shape = shape,
                    color = color,
                    contentColor = contentColor, modifier = Modifier.uiName("SettingsScreen.PersonalizationSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.settings.PersonalizationSettingsListItem", uiInspectionSource, "SettingsScreen")
                )
            }

            item {
                DiarySlotSettingsListItem(
                    onClick = onDiarySlots,
                    shape = shape,
                    color = color,
                    contentColor = contentColor, modifier = Modifier.uiName("SettingsScreen.DiarySlotSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.settings.DiarySlotSettingsListItem", uiInspectionSource, "SettingsScreen")
                )
            }

            item {
                GoalsSettingsListItem(
                    onClick = onGoals,
                    shape = shape,
                    color = color,
                    contentColor = contentColor, modifier = Modifier.uiName("SettingsScreen.GoalsSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.settings.GoalsSettingsListItem", uiInspectionSource, "SettingsScreen")
                )
            }

            item {
                ExternalDatabasesSettingsListItem(
                    onClick = onExternalDatabases,
                    shape = shape,
                    color = color,
                    contentColor = contentColor, modifier = Modifier.uiName("SettingsScreen.ExternalDatabasesSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.settings.ExternalDatabasesSettingsListItem", uiInspectionSource, "SettingsScreen")
                )
            }

            item {
                DatabaseSettingsListItem(
                    onClick = onDatabase,
                    shape = shape,
                    color = color,
                    contentColor = contentColor, modifier = Modifier.uiName("SettingsScreen.DatabaseSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.settings.DatabaseSettingsListItem", uiInspectionSource, "SettingsScreen")
                )
            }

            item {
                AiSettingsListItem(
                    onClick = onAiSettings,
                    shape = shape,
                    color = color,
                    contentColor = contentColor, modifier = Modifier.uiName("SettingsScreen.AiSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.settings.AiSettingsListItem", uiInspectionSource, "SettingsScreen")
                )
            }

            item {
                RelaySettingsListItem(
                    onClick = onRelaySettings,
                    shape = shape,
                    color = color,
                    contentColor = contentColor, modifier = Modifier.uiName("SettingsScreen.RelaySettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.settings.RelaySettingsListItem", uiInspectionSource, "SettingsScreen")
                )
            }

            item {
                LanguageSettingsListItem(
                    onClick = onLanguage,
                    shape = shape,
                    color = color,
                    contentColor = contentColor, modifier = Modifier.uiName("SettingsScreen.LanguageSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.settings.LanguageSettingsListItem", uiInspectionSource, "SettingsScreen")
                )
            }

            item {
                val uriHandle = LocalUriHandler.current
                val appConfig = LocalAppConfig.current
                SettingsListItem(
                    icon = { Icon(Icons.Outlined.PrivacyTip, null, modifier = Modifier.uiName("SettingsScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "SettingsScreen")) },
                    label = { Text(stringResource(Res.string.headline_privacy_policy), modifier = Modifier.uiName("SettingsScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SettingsScreen")) },
                    supportingContent = {
                        Text(stringResource(Res.string.description_privacy_policy), modifier = Modifier.uiName("SettingsScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "SettingsScreen"))
                    },
                    onClick = { uriHandle.openUri(appConfig.privacyPolicyUri) },
                    shape = shape,
                    color = color,
                    contentColor = contentColor, modifier = Modifier.uiName("SettingsScreen.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "SettingsScreen")
                )
            }

            item {
                AboutSettingsListItem(
                    onClick = onAbout,
                    shape = shape,
                    color = color,
                    contentColor = contentColor, modifier = Modifier.uiName("SettingsScreen.AboutSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.settings.AboutSettingsListItem", uiInspectionSource, "SettingsScreen")
                )
            }
            item { UiNameBarSettingsListItem() }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/SettingsScreen.kt",
)
