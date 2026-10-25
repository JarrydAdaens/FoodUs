package com.maksimowiczm.foodyou.app.ui.database.importlibrary

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Android is the supported file-selection surface for v1; common import/model code is shared. */
@Composable
actual fun ImportLibraryScreen(onBack:()->Unit,onFinish:()->Unit,modifier:Modifier) {
    ImportLibraryContent(LibraryImportState.PlatformUnavailable,{}, {}, {},onBack,onFinish,modifier)
}
