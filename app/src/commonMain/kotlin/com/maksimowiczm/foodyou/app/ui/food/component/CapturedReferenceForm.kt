package com.maksimowiczm.foodyou.app.ui.food.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.common.domain.food.*
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

internal class CapturedReferenceForm(private val original:PortionDescription?) {
    var amount by mutableStateOf(original?.amount?.toString().orEmpty())
    var unitText by mutableStateOf(original?.unitText.orEmpty())
    var text by mutableStateOf(original?.text.orEmpty())
    var metric by mutableStateOf(original?.metricAmount?.toString().orEmpty())
    var metricUnit by mutableStateOf(original?.metricUnit)
    val isEmpty get()=amount.isBlank() && unitText.isBlank() && text.isBlank() && metric.isBlank() && metricUnit==null
    val isValid get()=(amount.isBlank() || positive(amount)!=null) &&
        (metric.isBlank() && metricUnit==null || positive(metric)!=null && metricUnit!=null) &&
        unitText.length<=256 && text.length<=16_384
    fun candidate():PortionDescription? {
        if(!isValid)return null
        if(original==null && isEmpty)return null
        return PortionDescription(
            if(amount==original?.amount?.toString().orEmpty())original?.amount else if(amount.isBlank())null else positive(amount),
            if(unitText==original?.unitText.orEmpty())original?.unitText else unitText.ifEmpty { null },
            if(text==original?.text.orEmpty())original?.text else text.ifEmpty { null },
            if(metric==original?.metricAmount?.toString().orEmpty())original?.metricAmount else if(metric.isBlank())null else positive(metric),metricUnit)
    }
    private fun positive(text:String)=text.toDoubleOrNull()?.takeIf { it.isFinite() && it>0.0 }
}

@Composable
internal fun CapturedReferenceFields(form:CapturedReferenceForm,prefix:String,allowedUnits:List<MetricUnit?> = listOf(null,MetricUnit.GRAM,MetricUnit.MILLILITER),metric100:Boolean=false) {
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        CapturedTextField(form.amount,{form.amount=it},stringResource(Res.string.captured_reference_amount),prefix+"amount",true)
        CapturedTextField(form.unitText,{form.unitText=it},stringResource(Res.string.captured_reference_unit),prefix+"unit")
        CapturedTextField(form.text,{form.text=it},stringResource(Res.string.captured_reference_text),prefix+"text")
        if(metric100)Text("100 "+if(form.metricUnit==MetricUnit.MILLILITER)stringResource(Res.string.unit_milliliter_short)else stringResource(Res.string.unit_gram_short))
        else CapturedTextField(form.metric,{form.metric=it},stringResource(Res.string.captured_reference_metric),prefix+"metric",true)
        Text(stringResource(Res.string.captured_metric_unit))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            allowedUnits.filter { !metric100 || it!=null }.forEach { unit ->
                FilterChip(selected=form.metricUnit==unit,onClick={form.metricUnit=unit;if(unit==null)form.metric=""},label={
                    Text(stringResource(when(unit){null->Res.string.captured_no_metric;MetricUnit.GRAM->Res.string.unit_gram_short;MetricUnit.MILLILITER->Res.string.unit_milliliter_short}))
                })
            }
        }
    }
}

@Composable
internal fun CapturedReferenceText(reference:PortionDescription?) {
    reference ?: return
    if(reference.amount!=null || reference.unitText!=null)Text(listOfNotNull(reference.amount?.toString(),reference.unitText).joinToString(" "))
    reference.text?.let { Text(it) }
    val metric=reference.metricAmount
    if(metric==null)Text(stringResource(Res.string.captured_no_metric)) else Text(metric.toString()+" "+stringResource(
        if(reference.metricUnit==MetricUnit.MILLILITER)Res.string.unit_milliliter_short else Res.string.unit_gram_short))
}

@Composable
internal fun CapturedMetadataView(metadata:NutritionMetadata?,definition:Boolean=false) {
    metadata ?: return
    if(metadata.notes==null && metadata.provenance==null && metadata.percentDailyValues.isEmpty() && metadata.observations.isEmpty() && metadata.textTruncated!=true)return
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text(stringResource(if(definition)Res.string.captured_definition_metadata else Res.string.captured_metadata),style=MaterialTheme.typography.titleSmall)
        metadata.notes?.let { Text(it) };metadata.provenance?.let { Text(it) }
        if(metadata.percentDailyValues.isNotEmpty())Text(stringResource(Res.string.captured_percent_daily))
        metadata.percentDailyValues.forEach { (name,value) -> Text(name+": "+value+"%") }
        metadata.observations.forEach { observation ->
            Text(listOfNotNull(observation.text ?: observation.kind,observation.value?.toString(),observation.unitText).joinToString(" "))
        }
        if(metadata.textTruncated==true)Text(stringResource(Res.string.captured_truncated))
    }
}

@Composable internal fun reportedBasisLabel(basis:ReportedBasis):String=stringResource(when(basis){
    ReportedBasis.WHOLE_ITEM->Res.string.captured_whole;ReportedBasis.REFERENCE_SERVING->Res.string.captured_serving
    ReportedBasis.UNSPECIFIED->Res.string.captured_unspecified;ReportedBasis.UNAVAILABLE->Res.string.captured_unavailable
})
