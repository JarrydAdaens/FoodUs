package com.maksimowiczm.foodyou.app.ui.developer.inspection

/**
 * Literal source metadata for an app-owned inspection target. Never construct these fields from
 * labels, route arguments, database identifiers, credentials, or user-entered text.
 *
 * Symbols and repository-relative paths are explicit so they survive release minification.
 */
internal data class UiSourceDescriptor(
    val name: String,
    val composable: String,
    val owner: String,
    val source: String,
    val callSiteOwner: String? = null,
    val callSiteSource: String? = null,
) {
    init {
        require(sourceName.matches(name)) { "UI name must be a stable source identifier" }
        require(qualifiedSymbol.matches(composable)) { "Composable must be fully qualified" }
        require(qualifiedSymbol.matches(owner)) { "Owner must be fully qualified" }
        requireSourcePath(source)
        require((callSiteOwner == null) == (callSiteSource == null)) {
            "Call-site owner and source must be declared together"
        }
        callSiteOwner?.let { require(qualifiedSymbol.matches(it)) }
        callSiteSource?.let(::requireSourcePath)
    }

    private companion object {
        val sourceName = Regex("[A-Za-z_][A-Za-z0-9_.-]*")
        val qualifiedSymbol = Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)+")

        fun requireSourcePath(path: String) {
            require(path.endsWith(".kt") && !path.startsWith('/') && ':' !in path && '\\' !in path) {
                "Source must be a repository-relative Kotlin path using forward slashes"
            }
            require(path.split('/').all { it.isNotBlank() && it != "." && it != ".." }) {
                "Source must not contain empty or traversal segments"
            }
            require(path.none { it.code < 0x20 }) { "Source must not contain control characters" }
        }
    }
}

/** Window-coordinate rectangle captured at selection time, without a retained layout object. */
internal data class UiInspectionBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    init {
        require(listOf(left, top, right, bottom).all(Float::isFinite))
        require(left <= right && top <= bottom)
    }
}

/** Occurrence is a one-based structural position among matching siblings, never a database ID. */
internal data class UiElementSnapshot(
    val descriptor: UiSourceDescriptor,
    val occurrence: Int,
    val enabled: Boolean? = null,
    val bounds: UiInspectionBounds? = null,
) {
    init {
        require(occurrence > 0)
    }
}
