package com.maksimowiczm.foodyou.app.ui.database.importlibrary

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.importexport.structuredlibrary.domain.LibraryErrorCode
import com.maksimowiczm.foodyou.importexport.structuredlibrary.domain.LibraryImportException
import com.maksimowiczm.foodyou.importexport.structuredlibrary.infrastructure.StructuredLibraryParser
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.koin.compose.viewmodel.koinViewModel
import java.io.ByteArrayOutputStream

@Composable
actual fun ImportLibraryScreen(onBack:()->Unit,onFinish:()->Unit,modifier:Modifier) {
    val viewModel:ImportLibraryViewModel=koinViewModel()
    val context=LocalContext.current.applicationContext
    val state by viewModel.state.collectAsStateWithLifecycle()
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri==null)viewModel.cancel() else viewModel.open {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val output=ByteArrayOutputStream()
                val buffer=ByteArray(8192)
                while(true) {
                    currentCoroutineContext().ensureActive()
                    val count=stream.read(buffer)
                    if(count<0)break
                    if(output.size().toLong()+count>StructuredLibraryParser.MAX_BYTES)throw LibraryImportException(LibraryErrorCode.FILE_TOO_LARGE)
                    output.write(buffer,0,count)
                }
                output.toByteArray()
            } ?: throw LibraryImportException(LibraryErrorCode.FILE_UNAVAILABLE)
        }
    }
    ImportLibraryContent(state,{picker.launch(arrayOf("*/*"))},viewModel::confirm,viewModel::cancel,
        {viewModel.cancel();onBack()},onFinish,(modifier).uiName("ImportLibraryScreen.ImportLibraryContent.1", "com.maksimowiczm.foodyou.app.ui.database.importlibrary.ImportLibraryContent", uiInspectionSource, "ImportLibraryScreen"))
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.database.importlibrary",
    "app/src/androidMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/importlibrary/ImportLibraryScreen.android.kt",
)
