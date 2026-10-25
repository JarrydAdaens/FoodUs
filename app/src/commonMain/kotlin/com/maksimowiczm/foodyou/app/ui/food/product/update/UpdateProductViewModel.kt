package com.maksimowiczm.foodyou.app.ui.food.product.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.app.ui.food.product.ProductFormState
import com.maksimowiczm.foodyou.app.ui.food.product.nutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import com.maksimowiczm.foodyou.common.result.onError
import com.maksimowiczm.foodyou.common.result.onSuccess
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Product
import com.maksimowiczm.foodyou.food.domain.entity.ProductNutritionBasis
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.UpdateProductUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

internal class UpdateProductViewModel(
    observeFoodUseCase: ObserveNutritionalItemUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val productId: NutritionalItemId.Product,
) : ViewModel() {

    val product =
        observeFoodUseCase
            .observe(productId)
            .mapNotNull { it as Product }
            .stateIn(scope = viewModelScope, initialValue = null, started = WhileSubscribed(2_000))

    private val eventBus = Channel<UpdateProductEvent>()
    val events = eventBus.receiveAsFlow()

    fun updateProduct(form: ProductFormState) {
        form.saveFailed = false
        if (!form.isValid) {
            return
        }

        val multiplier = ProductNutritionBasis.multiplier(
            form.measurement, form.isLiquid,
            form.packageWeight.value?.toDouble(), form.servingWeight.value?.toDouble(),
        )

        if (multiplier == null) {
            form.saveFailed = true
            return
        }

        viewModelScope.launch {
            try {
                updateProductUseCase
                    .update(
                        id = productId,
                        name = form.name.value,
                        brand = form.brand.value,
                        barcode = form.barcode.value,
                        nutritionFacts = form.nutritionFacts(multiplier),
                        packageWeight = form.packageWeight.value?.toDouble(),
                        servingWeight = form.servingWeight.value?.toDouble(),
                        note = form.note.value,
                        source = ProductSource(type = form.sourceType, url = form.sourceUrl.value),
                        isLiquid = form.isLiquid,
                    )
                    .onSuccess { eventBus.send(UpdateProductEvent.Updated) }
                    .onError {
                        form.saveFailed = true
                    }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                form.saveFailed = true
            }
        }
    }
}
