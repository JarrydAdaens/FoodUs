package com.maksimowiczm.foodyou.app.ui.theme

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import android.os.Build
import androidx.compose.foundation.clickable
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
actual fun ColumnScope.PlatformAdditionalSettings(
    themeSettings: ThemeSettings,
    onUpdateTheme: (Theme) -> Unit,
) {

    val isDynamic = themeSettings.theme is Theme.Dynamic || themeSettings.theme is Theme.Default
    val themes = rememberThemes()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.headline_dynamic_colors), modifier = Modifier.uiName("PlatformAdditionalSettings.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "PlatformAdditionalSettings")) },
            modifier =
                (Modifier.clickable {
                        if (isDynamic) onUpdateTheme(themes.first())
                        else onUpdateTheme(Theme.Dynamic)
                    }
                    .semantics { role = Role.Switch }).uiName("PlatformAdditionalSettings.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "PlatformAdditionalSettings"),
            supportingContent = { Text(stringResource(Res.string.description_dynamic_colors), modifier = Modifier.uiName("PlatformAdditionalSettings.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "PlatformAdditionalSettings")) },
            trailingContent = { Switch(checked = isDynamic, onCheckedChange = null, modifier = Modifier.uiName("PlatformAdditionalSettings.Switch.1", "androidx.compose.material3.Switch", uiInspectionSource, "PlatformAdditionalSettings")) },
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.theme",
    "app/src/androidMain/kotlin/com/maksimowiczm/foodyou/app/ui/theme/AdditionalSettings.android.kt",
)
