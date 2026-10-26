package com.maksimowiczm.foodyou.app.ui.common.component
import androidx.compose.ui.Modifier

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope
import com.maksimowiczm.foodyou.app.ui.developer.inspection.BarcodeUiInspectionScope

import androidx.compose.runtime.*
import com.maksimowiczm.foodyou.barcodescanner.ui.CameraBarcodeScannerScreen
import com.maksimowiczm.foodyou.common.compose.component.FullScreenDialog

@Composable
fun FullScreenCameraBarcodeScanner(onBarcodeScan: (String) -> Unit, onClose: () -> Unit) {
    // TODO
    //  Predictive back handling
    BarcodeUiInspectionScope {
    FullScreenDialog(onDismissRequest = onClose) {
        UiSourceScope(uiInspectionSource.element("FullScreenCameraBarcodeScanner.CameraBarcodeScannerScreen.1", "com.maksimowiczm.foodyou.barcodescanner.ui.CameraBarcodeScannerScreen", "FullScreenCameraBarcodeScanner")) { CameraBarcodeScannerScreen(onBarcodeScan = onBarcodeScan, onClose = onClose) }
    }
    }
}

@Composable
fun FullScreenCameraBarcodeScanner(
    visible: Boolean,
    onBarcodeScan: (String) -> Unit,
    onClose: () -> Unit,
) {
    if (visible) {
        FullScreenCameraBarcodeScanner(onBarcodeScan, onClose)
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/FullScreenCameraBarcodeScanner.kt",
)
