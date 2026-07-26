package com.maksimowiczm.foodyou.app.ui.food.diary.fasttext

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import foodyou.app.generated.resources.*
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Fast-text placeholder capture (Milestone 2, Story 8). The fastest logging path: type a name (and,
 * optionally, a description) and save with one tap. Saving records a zero-nutrition placeholder in
 * the diary via [FastTextViewModel]. [date] and [mealId] identify the diary meal being logged into.
 */
@Composable
fun FastTextScreen(
    date: LocalDate,
    mealId: Long,
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: FastTextViewModel = koinViewModel { parametersOf(date, mealId) }

    val latestOnSave by rememberUpdatedState(onSave)
    LaunchedCollectWithLifecycle(viewModel.events) {
        when (it) {
            FastTextEvent.Saved -> latestOnSave()
        }
    }

    val nameState = rememberTextFieldState()
    val descriptionState = rememberTextFieldState()
    val canSave by remember { derivedStateOf { nameState.text.isNotBlank() } }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(100)
        val _ = runCatching { focusRequester.requestFocus() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_fast_text)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
                actions = {
                    FilledIconButton(
                        onClick = {
                            if (canSave) {
                                viewModel.savePlaceholder(
                                    name = nameState.text.toString(),
                                    description = descriptionState.text.toString(),
                                )
                            }
                        },
                        enabled = canSave,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = stringResource(Res.string.action_save),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(Res.string.description_fast_text),
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                state = nameState,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                label = { Text(stringResource(Res.string.product_name)) },
                supportingText = { Text(stringResource(Res.string.neutral_required)) },
                lineLimits = TextFieldLineLimits.SingleLine,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                state = descriptionState,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.label_description)) },
                supportingText = { Text(stringResource(Res.string.neutral_optional)) },
            )
        }
    }
}
