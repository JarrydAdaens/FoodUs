package com.maksimowiczm.foodyou.app.ui.developer.inspection

import androidx.compose.runtime.*

internal val LocalUiSourceAncestry = staticCompositionLocalOf<List<UiSourceDescriptor>> { emptyList() }

/** Logical source ancestry crosses Compose Dialog/Popup window boundaries through composition. */
@Composable
internal fun UiSourceScope(descriptor: UiSourceDescriptor, content: @Composable () -> Unit) {
    val inspector = LocalUiInspector.current
    val ancestors = LocalUiSourceAncestry.current
    val sourceAncestry = remember(inspector, ancestors, descriptor) {
        if (inspector == null) emptyList() else ancestors + descriptor
    }
    CompositionLocalProvider(LocalUiSourceAncestry provides sourceAncestry, content = content)
}
