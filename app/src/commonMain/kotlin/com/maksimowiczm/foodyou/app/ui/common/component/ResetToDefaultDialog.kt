package com.maksimowiczm.foodyou.app.ui.common.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResetToDefaultDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
) {
    UiSourceScope(uiInspectionSource.element("ResetToDefaultDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "ResetToDefaultDialog")) { AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { TextButton(onConfirm, modifier = Modifier.uiName("ResetToDefaultDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "ResetToDefaultDialog")) { Text(stringResource(Res.string.action_reset), modifier = Modifier.uiName("ResetToDefaultDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ResetToDefaultDialog")) } },
        modifier = (modifier).uiName("ResetToDefaultDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "ResetToDefaultDialog"),
        dismissButton = {
            TextButton(onDismissRequest, modifier = Modifier.uiName("ResetToDefaultDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "ResetToDefaultDialog")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("ResetToDefaultDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ResetToDefaultDialog")) }
        },
        title = { Text(stringResource(Res.string.headline_reset_to_default), modifier = Modifier.uiName("ResetToDefaultDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ResetToDefaultDialog")) },
        text = text,
    ) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/ResetToDefaultDialog.kt",
)
