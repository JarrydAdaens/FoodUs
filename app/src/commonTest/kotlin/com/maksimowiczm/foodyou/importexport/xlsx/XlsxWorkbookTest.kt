package com.maksimowiczm.foodyou.importexport.xlsx

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class XlsxWorkbookTest {

    private val workbookXml =
        """
        <workbook><sheets>
          <sheet name="Contents" sheetId="13" r:id="rId1"/>
          <sheet name="All solids &amp; liquids per 100 g" sheetId="6" r:id="rId2"/>
          <sheet name="Liquids only per 100 mL" sheetId="10" r:id="rId3"/>
        </sheets></workbook>
        """
            .trimIndent()

    private val relsXml =
        """
        <Relationships>
          <Relationship Id="rId1" Target="worksheets/sheet1.xml"/>
          <Relationship Id="rId2" Target="worksheets/sheet2.xml"/>
          <Relationship Id="rId3" Target="worksheets/sheet3.xml"/>
        </Relationships>
        """
            .trimIndent()

    @Test
    fun resolvesSheetPathByEscapedName() {
        val path =
            XlsxWorkbook.resolveSheetPath(
                workbookXml = workbookXml,
                workbookRelsXml = relsXml,
                sheetName = "All solids & liquids per 100 g",
            )

        assertEquals("xl/worksheets/sheet2.xml", path)
    }

    @Test
    fun returnsNullForUnknownSheet() {
        assertNull(XlsxWorkbook.resolveSheetPath(workbookXml, relsXml, "Nonexistent"))
    }
}
