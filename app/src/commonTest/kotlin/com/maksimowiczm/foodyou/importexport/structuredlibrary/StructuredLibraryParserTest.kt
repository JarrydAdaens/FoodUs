package com.maksimowiczm.foodyou.importexport.structuredlibrary

import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.importexport.structuredlibrary.domain.*
import com.maksimowiczm.foodyou.importexport.structuredlibrary.infrastructure.StructuredLibraryParser
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class StructuredLibraryParserTest {
    private val parser=StructuredLibraryParser()
    private val json=Json { encodeDefaults=true }
    private fun facts(vararg values:Pair<String,Double?>)=LibraryFacts("kcal-g",mapOf(*values))
    private fun product(id:String="1")=LibraryProduct(id,"Synthetic reference Food",facts("energy" to 123.0,"proteins" to 0.0),
        ProductFactBasis.REFERENCE_SERVING,PortionDescription(2.0,"pieces","2 pieces"))
    private fun recipe(id:String="1")=LibraryRecipe(id,"Synthetic reported Recipe",LibraryRecipeMode.REPORTED,
        reportedPanel=LibraryPanel(facts("energy" to 456.0),ReportedBasis.WHOLE_ITEM),
        components=listOf(LibraryComponent("Synthetic inline component",PortionDescription(0.5,"bowl","about half a bowl"))))
    private fun bundle(products:List<LibraryProduct> = listOf(product()),recipes:List<LibraryRecipe> = listOf(recipe())) =
        LibraryBundle("foodus-library",1,"synthetic-library",products,recipes)
    private suspend fun parse(bundle:LibraryBundle)=parser.parse(json.encodeToString(bundle).encodeToByteArray())
    private suspend fun fails(bundle:LibraryBundle,code:LibraryErrorCode) {
        val error=assertFailsWith<LibraryImportException> { parse(bundle) }
        assertEquals(code,error.code)
        assertEquals(code.name,error.message)
    }

    @Test fun mixedKindsWithEqualExternalKeysPreserveUnknownYieldAndRawComponents()=runBlocking<Unit> {
        val parsed=parse(bundle())
        assertEquals(2,parsed.digests.size)
        assertNotEquals(parsed.digests[LibraryKey(DefinitionKind.PRODUCT,"1")],parsed.digests[LibraryKey(DefinitionKind.RECIPE,"1")])
        assertNull(parsed.bundle.recipes.single().yield)
        assertEquals("about half a bowl",parsed.bundle.recipes.single().components.single().portion!!.text)
        assertNull(parsed.bundle.products.single().reference.metricAmount)
    }
    @Test fun duplicateKeysIncludingEscapedEquivalentNamesAreRejectedBeforeMapDecoding()=runBlocking<Unit> {
        for (text in listOf(
            """{"format":"foodus-library","formatVersion":1,"namespace":"synthetic","namespace":"other"}""",
            """{"format":"foodus-library","formatVersion":1,"namespace":"synthetic","\u006eamespace":"other"}""")) {
            assertEquals(LibraryErrorCode.MALFORMED,assertFailsWith<LibraryImportException> { parser.parse(text.encodeToByteArray()) }.code)
        }
    }
    @Test fun quotedNumbersBooleansAndNonstringIdsCannotBeCoercedByTheDtoDecoder()=runBlocking<Unit> {
        val input=bundle(products=listOf(product().copy(metadata=LibraryMetadata(textTruncated=true))),recipes=listOf(recipe().copy(yield=3)))
        val text=json.encodeToString(input)
        val invalid=listOf(
            text.replace("\"formatVersion\":1","\"formatVersion\":\"1\""),
            text.replace("\"energy\":123.0","\"energy\":\"123.0\""),
            text.replace("\"amount\":2.0","\"amount\":\"2.0\""),
            text.replace("\"yield\":3","\"yield\":\"3\""),
            text.replace("\"textTruncated\":true","\"textTruncated\":\"true\""),
            text.replace("\"externalId\":\"1\"","\"externalId\":1"),
            // isLiquid is deliberately not a v1 field: only explicit paired metric evidence is accepted.
            text.replace("\"basis\":\"REFERENCE_SERVING\"","\"isLiquid\":\"false\",\"basis\":\"REFERENCE_SERVING\""),
        )
        for(candidate in invalid) {
            assertNotEquals(text,candidate)
            assertEquals(LibraryErrorCode.MALFORMED,assertFailsWith<LibraryImportException> { parser.parse(candidate.encodeToByteArray()) }.code)
        }
    }
    @Test fun nonJsonNumberGrammarIsRejectedBeforeNumericDeserialization()=runBlocking<Unit> {
        val text=json.encodeToString(bundle())
        for(token in listOf("01","+1","0x1","0x1.0p0","1f","1d",".5","1.","NaN","Infinity","-Infinity")) {
            val candidate=text.replace("\"energy\":123.0","\"energy\":"+token)
            assertNotEquals(text,candidate)
            assertEquals(LibraryErrorCode.MALFORMED,assertFailsWith<LibraryImportException> { parser.parse(candidate.encodeToByteArray()) }.code)
        }
        for(token in listOf("1e309","1e-400","-1e-400")) {
            val candidate=text.replace("\"energy\":123.0","\"energy\":"+token)
            assertEquals(LibraryErrorCode.NUMBER,assertFailsWith<LibraryImportException> { parser.parse(candidate.encodeToByteArray()) }.code)
        }
        parse(bundle(products=listOf(product().copy(nutrition=facts("energy" to 1.25e2)))))
    }
    @Test fun rawControlCharactersAndInvalidEscapesAreNotJsonStrings()=runBlocking<Unit> {
        for(text in listOf(
            """{"format":"foodus-library","formatVersion":1,"namespace":"synthetic\q"}""",
            "{\"format\":\"foodus-library\",\"formatVersion\":1,\"namespace\":\"synthetic\nraw\"}")) {
            assertEquals(LibraryErrorCode.MALFORMED,assertFailsWith<LibraryImportException> { parser.parse(text.encodeToByteArray()) }.code)
        }
    }
    @Test fun versionUnknownFieldsInvalidUtf8AndOversizedInputHaveSanitizedFailures()=runBlocking<Unit> {
        fails(bundle().copy(formatVersion=2),LibraryErrorCode.VERSION)
        assertEquals(LibraryErrorCode.MALFORMED,assertFailsWith<LibraryImportException> {
            parser.parse("""{"format":"foodus-library","formatVersion":1,"namespace":"synthetic","unexpected":"Synthetic private text"}""".encodeToByteArray())
        }.code)
        assertEquals(LibraryErrorCode.MALFORMED,assertFailsWith<LibraryImportException> { parser.parse(byteArrayOf(0xC3.toByte(),0x28)) }.code)
        assertEquals(LibraryErrorCode.FILE_TOO_LARGE,assertFailsWith<LibraryImportException> { parser.parse(ByteArray(StructuredLibraryParser.MAX_BYTES+1)) }.code)
    }
    @Test fun duplicateIdentityAndDanglingOrWrongKindReferenceCannotEstablishNameMatches()=runBlocking<Unit> {
        fails(bundle(products=listOf(product(),product())),LibraryErrorCode.IDENTITY)
        val component=LibraryComponent("Synthetic same name",reference=LibraryReference(DefinitionKind.RECIPE,"missing"))
        fails(bundle(recipes=listOf(recipe().copy(components=listOf(component)))),LibraryErrorCode.REFERENCE)
        fails(bundle(recipes=listOf(recipe().copy(components=listOf(component.copy(reference=LibraryReference(DefinitionKind.MEAL,"1")))))),LibraryErrorCode.REFERENCE)
    }
    @Test fun cyclesSharedBranchesAndDepthAreValidatedAsTypedRecipeEdges()=runBlocking<Unit> {
        fun edge(from:String,to:String)=recipe(from).copy(components=listOf(LibraryComponent("Synthetic edge",reference=LibraryReference(DefinitionKind.RECIPE,to))))
        fails(bundle(recipes=listOf(edge("a","b"),edge("b","a"))),LibraryErrorCode.CYCLE)
        parse(bundle(recipes=listOf(edge("a","c"),edge("b","c"),recipe("c"))))
        val chain=(0..64).map { if(it==64)recipe(it.toString())else edge(it.toString(),(it+1).toString()) }
        fails(bundle(recipes=chain),LibraryErrorCode.LIMIT)
        parse(bundle(recipes=chain.drop(1)))
    }
    @Test fun factsRejectWrongUnitsUnknownKeysNegativesAndFiniteEncodedOverflow()=runBlocking<Unit> {
        fails(bundle(products=listOf(product().copy(nutrition=LibraryFacts("kcal-mg")))),LibraryErrorCode.UNIT)
        fails(bundle(products=listOf(product().copy(nutrition=facts("unsupported" to 1.0)))),LibraryErrorCode.UNIT)
        fails(bundle(products=listOf(product().copy(nutrition=facts("energy" to -1.0)))),LibraryErrorCode.NUMBER)
        fails(bundle(products=listOf(product().copy(nutrition=facts("vitaminA" to 1e303)))),LibraryErrorCode.NUMBER)
        parse(bundle(products=listOf(product().copy(nutrition=facts("vitaminA" to 1e300)))))
    }
    @Test fun metricBasesNeedExplicitPairedCompatibleEvidenceAndNeverDensity()=runBlocking<Unit> {
        fails(bundle(products=listOf(product().copy(basis=ProductFactBasis.METRIC_100))),LibraryErrorCode.BASIS)
        fails(bundle(products=listOf(product().copy(reference=PortionDescription(metricAmount=50.0)))),LibraryErrorCode.BASIS)
        fails(bundle(products=listOf(product().copy(reference=PortionDescription(metricAmount=50.0,metricUnit=MetricUnit.GRAM),packageMetric=LibraryMetric(100.0,MetricUnit.MILLILITER)))),LibraryErrorCode.BASIS)
        parse(bundle(products=listOf(product().copy(basis=ProductFactBasis.METRIC_100,reference=PortionDescription(metricAmount=100.0,metricUnit=MetricUnit.GRAM)))))
    }
    @Test fun reportedModeCannotSilentlyBecomeDerivedOrInventYield()=runBlocking<Unit> {
        fails(bundle(recipes=listOf(recipe().copy(mode=LibraryRecipeMode.DERIVED))),LibraryErrorCode.BASIS)
        fails(bundle(recipes=listOf(recipe().copy(yield=0))),LibraryErrorCode.NUMBER)
        val linked=LibraryIngredient(LibraryReference(DefinitionKind.PRODUCT,"1"),LibraryPortion(1.0,LibraryPortionUnit.SERVING))
        fails(bundle(recipes=listOf(recipe().copy(ingredients=listOf(linked)))),LibraryErrorCode.BASIS)
        parse(bundle(recipes=listOf(LibraryRecipe("1","Synthetic derived",LibraryRecipeMode.DERIVED,2,ingredients=listOf(linked)))))
    }
    @Test fun percentagesSecondaryClaimsAndTruncationStayOutsideActiveFacts()=runBlocking<Unit> {
        val metadata=LibraryMetadata(percentDailyValues=mapOf("proteins" to 50.0,"vitaminA" to 0.0),
            observations=listOf(NutritionObservation("energy-claim",999.0,"kcal","separate report")),textTruncated=true)
        val parsed=parse(bundle(products=listOf(product().copy(nutrition=facts(),metadata=metadata))))
        val food=parsed.bundle.products.single()
        assertNull(food.nutrition.toNutritionFacts().proteins.value)
        assertNull(food.nutrition.toNutritionFacts().energy.value)
        assertEquals(metadata,food.metadata)
    }
    @Test fun recipeDefinitionMetadataRemainsSeparateFromReportedPanelMetadata()=runBlocking<Unit> {
        val source=recipe().copy(metadata=LibraryMetadata(notes="Synthetic definition note"),
            reportedPanel=recipe().reportedPanel!!.copy(metadata=LibraryMetadata(notes="Synthetic panel note")))
        val captured=parse(bundle(recipes=listOf(source))).bundle.recipes.single().capturedPanel()!!
        assertEquals("Synthetic definition note",captured.definitionMetadata!!.notes)
        assertEquals("Synthetic panel note",captured.metadata!!.notes)
    }
    @Test fun canonicalDigestsNormalizeFormattingMapOrderNullUnknownAndSignedZero()=runBlocking<Unit> {
        val first=bundle(products=listOf(product().copy(nutrition=facts("energy" to 1.0,"proteins" to 0.0))))
        val second=bundle(products=listOf(product().copy(nutrition=facts("fats" to null,"proteins" to -0.0,"energy" to 1.0))))
        assertEquals(parse(first).digests,parse(second).digests)
        val changed=second.copy(products=second.products.map { it.copy(nutrition=facts("energy" to 2.0)) })
        assertNotEquals(parse(first).digests,parse(changed).digests)
    }
    @Test fun textMetadataAndComponentLimitsAreAppliedBeforePersistence()=runBlocking<Unit> {
        fails(bundle(products=listOf(product().copy(name="x".repeat(513)))),LibraryErrorCode.LIMIT)
        fails(bundle(products=listOf(product().copy(metadata=LibraryMetadata(notes="x".repeat(16_385))))),LibraryErrorCode.LIMIT)
        fails(bundle(recipes=listOf(recipe().copy(components=List(10_001){LibraryComponent("Synthetic") }))),LibraryErrorCode.LIMIT)
    }
}
