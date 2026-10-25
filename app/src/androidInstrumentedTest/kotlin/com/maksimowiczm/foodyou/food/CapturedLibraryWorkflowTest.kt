package com.maksimowiczm.foodyou.food

import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.compose.*
import androidx.room.Room
import androidx.test.core.app.launchActivity
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.android.DeveloperActivity
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.ui.food.component.*
import com.maksimowiczm.foodyou.app.ui.food.diary.add.*
import com.maksimowiczm.foodyou.app.ui.food.diary.update.UpdateEntryScreen
import com.maksimowiczm.foodyou.app.ui.food.diary.update.UpdateNutritionalItemDiaryEntryViewModel
import com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.ScanTestDriver
import com.maksimowiczm.foodyou.app.ui.food.product.UpdateProductScreen
import com.maksimowiczm.foodyou.app.ui.food.product.update.UpdateProductViewModel
import com.maksimowiczm.foodyou.app.ui.food.recipe.*
import com.maksimowiczm.foodyou.app.ui.home.meals.card.*
import com.maksimowiczm.foodyou.common.domain.event.*
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.*
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.usecase.*
import com.maksimowiczm.foodyou.food.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.junit.Assert.*
import org.junit.Test
import org.koin.compose.KoinIsolatedContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Real mode routing/editors/details with the accepted bounded accessibility driver. */
@SdkSuppress(minSdkVersion=30)
class CapturedLibraryWorkflowTest {
    private val instrumentation=InstrumentationRegistry.getInstrumentation()
    private var decor:View?=null
    private val driver=ScanTestDriver(instrumentation,{decor},{instrumentation.targetContext.packageName},{null})
    private lateinit var db:FoodYouDatabase
    private lateinit var products:RoomProductRepository
    private lateinit var recipes:RoomRecipeRepository
    private lateinit var diary:RoomNutritionalItemDiaryEntryRepository
    private lateinit var productId:NutritionalItemId.Product
    private lateinit var recipeId:NutritionalItemId.Recipe
    private lateinit var unavailableId:NutritionalItemId.Recipe
    private val date=LocalDate(2026,10,26)
    private var pickerHasMass by mutableStateOf(true)
    private var pickerRevision by mutableStateOf(0)
    private val pickerValue=java.util.concurrent.atomic.AtomicReference<Measurement>()

    @Test fun capturedFoodSaveRequiresReferenceConfirmationAndCancelPreservesEveryFact()=withFlow {
        val before=product()
        driver.clickText("Edit captured Food")
        driver.setField(Res.string.product_name,"Synthetic renamed captured Food")
        driver.setField(Res.string.captured_reference_amount,"3")
        click(Res.string.action_save)
        driver.findText(label(Res.string.captured_invalid))
        assertEquals(before,product())
        click(Res.string.captured_confirm_reference)
        click(Res.string.action_save)
        driver.findText("Synthetic captured library hub")
        val saved=product()
        assertEquals("Synthetic renamed captured Food",saved.name)
        assertEquals(3.0,saved.referencePortion!!.amount)
        assertEquals(before.nutritionFacts,saved.nutritionFacts);assertEquals(before.nutritionMetadata,saved.nutritionMetadata)
        assertNull(saved.weight(Measurement.Serving(1.0)))
        driver.clickText("Edit captured Food")
        driver.setField(Res.string.headline_note,"Synthetic discarded note")
        click(Res.string.action_cancel)
        driver.findText("Synthetic captured library hub")
        assertEquals(saved,product())
    }

    @Test fun componentEditRetainsReportedFactsButBlocksLoggingUntilExplicitConfirmationAndLeavesHistory()=withFlow {
        val before=recipe()
        val snapshots=entries()
        driver.clickText("Edit captured Recipe")
        click(Res.string.captured_component_edit)
        driver.setField(Res.string.captured_component_name,"Synthetic changed component")
        click(Res.string.meals_apply_changes)
        click(Res.string.action_save)
        driver.findText("Synthetic captured library hub")
        val changed=recipe()
        assertEquals("Synthetic changed component",changed.reportedComponents.single().name)
        assertTrue(changed.reportedNutrition!!.needsReview)
        assertNull(changed.nutritionFor(Measurement.Serving(1.0)))
        assertEquals(before.reportedNutrition!!.facts,changed.reportedNutrition!!.facts)
        assertEquals(snapshots,entries())
        driver.clickText("Edit captured Recipe")
        click(Res.string.captured_confirm_panel)
        click(Res.string.action_save)
        driver.findText("Synthetic captured library hub")
        assertFalse(recipe().reportedNutrition!!.needsReview)
        assertEquals(345.0,recipe().nutritionFor(Measurement.Serving(1.0))!!.energy.value)
        assertNull(recipe().unpack(Measurement.Serving(1.0)))
        assertEquals(snapshots,entries())
    }

