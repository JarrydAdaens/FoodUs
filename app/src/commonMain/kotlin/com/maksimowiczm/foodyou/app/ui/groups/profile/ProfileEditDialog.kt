package com.maksimowiczm.foodyou.app.ui.groups.profile

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Single-field dialog used for both creating and renaming the profile — the username is the only
 * editable field, so a dedicated screen and navigation route would be overkill.
 */
@Composable
internal fun ProfileEditDialog(
    title: String,
    initialUsername: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textFieldState = rememberTextFieldState(initialUsername)
    val username = textFieldState.text.toString()

    UiSourceScope(uiInspectionSource.element("ProfileEditDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "ProfileEditDialog")) { AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirm(username) }, enabled = username.isNotBlank(), modifier = Modifier.uiName("ProfileEditDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "ProfileEditDialog")) {
                Text(stringResource(Res.string.action_save), modifier = Modifier.uiName("ProfileEditDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ProfileEditDialog"))
            }
        },
        modifier = (modifier).uiName("ProfileEditDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "ProfileEditDialog"),
        dismissButton = {
            TextButton(onDismissRequest, modifier = Modifier.uiName("ProfileEditDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "ProfileEditDialog")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("ProfileEditDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ProfileEditDialog")) }
        },
        title = { Text(title, modifier = Modifier.uiName("ProfileEditDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ProfileEditDialog")) },
        text = {
            OutlinedTextField(
                state = textFieldState,
                label = { Text(stringResource(Res.string.headline_username), modifier = Modifier.uiName("ProfileEditDialog.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "ProfileEditDialog")) },
                lineLimits = TextFieldLineLimits.SingleLine, modifier = Modifier.uiName("ProfileEditDialog.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "ProfileEditDialog")
            )
        },
    ) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.groups.profile",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/groups/profile/ProfileEditDialog.kt",
)
