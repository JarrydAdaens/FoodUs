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
fun DiscardDialog(
    onDismissRequest: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    UiSourceScope(uiInspectionSource.element("DiscardDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "DiscardDialog")) { AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = (modifier).uiName("DiscardDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "DiscardDialog"),
        confirmButton = {
            TextButton(onDiscard, modifier = Modifier.uiName("DiscardDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "DiscardDialog")) { Text(stringResource(Res.string.action_discard), modifier = Modifier.uiName("DiscardDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DiscardDialog")) }
        },
        dismissButton = {
            TextButton(onDismissRequest, modifier = Modifier.uiName("DiscardDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "DiscardDialog")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("DiscardDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DiscardDialog")) }
        },
        text = content,
    ) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/DiscardDialog.kt",
)
