package com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.importexport.domain.entity.ProductField
import com.maksimowiczm.foodyou.importexport.domain.usecase.ExportCsvProductsUseCase
import com.maksimowiczm.foodyou.importexport.domain.usecase.CsvMetricBasisUnavailableException
import java.io.File
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class ExportProductsViewModel(private val exportCsvProductsUseCase: ExportCsvProductsUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Preparing)
    val uiState = _uiState.asStateFlow()
    private var staged: File? = null
    private var count = 0
    private var started = false

    /** Validate and finish the private temporary CSV before asking for any destination. */
    fun prepare(context: Context) {
        if (started) return
        started = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var file: File? = null
                var retained = false
                try {
                    val prepared = File.createTempFile("foodus-products-", ".csv", context.cacheDir)
                    file = prepared
                    var lines = 0
                    prepared.bufferedWriter().use { writer ->
                        exportCsvProductsUseCase.export(ProductField.entries).collect { line ->
                            currentCoroutineContext().ensureActive()
                            writer.appendLine(line)
                            _uiState.value = UiState.Exporting((lines++ - 1).coerceAtLeast(0))
                        }
                    }
                    count = (lines - 1).coerceAtLeast(0)
                    currentCoroutineContext().ensureActive()
                    staged = prepared
                    retained = true
                    _uiState.value = UiState.WaitingForFile
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: CsvMetricBasisUnavailableException) { _uiState.value = UiState.Error(metricUnavailable = true) }
                catch (_: Exception) { _uiState.value = UiState.Error() }
                finally { if (!retained) file?.delete() }
            }
        }
    }

    fun handleCsv(uri: Uri, context: Context) {
        if (_uiState.value != UiState.WaitingForFile) return
        val file = staged ?: return
        _uiState.value = UiState.Exporting(count)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val output = context.contentResolver.openOutputStream(uri) ?: error("Destination unavailable")
                    output.use { target -> file.inputStream().use { input ->
                        val buffer = ByteArray(8192)
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val size = input.read(buffer)
                            if (size < 0) break
                            target.write(buffer, 0, size)
                        }
                        target.flush()
                    } }
                    _uiState.value = UiState.Exported(count)
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { _uiState.value = UiState.Error() }
                finally { file.delete(); staged = null }
            }
        }
    }

    override fun onCleared() { staged?.delete(); super.onCleared() }
}
