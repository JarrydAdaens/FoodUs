package com.maksimowiczm.foodyou.app.ui.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AiSettingsListItem(
    onClick: () -> Unit,
    shape: Shape,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    SettingsListItem(
        icon = { Icon(Icons.Outlined.SmartToy, null, modifier = Modifier.uiName("AiSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "AiSettingsListItem")) },
        label = { Text(stringResource(Res.string.headline_ai_settings), modifier = Modifier.uiName("AiSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "AiSettingsListItem")) },
        supportingContent = { Text(stringResource(Res.string.description_ai_settings), modifier = Modifier.uiName("AiSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "AiSettingsListItem")) },
        onClick = onClick,
        modifier = (modifier).uiName("AiSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "AiSettingsListItem"),
        shape = shape,
        color = color,
        contentColor = contentColor,
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/AiSettingsListItem.kt",
)
