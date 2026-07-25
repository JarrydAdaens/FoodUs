package com.maksimowiczm.foodyou.importexport.xlsx

/**
 * A single parsed spreadsheet row.
 *
 * @param rowNumber The 1-based row index from the worksheet (`<row r="N">`).
 * @param cells Resolved cell text keyed by column letter (e.g. `"A"`, `"AM"`). Empty cells are
 *   omitted.
 */
data class XlsxRow(val rowNumber: Int, val cells: Map<String, String>)

/**
 * Minimal, dependency-free reader for the specific OOXML (`.xlsx`) shape produced by the Australian
 * Food Composition Database workbooks: a shared-strings table plus a single worksheet whose cells
 * are either numbers or shared-string references.
 *
 * This is deliberately not a general-purpose XLSX library. It supports exactly the cell encodings
 * the AFCD files use (`t="s"` shared strings, `t="str"`/`t="inlineStr"` inline text, and bare
 * numeric `<v>`), scanning the XML by hand so it stays pure Kotlin and testable on every target.
 * The platform layer is responsible only for unzipping the workbook entries into strings.
 */
object XlsxSheetReader {

    /** Parses `xl/sharedStrings.xml` into an ordered list of resolved strings. */
    fun parseSharedStrings(sharedStringsXml: String): List<String> {
        val result = mutableListOf<String>()
        var cursor = 0
        while (true) {
            val siStart = sharedStringsXml.indexOf("<si", cursor).takeIf { it >= 0 } ?: break
            val siEnd = sharedStringsXml.indexOf("</si>", siStart).takeIf { it >= 0 } ?: break
            val si = sharedStringsXml.substring(siStart, siEnd)
            result.add(concatTextRuns(si))
            cursor = siEnd + "</si>".length
        }
        return result
    }

    /**
     * Streams the worksheet rows, invoking [onRow] for each. Streaming keeps peak memory bounded for
     * the large AFCD nutrient sheet rather than materializing every row at once.
     */
    fun forEachRow(sharedStrings: List<String>, sheetXml: String, onRow: (XlsxRow) -> Unit) {
        var cursor = sheetXml.indexOf("<sheetData").takeIf { it >= 0 } ?: 0
        while (true) {
            val rowStart = sheetXml.indexOf("<row", cursor).takeIf { it >= 0 } ?: break
            val rowTagEnd = sheetXml.indexOf('>', rowStart).takeIf { it >= 0 } ?: break
            val rowOpenTag = sheetXml.substring(rowStart, rowTagEnd + 1)

            // Self-closing empty row (<row .../>)
            if (rowOpenTag.endsWith("/>")) {
                cursor = rowTagEnd + 1
                continue
            }

            val rowEnd = sheetXml.indexOf("</row>", rowTagEnd).takeIf { it >= 0 } ?: break
            val rowNumber = attribute(rowOpenTag, "r")?.toIntOrNull()
            val body = sheetXml.substring(rowTagEnd + 1, rowEnd)
            if (rowNumber != null) {
                onRow(XlsxRow(rowNumber, parseCells(sharedStrings, body)))
            }
            cursor = rowEnd + "</row>".length
        }
    }

    private fun parseCells(sharedStrings: List<String>, rowBody: String): Map<String, String> {
        val cells = LinkedHashMap<String, String>()
        var cursor = 0
        while (true) {
            val cStart = rowBody.indexOf("<c", cursor).takeIf { it >= 0 } ?: break
            val cTagEnd = rowBody.indexOf('>', cStart).takeIf { it >= 0 } ?: break
            val cOpenTag = rowBody.substring(cStart, cTagEnd + 1)
            val ref = attribute(cOpenTag, "r")
            val type = attribute(cOpenTag, "t")

            if (cOpenTag.endsWith("/>")) {
                cursor = cTagEnd + 1
                continue
            }

            val cEnd = rowBody.indexOf("</c>", cTagEnd).takeIf { it >= 0 } ?: break
            val content = rowBody.substring(cTagEnd + 1, cEnd)
            val value = resolveCellValue(sharedStrings, type, content)
            if (ref != null && value != null && value.isNotEmpty()) {
                cells[columnLetters(ref)] = value
            }
            cursor = cEnd + "</c>".length
        }
        return cells
    }

    private fun resolveCellValue(
        sharedStrings: List<String>,
        type: String?,
        content: String,
    ): String? =
        when (type) {
            "s" -> {
                val index = innerTagText(content, "v")?.trim()?.toIntOrNull()
                index?.let { sharedStrings.getOrNull(it) }
            }

            "inlineStr" -> concatTextRuns(content)
            "str" -> innerTagText(content, "v")?.let(::unescapeXml)
            else -> innerTagText(content, "v")?.let(::unescapeXml)
        }

    /** Concatenates every `<t>...</t>` run inside an `<si>` or `<is>` block. */
    private fun concatTextRuns(block: String): String {
        val builder = StringBuilder()
        var cursor = 0
        while (true) {
            val tStart = block.indexOf("<t", cursor).takeIf { it >= 0 } ?: break
            val tTagEnd = block.indexOf('>', tStart).takeIf { it >= 0 } ?: break
            // Ensure this is a <t> or <t ...> element, not e.g. <table>
            val nextChar = block[tStart + 2]
            if (nextChar != '>' && nextChar != ' ' && nextChar != '/') {
                cursor = tTagEnd + 1
                continue
            }
            if (block.substring(tStart, tTagEnd + 1).endsWith("/>")) {
                cursor = tTagEnd + 1
                continue
            }
            val tEnd = block.indexOf("</t>", tTagEnd).takeIf { it >= 0 } ?: break
            builder.append(unescapeXml(block.substring(tTagEnd + 1, tEnd)))
            cursor = tEnd + "</t>".length
        }
        return builder.toString()
    }

    private fun innerTagText(content: String, tag: String): String? {
        val open = content.indexOf("<$tag").takeIf { it >= 0 } ?: return null
        val openEnd = content.indexOf('>', open).takeIf { it >= 0 } ?: return null
        val close = content.indexOf("</$tag>", openEnd).takeIf { it >= 0 } ?: return null
        return content.substring(openEnd + 1, close)
    }

    private fun attribute(tag: String, name: String): String? {
        val needle = "$name=\""
        val start = tag.indexOf(needle).takeIf { it >= 0 } ?: return null
        val valueStart = start + needle.length
        val end = tag.indexOf('"', valueStart).takeIf { it >= 0 } ?: return null
        return tag.substring(valueStart, end)
    }

    /** Extracts the column letters from a cell reference (e.g. `"AM12"` -> `"AM"`). */
    fun columnLetters(cellRef: String): String {
        val end = cellRef.indexOfFirst { it.isDigit() }
        return if (end < 0) cellRef else cellRef.substring(0, end)
    }

    private fun unescapeXml(raw: String): String =
        if (raw.indexOf('&') < 0) {
            raw
        } else {
            raw.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&#10;", " ")
                .replace("&#13;", " ")
                .replace("&#39;", "'")
                .replace("&amp;", "&")
        }
}