    @Test fun unavailableReportedDetailKeepsFactsReadableAndSaveUnavailable()=withFlow {
        val snapshots=entries()
        driver.clickText("View unavailable Recipe")
        driver.findText(label(Res.string.captured_unavailable))
        click(Res.string.captured_show_details)
        driver.findText(label(Res.string.unit_energy)+": 345.0 "+label(Res.string.unit_kcal))
        driver.findText(label(Res.string.vitamin_b12)+": 4.0 "+label(Res.string.unit_microgram_short))
        driver.findText(label(Res.string.error_portion_save))
        assertEquals(snapshots,entries())
        val node=driver.findText(label(Res.string.portion_amount_to_log))
        val root=ancestors(node).last()
        val save=label(Res.string.action_save)
        assertTrue(driver.descendants(root).none { it.isVisibleToUser && it.isEnabled && it.isClickable &&
            (it.contentDescription?.toString()==save || it.text?.toString()?.lines()?.contains(save)==true) })
    }

    @Test fun pickerReplacesRemovedMetricTypeAndPreservesSupportedUserEdits()=withFlow {
        driver.clickText("Picker transition")
        setAmount("55")
        driver.await("synthetic initial gram amount"){pickerValue.get()==Measurement.Gram(55.0)}
        driver.clickText("Remove known metric")
        driver.await("removed metric selects supported serving"){pickerValue.get()==Measurement.Serving(1.0)}
        setAmount("3")
        driver.await("supported serving edit"){pickerValue.get()==Measurement.Serving(3.0)}
        driver.clickText("Unrelated revision")
        driver.await("supported edit survives recomposition"){pickerValue.get()==Measurement.Serving(3.0)}
        val node=driver.findText("Synthetic picker transition")
        val root=ancestors(node).last()
        assertTrue(driver.descendants(root).none { it.isVisibleToUser && it.text?.toString()=="55 g" })
    }

    @Test fun initiallyUnsupportedExplicitPortionStaysInvalidUntilConsciousSupportedChoice()=withFlow {
        driver.clickText("Picker initial unsupported")
        driver.await("explicit unsupported request retained"){pickerValue.get()==Measurement.Serving(2.0)}
        driver.findText(label(Res.string.error_portion_save))
        driver.clickText("Unrelated revision")
        driver.await("initial unsupported request survives recomposition"){pickerValue.get()==Measurement.Serving(2.0)}
        driver.clickText("Choose supported grams")
        driver.await("explicit supported choice accepted"){pickerValue.get()==Measurement.Gram(2.0)}
    }

    @Test fun capturedDiaryRowsShowKnownEnergyUnknownMacrosAndRawContextWhileLegacyAndInvalidStayErrors()=withFlow {
        driver.clickText("Captured diary rows")
        fun texts(context:String)=driver.descendants(driver.findText(context)).flatMap { it.text?.toString()?.lines().orEmpty().asSequence() }.toList()
        val unknown=label(Res.string.captured_unknown_value)
        val missing=label(Res.string.error_food_is_missing_required_fields)
        val measurementError=label(Res.string.error_measurement_error)
        val kcal=label(Res.string.unit_kcal)
        for((context,energy,raw)in listOf(Triple("Synthetic captured manual row","90", "about three pieces"),
            Triple("Synthetic captured Food row","246","two pieces, mass unknown"),Triple("Synthetic captured Recipe row","228","reported bowl, mass unknown"))) {
            val visible=texts(context)
            assertTrue(context,visible.contains(energy+" "+kcal))
            assertTrue(context,visible.count { it==unknown }>=2)
            assertTrue(context,visible.contains(raw))
            assertFalse(context,visible.contains(missing));assertFalse(context,visible.contains(measurementError))
            if(context!="Synthetic captured Recipe row")assertTrue(context,visible.contains("0 "+label(Res.string.unit_gram_short)))
        }
        assertTrue(texts("Synthetic invalid captured row").contains(missing))
        assertTrue(texts("Synthetic legacy manual row").contains(missing))
        assertTrue(texts("Synthetic legacy Food row").contains(missing))
        assertTrue(texts("Synthetic unsupported captured row").contains(measurementError))
    }

