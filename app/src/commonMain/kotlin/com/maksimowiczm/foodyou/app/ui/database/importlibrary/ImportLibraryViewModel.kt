package com.maksimowiczm.foodyou.app.ui.database.importlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.food.DefinitionKind
import com.maksimowiczm.foodyou.importexport.structuredlibrary.domain.*
import com.maksimowiczm.foodyou.importexport.structuredlibrary.infrastructure.StructuredLibraryParser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal sealed interface LibraryImportState {
    data object Waiting:LibraryImportState
    data object Reading:LibraryImportState
    data class Preview(val summary:LibraryImportPreview):LibraryImportState
    data object Importing:LibraryImportState
    data class Complete(val result:LibraryImportResult):LibraryImportState
    data class Failed(val code:LibraryErrorCode,val kind:DefinitionKind?=null,val index:Int?=null):LibraryImportState
    data object PlatformUnavailable:LibraryImportState
}

/** The file/draft stays in memory only; the UI never receives raw values or exception payloads. */
internal class ImportLibraryViewModel(
    private val parser:StructuredLibraryParser,
    private val importer:LibraryImporter,
):ViewModel() {
    private val mutableState=MutableStateFlow<LibraryImportState>(LibraryImportState.Waiting)
    val state=mutableState.asStateFlow()
    private var draft:ValidatedLibrary?=null
    private var operation:Job?=null

    fun open(read:suspend()->ByteArray) {
        cancel()
        operation=viewModelScope.launch {
            mutableState.value=LibraryImportState.Reading
            try {
                val (validated,preview)=withContext(Dispatchers.IO) {
                    val bytes=read()
                    val parsed=try { parser.parse(bytes) } finally { bytes.fill(0) }
                    parsed to importer.preview(parsed)
                }
                draft=validated
                mutableState.value=LibraryImportState.Preview(preview)
            } catch(cancelled:CancellationException) { throw cancelled }
            catch(error:LibraryImportException) { failure(error) }
            catch(_:Exception) { mutableState.value=LibraryImportState.Failed(LibraryErrorCode.FILE_UNAVAILABLE) }
        }
    }

    fun confirm() {
        val preview=mutableState.value as? LibraryImportState.Preview ?: return
        val captured=draft ?: return
        if(preview.summary.conflicts!=0 || !mutableState.compareAndSet(preview,LibraryImportState.Importing))return
        operation=viewModelScope.launch {
            try {
                val result=withContext(Dispatchers.IO) { importer.apply(captured) }
                draft=null
                mutableState.value=LibraryImportState.Complete(result)
            } catch(cancelled:CancellationException) { throw cancelled }
            catch(error:LibraryImportException) { failure(error) }
            catch(_:Exception) { draft=null; mutableState.value=LibraryImportState.Failed(LibraryErrorCode.PERSISTENCE) }
        }
    }
    fun cancel() { operation?.cancel(); operation=null; draft=null; mutableState.value=LibraryImportState.Waiting }
    private fun failure(error:LibraryImportException) {
        draft=null
        mutableState.value=LibraryImportState.Failed(error.code,error.kind,error.index)
    }
    override fun onCleared() { draft=null; super.onCleared() }
}
