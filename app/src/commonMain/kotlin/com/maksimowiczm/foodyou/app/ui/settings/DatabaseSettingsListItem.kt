package com.maksimowiczm.foodyou.app.ui.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DatabaseSettingsListItem(
    onClick: () -> Unit,
    shape: Shape,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    SettingsListItem(
        icon = { Icon(painterResource(Res.drawable.ic_database), null, modifier = Modifier.uiName("DatabaseSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DatabaseSettingsListItem")) },
        label = { Text(stringResource(Res.string.headline_data_backup_and_export), modifier = Modifier.uiName("DatabaseSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DatabaseSettingsListItem")) },
        supportingContent = { Text(stringResource(Res.string.description_manage_database), modifier = Modifier.uiName("DatabaseSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DatabaseSettingsListItem")) },
        onClick = onClick,
        modifier = (modifier).uiName("DatabaseSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "DatabaseSettingsListItem"),
        shape = shape,
        color = color,
        contentColor = contentColor,
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/DatabaseSettingsListItem.kt",
)
