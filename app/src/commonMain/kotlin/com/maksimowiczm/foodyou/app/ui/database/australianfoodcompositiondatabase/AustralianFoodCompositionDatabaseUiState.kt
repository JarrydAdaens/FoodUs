package com.maksimowiczm.foodyou.app.ui.database.australianfoodcompositiondatabase

import androidx.compose.runtime.Immutable
import com.maksimowiczm.foodyou.importexport.providermetadata.domain.ProviderMetadata

@Immutable
internal data class AustralianFoodCompositionDatabaseUiState(
    val enabled: Boolean = false,
    val metadata: ProviderMetadata? = null,
    val phase: Phase = Phase.Idle,
) {
    sealed interface Phase {
        data object Idle : Phase

        data object Downloading : Phase

        data class Importing(val fraction: Float) : Phase

        data object Finished : Phase

        data class Error(val message: String?) : Phase
    }
}
