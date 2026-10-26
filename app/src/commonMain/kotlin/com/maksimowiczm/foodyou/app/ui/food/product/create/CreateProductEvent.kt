package com.maksimowiczm.foodyou.app.ui.food.product.create

import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId

internal sealed interface CreateProductEvent {

    data class Created(val productId: NutritionalItemId.Product) : CreateProductEvent
}
