package com.maksimowiczm.foodyou.app.ui.meal


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import foodyou.app.generated.resources.*
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DiarySlotCard(
    state: DiarySlotCardState,
    isDragging: Boolean,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val dateFormatter = LocalDateFormatter.current

    val tonalElevation by animateDpAsState(if (isDragging) 16.dp else 0.dp)
    val shadowElevation by animateDpAsState(if (isDragging) 16.dp else 0.dp)

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    if (showDeleteDialog) {
        DeleteDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            }, modifier = Modifier.uiName("DiarySlotCard.DeleteDialog.1", "com.maksimowiczm.foodyou.app.ui.meal.DeleteDialog", uiInspectionSource, "DiarySlotCard")
        )
    }

    val nameInput: @Composable RowScope.() -> Unit = {
        BasicTextField(
            state = state.name.textFieldState,
            modifier =
                (Modifier.defaultMinSize(minWidth = 150.dp)
                    .width(IntrinsicSize.Min)
                    .weight(1f, false)).uiName("DiarySlotCard.BasicTextField.1", "androidx.compose.foundation.text.BasicTextField", uiInspectionSource, "DiarySlotCard"),
            textStyle =
                LocalTextStyle.current
                    .merge(MaterialTheme.typography.headlineMedium)
                    .merge(LocalContentColor.current),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            decorator = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.uiName("app.ui.meal.DiarySlotCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DiarySlotCard")
                ) {
                    Column(modifier = (Modifier.weight(1f, false)).uiName("app.ui.meal.DiarySlotCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DiarySlotCard")) {
                        Box(modifier = (Modifier.padding(horizontal = 4.dp)).uiName("DiarySlotCard.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "DiarySlotCard")) { it() }

                        HorizontalDivider(
                            color =
                                if (state.name.error != null) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }, modifier = Modifier.uiName("DiarySlotCard.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "DiarySlotCard")
                        )
                    }

                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null, modifier = Modifier.uiName("app.ui.meal.DiarySlotCard.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotCard"))
                }
            },
        )
    }
    val iconButton: @Composable RowScope.() -> Unit = {
        when {
            action != null -> action()

            state.isModified -> {
                FilledIconButton(
                    onClick = {
                        if (state.isValid) {
                            onSave()
                        }
                    },
                    enabled = state.isValid, modifier = Modifier.uiName("app.ui.meal.DiarySlotCard.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "DiarySlotCard")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = stringResource(Res.string.action_save), modifier = Modifier.uiName("app.ui.meal.DiarySlotCard.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotCard")
                    )
                }
            }

            !state.isModified -> {
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.uiName("DiarySlotCard.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "DiarySlotCard")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.action_delete), modifier = Modifier.uiName("app.ui.meal.DiarySlotCard.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotCard")
                    )
                }
            }
        }
    }

    var showFromTimePicker by rememberSaveable { mutableStateOf(false) }
    if (showFromTimePicker) {
        val timePickerState =
            rememberTimePickerState(
                initialHour = state.fromTime.hour,
                initialMinute = state.fromTime.minute,
            )

        UiSourceScope(uiInspectionSource.element("DiarySlotCard.TimePickerDialog.1", "androidx.compose.material3.TimePickerDialog", "DiarySlotCard")) { TimePickerDialog(
            onDismissRequest = { showFromTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFromTimePicker = false
                        state.fromTime =
                            LocalTime(hour = timePickerState.hour, minute = timePickerState.minute)
                    }, modifier = Modifier.uiName("DiarySlotCard.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "DiarySlotCard")
                ) {
                    Text(text = stringResource(Res.string.action_confirm), modifier = Modifier.uiName("app.ui.meal.DiarySlotCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotCard"))
                }
            },
            title = {},
            dismissButton = {
                TextButton(onClick = { showFromTimePicker = false }, modifier = Modifier.uiName("DiarySlotCard.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "DiarySlotCard")) {
                    Text(text = stringResource(Res.string.action_cancel), modifier = Modifier.uiName("app.ui.meal.DiarySlotCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotCard"))
                }
            }, modifier = Modifier.uiName("DiarySlotCard.TimePickerDialog.1.root", "androidx.compose.material3.TimePickerDialog", uiInspectionSource, "DiarySlotCard")
        ) {
            TimePicker(timePickerState, modifier = Modifier.uiName("DiarySlotCard.TimePicker.1", "androidx.compose.material3.TimePicker", uiInspectionSource, "DiarySlotCard"))
        } }
    }

    var showToTimePicker by rememberSaveable { mutableStateOf(false) }
    if (showToTimePicker) {
        val timePickerState =
            rememberTimePickerState(
                initialHour = state.toTime.hour,
                initialMinute = state.toTime.minute,
            )

        UiSourceScope(uiInspectionSource.element("DiarySlotCard.TimePickerDialog.2", "androidx.compose.material3.TimePickerDialog", "DiarySlotCard")) { TimePickerDialog(
            onDismissRequest = { showToTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showToTimePicker = false
                        state.toTime =
                            LocalTime(hour = timePickerState.hour, minute = timePickerState.minute)
                    }, modifier = Modifier.uiName("DiarySlotCard.TextButton.3", "androidx.compose.material3.TextButton", uiInspectionSource, "DiarySlotCard")
                ) {
                    Text(text = stringResource(Res.string.action_confirm), modifier = Modifier.uiName("DiarySlotCard.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotCard"))
                }
            },
            title = {},
            dismissButton = {
                TextButton(onClick = { showToTimePicker = false }, modifier = Modifier.uiName("DiarySlotCard.TextButton.4", "androidx.compose.material3.TextButton", uiInspectionSource, "DiarySlotCard")) {
                    Text(text = stringResource(Res.string.action_cancel), modifier = Modifier.uiName("DiarySlotCard.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotCard"))
                }
            }, modifier = Modifier.uiName("DiarySlotCard.TimePickerDialog.2.root", "androidx.compose.material3.TimePickerDialog", uiInspectionSource, "DiarySlotCard")
        ) {
            TimePicker(timePickerState, modifier = Modifier.uiName("DiarySlotCard.TimePicker.2", "androidx.compose.material3.TimePicker", uiInspectionSource, "DiarySlotCard"))
        } }
    }

    val dateInput =
        @Composable {
            val containerColor by
                animateColorAsState(
                    if (state.isModified) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    }
                )

            val contentColor by
                animateColorAsState(
                    if (state.isModified) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("app.ui.meal.DiarySlotCard.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DiarySlotCard"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Card(
                    onClick = { showFromTimePicker = true },
                    colors =
                        CardDefaults.cardColors(
                            containerColor = containerColor,
                            contentColor = contentColor,
                        ), modifier = Modifier.uiName("DiarySlotCard.Card.1", "androidx.compose.material3.Card", uiInspectionSource, "DiarySlotCard")
                ) {
                    Text(
                        modifier = (Modifier.padding(8.dp)).uiName("DiarySlotCard.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotCard"),
                        text = dateFormatter.formatTime(state.fromTime),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                Text(
                    text = stringResource(Res.string.en_dash),
                    style = MaterialTheme.typography.headlineSmall, modifier = Modifier.uiName("DiarySlotCard.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotCard")
                )
                Card(
                    onClick = { showToTimePicker = true },
                    colors =
                        CardDefaults.cardColors(
                            containerColor = containerColor,
                            contentColor = contentColor,
                        ), modifier = Modifier.uiName("DiarySlotCard.Card.2", "androidx.compose.material3.Card", uiInspectionSource, "DiarySlotCard")
                ) {
                    Text(
                        modifier = (Modifier.padding(8.dp)).uiName("DiarySlotCard.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotCard"),
                        text = dateFormatter.formatTime(state.toTime),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
        }

    val surfaceColor by
        animateColorAsState(
            targetValue =
                if (state.isModified) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
        )

    val contentColor by
        animateColorAsState(
            targetValue =
                if (state.isModified) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
        )

    Surface(
        modifier = (modifier).uiName("DiarySlotCard.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "DiarySlotCard"),
        shape = MaterialTheme.shapes.medium,
        color = surfaceColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    ) {
        Column(
            modifier =
                (Modifier.fillMaxWidth()
                    .padding(top = 8.dp, start = 16.dp, end = 8.dp, bottom = 16.dp)).uiName("app.ui.meal.DiarySlotCard.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DiarySlotCard")
        ) {
            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("app.ui.meal.DiarySlotCard.Row.3", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DiarySlotCard"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                nameInput()
                iconButton()
            }

            AnimatedVisibility(visible = !state.isAllDay, modifier = Modifier.uiName("app.ui.meal.DiarySlotCard.AnimatedVisibility.1", "androidx.compose.animation.AnimatedVisibility", uiInspectionSource, "DiarySlotCard")) {
                Column (modifier = Modifier.uiName("DiarySlotCard.Column.3", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DiarySlotCard")) {
                    Spacer(Modifier.height(8.dp))

                    dateInput()
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier =
                    (Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        onClick = { state.isAllDay = !state.isAllDay },
                    )).uiName("DiarySlotCard.Row.4", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DiarySlotCard"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(checked = state.isAllDay, onCheckedChange = null, modifier = Modifier.uiName("DiarySlotCard.Switch.1", "androidx.compose.material3.Switch", uiInspectionSource, "DiarySlotCard"))
                Spacer(Modifier.width(16.dp))
                Text(
                    text = stringResource(Res.string.headline_all_day),
                    style = MaterialTheme.typography.bodyLarge, modifier = Modifier.uiName("DiarySlotCard.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotCard")
                )
            }
        }
    }
}

@Composable
private fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    UiSourceScope(uiInspectionSource.element("app.ui.meal.DeleteDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "DeleteDialog")) { AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.uiName("app.ui.meal.DeleteDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "DeleteDialog")) {
                Text(text = stringResource(Res.string.action_delete), modifier = Modifier.uiName("app.ui.meal.DeleteDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DeleteDialog"))
            }
        },
        modifier = (modifier).uiName("app.ui.meal.DeleteDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "DeleteDialog"),
        dismissButton = {
            TextButton(onClick = onDismissRequest, modifier = Modifier.uiName("app.ui.meal.DeleteDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "DeleteDialog")) {
                Text(text = stringResource(Res.string.action_cancel), modifier = Modifier.uiName("app.ui.meal.DeleteDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DeleteDialog"))
            }
        },
        icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.uiName("app.ui.meal.DeleteDialog.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DeleteDialog")) },
        title = { Text(text = stringResource(Res.string.neutral_permanently_delete_diary_slot), modifier = Modifier.uiName("app.ui.meal.DeleteDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "DeleteDialog")) },
        text = { Text(text = stringResource(Res.string.neutral_diary_slot_will_be_deleted_permanently), modifier = Modifier.uiName("app.ui.meal.DeleteDialog.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "DeleteDialog")) },
    ) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.meal",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/meal/DiarySlotCard.kt",
)
