package com.maksimowiczm.foodyou.app.ui.food.diary.component
import androidx.compose.ui.Modifier

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PortionSaveErrorDialog(onDismiss: () -> Unit) {
    UiSourceScope(uiInspectionSource.element("PortionSaveErrorDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "PortionSaveErrorDialog")) { AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(stringResource(Res.string.error_portion_save), modifier = Modifier.uiName("PortionSaveErrorDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "PortionSaveErrorDialog")) },
        confirmButton = {
            TextButton(onDismiss, modifier = Modifier.uiName("PortionSaveErrorDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "PortionSaveErrorDialog")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("PortionSaveErrorDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "PortionSaveErrorDialog")) }
        }, modifier = Modifier.uiName("PortionSaveErrorDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "PortionSaveErrorDialog")
    ) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/component/PortionSaveErrorDialog.kt",
)