    @Test fun incompleteMealConstituentTotalAndReportedPanelRemainSeparatelyLabeled()=withFlow {
        driver.clickText("Partial Meal panel")
        driver.findText(formatted(Res.string.captured_meal_items_energy,label(Res.string.captured_unknown_value)))
        driver.findText(formatted(Res.string.captured_meal_reported_energy,formatted(Res.string.captured_incomplete_value,"999.0")))
        click(Res.string.captured_show_details)
        driver.findText(label(Res.string.unit_energy)+": "+formatted(Res.string.captured_incomplete_value,"999.0 "+label(Res.string.unit_kcal)))
    }

    @Test fun reportedHistoricalPortionHasNoUnpackAndEditingAmountKeepsCapturedPanel()=withFlow {
        val original=entries().single()
        driver.clickText("Edit captured history")
        driver.findText(label(Res.string.captured_unpack_unavailable))
        val node=driver.findText(label(Res.string.portion_amount_to_log))
        val root=ancestors(node).last()
        val unpack=label(Res.string.action_unpack)
        assertTrue(driver.descendants(root).none { it.isVisibleToUser && it.text?.toString()?.lines()?.contains(unpack)==true })
        setAmount("2")
        click(Res.string.action_save)
        driver.findText("Synthetic captured library hub")
        val changed=entries().single()
        assertEquals(original.id,changed.id)
        assertEquals(original.food,changed.food)
        assertEquals(Measurement.Serving(2.0),changed.measurement)
        assertEquals(690.0,changed.nutritionFacts.energy.value)
    }

