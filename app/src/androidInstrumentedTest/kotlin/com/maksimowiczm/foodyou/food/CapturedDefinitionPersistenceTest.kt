package com.maksimowiczm.foodyou.food

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.ui.food.diary.add.toDiaryFood
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.repository.ProductRepository
import com.maksimowiczm.foodyou.food.domain.usecase.*
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.RoomNutritionalItemDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import com.maksimowiczm.foodyou.importexport.domain.entity.ProductField
import com.maksimowiczm.foodyou.importexport.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Test

/** Captured edits and legacy export use real repository/transaction boundaries. */
class CapturedDefinitionPersistenceTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private fun facts():NutritionFacts {
        var result=NutritionFacts()
        NutritionFactsField.entries.forEachIndexed { index,field -> result=result.withMealNutrient(field,when(index%3){0->0.0;1->null;else->(index+1)*0.000001}) }
        return result.copy(energy=NutrientValue.Complete(123.0),vitaminB12=NutrientValue.Complete(0.000004))
    }
    private fun product()=Product(NutritionalItemId.Product(0),"Synthetic captured Food",null,null,null,false,null,null,
        ProductSource(ProductSource.Type.User),facts(),ProductFactBasis.REFERENCE_SERVING,PortionDescription(2.0,"pieces","about two pieces"),
        NutritionMetadata(percentDailyValues=mapOf("protein" to 0.0),observations=listOf(NutritionObservation("separate-energy",777.0,"kcal"))))

    @Test fun referenceEditRequiresConfirmationAndEncodedOverflowCannotReplaceStoredFactsOrHistory()=runBlocking<Unit> {
        val f=Fixture()
        try {
            val id=f.products.insertProduct(product());val original=f.products.observeProduct(id).first()!!
            val entry=f.entries.insert(Measurement.Serving(2.0),2802,f.date,original.toDiaryFood(),ProductTestClock.now())
            val snapshot=f.entries.observe(entry).first()!!
            assertTrue(f.update.product(original.copy(name="Synthetic renamed Food"),false))
            val renamed=f.products.observeProduct(id).first()!!
            assertEquals(original.nutritionFacts,renamed.nutritionFacts);assertEquals(original.nutritionMetadata,renamed.nutritionMetadata)
            val changed=renamed.copy(referencePortion=renamed.referencePortion!!.copy(amount=3.0,text="three pieces"))
            assertFalse(f.update.product(changed,false));assertEquals(renamed,f.products.observeProduct(id).first())
            assertTrue(f.update.product(changed,true));assertNull(f.products.observeProduct(id).first()!!.weight(Measurement.Serving(1.0)))
            val before=f.products.observeProduct(id).first()!!
            assertFalse(f.update.product(before.copy(nutritionFacts=before.nutritionFacts.copy(vitaminA=NutrientValue.Complete(1e303))),false))
            assertEquals(before,f.products.observeProduct(id).first());assertEquals(snapshot,f.entries.observe(entry).first())
        } finally { f.close() }
    }

    @Test fun reportedCompositionInvalidatesUntilConfirmedWhileReorderRenameAndHistoryStayIndependent()=runBlocking<Unit> {
        val f=Fixture()
        try {
            val components=listOf(ReportedRecipeComponent("Synthetic first",PortionDescription(text="about half a bowl")),ReportedRecipeComponent("Synthetic second"))
            val id=f.recipes.insertRecipe(Recipe(NutritionalItemId.Recipe(0),"Synthetic reported Recipe",null,emptyList(),null,false,
                ReportedNutrition(facts(),ReportedBasis.REFERENCE_SERVING,metadata=NutritionMetadata(notes="Synthetic panel evidence")),components))
            val original=f.recipes.observeRecipe(id).first()!!
            val entry=f.entries.insert(Measurement.Serving(1.0),2802,f.date,original.toDiaryFood(),ProductTestClock.now())
            val snapshot=f.entries.observe(entry).first()!!
            assertTrue(f.update.recipe(original.copy(name="Synthetic renamed Recipe",reportedComponents=components.reversed()),false))
            var current=f.recipes.observeRecipe(id).first()!!
            assertFalse(current.reportedNutrition!!.needsReview)
            assertEquals(original.reportedNutrition!!.facts,current.reportedNutrition!!.facts)
            assertTrue(f.update.recipe(current.copy(reportedComponents=current.reportedComponents+ReportedRecipeComponent("Synthetic extra")),false))
            current=f.recipes.observeRecipe(id).first()!!
            assertTrue(current.reportedNutrition!!.needsReview);assertNull(current.nutritionFor(Measurement.Serving(1.0)))
            assertTrue(f.update.recipe(current.copy(note="Synthetic unrelated note"),false))
            assertTrue(f.recipes.observeRecipe(id).first()!!.reportedNutrition!!.needsReview)
            assertTrue(f.update.recipe(f.recipes.observeRecipe(id).first()!!,true))
            current=f.recipes.observeRecipe(id).first()!!
            assertFalse(current.reportedNutrition!!.needsReview);assertEquals(123.0,current.nutritionFor(Measurement.Serving(1.0))!!.energy.value)
            assertNull(current.unpack(Measurement.Serving(1.0)))
            assertTrue(f.update.recipe(current.copy(servings=4),false))
            assertTrue(f.recipes.observeRecipe(id).first()!!.reportedNutrition!!.needsReview)
            assertEquals(snapshot,f.entries.observe(entry).first())
        } finally { f.close() }
    }

    @Test fun removingMetricReferenceFiltersOldHistorySuggestionsAndKeepsDeclaredServing()=runBlocking<Unit> {
        val f=Fixture()
        try {
            val known=product().copy(referencePortion=PortionDescription(2.0,"pieces",metricAmount=20.0,metricUnit=MetricUnit.GRAM),servingWeight=20.0)
            val id=f.products.insertProduct(known)
            val history=RoomNutritionalItemMeasurementSuggestionRepository(f.db.measurementSuggestionDao)
            history.insert(id,Measurement.Gram(55.0))
            val observe=ObserveMeasurementSuggestionsUseCase(ObserveNutritionalItemUseCase(f.products,f.recipes),history)
            assertTrue(observe.observe(id,5).first().contains(Measurement.Gram(55.0)))
            val original=f.products.observeProduct(id).first()!!
            assertTrue(f.update.product(original.copy(referencePortion=original.referencePortion!!.copy(metricAmount=null,metricUnit=null),servingWeight=null),true))
            val suggestions=observe.observe(id,5).first()
            assertTrue(suggestions.isNotEmpty());assertTrue(suggestions.all { it is Measurement.Serving })
            assertNull(f.products.observeProduct(id).first()!!.nutritionFor(Measurement.Gram(55.0)))
        } finally { f.close() }
    }

    @Test fun csvConvertsAll43FieldsWithRealMetricAndPreflightsLateUnsupportedRowsBeforeAnyEmission()=runBlocking<Unit> {
        val f=Fixture()
        try {
            val known=product().copy(referencePortion=PortionDescription(2.0,"pieces",metricAmount=20.0,metricUnit=MetricUnit.GRAM),servingWeight=20.0)
            val id=f.products.insertProduct(known);val stored=f.products.observeProduct(id).first()!!
            val fields=NutritionFactsField.entries.map { nutrient -> ProductField.entries.single { it.name==nutrient.name } }+ProductField.ServingWeight
            val lines=ExportCsvProductsUseCaseImpl(f.products,f.db).export(fields).toList()
            assertEquals(2,lines.size)
            val values=lines[1].split(",")
            NutritionFactsField.entries.forEachIndexed { index,field ->
                val expected=stored.nutritionFacts[field].value?.times(5.0)
                if(expected==null)assertEquals("",values[index]) else assertEquals(expected,values[index].toDouble(),0.0)
            }
            assertEquals(20.0,values.last().toDouble(),0.0)
            val late=List(250){stored}+product()
            val paged=object:ProductRepository by f.products {
                override fun observeProducts(limit:Int,offset:Int)=flowOf(late.drop(offset).take(limit))
            }
            val emitted=mutableListOf<String>()
            try { ExportCsvProductsUseCaseImpl(paged,f.db).export(fields).collect { emitted.add(it) };fail("Unknown metric must reject CSV") }
            catch(_:CsvMetricBasisUnavailableException){}
            assertTrue(emitted.isEmpty())
            assertEquals(stored,f.products.observeProduct(id).first())
        } finally { f.close() }
    }

    private inner class Fixture {
        val db=Room.inMemoryDatabaseBuilder(context,FoodYouDatabase::class.java).build()
        val products=RoomProductRepository(db.productDao)
        val recipes=RoomRecipeRepository(db.recipeDao,products)
        val update=UpdateCapturedDefinitionUseCase(products,recipes,db)
        val entries=RoomNutritionalItemDiaryEntryRepository(db,db.measurementDao)
        val date=LocalDate(2026,10,26)
        init { runBlocking { db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2802,"Synthetic captured edit slot",0,0,23,59)) } }
        fun close(){db.close()}
    }
}
