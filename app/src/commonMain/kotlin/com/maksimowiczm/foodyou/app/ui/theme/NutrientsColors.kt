package com.maksimowiczm.foodyou.app.ui.theme

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.ResetToDefaultDialog
import com.maksimowiczm.foodyou.common.compose.extension.horizontal
import com.maksimowiczm.foodyou.common.compose.extension.vertical
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NutrientsColors(
    proteinsColor: Color,
    onProteinsColorChange: (Color) -> Unit,
    carbsColor: Color,
    onCarbsColorChange: (Color) -> Unit,
    fatsColor: Color,
    onFatsColorChange: (Color) -> Unit,
    onReset: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    var proteinsDialog by rememberSaveable { mutableStateOf(false) }
    if (proteinsDialog) {
        ColorPickerDialog(
            initialColor = proteinsColor,
            onConfirm = {
                onProteinsColorChange(it)
                proteinsDialog = false
            },
            onDismissRequest = { proteinsDialog = false }, modifier = Modifier.uiName("NutrientsColors.ColorPickerDialog.1", "com.maksimowiczm.foodyou.app.ui.theme.ColorPickerDialog", uiInspectionSource, "NutrientsColors")
        )
    }

    var carbsDialog by rememberSaveable { mutableStateOf(false) }
    if (carbsDialog) {
        ColorPickerDialog(
            initialColor = carbsColor,
            onConfirm = {
                onCarbsColorChange(it)
                carbsDialog = false
            },
            onDismissRequest = { carbsDialog = false }, modifier = Modifier.uiName("NutrientsColors.ColorPickerDialog.2", "com.maksimowiczm.foodyou.app.ui.theme.ColorPickerDialog", uiInspectionSource, "NutrientsColors")
        )
    }

    var fatsDialog by rememberSaveable { mutableStateOf(false) }
    if (fatsDialog) {
        ColorPickerDialog(
            initialColor = fatsColor,
            onConfirm = {
                onFatsColorChange(it)
                fatsDialog = false
            },
            onDismissRequest = { fatsDialog = false }, modifier = Modifier.uiName("NutrientsColors.ColorPickerDialog.3", "com.maksimowiczm.foodyou.app.ui.theme.ColorPickerDialog", uiInspectionSource, "NutrientsColors")
        )
    }

    var resetDialog by rememberSaveable { mutableStateOf(false) }
    if (resetDialog) {
        ResetToDefaultDialog(
            onConfirm = {
                onReset()
                resetDialog = false
            },
            onDismissRequest = { resetDialog = false }, modifier = Modifier.uiName("NutrientsColors.ResetToDefaultDialog.1", "com.maksimowiczm.foodyou.app.ui.common.component.ResetToDefaultDialog", uiInspectionSource, "NutrientsColors")
        ) {
            Text(stringResource(Res.string.description_nutrients_colors_reset), modifier = Modifier.uiName("NutrientsColors.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "NutrientsColors"))
        }
    }

    Column(modifier = (modifier.padding(contentPadding.vertical())).uiName("NutrientsColors.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "NutrientsColors")) {
        Text(
            text = stringResource(Res.string.headline_nutrients_colors),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = (Modifier.padding(contentPadding.horizontal())).uiName("NutrientsColors.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "NutrientsColors"),
        )
        ListItem(
            headlineContent = { Text(stringResource(Res.string.nutriment_proteins), modifier = Modifier.uiName("NutrientsColors.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "NutrientsColors")) },
            modifier = (Modifier.clickable { proteinsDialog = true }).uiName("NutrientsColors.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "NutrientsColors"),
            leadingContent = {
                Canvas((Modifier.size(24.dp).clip(MaterialTheme.shapes.small)).uiName("NutrientsColors.Canvas.1", "androidx.compose.foundation.Canvas", uiInspectionSource, "NutrientsColors")) {
                    drawRect(proteinsColor)
                }
            },
            supportingContent = { Text(proteinsColor.value.toColorString(), modifier = Modifier.uiName("NutrientsColors.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "NutrientsColors")) },
        )
        ListItem(
            headlineContent = { Text(stringResource(Res.string.nutriment_carbohydrates), modifier = Modifier.uiName("NutrientsColors.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "NutrientsColors")) },
            modifier = (Modifier.clickable { carbsDialog = true }).uiName("NutrientsColors.ListItem.2", "androidx.compose.material3.ListItem", uiInspectionSource, "NutrientsColors"),
            leadingContent = {
                Canvas((Modifier.size(24.dp).clip(MaterialTheme.shapes.small)).uiName("NutrientsColors.Canvas.2", "androidx.compose.foundation.Canvas", uiInspectionSource, "NutrientsColors")) {
                    drawRect(carbsColor)
                }
            },
            supportingContent = { Text(carbsColor.value.toColorString(), modifier = Modifier.uiName("NutrientsColors.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "NutrientsColors")) },
        )
        ListItem(
            headlineContent = { Text(stringResource(Res.string.nutriment_fats), modifier = Modifier.uiName("NutrientsColors.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "NutrientsColors")) },
            modifier = (Modifier.clickable { fatsDialog = true }).uiName("NutrientsColors.ListItem.3", "androidx.compose.material3.ListItem", uiInspectionSource, "NutrientsColors"),
            leadingContent = {
                Canvas((Modifier.size(24.dp).clip(MaterialTheme.shapes.small)).uiName("NutrientsColors.Canvas.3", "androidx.compose.foundation.Canvas", uiInspectionSource, "NutrientsColors")) {
                    drawRect(fatsColor)
                }
            },
            supportingContent = { Text(fatsColor.value.toColorString(), modifier = Modifier.uiName("NutrientsColors.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "NutrientsColors")) },
        )
        ListItem(
            headlineContent = { Text(stringResource(Res.string.headline_reset_to_default), modifier = Modifier.uiName("NutrientsColors.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "NutrientsColors")) },
            modifier = (Modifier.clickable { resetDialog = true }).uiName("NutrientsColors.ListItem.4", "androidx.compose.material3.ListItem", uiInspectionSource, "NutrientsColors"),
        )
    }
}

private fun ULong.toColorString(): String = "#${this.toString(16).substring(0, 8).uppercase()}"


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.theme",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/theme/NutrientsColors.kt",
)
