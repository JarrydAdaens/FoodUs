package com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase

import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AfcdFormatException
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AfcdWorkbookFiles
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.domain.AfcdWorkbookParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AfcdWorkbookParserTest {

    private val workbookXml =
        """
        <workbook><sheets>
          <sheet name="Contents" sheetId="13" r:id="rId1"/>
          <sheet name="All solids &amp; liquids per 100 g" sheetId="6" r:id="rId2"/>
        </sheets></workbook>
        """
            .trimIndent()

    private val relsXml =
        """
        <Relationships>
          <Relationship Id="rId1" Target="worksheets/sheet1.xml"/>
          <Relationship Id="rId2" Target="worksheets/sheet2.xml"/>
        </Relationships>
        """
            .trimIndent()

    // Shared strings: headers plus one food name.
    private val sharedStringsXml =
        """
        <sst>
          <si><t>Public Food Key</t></si>
          <si><t>Food Name</t></si>
          <si><t>Energy with dietary fibre, equated (kJ)</t></si>
          <si><t>Protein (g)</t></si>
          <si><t>Fat, total (g)</t></si>
          <si><t>Available carbohydrate, with sugar alcohols (g)</t></si>
          <si><t>Vegemite</t></si>
        </sst>
        """
            .trimIndent()

    // A title row, a header row (row 3), and one data row (row 4).
    private val sheetXml =
        """
        <worksheet><sheetData>
          <row r="1"><c r="A1" t="str"><v>Release 3 - Nutrient profiles</v></c></row>
          <row r="3">
            <c r="A3" t="s"><v>0</v></c>
            <c r="D3" t="s"><v>1</v></c>
            <c r="E3" t="s"><v>2</v></c>
            <c r="H3" t="s"><v>3</v></c>
            <c r="J3" t="s"><v>4</v></c>
            <c r="AN3" t="s"><v>5</v></c>
          </row>
          <row r="4">
            <c r="A4" t="str"><v>F009363</v></c>
            <c r="D4" t="s"><v>6</v></c>
            <c r="E4"><v>836</v></c>
            <c r="H4"><v>24.9</v></c>
            <c r="J4"><v>0.9</v></c>
            <c r="AN4"><v>19.4</v></c>
          </row>
        </sheetData></worksheet>
        """
            .trimIndent()

    private fun files(sheet: String = sheetXml) =
        AfcdWorkbookFiles(
            workbookXml = workbookXml,
            workbookRelsXml = relsXml,
            sharedStringsXml = sharedStringsXml,
            worksheets = mapOf("xl/worksheets/sheet2.xml" to sheet),
            publicationDate = "Mon, 22 Dec 2025 23:20:49 GMT",
            checksum = "abc",
        )

    @Test
    fun parsesFoodsFromWorkbook() {
        val products = AfcdWorkbookParser.parse(files())

        assertEquals(1, products.size)
        val vegemite = products.single()
        assertEquals("F009363", vegemite.publicFoodKey)
        assertEquals("Vegemite", vegemite.name)
        assertEquals(836.0 / 4.184, vegemite.nutritionFacts.energy.value!!, 1e-6)
        assertEquals(24.9, vegemite.nutritionFacts.proteins.value!!, 1e-6)
    }

    @Test
    fun failsWhenNoFoodRows() {
        val headerOnly =
            """
            <worksheet><sheetData>
              <row r="3">
                <c r="A3" t="s"><v>0</v></c>
                <c r="D3" t="s"><v>1</v></c>
                <c r="E3" t="s"><v>2</v></c>
                <c r="H3" t="s"><v>3</v></c>
                <c r="J3" t="s"><v>4</v></c>
                <c r="AN3" t="s"><v>5</v></c>
              </row>
            </sheetData></worksheet>
            """
                .trimIndent()

        assertFailsWith<AfcdFormatException> { AfcdWorkbookParser.parse(files(headerOnly)) }
    }
}
