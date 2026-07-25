package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.result.onError
import com.maksimowiczm.foodyou.common.result.onSuccess
import com.maksimowiczm.foodyou.food.domain.entity.FoodHistory
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import com.maksimowiczm.foodyou.food.domain.usecase.CreateProductUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.DeleteFoodUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns the backing "placeholder" product for a Promote-to-Recipe flow (Story 19).
 *
 * Food You recipes derive their nutrition entirely from ingredients (Story 14 §10.6), so a Quick Add
 * estimate is seeded into a recipe as a single placeholder ingredient — a real custom [Product]
 * carrying the estimate as per-100 g nutrition. That product must exist before the recipe editor can
 * display and reference it, so it is created up front here.
 *
 * Creating nothing on cancel (spec §6.6) is preserved by [discardPlaceholderIfUncommitted]: if the
 * recipe editor is dismissed without saving, the placeholder is deleted. Deletion runs on the
 * application scope so it completes even though popping the destination clears this view model. The
 * created id is kept in [SavedStateHandle] so process death does not create a second placeholder.
 */
internal class PromoteToRecipePlaceholderViewModel(
    private val seed: QuickAddPromotionSeed,
    private val createProductUseCase: CreateProductUseCase,
    private val deleteFoodUseCase: DeleteFoodUseCase,
    private val dateProvider: DateProvider,
    private val applicationScope: CoroutineScope,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productIdState = MutableStateFlow(savedStateHandle.get<Long>(KEY))
    val productId = productIdState.asStateFlow()

    private var settled = false

    init {
        if (productIdState.value == null) {
            val product = seed.toProductPrefill()
            viewModelScope.launch {
                createProductUseCase
                    .create(
                        name = product.name,
                        brand = product.brand,
                        barcode = product.barcode,
                        note = product.note,
                        isLiquid = product.isLiquid,
                        packageWeight = product.packageWeight,
                        servingWeight = product.servingWeight,
                        source = product.source,
                        nutritionFacts = product.nutritionFacts,
                        history = FoodHistory.Created(dateProvider.nowInstant()),
                    )
                    .onSuccess {
                        savedStateHandle[KEY] = it.id
                        productIdState.value = it.id
                    }
                    .onError { error("Failed to create recipe placeholder product: $it") }
            }
        }
    }

    /** Marks the placeholder as kept once the recipe that references it has been created. */
    fun markCommitted() {
        settled = true
    }

    /** Deletes the placeholder product unless the recipe was saved (spec §6.6). Idempotent. */
    fun discardPlaceholderIfUncommitted() {
        if (settled) {
            return
        }
        settled = true

        val id = productIdState.value ?: return
        applicationScope.launch { deleteFoodUseCase.delete(FoodId.Product(id)) }
    }

    private companion object {
        const val KEY = "placeholderProductId"
    }
}
