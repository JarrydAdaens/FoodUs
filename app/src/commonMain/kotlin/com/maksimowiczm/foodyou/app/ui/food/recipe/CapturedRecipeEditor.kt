package com.maksimowiczm.foodyou.app.ui.food.recipe

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

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
    Scaffold(modifier=(modifier).uiName("CapturedRecipeEditor.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "CapturedRecipeEditor"),topBar={TopAppBar(title={Text(stringResource(Res.string.headline_edit_recipe), modifier = Modifier.uiName("CapturedRecipeEditor.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))},navigationIcon={ArrowBackIconButton(onBack, modifier = Modifier.uiName("CapturedRecipeEditor.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "CapturedRecipeEditor"))},actions={
        TextButton(onClick=onBack, modifier = Modifier.uiName("CapturedRecipeEditor.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "CapturedRecipeEditor")){Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("CapturedRecipeEditor.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))}
        TextButton(onClick=::save,enabled=!saving,modifier=(Modifier.testTag("captured-recipe-save")).uiName("CapturedRecipeEditor.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "CapturedRecipeEditor")){Text(stringResource(Res.string.action_save), modifier = Modifier.uiName("CapturedRecipeEditor.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))}
    }, modifier = Modifier.uiName("CapturedRecipeEditor.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "CapturedRecipeEditor"))}) { padding ->
        LazyColumn((Modifier.fillMaxSize().testTag("captured-recipe-editor")).uiName("CapturedRecipeEditor.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "CapturedRecipeEditor"),contentPadding=PaddingValues(16.dp,padding.calculateTopPadding()+8.dp,16.dp,padding.calculateBottomPadding()+16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            if(failed)item{Text(stringResource(Res.string.captured_invalid),color=MaterialTheme.colorScheme.error, modifier = Modifier.uiName("CapturedRecipeEditor.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))}
            item{CapturedTextField(name,{name=it},stringResource(Res.string.product_name),"captured-recipe-name")}
            item{CapturedTextField(note,{note=it},stringResource(Res.string.headline_note),"captured-recipe-note")}
            item{CapturedTextField(yield,{yield=it},stringResource(Res.string.captured_yield),"captured-recipe-yield",true)}
            item {
                Text(stringResource(Res.string.captured_panel_basis),style=MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("CapturedRecipeEditor.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))
                Column (modifier = Modifier.uiName("CapturedRecipeEditor.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "CapturedRecipeEditor")) { ReportedBasis.entries.forEach { option ->
                    FilterChip(selected=basis==option,onClick={basis=option},label={Text(reportedBasisLabel(option), modifier = Modifier.uiName("CapturedRecipeEditor.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))}, modifier = Modifier.uiName("CapturedRecipeEditor.FilterChip.1", "androidx.compose.material3.FilterChip", uiInspectionSource, "CapturedRecipeEditor"))
                } }
                CapturedReferenceFields(reference,"captured-recipe-reference-")
                Text(stringResource(Res.string.captured_unpack_unavailable), modifier = Modifier.uiName("CapturedRecipeEditor.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))
                if(original.needsReview)Text(stringResource(Res.string.captured_needs_review),color=MaterialTheme.colorScheme.error, modifier = Modifier.uiName("CapturedRecipeEditor.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))
                CapturedMetadataView(original.definitionMetadata,true)
                CapturedMetadataView(original.metadata)
                Text(stringResource(Res.string.captured_components),style=MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("CapturedRecipeEditor.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))
                TextButton(onClick={editing=-1},modifier=(Modifier.testTag("captured-component-add")).uiName("CapturedRecipeEditor.TextButton.3", "androidx.compose.material3.TextButton", uiInspectionSource, "CapturedRecipeEditor")){Text(stringResource(Res.string.captured_component_add), modifier = Modifier.uiName("CapturedRecipeEditor.Text.10", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))}
            }
            itemsIndexed(components) { index,component ->
                ElevatedCard((Modifier.fillMaxWidth().testTag("captured-component-$index")).uiName("CapturedRecipeEditor.ElevatedCard.1", "androidx.compose.material3.ElevatedCard", uiInspectionSource, "CapturedRecipeEditor")) { Column((Modifier.padding(12.dp)).uiName("CapturedRecipeEditor.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "CapturedRecipeEditor")) {
                    Text(component.name,style=MaterialTheme.typography.titleSmall, modifier = Modifier.uiName("CapturedRecipeEditor.Text.11", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))
                    CapturedReferenceText(component.portion)
                    Row (modifier = Modifier.uiName("CapturedRecipeEditor.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "CapturedRecipeEditor")) {
                        TextButton(onClick={editing=index}, modifier = Modifier.uiName("CapturedRecipeEditor.TextButton.4", "androidx.compose.material3.TextButton", uiInspectionSource, "CapturedRecipeEditor")){Text(stringResource(Res.string.captured_component_edit), modifier = Modifier.uiName("CapturedRecipeEditor.Text.12", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))}
                        TextButton(onClick={components=components.toMutableList().also { it.removeAt(index) }}, modifier = Modifier.uiName("CapturedRecipeEditor.TextButton.5", "androidx.compose.material3.TextButton", uiInspectionSource, "CapturedRecipeEditor")){Text(stringResource(Res.string.captured_remove), modifier = Modifier.uiName("CapturedRecipeEditor.Text.13", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))}
                    }
                    Row (modifier = Modifier.uiName("CapturedRecipeEditor.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "CapturedRecipeEditor")) {
                        if(index>0)TextButton(onClick={components=components.toMutableList().also { val value=it.removeAt(index);it.add(index-1,value) }}, modifier = Modifier.uiName("CapturedRecipeEditor.TextButton.6", "androidx.compose.material3.TextButton", uiInspectionSource, "CapturedRecipeEditor")){Text(stringResource(Res.string.captured_move_up), modifier = Modifier.uiName("CapturedRecipeEditor.Text.14", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))}
                        if(index<components.lastIndex)TextButton(onClick={components=components.toMutableList().also { val value=it.removeAt(index);it.add(index+1,value) }}, modifier = Modifier.uiName("CapturedRecipeEditor.TextButton.7", "androidx.compose.material3.TextButton", uiInspectionSource, "CapturedRecipeEditor")){Text(stringResource(Res.string.captured_move_down), modifier = Modifier.uiName("CapturedRecipeEditor.Text.15", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))}
                    }
                } }
            }
            item{Text(stringResource(Res.string.captured_reported),style=MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("CapturedRecipeEditor.Text.16", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))}
            capturedNutritionFields(nutrition,"captured-recipe-nutrient-")
            item{Row((Modifier.fillMaxWidth().toggleable(confirmed==signature(),role=Role.Checkbox,onValueChange={confirmed=if(it)signature()else null})).uiName("CapturedRecipeEditor.Row.3", "androidx.compose.foundation.layout.Row", uiInspectionSource, "CapturedRecipeEditor")) {
                Checkbox(confirmed==signature(),null, modifier = Modifier.uiName("CapturedRecipeEditor.Checkbox.1", "androidx.compose.material3.Checkbox", uiInspectionSource, "CapturedRecipeEditor"))
                Text(stringResource(Res.string.captured_confirm_panel),(Modifier.padding(top=12.dp)).uiName("CapturedRecipeEditor.Text.17", "androidx.compose.material3.Text", uiInspectionSource, "CapturedRecipeEditor"))
            }}
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.recipe",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/recipe/CapturedRecipeEditor.kt",
)
