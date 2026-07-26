package com.maksimowiczm.foodyou.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.app.ui.common.utility.EnergyFormatter
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.settings.domain.entity.EnergyFormat
import com.maksimowiczm.foodyou.settings.domain.entity.GraphStyle
import com.maksimowiczm.foodyou.settings.domain.entity.NutrientsOrder
import com.maksimowiczm.foodyou.settings.domain.entity.Settings
import com.maksimowiczm.foodyou.settings.domain.entity.WeekLayout
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class AppViewModel(private val settingsRepository: UserPreferencesRepository<Settings>) :
    ViewModel() {

    private val settings = settingsRepository.observe()

    val nutrientsOrder =
        settings
            .map { it.nutrientsOrder }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = NutrientsOrder.defaultOrder,
            )

    val onboardingFinished =
        settings
            .map { it.onboardingFinished }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = runBlocking { settings.map { it.onboardingFinished }.first() },
            )

    val energyFormatter =
        settings
            .map { it.energyFormat.toFormatter() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = EnergyFormat.DEFAULT.toFormatter(),
            )

    val graphStyle =
        settings
            .map { it.graphStyle }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = GraphStyle.DEFAULT,
            )

    val weekLayout =
        settings
            .map { it.weekLayout }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = WeekLayout.DEFAULT,
            )

    fun finishOnboarding() {
        viewModelScope.launch { settingsRepository.update { copy(onboardingFinished = true) } }
    }
}

private fun EnergyFormat.toFormatter(): EnergyFormatter =
    when (this) {
        EnergyFormat.Kilocalories -> EnergyFormatter.kilocalories
        EnergyFormat.Kilojoules -> EnergyFormatter.kilojoules
    }
