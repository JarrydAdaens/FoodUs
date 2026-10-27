package com.maksimowiczm.foodyou.app.ui.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
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
internal fun LibrarySettingsListItem(
    onClick: () -> Unit,
    shape: Shape,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    SettingsListItem(
        icon = { Icon(imageVector = Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.uiName("LibrarySettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "LibrarySettingsListItem")) },
        label = { Text(stringResource(Res.string.library_title), modifier = Modifier.uiName("LibrarySettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "LibrarySettingsListItem")) },
        supportingContent = { Text(stringResource(Res.string.library_description), modifier = Modifier.uiName("LibrarySettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "LibrarySettingsListItem")) },
        onClick = onClick,
        modifier = (modifier).uiName("LibrarySettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "LibrarySettingsListItem"),
        shape = shape,
        color = color,
        contentColor = contentColor,
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/LibrarySettingsListItem.kt",
)
