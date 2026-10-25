package com.maksimowiczm.foodyou.app.ui.food.product.update

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.food.component.*
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.food.domain.entity.Product
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CapturedProductEditor(product:Product,onBack:()->Unit,onSaved:()->Unit,modifier:Modifier=Modifier) {
    val viewModel:CapturedDefinitionViewModel=koinViewModel()
    var name by remember(product){mutableStateOf(product.name)}
    var brand by remember(product){mutableStateOf(product.brand.orEmpty())}
    var barcode by remember(product){mutableStateOf(product.barcode.orEmpty())}
    var note by remember(product){mutableStateOf(product.note.orEmpty())}
    var packageMetric by remember(product){mutableStateOf(product.packageWeight?.toString().orEmpty())}
    var servingMetric by remember(product){mutableStateOf(product.servingWeight?.toString().orEmpty())}
    val reference=remember(product){CapturedReferenceForm(product.referencePortion)}
    val energy=LocalEnergyFormatter.current
    val nutrition=remember(product,energy){CapturedNutritionForm(product.nutritionFacts,energy)}
    var quantityUnit by remember(product){mutableStateOf<MetricUnit?>(if(product.packageWeight!=null || product.factBasis==ProductFactBasis.METRIC_100 && product.servingWeight!=null)
        if(product.isLiquid)MetricUnit.MILLILITER else MetricUnit.GRAM else null)}
    var confirmedSignature by remember(product){mutableStateOf<List<String?>?>(null)}
    fun signature()=listOf(reference.amount,reference.unitText,reference.text,reference.metric,reference.metricUnit?.name,packageMetric,servingMetric,quantityUnit?.name)
    fun updatePackage(text:String) {
        if(text.isNotBlank() && quantityUnit==null)quantityUnit=reference.metricUnit ?: if(product.isLiquid)MetricUnit.MILLILITER else MetricUnit.GRAM
        packageMetric=text
        if(text.isBlank() && (product.factBasis==ProductFactBasis.REFERENCE_SERVING || servingMetric.isBlank()))quantityUnit=null
    }
    fun updateServing(text:String) {
        if(text.isNotBlank() && quantityUnit==null)quantityUnit=reference.metricUnit ?: if(product.isLiquid)MetricUnit.MILLILITER else MetricUnit.GRAM
        servingMetric=text
        if(text.isBlank() && packageMetric.isBlank())quantityUnit=null
    }
    var failed by remember(product){mutableStateOf(false)}
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val latestSaved by rememberUpdatedState(onSaved)
    LaunchedCollectWithLifecycle(viewModel.results) { if(it)latestSaved()else failed=true }
    fun positive(text:String):Double?=text.toDoubleOrNull()?.takeIf { it.isFinite() && it>0.0 }
    fun optional(text:String,old:String?)=if(text==old.orEmpty())old else text.ifEmpty { null }
    fun save() {
        val facts=nutrition.candidate()
        if(!reference.isValid || facts==null || name.isBlank() || name.length>512 || brand.length>512 || barcode.length>256 || note.length>16_384 ||
            packageMetric.isNotBlank() && positive(packageMetric)==null || servingMetric.isNotBlank() && positive(servingMetric)==null) {failed=true;return}
        val portion=reference.candidate()
        val liquid=(portion?.metricUnit ?: quantityUnit)?.let { it==MetricUnit.MILLILITER } ?: product.isLiquid
        val serving=if(product.factBasis==ProductFactBasis.REFERENCE_SERVING)portion?.metricAmount else if(servingMetric.isBlank())null else positive(servingMetric)
        viewModel.product(product.copy(name=name.trim(),brand=optional(brand,product.brand),barcode=optional(barcode,product.barcode),note=optional(note,product.note),
            nutritionFacts=facts,referencePortion=portion,isLiquid=liquid,
            packageWeight=if(packageMetric.isBlank())null else positive(packageMetric),servingWeight=serving),confirmedSignature==signature())
    }
    Scaffold(modifier=modifier,topBar={TopAppBar(title={Text(stringResource(Res.string.headline_edit_product))},navigationIcon={ArrowBackIconButton(onBack)},actions={
        TextButton(onClick=onBack){Text(stringResource(Res.string.action_cancel))}
        TextButton(onClick=::save,enabled=!saving,modifier=Modifier.testTag("captured-food-save")){Text(stringResource(Res.string.action_save))}
    })}) { padding ->
        LazyColumn(Modifier.fillMaxSize().testTag("captured-food-editor"),contentPadding=PaddingValues(16.dp,padding.calculateTopPadding()+8.dp,16.dp,padding.calculateBottomPadding()+16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            if(failed)item{Text(stringResource(Res.string.captured_invalid),color=MaterialTheme.colorScheme.error)}
            item{CapturedTextField(name,{name=it},stringResource(Res.string.product_name),"captured-food-name")}
            item{CapturedTextField(brand,{brand=it},stringResource(Res.string.product_brand),"captured-food-brand")}
            item{CapturedTextField(barcode,{barcode=it},stringResource(Res.string.product_barcode),"captured-food-barcode")}
            item{CapturedTextField(note,{note=it},stringResource(Res.string.headline_note),"captured-food-note")}
            item {
                Text(stringResource(if(product.factBasis==ProductFactBasis.REFERENCE_SERVING)Res.string.captured_reference_serving else Res.string.captured_metric_100),style=MaterialTheme.typography.titleMedium)
                CapturedReferenceFields(reference,"captured-food-reference-",quantityUnit?.let { listOf(null,it) } ?: listOf(null,MetricUnit.GRAM,MetricUnit.MILLILITER),product.factBasis==ProductFactBasis.METRIC_100)
                val metricUnit=quantityUnit ?: reference.metricUnit ?: if(product.isLiquid)MetricUnit.MILLILITER else MetricUnit.GRAM
                val unit=stringResource(if(metricUnit==MetricUnit.MILLILITER)Res.string.unit_milliliter_short else Res.string.unit_gram_short)
                CapturedTextField(packageMetric,::updatePackage,stringResource(Res.string.product_package_weight)+" ("+unit+")","captured-food-package",true)
                if(product.factBasis==ProductFactBasis.METRIC_100)CapturedTextField(servingMetric,::updateServing,stringResource(Res.string.product_reference_serving)+" ("+unit+")","captured-food-serving",true)
                Row(Modifier.fillMaxWidth().toggleable(confirmedSignature==signature(),role=Role.Checkbox,onValueChange={confirmedSignature=if(it)signature()else null})) {
                    Checkbox(confirmedSignature==signature(),null)
                    Text(stringResource(Res.string.captured_confirm_reference),Modifier.padding(top=12.dp))
                }
                CapturedMetadataView(product.nutritionMetadata)
            }
            capturedNutritionFields(nutrition,"captured-food-nutrient-")
        }
    }
}
