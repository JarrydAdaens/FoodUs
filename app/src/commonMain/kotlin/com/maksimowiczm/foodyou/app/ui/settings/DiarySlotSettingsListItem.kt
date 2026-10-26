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
internal fun DiarySlotSettingsListItem(
    onClick: () -> Unit,
    shape: Shape,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    SettingsListItem(
        icon = { Icon(Icons.Outlined.Restaurant, null, modifier = Modifier.uiName("DiarySlotSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotSettingsListItem")) },
        label = { Text(stringResource(Res.string.headline_diary_slots), modifier = Modifier.uiName("DiarySlotSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotSettingsListItem")) },
        supportingContent = { Text(stringResource(Res.string.neutral_set_your_diary_slot_schedule), modifier = Modifier.uiName("DiarySlotSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotSettingsListItem")) },
        onClick = onClick,
        modifier = (modifier).uiName("DiarySlotSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "DiarySlotSettingsListItem"),
        shape = shape,
        color = color,
        contentColor = contentColor,
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/DiarySlotSettingsListItem.kt",
)
