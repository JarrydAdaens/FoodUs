package com.maksimowiczm.foodyou.app.ui.food.product

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.maksimowiczm.foodyou.app.ui.food.product.create.CreateProductApp
import com.maksimowiczm.foodyou.app.ui.food.product.create.CreateProductEvent
import com.maksimowiczm.foodyou.app.ui.food.product.create.CreateProductViewModel
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import com.maksimowiczm.foodyou.food.domain.entity.Product
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateProductScreen(
    onBack: () -> Unit,
    onCreate: (FoodId.Product) -> Unit,
    onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    modifier: Modifier = Modifier,
    url: String? = null,
    // Story 19 promotion prefill. When set, the editor opens seeded from a Quick Add estimate. The
    // product is committed only when the editor is saved (spec §6.2).
    prefillProduct: Product? = null,
) {
    val viewModel: CreateProductViewModel = koinViewModel()

    val latestOnCreate by rememberUpdatedState(onCreate)
    LaunchedCollectWithLifecycle(viewModel.events) { event ->
        when (event) {
            is CreateProductEvent.Created -> latestOnCreate(event.productId)
        }
    }

    CreateProductApp(
        onBack = onBack,
        onCreate = viewModel::createProduct,
        onUpdateUsdaApiKey = onUpdateUsdaApiKey,
        onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials,
        modifier = modifier,
        url = url,
        prefillProduct = prefillProduct,
    )
}
