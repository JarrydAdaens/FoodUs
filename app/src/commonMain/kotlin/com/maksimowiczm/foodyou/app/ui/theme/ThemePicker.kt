package com.maksimowiczm.foodyou.app.ui.theme


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TonalToggleButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.maksimowiczm.foodyou.theme.ThemeOption
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ThemePicker(
    themeOption: ThemeOption,
    onThemeOptionChange: (ThemeOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = (modifier).uiName("ThemePicker.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "ThemePicker"),
        horizontalArrangement =
            Arrangement.spacedBy(
                ButtonGroupDefaults.ConnectedSpaceBetween,
                Alignment.CenterHorizontally,
            ),
    ) {
        TonalToggleButton(
            checked = themeOption == ThemeOption.System,
            onCheckedChange = { onThemeOptionChange(ThemeOption.System) },
            modifier = (Modifier.semantics { role = Role.RadioButton }).uiName("ThemePicker.TonalToggleButton.1", "androidx.compose.material3.TonalToggleButton", uiInspectionSource, "ThemePicker"),
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
            content = { Text(stringResource(Res.string.headline_system), modifier = Modifier.uiName("ThemePicker.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ThemePicker")) },
        )
        TonalToggleButton(
            checked = themeOption == ThemeOption.Light,
            onCheckedChange = { onThemeOptionChange(ThemeOption.Light) },
            modifier = (Modifier.semantics { role = Role.RadioButton }).uiName("ThemePicker.TonalToggleButton.2", "androidx.compose.material3.TonalToggleButton", uiInspectionSource, "ThemePicker"),
            shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
            content = { Text(stringResource(Res.string.headline_light), modifier = Modifier.uiName("ThemePicker.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ThemePicker")) },
        )
        TonalToggleButton(
            checked = themeOption == ThemeOption.Dark,
            onCheckedChange = { onThemeOptionChange(ThemeOption.Dark) },
            modifier = (Modifier.semantics { role = Role.RadioButton }).uiName("ThemePicker.TonalToggleButton.3", "androidx.compose.material3.TonalToggleButton", uiInspectionSource, "ThemePicker"),
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
            content = { Text(stringResource(Res.string.headline_dark), modifier = Modifier.uiName("ThemePicker.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ThemePicker")) },
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.theme",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/theme/ThemePicker.kt",
)
