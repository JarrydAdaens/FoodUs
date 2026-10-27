package com.maksimowiczm.foodyou.app.ui.common.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.theme.LocalNutrientsPalette
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalNutrientsOrder
import com.maksimowiczm.foodyou.settings.domain.entity.NutrientsOrder
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FoodListItem(
    name: @Composable () -> Unit,
    proteins: @Composable () -> Unit,
    carbohydrates: @Composable () -> Unit,
    fats: @Composable () -> Unit,
    calories: @Composable () -> Unit,
    measurement: @Composable () -> Unit,
    isRecipe: Boolean,
    alignNutritionColumns: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    containerColor: Color = Color.Transparent,
    contentColor: Color = LocalContentColor.current,
    shape: Shape = RectangleShape,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
) {
    val nutrientsPalette = LocalNutrientsPalette.current
    val order = LocalNutrientsOrder.current

    val headlineContent =
        @Composable {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleMediumEmphasized
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier.uiName("FoodListItem.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "FoodListItem")
                ) {
                    name()
                    if (isRecipe) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_skillet_filled),
                            contentDescription = stringResource(Res.string.headline_recipe),
                            modifier = (Modifier.size(18.dp)).uiName("FoodListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "FoodListItem"),
                        )
                    }
                }
            }
        }

    val supportingContent =
        @Composable {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                Column(modifier = (Modifier.fillMaxWidth()).uiName("FoodListItem.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "FoodListItem")) {
                    if (alignNutritionColumns) {
                        NutritionColumns(calories = calories,
                            proteins = { CompositionLocalProvider(LocalContentColor provides nutrientsPalette.proteinsOnSurfaceContainer) { proteins() } },
                            carbohydrates = { CompositionLocalProvider(LocalContentColor provides nutrientsPalette.carbohydratesOnSurfaceContainer) { carbohydrates() } },
                            fats = { CompositionLocalProvider(LocalContentColor provides nutrientsPalette.fatsOnSurfaceContainer) { fats() } }, modifier = Modifier.uiName("FoodListItem.NutritionColumns.phone1", "com.maksimowiczm.foodyou.app.ui.common.component.NutritionColumns", uiInspectionSource, "FoodListItem"))
                    } else Row(
                        modifier = (Modifier.fillMaxWidth()).uiName("FoodListItem.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "FoodListItem"),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CompositionLocalProvider(
                            LocalTextStyle provides MaterialTheme.typography.bodyMedium
                        ) {
                            calories()
                        }

                        order.forEach { field ->
                            when (field) {
                                NutrientsOrder.Proteins ->
                                    CompositionLocalProvider(
                                        LocalContentColor provides
                                            nutrientsPalette.proteinsOnSurfaceContainer,
                                        LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                                    ) {
                                        proteins()
                                    }

                                NutrientsOrder.Fats ->
                                    CompositionLocalProvider(
                                        LocalContentColor provides
                                            nutrientsPalette.fatsOnSurfaceContainer,
                                        LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                                    ) {
                                        fats()
                                    }

                                NutrientsOrder.Carbohydrates ->
                                    CompositionLocalProvider(
                                        LocalContentColor provides
                                            nutrientsPalette.carbohydratesOnSurfaceContainer,
                                        LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                                    ) {
                                        carbohydrates()
                                    }

                                NutrientsOrder.Other,
                                NutrientsOrder.Vitamins,
                                NutrientsOrder.Minerals -> Unit
                            }
                        }
                    }
                }
            }
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                measurement()
            }
        }

    val content =
        @Composable {
            Row(
                modifier = (Modifier.padding(contentPadding)).uiName("FoodListItem.Row.3", "androidx.compose.foundation.layout.Row", uiInspectionSource, "FoodListItem"),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = (Modifier.weight(1f)).uiName("FoodListItem.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "FoodListItem"),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    headlineContent()
                    supportingContent()
                }

                trailingContent?.invoke()
            }
        }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = (modifier).uiName("FoodListItem.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "FoodListItem"),
            color = containerColor,
            contentColor = contentColor,
            shape = shape,
            content = content,
        )
    } else {
        Surface(
            modifier = (modifier).uiName("FoodListItem.Surface.2", "androidx.compose.material3.Surface", uiInspectionSource, "FoodListItem"),
            color = containerColor,
            contentColor = contentColor,
            shape = shape,
            content = content,
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/FoodListItem.kt",
)
