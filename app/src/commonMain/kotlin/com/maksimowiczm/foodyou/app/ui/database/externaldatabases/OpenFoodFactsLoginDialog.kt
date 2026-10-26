package com.maksimowiczm.foodyou.app.ui.database.externaldatabases


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.food.domain.repository.OpenFoodFactsCredentialsRepository
import com.maksimowiczm.foodyou.food.infrastructure.openfoodfacts.OpenFoodFactsRemoteDataSource
import foodyou.app.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun OpenFoodFactsLoginDialog(
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val repository: OpenFoodFactsCredentialsRepository = koinInject()
    val networkSource: OpenFoodFactsRemoteDataSource = koinInject()

    val login = rememberTextFieldState()
    val password = rememberTextFieldState()

    val isLoginValid by remember(login) { derivedStateOf { login.text.isNotBlank() } }
    val isPasswordValid by remember(password) { derivedStateOf { password.text.isNotBlank() } }
    val isFormValid by
        remember(login, password) {
            derivedStateOf { login.text.isNotBlank() && password.text.isNotBlank() }
        }

    var requestInProgress by rememberSaveable { mutableStateOf(false) }
    var authenticationFailure by rememberSaveable { mutableStateOf(false) }
    var hidePassword by rememberSaveable { mutableStateOf(true) }

    val onSignIn = {
        if (isFormValid) {
            requestInProgress = true

            scope.launch {
                val username = login.text.toString()
                val password = password.text.toString()

                runCatching { networkSource.login(username, password) }
                    .onFailure { authenticationFailure = true }
                    .onSuccess {
                        repository.store(username, password)
                        onSave()
                    }

                requestInProgress = false
            }
        }
    }

    UiSourceScope(uiInspectionSource.element("OpenFoodFactsLoginDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "OpenFoodFactsLoginDialog")) { AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onSignIn,
                shapes = ButtonDefaults.shapes(),
                enabled = isFormValid && !requestInProgress, modifier = Modifier.uiName("OpenFoodFactsLoginDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "OpenFoodFactsLoginDialog")
            ) {
                Text(stringResource(Res.string.action_sign_in), modifier = Modifier.uiName("OpenFoodFactsLoginDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsLoginDialog"))
            }
        },
        modifier = (modifier).uiName("OpenFoodFactsLoginDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "OpenFoodFactsLoginDialog"),
        dismissButton = {
            TextButton(onClick = onDismissRequest, shapes = ButtonDefaults.shapes(), modifier = Modifier.uiName("OpenFoodFactsLoginDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "OpenFoodFactsLoginDialog")) {
                Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("OpenFoodFactsLoginDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsLoginDialog"))
            }
        },
        title = { Text(stringResource(Res.string.action_sign_in), modifier = Modifier.uiName("OpenFoodFactsLoginDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsLoginDialog")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("OpenFoodFactsLoginDialog.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "OpenFoodFactsLoginDialog")) {
                if (requestInProgress) LinearProgressIndicator((Modifier.fillMaxWidth()).uiName("OpenFoodFactsLoginDialog.LinearProgressIndicator.1", "androidx.compose.material3.LinearProgressIndicator", uiInspectionSource, "OpenFoodFactsLoginDialog"))
                else Spacer(Modifier.height(4.dp))
                Text(
                    text =
                        stringResource(Res.string.description_open_food_facts_credentials_message),
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("OpenFoodFactsLoginDialog.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsLoginDialog")
                )
                if (authenticationFailure) {
                    Text(
                        text =
                            stringResource(Res.string.error_open_food_facts_failed_to_authenticate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error, modifier = Modifier.uiName("OpenFoodFactsLoginDialog.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsLoginDialog")
                    )
                }
                OutlinedTextField(
                    state = login,
                    modifier = (Modifier.semantics { contentType = ContentType.Username }).uiName("OpenFoodFactsLoginDialog.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "OpenFoodFactsLoginDialog"),
                    label = { Text(stringResource(Res.string.headline_username), modifier = Modifier.uiName("OpenFoodFactsLoginDialog.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsLoginDialog")) },
                    isError = !isLoginValid,
                    supportingText = {
                        if (!isLoginValid) {
                            Text(stringResource(Res.string.neutral_required), modifier = Modifier.uiName("OpenFoodFactsLoginDialog.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsLoginDialog"))
                        }
                    },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                SecureTextField(
                    state = password,
                    modifier = (Modifier.semantics { contentType = ContentType.Password }).uiName("OpenFoodFactsLoginDialog.SecureTextField.1", "androidx.compose.material3.SecureTextField", uiInspectionSource, "OpenFoodFactsLoginDialog"),
                    label = { Text(stringResource(Res.string.headline_password), modifier = Modifier.uiName("OpenFoodFactsLoginDialog.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsLoginDialog")) },
                    trailingIcon = {
                        IconButton(
                            onClick = { hidePassword = !hidePassword },
                            shapes = IconButtonDefaults.shapes(), modifier = Modifier.uiName("OpenFoodFactsLoginDialog.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "OpenFoodFactsLoginDialog")
                        ) {
                            if (hidePassword) {
                                Icon(
                                    Icons.Outlined.Visibility,
                                    stringResource(Res.string.action_show_password), modifier = Modifier.uiName("OpenFoodFactsLoginDialog.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "OpenFoodFactsLoginDialog")
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.VisibilityOff,
                                    stringResource(Res.string.action_hide_password), modifier = Modifier.uiName("OpenFoodFactsLoginDialog.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "OpenFoodFactsLoginDialog")
                                )
                            }
                        }
                    },
                    textObfuscationMode =
                        if (hidePassword) TextObfuscationMode.RevealLastTyped
                        else TextObfuscationMode.Visible,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    onKeyboardAction = { onSignIn() },
                    isError = !isPasswordValid,
                    supportingText = {
                        if (!isPasswordValid) {
                            Text(stringResource(Res.string.neutral_required), modifier = Modifier.uiName("OpenFoodFactsLoginDialog.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsLoginDialog"))
                        }
                    },
                )
            }
        },
    ) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.database.externaldatabases",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/externaldatabases/OpenFoodFactsLoginDialog.kt",
)
