package com.maksimowiczm.foodyou.app.ui.food.product.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.app.ui.food.product.ProductFormState
import com.maksimowiczm.foodyou.app.ui.food.product.nutritionFacts
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import com.maksimowiczm.foodyou.common.result.onError
import com.maksimowiczm.foodyou.common.result.onSuccess
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemHistory
import com.maksimowiczm.foodyou.food.domain.entity.ProductNutritionBasis
import com.maksimowiczm.foodyou.food.domain.usecase.CreateProductUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

internal class CreateProductViewModel(
    private val createProductUseCase: CreateProductUseCase,
    private val dateProvider: DateProvider,
) : ViewModel() {
    private val eventBus = Channel<CreateProductEvent>()
    val events = eventBus.receiveAsFlow()

    fun createProduct(form: ProductFormState) {
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
                createProductUseCase
                    .create(
                        name = form.name.value,
                        brand = form.brand.value,
                        barcode = form.barcode.value,
                        note = form.note.value,
                        isLiquid = form.isLiquid,
                        packageWeight = form.packageWeight.value?.toDouble(),
                        servingWeight = form.servingWeight.value?.toDouble(),
                        source = ProductSource(type = form.sourceType, url = form.sourceUrl.value),
                        nutritionFacts = form.nutritionFacts(multiplier),
                        history = NutritionalItemHistory.Created(dateProvider.nowInstant()),
                    )
                    .onSuccess { eventBus.send(CreateProductEvent.Created(it)) }
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
