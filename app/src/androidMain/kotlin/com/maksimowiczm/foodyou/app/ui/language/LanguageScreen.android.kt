package com.maksimowiczm.foodyou.app.ui.language

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal actual fun SystemSettingsListItem(modifier: Modifier) {
    val context = LocalContext.current
    val intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                val uri = Uri.fromParts("package", context.packageName, null)
                data = uri
            }
        } else {
            Intent()
        }

    val isSystemLocaleSettingsAvailable =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_ALL)
                .isNotEmpty()
        } else {
            false
        }

    if (isSystemLocaleSettingsAvailable) {
        Column((modifier).uiName("SystemSettingsListItem.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "SystemSettingsListItem")) {
            HorizontalDivider( modifier = Modifier.uiName("SystemSettingsListItem.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "SystemSettingsListItem"))
            ListItem(
                headlineContent = {
                    Text(stringResource(Res.string.headline_system_language_settings), modifier = Modifier.uiName("SystemSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SystemSettingsListItem"))
                },
                modifier = (Modifier.clickable { context.startActivity(intent) }).uiName("SystemSettingsListItem.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "SystemSettingsListItem"),
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null, modifier = Modifier.uiName("SystemSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "SystemSettingsListItem")
                    )
                },
            )
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.language",
    "app/src/androidMain/kotlin/com/maksimowiczm/foodyou/app/ui/language/LanguageScreen.android.kt",
)
