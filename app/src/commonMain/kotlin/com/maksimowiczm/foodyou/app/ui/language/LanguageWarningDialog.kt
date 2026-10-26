package com.maksimowiczm.foodyou.app.ui.language

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LanguageWarningDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    UiSourceScope(uiInspectionSource.element("LanguageWarningDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "LanguageWarningDialog")) { AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = (modifier).uiName("LanguageWarningDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "LanguageWarningDialog"),
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.uiName("LanguageWarningDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "LanguageWarningDialog")) { Text(stringResource(Res.string.positive_ok), modifier = Modifier.uiName("LanguageWarningDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "LanguageWarningDialog")) }
        },
        icon = { Icon(imageVector = Icons.Default.Translate, contentDescription = null, modifier = Modifier.uiName("LanguageWarningDialog.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "LanguageWarningDialog")) },
        text = { Text(stringResource(Res.string.description_translation_warning), modifier = Modifier.uiName("LanguageWarningDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "LanguageWarningDialog")) },
    ) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.language",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/language/LanguageWarningDialog.kt",
)
