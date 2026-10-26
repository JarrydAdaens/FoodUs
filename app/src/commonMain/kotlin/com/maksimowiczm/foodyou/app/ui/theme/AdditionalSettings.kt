package com.maksimowiczm.foodyou.app.ui.theme

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.maksimowiczm.foodyou.theme.Theme
import com.maksimowiczm.foodyou.theme.ThemeSettings
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdditionalSettings(
    themeSettings: ThemeSettings,
    onRandomizeTheme: (Boolean) -> Unit,
    onUpdateTheme: (Theme) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column((modifier).uiName("AdditionalSettings.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "AdditionalSettings")) {
        PlatformAdditionalSettings(themeSettings = themeSettings, onUpdateTheme = onUpdateTheme)
        ListItem(
            headlineContent = { Text(stringResource(Res.string.headline_random_theme), modifier = Modifier.uiName("AdditionalSettings.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "AdditionalSettings")) },
            modifier =
                (Modifier.clickable { onRandomizeTheme(!themeSettings.randomizeOnLaunch) }
                    .semantics { role = Role.Switch }).uiName("AdditionalSettings.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "AdditionalSettings"),
            supportingContent = { Text(stringResource(Res.string.description_random_theme), modifier = Modifier.uiName("AdditionalSettings.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "AdditionalSettings")) },
            trailingContent = {
                Switch(checked = themeSettings.randomizeOnLaunch, onCheckedChange = null, modifier = Modifier.uiName("AdditionalSettings.Switch.1", "androidx.compose.material3.Switch", uiInspectionSource, "AdditionalSettings"))
            },
        )
    }
}

@Composable
expect fun ColumnScope.PlatformAdditionalSettings(
    themeSettings: ThemeSettings,
    onUpdateTheme: (Theme) -> Unit,
)


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.theme",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/theme/AdditionalSettings.kt",
)
