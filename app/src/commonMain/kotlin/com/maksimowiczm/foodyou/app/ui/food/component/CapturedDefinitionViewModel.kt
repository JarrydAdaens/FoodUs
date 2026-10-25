package com.maksimowiczm.foodyou.app.ui.food.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.food.domain.entity.Product
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import com.maksimowiczm.foodyou.food.domain.usecase.UpdateCapturedDefinitionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class CapturedDefinitionViewModel(private val update:UpdateCapturedDefinitionUseCase):ViewModel() {
    val saving=MutableStateFlow(false)
    private val bus=Channel<Boolean>()
    val results=bus.receiveAsFlow()
    fun product(candidate:Product,confirmed:Boolean)=save { update.product(candidate,confirmed) }
    fun recipe(candidate:Recipe,confirmed:Boolean)=save { update.recipe(candidate,confirmed) }
    private fun save(block:suspend()->Boolean) {
        if(saving.value)return
        saving.value=true
        viewModelScope.launch { try { bus.send(block()) }finally { saving.value=false } }
    }
}
