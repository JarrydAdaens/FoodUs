package com.maksimowiczm.foodyou.app.ui.food.recipe

import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.result.onError
import com.maksimowiczm.foodyou.common.result.onSuccess
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.UpdateRecipeUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class UpdateRecipeViewModel(
    private val foodId: NutritionalItemId.Recipe,
    observeFoodUseCase: ObserveNutritionalItemUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
) : RecipeViewModel(observeFoodUseCase) {

    val loading = kotlinx.coroutines.flow.MutableStateFlow(true)
    private val refresh = kotlinx.coroutines.flow.MutableStateFlow(0)
    fun retryRead() { refresh.value++ }
    val recipe = refresh.flatMapLatest {
        kotlinx.coroutines.flow.flow { emitAll(observeFoodUseCase.observe(foodId)) }
            .map { it as? Recipe }
            .onEach { loading.value = false }
            .onStart { loading.value = true }
            .catch { loading.value = false; emit(null) }
    }.stateIn(scope = viewModelScope, initialValue = null, started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(2_000))

    val saving = kotlinx.coroutines.flow.MutableStateFlow(false)

    private val eventBus = Channel<UpdateRecipeEvent>()
    val events = eventBus.receiveAsFlow()

    fun update(form: RecipeFormState) {
        if (!form.isValid || saving.value) {
            return
        }

        if (!form.isModified) {
            viewModelScope.launch { eventBus.send(UpdateRecipeEvent.Updated) }
            return
        }

        val draft = form.draft()
        saving.value = true
        viewModelScope.launch {
            try {
                updateRecipeUseCase
                    .update(
                        id = foodId,
                        name = draft.name,
                        servings = requireNotNull(draft.servings),
                        note = draft.note,
                        isLiquid = draft.isLiquid,
                        ingredients = draft.ingredients.map { it.intoPair() },
                    )
                    .onSuccess { eventBus.send(UpdateRecipeEvent.Updated) }
                    .onError {
                        eventBus.send(UpdateRecipeEvent.SaveFailed)
                    }
            } finally { saving.value = false }
        }
    }
}
