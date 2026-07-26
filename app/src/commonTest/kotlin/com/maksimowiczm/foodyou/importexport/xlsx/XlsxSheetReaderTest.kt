package com.maksimowiczm.foodyou.importexport.xlsx

import kotlin.test.Test
import kotlin.test.assertEquals

class XlsxSheetReaderTest {

    @Test
    fun parsesSharedStringsIncludingRichTextAndEscapes() {
        val xml =
            """
            <sst>
              <si><t>Public Food Key</t></si>
              <si><t>Fat, total </t><t>(g)</t></si>
              <si><t>Salt &amp; pepper</t></si>
            </sst>
            """
                .trimIndent()

        val strings = XlsxSheetReader.parseSharedStrings(xml)

        assertEquals(listOf("Public Food Key", "Fat, total (g)", "Salt & pepper"), strings)
    }

    @Test
    fun parsesRowsWithSharedStringInlineAndNumericCells() {
        val shared = listOf("Public Food Key", "Food Name", "Cardamom")
        val sheet =
            """
            <worksheet><sheetData>
              <row r="3"><c r="A3" t="s"><v>0</v></c><c r="D3" t="s"><v>1</v></c></row>
              <row r="4"><c r="A4" t="str"><v>F002258</v></c><c r="D4" t="s"><v>2</v></c><c r="E4"><v>1236</v></c></row>
              <row r="5"/>
            </sheetData></worksheet>
            """
                .trimIndent()

        val rows = mutableListOf<XlsxRow>()
        XlsxSheetReader.forEachRow(shared, sheet) { rows.add(it) }

        assertEquals(2, rows.size)
        assertEquals(3, rows[0].rowNumber)
        assertEquals(mapOf("A" to "Public Food Key", "D" to "Food Name"), rows[0].cells)
        assertEquals(4, rows[1].rowNumber)
        assertEquals(mapOf("A" to "F002258", "D" to "Cardamom", "E" to "1236"), rows[1].cells)
    }

    @Test
    fun extractsColumnLetters() {
        assertEquals("A", XlsxSheetReader.columnLetters("A1"))
        assertEquals("AM", XlsxSheetReader.columnLetters("AM1591"))
    }
}
