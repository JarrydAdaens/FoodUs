package com.maksimowiczm.foodyou.app.ui.developer.inspection

import androidx.compose.ui.Modifier

/** Shared literal file/package metadata keeps each call site's searchable name close to its code. */
internal class UiSourceFile(private val packageName: String, private val source: String) {
    fun element(name: String, composable: String, owner: String): UiSourceDescriptor =
        UiSourceDescriptor(name, composable, "$packageName.$owner", source)
}

internal fun Modifier.uiName(
    name: String,
    composable: String,
    source: UiSourceFile,
    owner: String,
    enabled: Boolean? = null,
): Modifier = uiName(source.element(name, composable, owner), enabled)
