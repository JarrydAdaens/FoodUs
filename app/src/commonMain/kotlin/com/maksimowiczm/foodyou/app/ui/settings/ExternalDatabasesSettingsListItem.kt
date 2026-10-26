package com.maksimowiczm.foodyou.app.ui.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExternalDatabasesSettingsListItem(
    onClick: () -> Unit,
    shape: Shape,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    SettingsListItem(
        icon = { Icon(Icons.Outlined.CloudDownload, null, modifier = Modifier.uiName("ExternalDatabasesSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ExternalDatabasesSettingsListItem")) },
        label = { Text(stringResource(Res.string.headline_external_databases), modifier = Modifier.uiName("ExternalDatabasesSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ExternalDatabasesSettingsListItem")) },
        supportingContent = { Text(stringResource(Res.string.description_external_databases), modifier = Modifier.uiName("ExternalDatabasesSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ExternalDatabasesSettingsListItem")) },
        onClick = onClick,
        modifier = (modifier).uiName("ExternalDatabasesSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "ExternalDatabasesSettingsListItem"),
        shape = shape,
        color = color,
        contentColor = contentColor,
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/ExternalDatabasesSettingsListItem.kt",
)
