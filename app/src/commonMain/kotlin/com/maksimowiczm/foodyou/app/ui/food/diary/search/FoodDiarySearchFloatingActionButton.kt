package com.maksimowiczm.foodyou.app.ui.food.diary.search


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FoodDiarySearchFloatingActionButton(
    fabExpanded: Boolean,
    onFabExpandedChange: (Boolean) -> Unit,
    onCreateRecipe: () -> Unit,
    onCreateProduct: () -> Unit,
    modifier: Modifier = Modifier.Companion,
) {
    val colorScheme = MaterialTheme.colorScheme

    FloatingActionButtonMenu(
        expanded = fabExpanded,
        modifier = (modifier).uiName("FoodDiarySearchFloatingActionButton.FloatingActionButtonMenu.1", "androidx.compose.material3.FloatingActionButtonMenu", uiInspectionSource, "FoodDiarySearchFloatingActionButton"),
        button = {
            ToggleFloatingActionButton(
                checked = fabExpanded,
                onCheckedChange = onFabExpandedChange,
                containerColor = {
                    lerp(
                        start = colorScheme.secondaryContainer,
                        stop = colorScheme.secondary,
                        fraction = it,
                    )
                }, modifier = Modifier.uiName("FoodDiarySearchFloatingActionButton.ToggleFloatingActionButton.1", "androidx.compose.material3.ToggleFloatingActionButton", uiInspectionSource, "FoodDiarySearchFloatingActionButton")
            ) {
                val rotation by remember { derivedStateOf { checkedProgress * 45f } }

                val tintColor =
                    lerp(
                        start = MaterialTheme.colorScheme.onSecondaryContainer,
                        stop = MaterialTheme.colorScheme.onSecondary,
                        fraction = checkedProgress,
                    )

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription =
                        if (fabExpanded) {
                            stringResource(Res.string.action_close)
                        } else {
                            stringResource(Res.string.action_create)
                        },
                    tint = tintColor,
                    modifier = (Modifier.graphicsLayer { rotationZ = rotation }).uiName("FoodDiarySearchFloatingActionButton.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "FoodDiarySearchFloatingActionButton"),
                )
            }
        },
    ) {
        FloatingActionButtonMenuItem(
            modifier = (Modifier).uiName("FoodDiarySearchFloatingActionButton.FloatingActionButtonMenuItem.1", "androidx.compose.material3.FloatingActionButtonMenuItem", uiInspectionSource, "FoodDiarySearchFloatingActionButton"),
            onClick = {
                onCreateRecipe()
                onFabExpandedChange(false)
            },
            icon = { Icon(painterResource(Res.drawable.ic_skillet_filled), null, modifier = Modifier.uiName("FoodDiarySearchFloatingActionButton.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "FoodDiarySearchFloatingActionButton")) },
            text = { Text(stringResource(Res.string.headline_recipe), modifier = Modifier.uiName("FoodDiarySearchFloatingActionButton.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "FoodDiarySearchFloatingActionButton")) },
        )
        FloatingActionButtonMenuItem(
            modifier = (Modifier).uiName("FoodDiarySearchFloatingActionButton.FloatingActionButtonMenuItem.2", "androidx.compose.material3.FloatingActionButtonMenuItem", uiInspectionSource, "FoodDiarySearchFloatingActionButton"),
            onClick = {
                onCreateProduct()
                onFabExpandedChange(false)
            },
            icon = { Icon(Icons.Filled.LunchDining, null, modifier = Modifier.uiName("FoodDiarySearchFloatingActionButton.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "FoodDiarySearchFloatingActionButton")) },
            text = { Text(stringResource(Res.string.headline_product), modifier = Modifier.uiName("FoodDiarySearchFloatingActionButton.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "FoodDiarySearchFloatingActionButton")) },
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.search",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/search/FoodDiarySearchFloatingActionButton.kt",
)
