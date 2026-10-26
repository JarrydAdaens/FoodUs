package com.maksimowiczm.foodyou.app.ui.home.meals.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlotsCardsLayout
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LayoutPicker(
    layout: DiarySlotsCardsLayout,
    onLayoutChange: (DiarySlotsCardsLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = (modifier).uiName("LayoutPicker.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "LayoutPicker"), horizontalArrangement = Arrangement.SpaceEvenly) {
        LayoutContainer(
            selected = layout == DiarySlotsCardsLayout.Horizontal,
            onClick = { onLayoutChange(DiarySlotsCardsLayout.Horizontal) },
            layout = { LayoutHorizontal( modifier = Modifier.uiName("LayoutPicker.LayoutHorizontal.1", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.LayoutHorizontal", uiInspectionSource, "LayoutPicker")) },
            label = {
                Text(
                    text = stringResource(Res.string.headline_horizontal),
                    style = MaterialTheme.typography.labelLarge, modifier = Modifier.uiName("LayoutPicker.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "LayoutPicker")
                )
            }, modifier = Modifier.uiName("LayoutPicker.LayoutContainer.1", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.LayoutContainer", uiInspectionSource, "LayoutPicker")
        )

        LayoutContainer(
            selected = layout == DiarySlotsCardsLayout.Vertical,
            onClick = { onLayoutChange(DiarySlotsCardsLayout.Vertical) },
            layout = { LayoutVertical( modifier = Modifier.uiName("LayoutPicker.LayoutVertical.1", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.LayoutVertical", uiInspectionSource, "LayoutPicker")) },
            label = {
                Text(
                    text = stringResource(Res.string.headline_vertical),
                    style = MaterialTheme.typography.labelLarge, modifier = Modifier.uiName("LayoutPicker.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "LayoutPicker")
                )
            }, modifier = Modifier.uiName("LayoutPicker.LayoutContainer.2", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.LayoutContainer", uiInspectionSource, "LayoutPicker")
        )
    }
}

@Composable
private fun LayoutContainer(
    selected: Boolean,
    onClick: () -> Unit,
    layout: @Composable () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
            animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        )
    val contentColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        )

    val animatedCornerSize by
        animateDpAsState(
            targetValue = if (selected) 28.dp else 12.dp,
            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        )

    Box(modifier = (modifier).uiName("LayoutContainer.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "LayoutContainer")) {
        Column(
            modifier =
                (Modifier.graphicsLayer {
                        clip = true
                        shape = RoundedCornerShape(animatedCornerSize)
                    }
                    .drawBehind { drawRect(containerColor) }
                    .clickable { onClick() }
                    .padding(16.dp)).uiName("LayoutContainer.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "LayoutContainer"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                layout()
                label()
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.meals.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/meals/settings/LayoutPicker.kt",
)
