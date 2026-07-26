package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * iOS placeholder: AI photo capture is Android-only for Milestone 2, Story 6. This keeps the iOS
 * target compiling without shipping an unfinished camera flow.
 */
@Composable
actual fun AiScanCameraSection(
    jpeg: ByteArray?,
    onPhotoCaptured: (ByteArray) -> Unit,
    onDiscardRetry: () -> Unit,
    modifier: Modifier,
) {
    OutlinedCard(modifier = modifier.fillMaxWidth().height(160.dp)) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("AI scanning is only available on Android.")
        }
    }
}
