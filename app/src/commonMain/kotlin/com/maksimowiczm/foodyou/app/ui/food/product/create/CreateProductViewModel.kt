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
    private var saving = false
    private val eventBus = Channel<CreateProductEvent>()
    val events = eventBus.receiveAsFlow()

    fun createProduct(form: ProductFormState) {
        if (saving) return
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

        // Freeze one submission before suspension; later form changes cannot alter the write.
        val name = form.name.value
        val brand = form.brand.value
        val barcode = form.barcode.value
        val note = form.note.value
        val isLiquid = form.isLiquid
        val packageWeight = form.packageWeight.value?.toDouble()
        val servingWeight = form.servingWeight.value?.toDouble()
        val source = ProductSource(type = if(form.sourceTypeLocked) ProductSource.Type.User else form.sourceType, url = form.sourceUrl.value)
        val nutritionFacts = form.nutritionFacts(multiplier)
        val history = NutritionalItemHistory.Created(dateProvider.nowInstant())
        saving = true
        form.saving = true
        viewModelScope.launch {
            try {
                createProductUseCase
                    .create(
                        name = name,
                        brand = brand,
                        barcode = barcode,
                        note = note,
                        isLiquid = isLiquid,
                        packageWeight = packageWeight,
                        servingWeight = servingWeight,
                        source = source,
                        nutritionFacts = nutritionFacts,
                        history = history,
                    )
                    .onSuccess { eventBus.send(CreateProductEvent.Created(it)) }
                    .onError {
                        form.saveFailed = true
                    }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                form.saveFailed = true
            } finally {
                saving = false
                form.saving = false
            }
        }
    }
}
