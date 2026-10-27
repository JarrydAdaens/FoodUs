package com.maksimowiczm.foodyou.app.ui.food.product


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.maksimowiczm.foodyou.app.ui.food.product.update.UpdateProductScreen
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun UpdateProductScreen(
    onBack: () -> Unit,
    onUpdate: () -> Unit,
    productId: NutritionalItemId.Product,
    modifier: Modifier = Modifier,
    personalCollection: Boolean = false,
) {
    UiSourceScope(uiInspectionSource.element("app.ui.food.product.UpdateProductScreen.UpdateProductScreen.1", "com.maksimowiczm.foodyou.app.ui.food.product.update.UpdateProductScreen", "UpdateProductScreen")) { UiSourceScope(uiInspectionSource.element("app.ui.food.product.UpdateProductScreen.UpdateProductScreen.1.declaration2", "com.maksimowiczm.foodyou.app.ui.food.product.UpdateProductScreen", "UpdateProductScreen")) { UpdateProductScreen(
        onBack = onBack,
        onUpdate = onUpdate, personalCollection = personalCollection,
        viewModel = koinViewModel(parameters = { parametersOf(productId) }),
        modifier = modifier,
    ) } }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.product",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/product/UpdateProductScreen.kt",
)
