package com.maksimowiczm.foodyou.app.ui.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
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
internal fun GoalsSettingsListItem(
    onClick: () -> Unit,
    shape: Shape,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    SettingsListItem(
        icon = { Icon(imageVector = Icons.Outlined.Flag, contentDescription = null, modifier = Modifier.uiName("GoalsSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "GoalsSettingsListItem")) },
        label = { Text(stringResource(Res.string.headline_daily_goals), modifier = Modifier.uiName("GoalsSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "GoalsSettingsListItem")) },
        supportingContent = { Text(stringResource(Res.string.neutral_set_your_daily_goals), modifier = Modifier.uiName("GoalsSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "GoalsSettingsListItem")) },
        onClick = onClick,
        modifier = (modifier).uiName("GoalsSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "GoalsSettingsListItem"),
        shape = shape,
        color = color,
        contentColor = contentColor,
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/GoalsSettingsListItem.kt",
)
