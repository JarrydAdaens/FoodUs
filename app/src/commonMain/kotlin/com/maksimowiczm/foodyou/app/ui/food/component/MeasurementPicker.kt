package com.maksimowiczm.foodyou.app.ui.food.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.form.FormField
import com.maksimowiczm.foodyou.app.ui.common.form.nullableFloatParser
import com.maksimowiczm.foodyou.app.ui.common.form.positiveFloatValidator
import com.maksimowiczm.foodyou.app.ui.common.form.rememberFormField
import com.maksimowiczm.foodyou.app.ui.common.utility.Saver
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResource
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.domain.measurement.from
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.common.domain.measurement.type
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun MeasurementPicker(state: MeasurementPickerState, modifier: Modifier = Modifier) {
    val latestState by rememberUpdatedState(state)
    LaunchedEffect(state.inputField.textFieldState.text, state.type, state.possibleTypes) {
        if (latestState.type !in latestState.possibleTypes) return@LaunchedEffect
        val value = state.inputField.textFieldState.text.toString().toFloatOrNull()
            ?.takeIf { it.isFinite() && it > 0f } ?: return@LaunchedEffect
        val measurement = Measurement.from(state.type, value.toDouble())
        latestState.measurement = measurement
    }

    Column((modifier).uiName("MeasurementPicker.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "MeasurementPicker")) {
        if (state.inputField.error != null) {
            Text(
                text = org.jetbrains.compose.resources.stringResource(Res.string.error_portion_amount),
                color = MaterialTheme.colorScheme.error,
                modifier = (Modifier.padding(horizontal = 16.dp)).uiName("app.ui.food.component.MeasurementPicker.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "MeasurementPicker"),
            )
        }
        Row (modifier = Modifier.uiName("app.ui.food.component.MeasurementPicker.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "MeasurementPicker")) {
            Box(modifier = (Modifier.size(48.dp)).uiName("MeasurementPicker.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "MeasurementPicker"), contentAlignment = Alignment.Center) {
                Icon(painter = painterResource(Res.drawable.ic_weight), contentDescription = null, modifier = Modifier.uiName("app.ui.food.component.MeasurementPicker.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "MeasurementPicker"))
            }

            Spacer(Modifier.width(8.dp))

            Input(
                formField = state.inputField,
                type = state.type,
                types = state.possibleTypes,
                onSelect = { state.type = it },
                modifier = (Modifier.weight(1f).padding(end = 8.dp)).uiName("MeasurementPicker.Input.1", "com.maksimowiczm.foodyou.app.ui.food.component.Input", uiInspectionSource, "MeasurementPicker"),
            )
        }

        Spacer(Modifier.height(8.dp))

        FlowRow(
            modifier = (Modifier.padding(horizontal = 8.dp)).uiName("MeasurementPicker.FlowRow.1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "MeasurementPicker"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.suggestions.filter { it.type in state.possibleTypes }.forEach { measurement ->
                SuggestionChip(
                    onClick = {
                        state.inputField.textFieldState.setTextAndPlaceCursorAtEnd(
                            text = measurement.rawValue.formatClipZeros()
                        )
                        state.type = measurement.type
                    },
                    label = { Text(measurement.stringResource(), modifier = Modifier.uiName("app.ui.food.component.MeasurementPicker.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "MeasurementPicker")) }, modifier = Modifier.uiName("MeasurementPicker.SuggestionChip.1", "androidx.compose.material3.SuggestionChip", uiInspectionSource, "MeasurementPicker")
                )
            }
        }
    }
}

@Composable
private fun Input(
    formField: FormField<Float?, String>,
    type: MeasurementType,
    types: List<MeasurementType>,
    onSelect: (MeasurementType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val amountLabel = org.jetbrains.compose.resources.stringResource(Res.string.measurement_amount)
    var expanded by rememberSaveable { mutableStateOf(false) }

    val inputColor by
        animateColorAsState(
            targetValue =
                if (formField.error == null) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
        )
    val contentColor by
        animateColorAsState(
            targetValue =
                if (formField.error == null) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
        )

    Row(modifier = (modifier).uiName("Input.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "Input"), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Surface(
            modifier = (Modifier.height(48.dp).width(120.dp)).uiName("Input.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "Input"),
            color = inputColor,
            contentColor = contentColor,
            shape =
                RoundedCornerShape(
                    topStart = 16.dp,
                    bottomStart = 16.dp,
                    topEnd = 4.dp,
                    bottomEnd = 4.dp,
                ),
        ) {
            BasicTextField(
                state = formField.textFieldState,
                modifier = (Modifier.height(48.dp).padding(horizontal = 16.dp).semantics {
                    contentDescription = amountLabel
                }).uiName("Input.BasicTextField.1", "androidx.compose.foundation.text.BasicTextField", uiInspectionSource, "Input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = LocalTextStyle.current.merge(LocalContentColor.current),
                lineLimits = TextFieldLineLimits.SingleLine,
                cursorBrush = SolidColor(LocalContentColor.current),
                decorator = { Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.uiName("Input.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "Input")) { it() } },
            )
        }

        Surface(
            onClick = { expanded = true },
            modifier = (Modifier.heightIn(min = 48.dp).weight(1f)).uiName("Input.Surface.2", "androidx.compose.material3.Surface", uiInspectionSource, "Input"),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape =
                RoundedCornerShape(
                    topStart = 4.dp,
                    bottomStart = 4.dp,
                    topEnd = 16.dp,
                    bottomEnd = 16.dp,
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.uiName("Input.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "Input")) {
                Text(
                    text = type.stringResource(),
                    modifier = (Modifier.weight(1f)).uiName("Input.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Input"),
                    textAlign = TextAlign.Center,
                )
                Box(modifier = (Modifier.size(48.dp)).uiName("Input.Box.2", "androidx.compose.foundation.layout.Box", uiInspectionSource, "Input"), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Outlined.KeyboardArrowDown, contentDescription = null, modifier = Modifier.uiName("Input.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "Input"))

                    UiSourceScope(uiInspectionSource.element("Input.DropdownMenu.1", "androidx.compose.material3.DropdownMenu", "Input")) { DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.uiName("Input.DropdownMenu.1.root", "androidx.compose.material3.DropdownMenu", uiInspectionSource, "Input")) {
                        types.forEach {
                            DropdownMenuItem(
                                text = { Text(it.stringResource(), modifier = Modifier.uiName("Input.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Input")) },
                                onClick = {
                                    onSelect(it)
                                    expanded = false
                                }, modifier = Modifier.uiName("Input.DropdownMenuItem.1", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "Input")
                            )
                        }
                    } }
                }
            }
        }
    }
}

@Composable
fun rememberMeasurementPickerState(
    suggestions: List<Measurement>,
    possibleTypes: List<MeasurementType>,
    selectedMeasurement: Measurement,
): MeasurementPickerState {
    val inputField =
        rememberFormField(
            initialValue = selectedMeasurement.rawValue.toFloat(),
            parser = nullableFloatParser(onNotANumber = { "Invalid number format" }),
            validator =
                positiveFloatValidator(
                    onNotPositive = { "Value must be positive" },
                    onNull = { "Value cannot be empty" },
                ),
            textFieldState = rememberTextFieldState(selectedMeasurement.rawValue.formatClipZeros()),
            validateFirst = true,
        )
    val typeState = rememberSaveable { mutableStateOf(selectedMeasurement.type) }
    val lastSupportedType = rememberSaveable {
        mutableStateOf(typeState.value.takeIf { it in possibleTypes })
    }
    val measurementState =
        rememberSaveable(stateSaver = Measurement.Saver) {
            mutableStateOf(selectedMeasurement)
        }

    LaunchedEffect(possibleTypes, selectedMeasurement, typeState.value) {
        if (typeState.value in possibleTypes) {
            lastSupportedType.value = typeState.value
        } else if (lastSupportedType.value == typeState.value) {
            // An explicit invalid initial request stays invalid. Only reconcile a selection
            // whose type this picker previously offered and which has since become unavailable.
            val replacement = selectedMeasurement.takeIf { it.type in possibleTypes }
                ?: suggestions.firstOrNull { it.type in possibleTypes }
                ?: possibleTypes.firstOrNull()?.let { Measurement.from(it, 1.0) }
            if (replacement != null) {
                inputField.textFieldState.setTextAndPlaceCursorAtEnd(replacement.rawValue.formatClipZeros())
                typeState.value = replacement.type
                measurementState.value = replacement
                lastSupportedType.value = replacement.type
            }
        }
    }

    return remember(suggestions, possibleTypes, inputField, typeState, measurementState) {
        MeasurementPickerState(
            suggestions = suggestions,
            possibleTypes = possibleTypes,
            inputField = inputField,
            measurementState = measurementState,
            typeState = typeState,
        )
    }
}

class MeasurementPickerState(
    val suggestions: List<Measurement>,
    val possibleTypes: List<MeasurementType>,
    val inputField: FormField<Float?, String>,
    measurementState: MutableState<Measurement>,
    typeState: MutableState<MeasurementType>,
) {
    var measurement by measurementState
    var type by typeState
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/component/MeasurementPicker.kt",
)
