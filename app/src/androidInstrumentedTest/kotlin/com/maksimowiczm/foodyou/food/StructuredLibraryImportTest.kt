package com.maksimowiczm.foodyou.food

import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.infrastructure.room.toNutritionFacts
import com.maksimowiczm.foodyou.common.infrastructure.room.toValidatedEntityNutrients
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.repository.*
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import com.maksimowiczm.foodyou.importexport.importExportModule
import com.maksimowiczm.foodyou.importexport.structuredlibrary.domain.*
import com.maksimowiczm.foodyou.importexport.structuredlibrary.infrastructure.RoomLibraryImporter
import com.maksimowiczm.foodyou.importexport.structuredlibrary.infrastructure.StructuredLibraryParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.LocalDate
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Only disposable databases. Optional private input is never copied into the checkout or logged. */
class StructuredLibraryImportTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private val json=Json { encodeDefaults=true }
    private fun facts(energy:Double)=LibraryFacts("kcal-g",mapOf("energy" to energy,"proteins" to 0.0))
    private fun synthetic()=LibraryBundle("foodus-library",1,"synthetic-library",
        products=listOf(LibraryProduct("1","Synthetic reference Food",facts(100.0),ProductFactBasis.REFERENCE_SERVING,PortionDescription(2.0,"pieces","2 pieces"))),
        recipes=listOf(
            LibraryRecipe("1","Synthetic reported Recipe",LibraryRecipeMode.REPORTED,
                reportedPanel=LibraryPanel(facts(350.0),ReportedBasis.WHOLE_ITEM),components=listOf(
                    LibraryComponent("Synthetic first",PortionDescription(0.5,"bowl","about half a bowl"),reference=LibraryReference(DefinitionKind.PRODUCT,"1")),
                    LibraryComponent("Synthetic second",PortionDescription(text="amount unknown")))),
            LibraryRecipe("2","Synthetic derived Recipe",LibraryRecipeMode.DERIVED,2,
                ingredients=listOf(LibraryIngredient(LibraryReference(DefinitionKind.PRODUCT,"1"),LibraryPortion(2.0,LibraryPortionUnit.SERVING))))),
        meals=listOf(LibraryMeal("1","Synthetic Meal",listOf(
            LibraryMealItem("Synthetic absolute first",facts(90.0),PortionDescription(3.0,"pieces","3 pieces"),servingCount=3.0,reference=LibraryReference(DefinitionKind.PRODUCT,"1")),
            LibraryMealItem("Synthetic absolute second",facts(20.0))),LibraryPanel(facts(95.0),ReportedBasis.WHOLE_ITEM))))
    private suspend fun parsed(source:LibraryBundle)=StructuredLibraryParser().parse(json.encodeToString(source).encodeToByteArray())

    @Test fun productionBindingImportsTypedKindsAndReimportSkipsWithoutOverwritingLocalEdits()=runBlocking<Unit> {
        val fixture=Fixture()
        try {
            val source=parsed(synthetic())
            val before=fixture.counts()
            val preview=fixture.importer.preview(source)
            assertEquals(1,preview.products); assertEquals(2,preview.recipes); assertEquals(1,preview.meals)
            assertEquals(0,preview.conflicts); assertEquals(before,fixture.counts())
            assertEquals(LibraryImportResult(1,2,1,0),fixture.importer.apply(source))
            verifyReadback(source,fixture)
            val rows=fixture.db.libraryIdentityDao.getNamespace(source.bundle.namespace)
            assertEquals(4,rows.size)
            val foodId=rows.single { it.kind=="PRODUCT" }.localId
            val original=fixture.products.observeProduct(NutritionalItemId.Product(foodId)).first()!!
            fixture.products.updateProduct(original.copy(name="Synthetic local edit"))
            val after=fixture.counts()
            assertEquals(LibraryImportResult(0,0,0,4),fixture.importer.apply(source))
            assertEquals(after,fixture.counts())
            assertEquals("Synthetic local edit",fixture.products.observeProduct(original.id).first()!!.name)
            val changed=parsed(source.bundle.copy(products=source.bundle.products.map { it.copy(note="Synthetic changed source") }))
            rejected(LibraryErrorCode.CONFLICT) { fixture.importer.apply(changed) }
            assertEquals(after,fixture.counts())
            fixture.products.deleteProduct(fixture.products.observeProduct(original.id).first()!!)
            val missing=fixture.counts()
            rejected(LibraryErrorCode.CONFLICT) { fixture.importer.apply(source) }
            assertEquals(missing,fixture.counts())
        } finally { fixture.close() }
    }

    @Test fun conflictOnLocallyChangedLinkedDependencyRejectsNewDerivedRecipeAtomically()=runBlocking<Unit> {
        val fixture=Fixture()
        try {
            val first=synthetic().copy(recipes=emptyList(),meals=emptyList())
            fixture.importer.apply(parsed(first))
            val row=fixture.db.libraryIdentityDao.getNamespace(first.namespace).single()
            val original=fixture.products.observeProduct(NutritionalItemId.Product(row.localId)).first()!!
            fixture.products.updateProduct(original.copy(nutritionFacts=original.nutritionFacts.copy(energy=NutrientValue.Complete(101.0))))
            val next=parsed(synthetic().copy(recipes=synthetic().recipes.filter { it.mode==LibraryRecipeMode.DERIVED },meals=emptyList()))
            val before=fixture.counts()
            assertEquals(1,fixture.importer.preview(next).conflicts)
            rejected(LibraryErrorCode.CONFLICT) { fixture.importer.apply(next) }
            assertEquals(before,fixture.counts())
        } finally { fixture.close() }
    }

    @Test fun unresolvedDerivedPortionRejectsDuringPreflightBeforeAnyCatalogOrLedgerWrite()=runBlocking<Unit> {
        val fixture=Fixture()
        try {
            val source=synthetic()
            val bad=parsed(source.copy(recipes=listOf(source.recipes.last().copy(ingredients=listOf(
                LibraryIngredient(LibraryReference(DefinitionKind.PRODUCT,"1"),LibraryPortion(100.0,LibraryPortionUnit.GRAM))))),meals=emptyList()))
            val before=fixture.counts()
            rejected(LibraryErrorCode.BASIS) { fixture.importer.preview(bad) }
            rejected(LibraryErrorCode.BASIS) { fixture.importer.apply(bad) }
            assertEquals(before,fixture.counts())
        } finally { fixture.close() }
    }

    @Test fun sharedDerivedDagReimportAndNewParentCompareFlatStoredDefinitionsWithoutFlowExpansion()=runBlocking<Unit> {
        val fixture=Fixture()
        try {
            withTimeout(15_000) {
                val food=LibraryProduct("1","Synthetic metric Food",facts(1.0),ProductFactBasis.METRIC_100,
                    PortionDescription(metricAmount=100.0,metricUnit=MetricUnit.GRAM))
                val graph=(0 until 20).map { index ->
                    val ingredients=if(index==19)listOf(LibraryIngredient(LibraryReference(DefinitionKind.PRODUCT,"1"),LibraryPortion(100.0,LibraryPortionUnit.GRAM)))
                    else List(2){LibraryIngredient(LibraryReference(DefinitionKind.RECIPE,(index+1).toString()),LibraryPortion(1.0,LibraryPortionUnit.SERVING))}
                    LibraryRecipe(index.toString(),"Synthetic shared node $index",LibraryRecipeMode.DERIVED,1,ingredients=ingredients)
                }
                val bundle=LibraryBundle("foodus-library",1,"synthetic-shared-dag",listOf(food),graph)
                val source=parsed(bundle)
                assertEquals(LibraryImportResult(1,20,0,0),fixture.importer.apply(source))
                val before=fixture.counts()
                assertEquals(20L,before["Recipe"]); assertEquals(39L,before["RecipeIngredient"])
                assertEquals(LibraryImportResult(0,0,0,21),fixture.importer.apply(source))
                assertEquals(before,fixture.counts())
                val parent=LibraryRecipe("extension","Synthetic new parent",LibraryRecipeMode.DERIVED,1,
                    ingredients=listOf(LibraryIngredient(LibraryReference(DefinitionKind.RECIPE,"0"),LibraryPortion(1.0,LibraryPortionUnit.SERVING))))
                assertEquals(LibraryImportResult(0,1,0,21),fixture.importer.apply(parsed(bundle.copy(recipes=graph+parent))))
                assertEquals(21L,fixture.counts()["Recipe"])
            }
        } finally { fixture.close() }
    }

    @Test fun intermediateFailureAndCancellationRollBackDefinitionsHistoryAndIdentityLedger()=runBlocking<Unit> {
        for(cancel in listOf(false,true)) {
            val fixture=Fixture()
            try {
                var inserts=0
                val failing=object:ProductRepository by fixture.products {
                    override suspend fun insertProduct(product:Product):NutritionalItemId.Product {
                        val result=fixture.products.insertProduct(product)
                        if(++inserts==2) {
                            if(cancel)throw CancellationException("Synthetic cancellation")
                            error("Synthetic intermediate failure")
                        }
                        return result
                    }
                }
                val source=synthetic().copy(products=listOf(synthetic().products.single(),synthetic().products.single().copy(externalId="2")),recipes=emptyList(),meals=emptyList())
                val before=fixture.counts()
                val importer=RoomLibraryImporter(fixture.db,failing,fixture.recipes,fixture.meals,fixture.history,ProductTestClock)
                if(cancel) {
                    try { importer.apply(parsed(source)); fail("CANCELLATION_NOT_PROPAGATED") } catch(_:CancellationException) { }
                } else rejected(LibraryErrorCode.PERSISTENCE) { importer.apply(parsed(source)) }
                assertEquals(2,inserts)
                assertEquals(before,fixture.counts())
            } finally { fixture.close() }
        }
    }

    /** Root may stage a bundle in app-private files and pass only its safe basename as this argument. */
    @Test fun stagedBundleOrSyntheticDefaultParsesImportsReimportsAndRetainsStructure()=runBlocking<Unit> {
        val filename=InstrumentationRegistry.getArguments().getString("libraryInputFile")
        val fixture=Fixture()
        try {
            val bytes=if(filename==null)json.encodeToString(synthetic()).encodeToByteArray() else {
                assertTrue("INPUT_NAME",Regex("[A-Za-z0-9][A-Za-z0-9._-]{0,127}").matches(filename))
                try {
                    val file=java.io.File(context.filesDir,filename)
                    assertTrue("INPUT_LIMIT",file.length()<=StructuredLibraryParser.MAX_BYTES)
                    file.readBytes()
                } catch(_:Exception) { throw AssertionError("INPUT_READ") }
            }
            val source=try { fixture.parser.parse(bytes) } catch(error:LibraryImportException) { throw AssertionError("INPUT_"+error.code.name) }
            val date=LocalDate(2026,10,25)
            fixture.db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2801,"Synthetic existing slot",0,0,23,59))
            val manual=RoomManualDiaryEntryRepository(fixture.db.manualDiaryEntryDao)
            manual.insert("Synthetic existing entry",2801,date,NutritionFacts(energy=NutrientValue.Complete(7.0)),ProductTestClock.now())
            val before=manual.observeAll(2801,date).first()
            val first=fixture.importer.apply(source)
            assertTrue("IMPORT_COUNTS",first.products==source.bundle.products.size && first.recipes==source.bundle.recipes.size && first.meals==source.bundle.meals.size)
            verifyReadback(source,fixture)
            val counts=fixture.counts()
            val again=fixture.importer.apply(source)
            assertTrue("REIMPORT_NOOP",again.products==0 && again.recipes==0 && again.meals==0 && again.skipped==source.digests.size)
            assertTrue("REIMPORT_ROWS",counts==fixture.counts())
            assertTrue("DIARY_UNCHANGED",before==manual.observeAll(2801,date).first())
        } finally { fixture.close() }
    }

    private suspend fun verifyReadback(source:ValidatedLibrary,fixture:Fixture) {
        val rows=fixture.db.libraryIdentityDao.getNamespace(source.bundle.namespace).associateBy { LibraryKey(DefinitionKind.valueOf(it.kind),it.externalId) }
        assertTrue("LEDGER_COUNT",rows.size==source.digests.size)
        for(food in source.bundle.products) {
            val row=rows[LibraryKey(DefinitionKind.PRODUCT,food.externalId)] ?: throw AssertionError("PRODUCT_MAPPING")
            val actual=fixture.products.observeProduct(NutritionalItemId.Product(row.localId)).first()
            assertTrue("PRODUCT_PRESENT",actual!=null)
            actual!!
            assertTrue("PRODUCT_TEXT",actual.name==food.name && actual.brand==food.brand && actual.barcode==food.barcode && actual.note==food.note)
            assertTrue("PRODUCT_BASIS",actual.factBasis==food.basis && actual.referencePortion==food.reference)
            assertTrue("PRODUCT_METADATA",actual.nutritionMetadata?.copy(reference=null)==(food.metadata ?: LibraryMetadata()).captured())
            checkFacts("PRODUCT_FACTS",food.nutrition,actual.nutritionFacts,true)
        }
        for(recipe in source.bundle.recipes) {
            val row=rows[LibraryKey(DefinitionKind.RECIPE,recipe.externalId)] ?: throw AssertionError("RECIPE_MAPPING")
            val actual=fixture.recipes.observeRecipe(NutritionalItemId.Recipe(row.localId)).first()
            assertTrue("RECIPE_PRESENT",actual!=null)
            actual!!
            assertTrue("RECIPE_HEADER",actual.name==recipe.name && actual.note==recipe.note && actual.servings==recipe.yield && actual.isReported==(recipe.mode==LibraryRecipeMode.REPORTED))
            assertTrue("RECIPE_PANEL",actual.reportedNutrition==recipe.capturedPanel())
            assertTrue("RECIPE_COMPONENT_COUNT",actual.reportedComponents.size==recipe.components.size)
            for((component,expected)in actual.reportedComponents.zip(recipe.components)) {
                assertTrue("COMPONENT_TEXT",component.name==expected.name && component.portion==expected.portion && component.metadata==expected.metadata?.captured())
                assertTrue("COMPONENT_FACTS",component.facts==expected.nutrition?.toNutritionFacts())
                checkReference(component.reference,expected.reference,source.bundle.namespace)
            }
            assertTrue("RECIPE_INGREDIENT_COUNT",actual.ingredients.size==recipe.ingredients.size)
            for((ingredient,expected)in actual.ingredients.zip(recipe.ingredients)) {
                val mapped=rows[LibraryKey(expected.reference.kind,expected.reference.externalId)] ?: throw AssertionError("INGREDIENT_MAPPING")
                val actualId=when(val typed=ingredient.food.id) {
                    is NutritionalItemId.Product -> typed.id
                    is NutritionalItemId.Recipe -> typed.id
                }
                assertTrue("INGREDIENT_REFERENCE",actualId==mapped.localId &&
                    (ingredient.food.id is NutritionalItemId.Product)==(expected.reference.kind==DefinitionKind.PRODUCT))
                assertTrue("INGREDIENT_PORTION",ingredient.measurement==expected.portion.measurement())
            }
        }
        for(meal in source.bundle.meals) {
            val row=rows[LibraryKey(DefinitionKind.MEAL,meal.externalId)] ?: throw AssertionError("MEAL_MAPPING")
            val actual=fixture.meals.getMeal(MealId(row.localId))
            assertTrue("MEAL_PRESENT",actual!=null)
            actual!!
            assertTrue("MEAL_HEADER",actual.name==meal.name && actual.reportedNutrition==meal.reportedPanel?.captured() && actual.metadata==meal.metadata?.captured())
            assertTrue("MEAL_ITEM_COUNT",actual.items.size==meal.items.size)
            for((item,expected)in actual.items.zip(meal.items)) {
                assertTrue("MEAL_ITEM_TEXT",item.name==expected.name && item.portionDescription==expected.portion)
                assertTrue("MEAL_ITEM_QUANTITY",item.capturedPortion==expected.capturedPortion?.measurement() && item.servingCount==expected.servingCount && item.weightGrams==expected.weightGrams)
                checkFacts("MEAL_ITEM_FACTS",expected.nutrition,item.nutritionFacts,true)
                val expectedMetadata=if(expected.metadata!=null || expected.reference!=null)(expected.metadata ?: LibraryMetadata()).captured()else null
                assertTrue("MEAL_ITEM_METADATA",item.metadata?.copy(reference=null)==expectedMetadata)
                checkReference(item.metadata?.reference,expected.reference,source.bundle.namespace)
            }
        }
    }

    private fun checkReference(actual:DefinitionProvenance?,expected:LibraryReference?,namespace:String) {
        assertTrue("PROVENANCE_REFERENCE",if(expected==null)actual==null else
            actual?.kind==expected.kind && actual.externalId==expected.externalId && actual.namespace==namespace)
    }
    private fun checkFacts(code:String,expected:LibraryFacts,actual:NutritionFacts,encoded:Boolean) {
        val raw=expected.toNutritionFacts()
        val value=if(encoded)toValidatedEntityNutrients(raw).let { toNutritionFacts(it.first,it.second,it.third) }else raw
        for(field in NutritionFactsField.entries)assertTrue(code,value[field]==actual[field])
    }
    private suspend fun rejected(code:LibraryErrorCode,block:suspend()->Any) {
        try { block(); fail("EXPECTED_REJECTION") } catch(error:LibraryImportException) { assertEquals(code,error.code) }
    }

    private inner class Fixture {
        val name="story28-import-"+UUID.randomUUID()+".db"
        val db=Room.databaseBuilder(context,FoodYouDatabase::class.java,name).build()
        val products=RoomProductRepository(db.productDao)
        val recipes=RoomRecipeRepository(db.recipeDao,products)
        val meals=RoomMealRepository(db,db.mealDao)
        val history=RoomNutritionalItemHistoryRepository(db.foodEventDao)
        private val isolated=koinApplication { modules(importExportModule,module {
            single<FoodYouDatabase>{db}; single<ProductRepository>{products}; single<RecipeRepository>{recipes}
            single<MealRepository>{meals}; single<NutritionalItemHistoryRepository>{history}; single<DateProvider>{ProductTestClock}
        }) }
        val importer=isolated.koin.get<LibraryImporter>()
        val parser=isolated.koin.get<StructuredLibraryParser>()
        suspend fun counts():Map<String,Long> {
            products.observeProducts(1,0).first() // Open/validate schema before direct read-only counts.
            return AndroidSQLiteDriver().open(context.getDatabasePath(name).path).use { connection ->
                listOf("Product","Recipe","RecipeIngredient","ReportedRecipeComponent","MealTemplate","MealTemplateItem","FoodEvent","LibraryImportIdentity",
                    "DiaryProduct","DiaryRecipe","DiaryRecipeIngredient","Measurement","ManualDiaryEntry","Meal").associateWith { table ->
                    connection.prepare("SELECT count(*) FROM $table").use { assertTrue(it.step()); it.getLong(0) }
                }
            }
        }
        fun close(){isolated.close();db.close();context.deleteDatabase(name)}
    }
}
