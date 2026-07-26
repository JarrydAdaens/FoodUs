package com.maksimowiczm.foodyou.app.ui.database.australianfoodcompositiondatabase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.food.FoodSource
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.food.search.domain.FoodSearchPreferences
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AfcdImportProgress
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AfcdUpdateCheckOutcome
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.CheckAustralianFoodCompositionDatabaseUpdateUseCase
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.ImportAustralianFoodCompositionDatabaseUseCase
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.ProviderMetadataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class AustralianFoodCompositionDatabaseViewModel(
    private val importUseCase: ImportAustralianFoodCompositionDatabaseUseCase,
    private val checkUpdateUseCase: CheckAustralianFoodCompositionDatabaseUpdateUseCase,
    private val preferencesRepository: UserPreferencesRepository<FoodSearchPreferences>,
    metadataRepository: ProviderMetadataRepository,
) : ViewModel() {

    private val phase =
        MutableStateFlow<AustralianFoodCompositionDatabaseUiState.Phase>(
            AustralianFoodCompositionDatabaseUiState.Phase.Idle
        )
    private val mutex = Mutex()

    val uiState =
        combine(
                preferencesRepository.observe(),
                metadataRepository.observe(FoodSource.Type.AustralianFoodCompositionDatabase),
                phase,
            ) { prefs, metadata, phase ->
                AustralianFoodCompositionDatabaseUiState(
                    enabled = prefs.isAustralianFoodCompositionDatabaseEnabled,
                    metadata = metadata,
                    phase = phase,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = AustralianFoodCompositionDatabaseUiState(),
            )

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.update {
                copy(
                    australianFoodCompositionDatabase =
                        australianFoodCompositionDatabase.copy(enabled = enabled)
                )
            }
        }
    }

    fun checkForUpdates() {
        if (mutex.isLocked) return
        viewModelScope.launch {
            mutex.withLock {
                phase.value = AustralianFoodCompositionDatabaseUiState.Phase.Checking
                phase.value =
                    when (val outcome = checkUpdateUseCase.check()) {
                        AfcdUpdateCheckOutcome.UpToDate ->
                            AustralianFoodCompositionDatabaseUiState.Phase.UpToDate

                        AfcdUpdateCheckOutcome.UpdateAvailable ->
                            AustralianFoodCompositionDatabaseUiState.Phase.UpdateAvailable

                        is AfcdUpdateCheckOutcome.Failure ->
                            AustralianFoodCompositionDatabaseUiState.Phase.Error(outcome.message)
                    }
            }
        }
    }

    fun import() {
        if (mutex.isLocked) return
        viewModelScope.launch {
            mutex.withLock {
                importUseCase
                    .import()
                    .catch { error ->
                        phase.value =
                            AustralianFoodCompositionDatabaseUiState.Phase.Error(error.message)
                    }
                    .collect { progress ->
                        phase.update {
                            when (progress) {
                                AfcdImportProgress.Downloading ->
                                    AustralianFoodCompositionDatabaseUiState.Phase.Downloading

                                is AfcdImportProgress.Importing ->
                                    AustralianFoodCompositionDatabaseUiState.Phase.Importing(
                                        progress.fraction
                                    )

                                AfcdImportProgress.Finished ->
                                    AustralianFoodCompositionDatabaseUiState.Phase.Finished
                            }
                        }
                    }
                // On success, enable the provider so its foods participate in search immediately.
                if (phase.value is AustralianFoodCompositionDatabaseUiState.Phase.Finished) {
                    preferencesRepository.update {
                        copy(
                            australianFoodCompositionDatabase =
                                australianFoodCompositionDatabase.copy(enabled = true)
                        )
                    }
                }
            }
        }
    }
}
