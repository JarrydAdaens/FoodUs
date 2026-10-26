package com.maksimowiczm.foodyou.app.ui.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
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
internal fun PersonalizationSettingsListItem(
    onClick: () -> Unit,
    shape: Shape,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    SettingsListItem(
        icon = { Icon(imageVector = Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.uiName("PersonalizationSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "PersonalizationSettingsListItem")) },
        label = { Text(stringResource(Res.string.headline_personalization), modifier = Modifier.uiName("PersonalizationSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "PersonalizationSettingsListItem")) },
        supportingContent = { Text(stringResource(Res.string.description_personalization), modifier = Modifier.uiName("PersonalizationSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "PersonalizationSettingsListItem")) },
        onClick = onClick,
        modifier = (modifier).uiName("PersonalizationSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "PersonalizationSettingsListItem"),
        shape = shape,
        color = color,
        contentColor = contentColor,
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/PersonalizationSettingsListItem.kt",
)
