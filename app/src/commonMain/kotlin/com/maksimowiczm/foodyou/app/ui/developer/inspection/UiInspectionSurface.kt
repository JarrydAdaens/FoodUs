package com.maksimowiczm.foodyou.app.ui.developer.inspection

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Names the content root of window APIs (Dialog/Popup) which do not accept a modifier. */
@Composable
internal fun UiInspectionSurface(descriptor: UiSourceDescriptor, content: @Composable () -> Unit) {
    Box(modifier = Modifier.uiName(descriptor), propagateMinConstraints = true) { content() }
}
