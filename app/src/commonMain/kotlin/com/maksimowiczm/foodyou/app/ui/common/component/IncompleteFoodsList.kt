package com.maksimowiczm.foodyou.app.ui.common.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun IncompleteFoodsList(
    foods: List<String>,
    modifier: Modifier = Modifier,
    onFoodClick: ((index: Int) -> Unit)? = null,
) {
    val desc = "* " + stringResource(Res.string.description_incomplete_nutrition_data)

    Column((modifier).uiName("IncompleteFoodsList.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "IncompleteFoodsList")) {
        Text(
            text = desc,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.outline, modifier = Modifier.uiName("IncompleteFoodsList.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "IncompleteFoodsList")
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.headline_incomplete_products),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline, modifier = Modifier.uiName("IncompleteFoodsList.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "IncompleteFoodsList")
        )
        foods.forEachIndexed { i, food ->
            Text(
                text = food,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier =
                    (if (onFoodClick == null) Modifier
                    else
                        Modifier.clickable(
                            interactionSource = null,
                            indication = null,
                            onClick = { onFoodClick(i) },
                        )).uiName("IncompleteFoodsList.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "IncompleteFoodsList"),
            )
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/IncompleteFoodsList.kt",
)
