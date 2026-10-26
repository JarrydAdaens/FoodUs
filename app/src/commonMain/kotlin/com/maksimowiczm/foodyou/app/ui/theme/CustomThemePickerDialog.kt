package com.maksimowiczm.foodyou.app.ui.theme


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.maksimowiczm.foodyou.common.compose.utility.LocalClipboardManager
import com.maksimowiczm.foodyou.theme.Theme
import com.maksimowiczm.foodyou.theme.ThemeContrast
import com.maksimowiczm.foodyou.theme.ThemeStyle
import com.materialkolor.ktx.toHex
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.stringResource

@Composable
fun CustomThemePickerDialog(
    initialTheme: Theme.Custom?,
    onConfirm: (Theme.Custom) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current

    val initialColor = initialTheme?.seedColor?.let { Color(it) }
    val controller = rememberColorPickerController()
    LaunchedEffect(Unit) { initialColor?.let { controller.selectByColor(it, false) } }

    var paletteExpanded by rememberSaveable { mutableStateOf(false) }
    val paletteStyle = rememberSaveable {
        mutableStateOf(initialTheme?.style ?: ThemeStyle.TonalSpot)
    }
    var contrastExpanded by rememberSaveable { mutableStateOf(false) }
    val contrast = rememberSaveable {
        mutableStateOf(initialTheme?.contrast ?: ThemeContrast.Default)
    }
    val isAmoled = rememberSaveable { mutableStateOf(initialTheme?.isAmoled ?: false) }

    val content =
        @Composable {
            Column(
                modifier = (Modifier.verticalScroll(rememberScrollState())).uiName("CustomThemePickerDialog.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "CustomThemePickerDialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(Res.string.description_custom_palette), modifier = Modifier.uiName("CustomThemePickerDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "CustomThemePickerDialog"))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.uiName("CustomThemePickerDialog.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "CustomThemePickerDialog")
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.uiName("CustomThemePickerDialog.Column.3", "androidx.compose.foundation.layout.Column", uiInspectionSource, "CustomThemePickerDialog")
                    ) {
                        HsvColorPicker(
                            modifier = (Modifier.fillMaxWidth().aspectRatio(1f)).uiName("CustomThemePickerDialog.HsvColorPicker.1", "com.github.skydoves.colorpicker.compose.HsvColorPicker", uiInspectionSource, "CustomThemePickerDialog"),
                            controller = controller,
                        )
                        BrightnessSlider(
                            modifier = (Modifier.fillMaxWidth().height(32.dp)).uiName("CustomThemePickerDialog.BrightnessSlider.1", "com.github.skydoves.colorpicker.compose.BrightnessSlider", uiInspectionSource, "CustomThemePickerDialog"),
                            controller = controller,
                        )
                        Text(
                            text = controller.selectedColor.value.toHex(),
                            color = controller.selectedColor.value,
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            modifier =
                                (Modifier.clickable(
                                    interactionSource = null,
                                    indication = null,
                                    onClick = {
                                        clipboardManager.copy(
                                            "Color",
                                            controller.selectedColor.value.toHex(),
                                        )
                                    },
                                )).uiName("CustomThemePickerDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "CustomThemePickerDialog"),
                        )
                    }
                    TextField(
                        value = paletteStyle.value.name,
                        onValueChange = {},
                        trailingIcon = {
                            Icon(Icons.Outlined.ArrowDropDown, null, modifier = Modifier.uiName("CustomThemePickerDialog.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "CustomThemePickerDialog"))
                            UiSourceScope(uiInspectionSource.element("CustomThemePickerDialog.DropdownMenu.1", "androidx.compose.material3.DropdownMenu", "CustomThemePickerDialog")) { DropdownMenu(
                                expanded = paletteExpanded,
                                onDismissRequest = { paletteExpanded = false }, modifier = Modifier.uiName("CustomThemePickerDialog.DropdownMenu.1.root", "androidx.compose.material3.DropdownMenu", uiInspectionSource, "CustomThemePickerDialog")
                            ) {
                                ThemeStyle.entries.forEach { style ->
                                    DropdownMenuItem(
                                        text = { Text(style.name, modifier = Modifier.uiName("CustomThemePickerDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "CustomThemePickerDialog")) },
                                        onClick = {
                                            paletteStyle.value = style
                                            paletteExpanded = false
                                        }, modifier = Modifier.uiName("CustomThemePickerDialog.DropdownMenuItem.1", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "CustomThemePickerDialog")
                                    )
                                }
                            } }
                        },
                        readOnly = true,
                        label = { Text(stringResource(Res.string.headline_palette_style), modifier = Modifier.uiName("CustomThemePickerDialog.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "CustomThemePickerDialog")) },
                        interactionSource =
                            remember { MutableInteractionSource() }
                                .apply {
                                    LaunchedEffect(Unit) {
                                        interactions
                                            .filter { it is PressInteraction.Release }
                                            .collectLatest { paletteExpanded = true }
                                    }
                                }, modifier = Modifier.uiName("CustomThemePickerDialog.TextField.1", "androidx.compose.material3.TextField", uiInspectionSource, "CustomThemePickerDialog")
                    )
                    TextField(
                        value = contrast.value.name,
                        onValueChange = {},
                        trailingIcon = {
                            Icon(Icons.Outlined.ArrowDropDown, null, modifier = Modifier.uiName("CustomThemePickerDialog.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "CustomThemePickerDialog"))
                            UiSourceScope(uiInspectionSource.element("CustomThemePickerDialog.DropdownMenu.2", "androidx.compose.material3.DropdownMenu", "CustomThemePickerDialog")) { DropdownMenu(
                                expanded = contrastExpanded,
                                onDismissRequest = { contrastExpanded = false }, modifier = Modifier.uiName("CustomThemePickerDialog.DropdownMenu.2.root", "androidx.compose.material3.DropdownMenu", uiInspectionSource, "CustomThemePickerDialog")
                            ) {
                                ThemeContrast.entries.forEach { contrastOption ->
                                    DropdownMenuItem(
                                        text = { Text(contrastOption.name, modifier = Modifier.uiName("CustomThemePickerDialog.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "CustomThemePickerDialog")) },
                                        onClick = {
                                            contrast.value = contrastOption
                                            contrastExpanded = false
                                        }, modifier = Modifier.uiName("CustomThemePickerDialog.DropdownMenuItem.2", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "CustomThemePickerDialog")
                                    )
                                }
                            } }
                        },
                        readOnly = true,
                        label = { Text(stringResource(Res.string.headline_contrast), modifier = Modifier.uiName("CustomThemePickerDialog.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "CustomThemePickerDialog")) },
                        interactionSource =
                            remember { MutableInteractionSource() }
                                .apply {
                                    LaunchedEffect(Unit) {
                                        interactions
                                            .filter { it is PressInteraction.Release }
                                            .collectLatest { contrastExpanded = true }
                                    }
                                }, modifier = Modifier.uiName("CustomThemePickerDialog.TextField.2", "androidx.compose.material3.TextField", uiInspectionSource, "CustomThemePickerDialog")
                    )
                }
                Row(
                    modifier =
                        (Modifier.clickable { isAmoled.value = !isAmoled.value }
                            .semantics { role = Role.Checkbox }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()).uiName("CustomThemePickerDialog.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "CustomThemePickerDialog"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Checkbox(checked = isAmoled.value, onCheckedChange = null, modifier = Modifier.uiName("CustomThemePickerDialog.Checkbox.1", "androidx.compose.material3.Checkbox", uiInspectionSource, "CustomThemePickerDialog"))
                    Text(stringResource(Res.string.headline_amoled), modifier = Modifier.uiName("CustomThemePickerDialog.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "CustomThemePickerDialog"))
                }
            }
        }

    UiSourceScope(uiInspectionSource.element("CustomThemePickerDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "CustomThemePickerDialog")) { AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.headline_custom_palette), modifier = Modifier.uiName("CustomThemePickerDialog.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "CustomThemePickerDialog")) },
        text = content,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        Theme.Custom(
                            seedColor = controller.selectedColor.value.value,
                            style = paletteStyle.value,
                            contrast = contrast.value,
                            isAmoled = isAmoled.value,
                        )
                    )
                }, modifier = Modifier.uiName("CustomThemePickerDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "CustomThemePickerDialog")
            ) {
                Text(stringResource(Res.string.action_confirm), modifier = Modifier.uiName("CustomThemePickerDialog.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "CustomThemePickerDialog"))
            }
        },
        dismissButton = {
            TextButton(onDismiss, modifier = Modifier.uiName("CustomThemePickerDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "CustomThemePickerDialog")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("CustomThemePickerDialog.Text.10", "androidx.compose.material3.Text", uiInspectionSource, "CustomThemePickerDialog")) }
        },
        modifier = (modifier).uiName("CustomThemePickerDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "CustomThemePickerDialog"),
    ) }
}

@Composable
fun ColorPickerDialog(
    initialColor: Color?,
    onConfirm: (Color) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current

    val controller = rememberColorPickerController()
    LaunchedEffect(Unit) { initialColor?.let { controller.selectByColor(it, false) } }

    UiSourceScope(uiInspectionSource.element("ColorPickerDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "ColorPickerDialog")) { AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.headline_pick_a_color), modifier = Modifier.uiName("ColorPickerDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ColorPickerDialog")) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.uiName("ColorPickerDialog.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ColorPickerDialog")
            ) {
                HsvColorPicker(
                    modifier = (Modifier.fillMaxWidth().aspectRatio(1f)).uiName("ColorPickerDialog.HsvColorPicker.1", "com.github.skydoves.colorpicker.compose.HsvColorPicker", uiInspectionSource, "ColorPickerDialog"),
                    controller = controller,
                )
                BrightnessSlider(
                    modifier = (Modifier.fillMaxWidth().height(32.dp)).uiName("ColorPickerDialog.BrightnessSlider.1", "com.github.skydoves.colorpicker.compose.BrightnessSlider", uiInspectionSource, "ColorPickerDialog"),
                    controller = controller,
                )
                Text(
                    text = controller.selectedColor.value.toHex(),
                    color = controller.selectedColor.value,
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    modifier =
                        (Modifier.clickable(
                            interactionSource = null,
                            indication = null,
                            onClick = {
                                clipboardManager.copy(
                                    "Color",
                                    controller.selectedColor.value.toHex(),
                                )
                            },
                        )).uiName("ColorPickerDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ColorPickerDialog"),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(controller.selectedColor.value) }, modifier = Modifier.uiName("ColorPickerDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "ColorPickerDialog")) {
                Text(stringResource(Res.string.action_confirm), modifier = Modifier.uiName("ColorPickerDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ColorPickerDialog"))
            }
        },
        dismissButton = {
            TextButton(onDismissRequest, modifier = Modifier.uiName("ColorPickerDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "ColorPickerDialog")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("ColorPickerDialog.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "ColorPickerDialog")) }
        },
        modifier = (modifier).uiName("ColorPickerDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "ColorPickerDialog"),
    ) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.theme",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/theme/CustomThemePickerDialog.kt",
)
