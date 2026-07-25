package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimate

/** UI state for the AI scanning screen (Milestone 2, Story 6). */
internal sealed interface AiScanUiState {

    /** The captured photo, or null before anything has been taken. */
    val jpeg: ByteArray?

    /** No photo yet: show the capture button. */
    data object NoPhoto : AiScanUiState {
        override val jpeg: ByteArray? = null
    }

    /** A photo is ready to send: show the preview and the Ask AI button. */
    data class Captured(override val jpeg: ByteArray) : AiScanUiState

    /** The photo is being analysed. */
    data class Scanning(override val jpeg: ByteArray) : AiScanUiState

    /** The AI returned an estimate: show the result and the save/align actions. */
    data class Result(override val jpeg: ByteArray, val estimate: AiFoodEstimate) : AiScanUiState

    /** The request failed; [message] is safe to display. */
    data class Failed(override val jpeg: ByteArray, val message: String) : AiScanUiState
}
