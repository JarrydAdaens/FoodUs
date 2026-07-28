package com.maksimowiczm.foodyou.app.ui.groups.profile

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

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirm(username) }, enabled = username.isNotBlank()) {
                Text(stringResource(Res.string.action_save))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onDismissRequest) { Text(stringResource(Res.string.action_cancel)) }
        },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                state = textFieldState,
                label = { Text(stringResource(Res.string.headline_username)) },
                lineLimits = TextFieldLineLimits.SingleLine,
            )
        },
    )
}
