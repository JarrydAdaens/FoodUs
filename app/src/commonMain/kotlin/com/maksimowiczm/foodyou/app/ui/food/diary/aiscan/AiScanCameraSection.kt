package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform capture-and-preview surface for AI scanning (Milestone 2, Story 6).
 *
 * When [jpeg] is null it shows a large button that launches the camera and emits a downscaled JPEG
 * through [onPhotoCaptured]. When [jpeg] is present it shows the photo; tapping it asks the user to
 * discard and retry via [onDiscardRetry]. Camera capture is Android-only; other platforms show an
 * unsupported message.
 */
@Composable
expect fun AiScanCameraSection(
    jpeg: ByteArray?,
    onPhotoCaptured: (ByteArray) -> Unit,
    onDiscardRetry: () -> Unit,
    modifier: Modifier = Modifier,
)