    private fun product()=runBlocking { products.observeProduct(productId).first()!! }
    private fun recipe()=runBlocking { recipes.observeRecipe(recipeId).first()!! }
    private fun entries()=runBlocking { diary.observeAll(2803,date).first() }
    private fun label(resource:StringResource)=driver.label(resource)
    private fun formatted(resource:StringResource,vararg args:Any)=runBlocking { org.jetbrains.compose.resources.getString(resource,*args) }
    private fun click(resource:StringResource)=driver.clickText(label(resource))
    private fun ancestors(node:AccessibilityNodeInfo):Sequence<AccessibilityNodeInfo> = sequence {
        var current:AccessibilityNodeInfo?=node
        val seen=mutableSetOf<AccessibilityNodeInfo>()
        repeat(32){val next=current ?: return@sequence;check(seen.add(next));yield(next);current=next.parent}
        check(current==null)
    }
    private fun amountField():AccessibilityNodeInfo = requireNotNull(ancestors(driver.findText(label(Res.string.measurement_amount))).firstOrNull { it.isEditable })
    private fun setAmount(value:String) {
        assertTrue(amountField().performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,value) }))
        instrumentation.waitForIdleSync()
        driver.await("synthetic portion text updated"){amountField().text?.toString()==value}
    }

    private fun withFlow(checkFlow:()->Unit) {
        pickerHasMass=true;pickerRevision=0;pickerValue.set(null)
        db=Room.inMemoryDatabaseBuilder(instrumentation.targetContext,FoodYouDatabase::class.java).build()
        products=RoomProductRepository(db.productDao);recipes=RoomRecipeRepository(db.recipeDao,products)
        diary=RoomNutritionalItemDiaryEntryRepository(db,db.measurementDao)
        val observe=ObserveNutritionalItemUseCase(products,recipes)
        val captured=UpdateCapturedDefinitionUseCase(products,recipes,db)
        val history=RoomNutritionalItemHistoryRepository(db.foodEventDao)
        val slots=RoomDiarySlotRepository(db.diarySlotDao)
        val suggestions=ObserveMeasurementSuggestionsUseCase(observe,RoomNutritionalItemMeasurementSuggestionRepository(db.measurementSuggestionDao))
        val delete=DeleteNutritionalItemUseCase(products,recipes,db,ProductTestLogger)
        val create=CreateNutritionalItemDiaryEntryUseCase(slots,diary,db,ProductTestClock,ProductTestLogger)
        val unpack=UnpackNutritionalItemDiaryEntryUseCase(diary,slots,db,ProductTestClock,ProductTestLogger)
        val events=object:EventBus {override val events:Flow<IntegrationEvent> = emptyFlow();override fun publish(integrationEvent:IntegrationEvent)=Unit}
        runBlocking {
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2803,"Synthetic captured slot",0,0,23,59))
            var facts=NutritionFacts()
            NutritionFactsField.entries.forEachIndexed { index,field -> facts=facts.withMealNutrient(field,if(index%3==0)0.0 else if(index%3==1)null else(index+1)*0.000001) }
            facts=facts.copy(energy=NutrientValue.Complete(345.0),vitaminB12=NutrientValue.Complete(0.000004))
            productId=products.insertProduct(Product(NutritionalItemId.Product(0),"Synthetic captured Food",null,null,null,false,null,null,
                ProductSource(ProductSource.Type.User),facts,ProductFactBasis.REFERENCE_SERVING,PortionDescription(2.0,"pieces","about two pieces"),NutritionMetadata(notes="Synthetic evidence")))
            recipeId=recipes.insertRecipe(Recipe(NutritionalItemId.Recipe(0),"Synthetic captured Recipe",null,emptyList(),null,false,
                ReportedNutrition(facts,ReportedBasis.REFERENCE_SERVING),listOf(ReportedRecipeComponent("Synthetic component",PortionDescription(text="a small bowl")))))
            unavailableId=recipes.insertRecipe(recipes.observeRecipe(recipeId).first()!!.copy(id=NutritionalItemId.Recipe(0),name="Synthetic unavailable Recipe",reportedNutrition=ReportedNutrition(facts,ReportedBasis.UNAVAILABLE)))
            diary.insert(Measurement.Serving(1.0),2803,date,recipes.observeRecipe(recipeId).first()!!.toDiaryFood(),ProductTestClock.now())
        }
        val isolated=koinApplication { modules(module {
            single { observe }
            viewModel { CapturedDefinitionViewModel(captured) }
            viewModel { (id:NutritionalItemId.Product)->UpdateProductViewModel(observe,UpdateProductUseCase(products,db,ProductTestLogger),id) }
            viewModel { (id:NutritionalItemId.Recipe)->UpdateRecipeViewModel(id,observe,UpdateRecipeUseCase(recipes,products,db,ProductTestLogger)) }
            viewModel { (id:NutritionalItemId)->AddEntryViewModel(unpack,create,observe,history,delete,suggestions,slots,ProductTestClock,events,id) }
            viewModel { (id:NutritionalItemDiaryEntryId)->UpdateNutritionalItemDiaryEntryViewModel(id,
                UpdateNutritionalItemDiaryEntryUseCase(slots,diary,ProductTestClock,db,ProductTestLogger),unpack,diary,slots,ProductTestClock) }
        }) }
        val automation=instrumentation.uiAutomation
        val originalInfo=automation.serviceInfo
        val flags=originalInfo.flags
        try {
            automation.serviceInfo=automation.serviceInfo.apply { this.flags=this.flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS }
            launchActivity<DeveloperActivity>().use { scenario ->
                scenario.onActivity { activity ->
                    decor=activity.window.decorView
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity.setContent { KoinIsolatedContext(context=isolated){MaterialTheme { Fixture() }} }
                }
                instrumentation.waitForIdleSync()
                driver.findText("Synthetic captured library hub")
                checkFlow()
            }
        } finally {isolated.close();db.close();decor=null;automation.serviceInfo=originalInfo.apply {this.flags=flags} }
    }

    @Composable private fun Fixture() {
        val nav=rememberNavController()
        NavHost(nav,startDestination="hub") {
            composable("hub") { Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                Text("Synthetic captured library hub")
                Button(onClick={nav.navigate("food")}){Text("Edit captured Food")}
                Button(onClick={nav.navigate("recipe")}){Text("Edit captured Recipe")}
                Button(onClick={nav.navigate("detail")}){Text("View unavailable Recipe")}
                Button(onClick={nav.navigate("picker")}){Text("Picker transition")}
                Button(onClick={nav.navigate("picker-initial")}){Text("Picker initial unsupported")}
                Button(onClick={nav.navigate("partial")}){Text("Partial Meal panel")}
                Button(onClick={nav.navigate("history")}){Text("Edit captured history")}
                Button(onClick={nav.navigate("captured-rows")}){Text("Captured diary rows")}
            } }
            composable("food") { UpdateProductScreen({nav.popBackStack()},{nav.popBackStack()},productId) }
            composable("recipe") { UpdateRecipeScreen({nav.popBackStack()},{error("Synthetic unavailable action")},{nav.popBackStack()},{error("Offline")},{error("Offline")},recipeId) }
            composable("detail") { AddEntryScreen({nav.popBackStack()},{error("Synthetic unavailable action")},{error("Unavailable nutrition must not log")},{error("Synthetic unavailable action")},{_,_->error("Synthetic unavailable action")},unavailableId,2803,date,null,this) }
            composable("history") {
                val items by diary.observeAll(2803,date).collectAsState(emptyList())
                if(items.isNotEmpty())UpdateEntryScreen(items.single().id.value,{nav.popBackStack()},{nav.popBackStack()},this)
            }
            composable("partial") { Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())) {
                CapturedMealPanel(Meal(MealId(1),"Synthetic partial Meal",kotlinx.datetime.LocalDateTime(2026,10,26,12,0),listOf(
                    MealItem("Synthetic known",NutritionFacts(energy=NutrientValue.Complete(90.0))),MealItem("Synthetic unknown",NutritionFacts())),
                    ReportedNutrition(NutritionFacts(energy=NutrientValue.Incomplete(999.0)),ReportedBasis.WHOLE_ITEM)))
            } }
            composable("picker") { Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                Text("Synthetic picker transition")
                val types=if(pickerHasMass)listOf(MeasurementType.Gram,MeasurementType.Serving)else listOf(MeasurementType.Serving)
                val selected=if(pickerHasMass)Measurement.Gram(55.0)else Measurement.Serving(1.0)
                val state=rememberMeasurementPickerState(listOf(Measurement.Gram(55.0),Measurement.Serving(1.0)),types,selected)
                MeasurementPicker(state)
                SideEffect {pickerValue.set(state.measurement)}
                Text("Synthetic revision "+pickerRevision)
                Button(onClick={pickerHasMass=false}){Text("Remove known metric")}
                Button(onClick={pickerRevision++}){Text("Unrelated revision")}
            } }
            composable("picker-initial") { Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                Text("Synthetic initial unsupported request")
                val state=rememberMeasurementPickerState(listOf(Measurement.Gram(100.0)),listOf(MeasurementType.Gram),Measurement.Serving(2.0))
                MeasurementPicker(state)
                SideEffect {pickerValue.set(state.measurement)}
                if(state.measurement.type !in state.possibleTypes)Text(org.jetbrains.compose.resources.stringResource(Res.string.error_portion_save))
                Text("Synthetic revision "+pickerRevision)
                Button(onClick={pickerRevision++}){Text("Unrelated revision")}
                Button(onClick={state.type=MeasurementType.Gram}){Text("Choose supported grams")}
            } }
            composable("captured-rows") { Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())) {
                val rows=remember { capturedRowCases() }
                rows.forEach { (context,row) -> DiarySlotFoodListItem(row,MaterialTheme.colorScheme.surface,MaterialTheme.colorScheme.onSurface,
                    MaterialTheme.shapes.medium,Modifier.semantics { contentDescription=context }) }
            } }
        }
    }

    private fun capturedRowCases():List<Pair<String,DiarySlotEntryModel>> {
        val now=kotlinx.datetime.LocalDateTime(2026,10,26,12,0)
        val facts=NutritionFacts(energy=NutrientValue.Complete(90.0),proteins=NutrientValue.Complete(0.0))
        val manual=ManualDiaryEntry(ManualDiaryEntryId(1),2803,date,"Synthetic partial Meal constituent",facts,now,now,
            portionDescription=PortionDescription(3.0,"pieces","about three pieces"),nutritionMetadata=NutritionMetadata(notes="Synthetic evidence"))
        val food=ProductSnapshot("Synthetic reference snapshot",facts.copy(energy=NutrientValue.Complete(123.0)),null,null,false,
            ProductSource(ProductSource.Type.User),null,ProductFactBasis.REFERENCE_SERVING,PortionDescription(2.0,"pieces","two pieces, mass unknown"))
        val recipe=RecipeSnapshot("Synthetic reported snapshot",null,emptyList(),false,null,2,
            ReportedNutrition(NutritionFacts(energy=NutrientValue.Complete(456.0)),ReportedBasis.REFERENCE_SERVING,
                PortionDescription(1.0,"bowl","reported bowl, mass unknown")))
        fun measured(snapshot:NutritionalItemSnapshot,measurement:Measurement)=NutritionalItemDiaryEntry(NutritionalItemDiaryEntryId(1),2803,date,measurement,snapshot,now,now).toDiarySlotEntryModel()
        return listOf(
            "Synthetic captured manual row" to manual.toDiarySlotEntryModel(),
            "Synthetic captured Food row" to measured(food,Measurement.Serving(2.0)),
            "Synthetic captured Recipe row" to measured(recipe,Measurement.Serving(0.5)),
            "Synthetic invalid captured row" to manual.copy(nutritionFacts=facts.copy(vitaminA=NutrientValue.Complete(Double.POSITIVE_INFINITY))).toDiarySlotEntryModel(),
            "Synthetic legacy manual row" to manual.copy(portionDescription=null,nutritionMetadata=null).toDiarySlotEntryModel(),
            "Synthetic legacy Food row" to measured(food.copy(factBasis=ProductFactBasis.METRIC_100,referencePortion=null),Measurement.Gram(100.0)),
            "Synthetic unsupported captured row" to measured(food,Measurement.Gram(100.0)),
        )
    }

}
