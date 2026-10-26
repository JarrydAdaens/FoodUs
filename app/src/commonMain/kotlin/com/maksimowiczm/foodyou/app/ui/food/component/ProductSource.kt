package com.maksimowiczm.foodyou.app.ui.food.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProductSource.Type.Icon(modifier: Modifier = Modifier) {
    when (this) {
        ProductSource.Type.User ->
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                modifier = (modifier.size(24.dp)).uiName("Icon.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "Icon"),
            )

        ProductSource.Type.OpenFoodFacts ->
            Image(
                painter = painterResource(Res.drawable.openfoodfacts_logo),
                contentDescription = null,
                modifier = (modifier.size(24.dp)).uiName("app.ui.food.component.Icon.Image.1", "androidx.compose.foundation.Image", uiInspectionSource, "Icon"),
            )

        ProductSource.Type.USDA ->
            Image(
                painter = painterResource(Res.drawable.usda_logo),
                contentDescription = null,
                modifier = (modifier.size(24.dp)).uiName("app.ui.food.component.Icon.Image.2", "androidx.compose.foundation.Image", uiInspectionSource, "Icon"),
            )

        ProductSource.Type.SwissFoodCompositionDatabase -> Text("CH", (modifier).uiName("Icon.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Icon"))

        ProductSource.Type.AustralianFoodCompositionDatabase -> Text("AU", (modifier).uiName("Icon.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Icon"))
    }
}

@Composable
fun ProductSource.Type.stringResource(): String =
    when (this) {
        ProductSource.Type.User -> stringResource(Res.string.headline_user)
        ProductSource.Type.OpenFoodFacts -> stringResource(Res.string.headline_open_food_facts)
        ProductSource.Type.USDA -> stringResource(Res.string.headline_food_data_central_usda)
        ProductSource.Type.SwissFoodCompositionDatabase ->
            stringResource(Res.string.headline_swiss_food_composition_database)
        ProductSource.Type.AustralianFoodCompositionDatabase ->
            stringResource(Res.string.headline_australian_food_composition_database)
    }


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/component/ProductSource.kt",
)
