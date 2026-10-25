package com.maksimowiczm.foodyou.app.ui.food.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CapturedRecipeEditor(recipe:Recipe,onBack:()->Unit,onSaved:()->Unit,modifier:Modifier=Modifier) {
    val original=requireNotNull(recipe.reportedNutrition)
    val viewModel:CapturedDefinitionViewModel=koinViewModel()
    var name by remember(recipe){mutableStateOf(recipe.name)}
    var note by remember(recipe){mutableStateOf(recipe.note.orEmpty())}
    var yield by remember(recipe){mutableStateOf(recipe.servings?.toString().orEmpty())}
    var basis by remember(recipe){mutableStateOf(original.basis)}
    val reference=remember(recipe){CapturedReferenceForm(original.reference)}
    val energy=LocalEnergyFormatter.current
    val nutrition=remember(recipe,energy){CapturedNutritionForm(original.facts,energy)}
    var components by remember(recipe){mutableStateOf(recipe.reportedComponents)}
    var editing by remember(recipe){mutableStateOf<Int?>(null)}
    var confirmed by remember(recipe){mutableStateOf<Any?>(null)}
    var failed by remember(recipe){mutableStateOf(false)}
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val latestSaved by rememberUpdatedState(onSaved)
    LaunchedCollectWithLifecycle(viewModel.results) { if(it)latestSaved()else failed=true }
    fun signature():Any=listOf(yield,basis,reference.amount,reference.unitText,reference.text,reference.metric,reference.metricUnit,components)
    fun save() {
        val count=if(yield.isBlank())null else yield.toIntOrNull()?.takeIf { it>0 }
        val facts=nutrition.candidate()
        if(name.isBlank() || name.length>512 || note.length>16_384 || yield.isNotBlank() && count==null || !reference.isValid || facts==null) {failed=true;return}
        val portion=reference.candidate()
        viewModel.recipe(recipe.copy(name=name.trim(),note=if(note==recipe.note.orEmpty())recipe.note else note.ifEmpty { null },servings=count,reportedComponents=components,
            isLiquid=portion?.metricUnit?.let { it==MetricUnit.MILLILITER } ?: recipe.isLiquid,
            reportedNutrition=original.copy(facts=facts,basis=basis,reference=portion)),confirmed==signature())
    }
    val selected=editing
    if(selected!=null) {
        CapturedComponentEditor(components.getOrNull(selected) ?: ReportedRecipeComponent(""),{editing=null}) { component ->
            components=if(selected<0)components+component else components.toMutableList().also { it[selected]=component }
            editing=null
        }
        return
    }
    Scaffold(modifier=modifier,topBar={TopAppBar(title={Text(stringResource(Res.string.headline_edit_recipe))},navigationIcon={ArrowBackIconButton(onBack)},actions={
        TextButton(onClick=onBack){Text(stringResource(Res.string.action_cancel))}
        TextButton(onClick=::save,enabled=!saving,modifier=Modifier.testTag("captured-recipe-save")){Text(stringResource(Res.string.action_save))}
    })}) { padding ->
        LazyColumn(Modifier.fillMaxSize().testTag("captured-recipe-editor"),contentPadding=PaddingValues(16.dp,padding.calculateTopPadding()+8.dp,16.dp,padding.calculateBottomPadding()+16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            if(failed)item{Text(stringResource(Res.string.captured_invalid),color=MaterialTheme.colorScheme.error)}
            item{CapturedTextField(name,{name=it},stringResource(Res.string.product_name),"captured-recipe-name")}
            item{CapturedTextField(note,{note=it},stringResource(Res.string.headline_note),"captured-recipe-note")}
            item{CapturedTextField(yield,{yield=it},stringResource(Res.string.captured_yield),"captured-recipe-yield",true)}
            item {
                Text(stringResource(Res.string.captured_panel_basis),style=MaterialTheme.typography.titleMedium)
                Column { ReportedBasis.entries.forEach { option ->
                    FilterChip(selected=basis==option,onClick={basis=option},label={Text(reportedBasisLabel(option))})
                } }
                CapturedReferenceFields(reference,"captured-recipe-reference-")
                Text(stringResource(Res.string.captured_unpack_unavailable))
                if(original.needsReview)Text(stringResource(Res.string.captured_needs_review),color=MaterialTheme.colorScheme.error)
                CapturedMetadataView(original.definitionMetadata,true)
                CapturedMetadataView(original.metadata)
                Text(stringResource(Res.string.captured_components),style=MaterialTheme.typography.titleMedium)
                TextButton(onClick={editing=-1},modifier=Modifier.testTag("captured-component-add")){Text(stringResource(Res.string.captured_component_add))}
            }
            itemsIndexed(components) { index,component ->
                ElevatedCard(Modifier.fillMaxWidth().testTag("captured-component-$index")) { Column(Modifier.padding(12.dp)) {
                    Text(component.name,style=MaterialTheme.typography.titleSmall)
                    CapturedReferenceText(component.portion)
                    Row {
                        TextButton(onClick={editing=index}){Text(stringResource(Res.string.captured_component_edit))}
                        TextButton(onClick={components=components.toMutableList().also { it.removeAt(index) }}){Text(stringResource(Res.string.captured_remove))}
                    }
                    Row {
                        if(index>0)TextButton(onClick={components=components.toMutableList().also { val value=it.removeAt(index);it.add(index-1,value) }}){Text(stringResource(Res.string.captured_move_up))}
                        if(index<components.lastIndex)TextButton(onClick={components=components.toMutableList().also { val value=it.removeAt(index);it.add(index+1,value) }}){Text(stringResource(Res.string.captured_move_down))}
                    }
                } }
            }
            item{Text(stringResource(Res.string.captured_reported),style=MaterialTheme.typography.titleMedium)}
            capturedNutritionFields(nutrition,"captured-recipe-nutrient-")
            item{Row(Modifier.fillMaxWidth().toggleable(confirmed==signature(),role=Role.Checkbox,onValueChange={confirmed=if(it)signature()else null})) {
                Checkbox(confirmed==signature(),null)
                Text(stringResource(Res.string.captured_confirm_panel),Modifier.padding(top=12.dp))
            }}
        }
    }
}
