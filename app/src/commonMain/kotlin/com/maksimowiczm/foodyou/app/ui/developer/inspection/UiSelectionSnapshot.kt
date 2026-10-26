package com.maksimowiczm.foodyou.app.ui.developer.inspection

/**
 * Copyable structural data, independent of live UI lifetime. Ancestry describes explicitly declared
 * application/source structure, not the complete Compose implementation tree. No runtime UI values
 * or arbitrary semantics/property bags are accepted by this export boundary.
 */
internal class UiSelectionSnapshot(
    val screen: UiSourceDescriptor,
    val surface: UiSourceDescriptor,
    ancestors: List<UiElementSnapshot>,
    val selected: UiElementSnapshot,
) {
    val ancestors: List<UiElementSnapshot> = ancestors.toList()

    fun copyText(includeHierarchy: Boolean): String =
        if (includeHierarchy) toXml() else selected.descriptor.name

    private fun toXml(): String = buildString {
        appendLine("<ui-selection format=\"foodus-ui-v1\" ancestry=\"declared-source\" snapshot=\"true\">")
        appendDescriptor("screen", screen)
        appendDescriptor("surface", surface)
        appendLine("  <ancestors>")
        ancestors.forEach { appendElement("ancestor", it, "    ") }
        appendLine("  </ancestors>")
        appendElement("element", selected, "  ")
        append("</ui-selection>")
    }

    private fun StringBuilder.appendDescriptor(tag: String, descriptor: UiSourceDescriptor) {
        append("  <").append(tag)
        appendSourceAttributes(descriptor)
        appendLine(" />")
    }

    private fun StringBuilder.appendElement(tag: String, element: UiElementSnapshot, indent: String) {
        append(indent).append('<').append(tag)
        appendSourceAttributes(element.descriptor)
        attribute("occurrence", element.occurrence.toString())
        element.enabled?.let { attribute("enabled", it.toString()) }
        element.bounds?.let {
            attribute("left", it.left.toString())
            attribute("top", it.top.toString())
            attribute("right", it.right.toString())
            attribute("bottom", it.bottom.toString())
        }
        appendLine(" />")
    }

    private fun StringBuilder.appendSourceAttributes(descriptor: UiSourceDescriptor) {
        attribute("name", descriptor.name)
        attribute("composable", descriptor.composable)
        attribute("owner", descriptor.owner)
        attribute("source", descriptor.source)
        descriptor.callSiteOwner?.let { attribute("call-site-owner", it) }
        descriptor.callSiteSource?.let { attribute("call-site-source", it) }
    }

    private fun StringBuilder.attribute(name: String, value: String) {
        append(' ').append(name).append("=\"")
        value.forEach {
            append(
                when (it) {
                    '&' -> "&amp;"
                    '<' -> "&lt;"
                    '>' -> "&gt;"
                    '"' -> "&quot;"
                    '\'' -> "&apos;"
                    else -> it.toString()
                }
            )
        }
        append('"')
    }
}
