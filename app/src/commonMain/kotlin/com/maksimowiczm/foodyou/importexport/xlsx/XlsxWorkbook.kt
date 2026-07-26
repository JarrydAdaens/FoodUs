package com.maksimowiczm.foodyou.importexport.xlsx

/**
 * Resolves worksheet entry paths from an OOXML workbook so the correct sheet can be located by its
 * visible name rather than a fragile hard-coded `sheetN.xml`. Pure string parsing, kept separate
 * from the platform unzip layer so it can be unit tested.
 */
object XlsxWorkbook {

    /**
     * Returns the zip entry path (e.g. `"xl/worksheets/sheet2.xml"`) for the worksheet whose visible
     * name equals [sheetName], or null if it cannot be resolved.
     */
    fun resolveSheetPath(workbookXml: String, workbookRelsXml: String, sheetName: String): String? {
        val relationshipId = findSheetRelationshipId(workbookXml, sheetName) ?: return null
        val target = findRelationshipTarget(workbookRelsXml, relationshipId) ?: return null
        return normalizeTarget(target)
    }

    private fun findSheetRelationshipId(workbookXml: String, sheetName: String): String? {
        var cursor = 0
        while (true) {
            val start = workbookXml.indexOf("<sheet", cursor).takeIf { it >= 0 } ?: break
            val end = workbookXml.indexOf('>', start).takeIf { it >= 0 } ?: break
            val tag = workbookXml.substring(start, end + 1)
            cursor = end + 1
            // Skip <sheetData>/<sheetView> style tags; a real sheet declaration has a name.
            val name = attribute(tag, "name")?.let(::unescape) ?: continue
            if (name == sheetName) {
                return attribute(tag, "r:id") ?: attribute(tag, "id")
            }
        }
        return null
    }

    private fun findRelationshipTarget(relsXml: String, relationshipId: String): String? {
        var cursor = 0
        while (true) {
            val start = relsXml.indexOf("<Relationship", cursor).takeIf { it >= 0 } ?: break
            val end = relsXml.indexOf('>', start).takeIf { it >= 0 } ?: break
            val tag = relsXml.substring(start, end + 1)
            cursor = end + 1
            if (attribute(tag, "Id") == relationshipId) {
                return attribute(tag, "Target")
            }
        }
        return null
    }

    private fun normalizeTarget(target: String): String {
        val cleaned = target.removePrefix("/xl/").removePrefix("/")
        return if (cleaned.startsWith("xl/")) cleaned else "xl/$cleaned"
    }

    private fun attribute(tag: String, name: String): String? {
        val needle = "$name=\""
        val start = tag.indexOf(needle).takeIf { it >= 0 } ?: return null
        val valueStart = start + needle.length
        val valueEnd = tag.indexOf('"', valueStart).takeIf { it >= 0 } ?: return null
        return tag.substring(valueStart, valueEnd)
    }

    private fun unescape(raw: String): String =
        raw.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
}
