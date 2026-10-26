package com.maksimowiczm.foodyou.app.ui.settings.ai


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.common.compose.extension.add
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AiSettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: AiSettingsViewModel = koinViewModel()
    val validation by viewModel.validation.collectAsStateWithLifecycle()
    var keyVisible by remember { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                keyVisible = false
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = (modifier).uiName("AiSettingsScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "AiSettingsScreen"),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_ai_settings), modifier = Modifier.uiName("AiSettingsScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "AiSettingsScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("AiSettingsScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "AiSettingsScreen")) },
                actions = {
                    TextButton(onClick = viewModel::save, modifier = Modifier.uiName("AiSettingsScreen.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "AiSettingsScreen")) {
                        Text(stringResource(Res.string.action_save), modifier = Modifier.uiName("AiSettingsScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "AiSettingsScreen"))
                    }
                },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("AiSettingsScreen.LargeFlexibleTopAppBar.1", "androidx.compose.material3.LargeFlexibleTopAppBar", uiInspectionSource, "AiSettingsScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                (Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("AiSettingsScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "AiSettingsScreen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = paddingValues.add(vertical = 8.dp),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.description_ai_settings_screen),
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("AiSettingsScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "AiSettingsScreen")
                )
            }

            item {
                OutlinedSecureTextField(
                    state = viewModel.apiKey,
                    modifier = (Modifier.fillMaxWidth()).uiName("AiSettingsScreen.OutlinedSecureTextField.1", "androidx.compose.material3.OutlinedSecureTextField", uiInspectionSource, "AiSettingsScreen"),
                    label = { Text(stringResource(Res.string.headline_api_key), modifier = Modifier.uiName("AiSettingsScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "AiSettingsScreen")) },
                    textObfuscationMode =
                        if (keyVisible) TextObfuscationMode.Visible else TextObfuscationMode.Hidden,
                    trailingIcon = {
                        IconButton(
                            onClick = { keyVisible = !keyVisible },
                            shapes = IconButtonDefaults.shapes(), modifier = Modifier.uiName("AiSettingsScreen.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "AiSettingsScreen")
                        ) {
                            Icon(
                                imageVector =
                                    if (keyVisible) Icons.Outlined.VisibilityOff
                                    else Icons.Outlined.Visibility,
                                contentDescription =
                                    stringResource(
                                        if (keyVisible) Res.string.action_hide_password
                                        else Res.string.action_show_password
                                    ), modifier = Modifier.uiName("AiSettingsScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "AiSettingsScreen")
                            )
                        }
                    },
                )
            }

            item {
                AiField(
                    state = viewModel.endpoint,
                    label = stringResource(Res.string.headline_ai_endpoint),
                    placeholder = viewModel.endpointPlaceholder, modifier = Modifier.uiName("AiSettingsScreen.AiField.1", "com.maksimowiczm.foodyou.app.ui.settings.ai.AiField", uiInspectionSource, "AiSettingsScreen")
                )
            }

            item {
                AiField(
                    state = viewModel.model,
                    label = stringResource(Res.string.headline_ai_model),
                    placeholder = viewModel.modelPlaceholder, modifier = Modifier.uiName("AiSettingsScreen.AiField.2", "com.maksimowiczm.foodyou.app.ui.settings.ai.AiField", uiInspectionSource, "AiSettingsScreen")
                )
            }

            item { ValidateRow(state = validation, onValidate = viewModel::validate, modifier = Modifier.uiName("AiSettingsScreen.ValidateRow.1", "com.maksimowiczm.foodyou.app.ui.settings.ai.ValidateRow", uiInspectionSource, "AiSettingsScreen")) }

            item {
                AiField(
                    state = viewModel.systemPrompt,
                    label = stringResource(Res.string.headline_ai_system_prompt),
                    supportingText = stringResource(Res.string.description_ai_system_prompt),
                    singleLine = false, modifier = Modifier.uiName("AiSettingsScreen.AiField.3", "com.maksimowiczm.foodyou.app.ui.settings.ai.AiField", uiInspectionSource, "AiSettingsScreen")
                )
            }
        }
    }
}

@Composable
private fun AiField(
    state: TextFieldState,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        state = state,
        modifier = (modifier.fillMaxWidth()).uiName("AiField.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "AiField"),
        label = { Text(label, modifier = Modifier.uiName("AiField.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "AiField")) },
        placeholder = placeholder?.let { { Text(it, modifier = Modifier.uiName("AiField.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "AiField")) } },
        supportingText = supportingText?.let { { Text(it, modifier = Modifier.uiName("AiField.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "AiField")) } },
        lineLimits =
            if (singleLine) TextFieldLineLimits.SingleLine
            else TextFieldLineLimits.MultiLine(minHeightInLines = 3),
    )
}

@Composable
private fun ValidateRow(
    state: AiValidationUiState,
    onValidate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = (modifier.fillMaxWidth()).uiName("ValidateRow.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "ValidateRow"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onValidate,
            enabled = state != AiValidationUiState.Validating, modifier = Modifier.uiName("ValidateRow.OutlinedButton.1", "androidx.compose.material3.OutlinedButton", uiInspectionSource, "ValidateRow")
        ) {
            Text(stringResource(Res.string.action_validate), modifier = Modifier.uiName("ValidateRow.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ValidateRow"))
        }

        when (state) {
            AiValidationUiState.Idle -> Unit
            AiValidationUiState.Validating -> {
                CircularProgressIndicator(modifier = (Modifier.size(20.dp)).uiName("ValidateRow.CircularProgressIndicator.1", "androidx.compose.material3.CircularProgressIndicator", uiInspectionSource, "ValidateRow"), strokeWidth = 2.dp)
                Text(
                    text = stringResource(Res.string.ai_validating),
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("ValidateRow.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ValidateRow")
                )
            }
            AiValidationUiState.Success -> {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = SuccessGreen, modifier = Modifier.uiName("ValidateRow.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ValidateRow")
                )
                Text(
                    text = stringResource(Res.string.ai_validation_success),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuccessGreen, modifier = Modifier.uiName("ValidateRow.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ValidateRow")
                )
            }
            is AiValidationUiState.Error ->
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = (Modifier.weight(1f)).uiName("ValidateRow.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "ValidateRow"),
                )
        }
    }
}

// Explicit success green for the validation tick; the Material scheme has no semantic success color.
private val SuccessGreen = Color(0xFF2E7D32)


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.settings.ai",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/ai/AiSettingsScreen.kt",
)
