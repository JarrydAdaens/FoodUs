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

        /** An update check is in flight (HEAD request only, no download). */
        data object Checking : Phase

        /** The last check found the installed dataset current. */
        data object UpToDate : Phase

        /** The last check found a newer dataset; the user may download and replace. */
        data object UpdateAvailable : Phase

        data object Downloading : Phase

        data class Importing(val fraction: Float) : Phase

        data object Finished : Phase

        data class Error(val message: String?) : Phase
    }

    /** True while a network or database operation is running and controls must be disabled. */
    val busy: Boolean
        get() = phase is Phase.Checking || phase is Phase.Downloading || phase is Phase.Importing
}
