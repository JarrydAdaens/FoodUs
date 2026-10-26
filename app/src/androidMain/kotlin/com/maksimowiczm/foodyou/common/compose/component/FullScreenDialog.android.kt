package com.maksimowiczm.foodyou.common.compose.component
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiInspectionSurface

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
actual fun FullScreenDialog(onDismissRequest: () -> Unit, content: @Composable (() -> Unit)) {
    UiSourceScope(uiInspectionSource.element("FullScreenDialog.Dialog.1", "androidx.compose.ui.window.Dialog", "FullScreenDialog")) { Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) { UiInspectionSurface(uiInspectionSource.element("FullScreenDialog.Dialog.1.content", "androidx.compose.ui.window.Dialog", "FullScreenDialog")) {
        Surface(modifier = (Modifier.fillMaxSize()).uiName("FullScreenDialog.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "FullScreenDialog")) { content() }
     } } }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.common.compose.component",
    "app/src/androidMain/kotlin/com/maksimowiczm/foodyou/common/compose/component/FullScreenDialog.android.kt",
)
