package com.maksimowiczm.foodyou.app.ui.food.recipe

import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.result.onError
import com.maksimowiczm.foodyou.common.result.onSuccess
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemHistory
import com.maksimowiczm.foodyou.food.domain.usecase.CreateRecipeUseCase
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class CreateRecipeViewModel(
    observeFoodUseCase: ObserveNutritionalItemUseCase,
    private val createRecipeUseCase: CreateRecipeUseCase,
    private val dateProvider: DateProvider,
) : RecipeViewModel(observeFoodUseCase) {

    val saving = kotlinx.coroutines.flow.MutableStateFlow(false)

    private val eventBus = Channel<CreateRecipeEvent>()
    val events = eventBus.receiveAsFlow()

    fun create(form: RecipeFormState) {
        if (!form.isValid || saving.value) {
            return
        }

        val draft = form.draft()
        saving.value = true
        viewModelScope.launch {
            try {
                createRecipeUseCase
                    .create(
                        name = draft.name,
                        servings = requireNotNull(draft.servings),
                        note = draft.note,
                        isLiquid = draft.isLiquid,
                        ingredients = draft.ingredients.map { it.intoPair() },
                        history = NutritionalItemHistory.Created(dateProvider.nowInstant()),
                    )
                    .onSuccess { eventBus.send(CreateRecipeEvent.Created(it)) }
                    .onError {
                        eventBus.send(CreateRecipeEvent.SaveFailed)
                    }
            } finally { saving.value = false }
        }
    }
}
