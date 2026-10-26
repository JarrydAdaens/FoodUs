package com.maksimowiczm.foodyou.app.ui.theme


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.theme.Theme
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

@Composable
fun PalettePicker(
    isDark: Boolean,
    selectedTheme: Theme,
    onThemeChange: (Theme.Custom) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = listOf<Theme.Custom?>(null) + rememberThemes()
    val chunks = remember(colors) { colors.chunked(5) }
    val initialPage = remember {
        chunks
            .indexOfFirst { chunk -> chunk.contains(selectedTheme as? Theme.Custom) }
            .coerceAtLeast(0)
    }
    val isPresetSelected = colors.contains(selectedTheme as? Theme.Custom)

    val pagerState = rememberPagerState(initialPage = initialPage) { chunks.size }
    val coroutineScope = rememberCoroutineScope()

    var showCustomColorDialog by rememberSaveable { mutableStateOf(false) }
    if (showCustomColorDialog) {
        CustomThemePickerDialog(
            initialTheme = selectedTheme as? Theme.Custom,
            onConfirm = {
                onThemeChange(it)
                showCustomColorDialog = false
            },
            onDismiss = { showCustomColorDialog = false }, modifier = Modifier.uiName("PalettePicker.CustomThemePickerDialog.1", "com.maksimowiczm.foodyou.app.ui.theme.CustomThemePickerDialog", uiInspectionSource, "PalettePicker")
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = (modifier).uiName("PalettePicker.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "PalettePicker"),
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.uiName("PalettePicker.HorizontalPager.1", "androidx.compose.foundation.pager.HorizontalPager", uiInspectionSource, "PalettePicker")) { page ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = (Modifier.fillMaxWidth().padding(horizontal = 16.dp)).uiName("PalettePicker.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "PalettePicker"),
            ) {
                chunks[page].forEach { theme ->
                    if (theme == null) {
                        PalettePickerItem(
                            selected = !isPresetSelected,
                            onSelect = { showCustomColorDialog = true }, modifier = Modifier.uiName("PalettePicker.PalettePickerItem.1", "com.maksimowiczm.foodyou.app.ui.theme.PalettePickerItem", uiInspectionSource, "PalettePicker")
                        )
                    } else {
                        PalettePickerItem(
                            colorScheme = theme.rememberColorScheme(isDark),
                            selected = theme == selectedTheme,
                            onSelect = { onThemeChange(theme) }, modifier = Modifier.uiName("PalettePicker.PalettePickerItem.2", "com.maksimowiczm.foodyou.app.ui.theme.PalettePickerItem", uiInspectionSource, "PalettePicker")
                        )
                    }
                }
            }
        }
        PagerIndicator(
            currentPage = pagerState.currentPage + pagerState.currentPageOffsetFraction,
            pageCount = chunks.size,
            onRequestPage = { page ->
                coroutineScope.launch { pagerState.animateScrollToPage(page) }
            }, modifier = Modifier.uiName("PalettePicker.PagerIndicator.1", "com.maksimowiczm.foodyou.app.ui.theme.PagerIndicator", uiInspectionSource, "PalettePicker")
        )
    }
}

@Composable
private fun PalettePickerItem(
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToggleButton(
        checked = selected,
        onCheckedChange = { onSelect() },
        modifier = (modifier.size(56.dp).semantics { role = Role.RadioButton }).uiName("PalettePickerItem.ToggleButton.1", "androidx.compose.material3.ToggleButton", uiInspectionSource, "PalettePickerItem"),
        shapes = ToggleButtonDefaults.shapesFor(56.dp),
        colors =
            ToggleButtonDefaults.toggleButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                checkedContainerColor = MaterialTheme.colorScheme.inversePrimary,
                checkedContentColor = MaterialTheme.colorScheme.primary,
            ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.uiName("PalettePickerItem.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "PalettePickerItem")) {
            Icon(
                imageVector = Icons.Outlined.Colorize,
                contentDescription = null,
                modifier = (Modifier.size(20.dp)).uiName("PalettePickerItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "PalettePickerItem"),
            )
        }
    }
}

@Composable
private fun PalettePickerItem(
    colorScheme: ColorScheme,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToggleButton(
        checked = selected,
        onCheckedChange = { onSelect() },
        modifier = (modifier.size(56.dp).semantics { role = Role.RadioButton }).uiName("PalettePickerItem.ToggleButton.2", "androidx.compose.material3.ToggleButton", uiInspectionSource, "PalettePickerItem"),
        shapes = ToggleButtonDefaults.shapesFor(56.dp),
        colors =
            ToggleButtonDefaults.toggleButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                checkedContainerColor = MaterialTheme.colorScheme.inversePrimary,
            ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.uiName("PalettePickerItem.Box.2", "androidx.compose.foundation.layout.Box", uiInspectionSource, "PalettePickerItem")) {
            PaletteColors(
                colorScheme = colorScheme,
                modifier = (Modifier.size(40.dp).clip(CircleShape)).uiName("PalettePickerItem.PaletteColors.1", "com.maksimowiczm.foodyou.app.ui.theme.PaletteColors", uiInspectionSource, "PalettePickerItem"),
            )
            if (selected) {
                Surface(
                    color = MaterialTheme.colorScheme.inversePrimary,
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape, modifier = Modifier.uiName("PalettePickerItem.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "PalettePickerItem")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = (Modifier.size(20.dp)).uiName("PalettePickerItem.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "PalettePickerItem"),
                    )
                }
            }
        }
    }
}

@Composable
private fun PagerIndicator(
    currentPage: Float,
    pageCount: Int,
    onRequestPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(modifier = (modifier).uiName("PagerIndicator.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "PagerIndicator"), verticalAlignment = Alignment.CenterVertically) {
        repeat(pageCount) { page ->
            val progress = 1f - (currentPage - page).coerceIn(-1f, 1f).absoluteValue

            Box(
                modifier =
                    (Modifier.padding(3.dp)
                        .clickable { onRequestPage(page) }
                        .graphicsLayer {
                            val scale = 1f + progress * 0.25f
                            scaleX = scale
                            scaleY = scale
                        }
                        .drawBehind {
                            val color =
                                lerp(colorScheme.secondaryContainer, colorScheme.primary, progress)
                            drawCircle(color = color)
                        }
                        .size(6.dp)
                        .clip(CircleShape)).uiName("PagerIndicator.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "PagerIndicator")
            )
        }
    }
}

@Composable
private fun PaletteColors(colorScheme: ColorScheme, modifier: Modifier = Modifier.Companion) {
    Canvas((modifier).uiName("PaletteColors.Canvas.1", "androidx.compose.foundation.Canvas", uiInspectionSource, "PaletteColors")) {
        // Convert to integer pixels to avoid sub-pixel gaps
        val fullWidth = size.width.toInt()
        val fullHeight = size.height.toInt()
        val halfWidth = fullWidth / 2
        val halfHeight = fullHeight / 2

        drawRect(
            color = colorScheme.primary,
            size = Size(fullWidth.toFloat(), halfHeight.toFloat()),
            topLeft = Offset(0f, 0f),
        )
        drawRect(
            color = colorScheme.secondary,
            size = Size(halfWidth.toFloat(), (fullHeight - halfHeight).toFloat()),
            topLeft = Offset(0f, halfHeight.toFloat()),
        )
        drawRect(
            color = colorScheme.tertiary,
            size = Size((fullWidth - halfWidth).toFloat(), (fullHeight - halfHeight).toFloat()),
            topLeft = Offset(halfWidth.toFloat(), halfHeight.toFloat()),
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.theme",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/theme/PalettePicker.kt",
)
