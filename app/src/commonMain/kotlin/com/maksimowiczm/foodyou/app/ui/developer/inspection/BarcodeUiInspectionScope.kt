package com.maksimowiczm.foodyou.app.ui.developer.inspection

import androidx.compose.runtime.*
import com.maksimowiczm.foodyou.barcodescanner.ui.BarcodeUiInspection
import com.maksimowiczm.foodyou.barcodescanner.ui.BarcodeUiInspectionProvider
import com.maksimowiczm.foodyou.barcodescanner.ui.BarcodeUiSource

@Composable
internal fun BarcodeUiInspectionScope(content: @Composable () -> Unit) {
    val inspection = remember {
        BarcodeUiInspection(
            modifier = { modifier, source -> modifier.uiName(source.toDescriptor()) },
            sourceScope = { source, children -> UiSourceScope(source.toDescriptor(), children) },
        )
    }
    BarcodeUiInspectionProvider(inspection, content)
}

private fun BarcodeUiSource.toDescriptor() = UiSourceDescriptor(name, composable, owner, source)
