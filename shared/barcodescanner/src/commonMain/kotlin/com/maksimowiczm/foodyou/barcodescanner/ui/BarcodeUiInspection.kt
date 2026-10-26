package com.maksimowiczm.foodyou.barcodescanner.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/** Optional source inspection seam. The scanner remains independent of its application's inspector. */
data class BarcodeUiSource(val name: String, val composable: String, val owner: String, val source: String)

class BarcodeUiInspection(
    val modifier: (Modifier, BarcodeUiSource) -> Modifier = { modifier, _ -> modifier },
    val sourceScope: @Composable (BarcodeUiSource, @Composable () -> Unit) -> Unit = { _, content -> content() },
)

private val LocalBarcodeUiInspection = staticCompositionLocalOf { BarcodeUiInspection() }

@Composable
fun BarcodeUiInspectionProvider(inspection: BarcodeUiInspection, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalBarcodeUiInspection provides inspection, content = content)
}

internal class BarcodeUiSourceFile(private val packageName: String, private val source: String) {
    fun element(name: String, composable: String, owner: String) =
        BarcodeUiSource(name, composable, "$packageName.$owner", source)
}

internal fun Modifier.barcodeUiName(name: String, composable: String, source: BarcodeUiSourceFile, owner: String): Modifier =
    composed { LocalBarcodeUiInspection.current.modifier(this, source.element(name, composable, owner)) }

@Composable
internal fun BarcodeUiSourceScope(source: BarcodeUiSource, content: @Composable () -> Unit) {
    LocalBarcodeUiInspection.current.sourceScope(source, content)
}
