package com.maksimowiczm.foodyou.app.ui.food.recipe

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.food.component.*
import com.maksimowiczm.foodyou.common.domain.food.*
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CapturedComponentEditor(component:ReportedRecipeComponent,onCancel:()->Unit,onSave:(ReportedRecipeComponent)->Unit) {
    var name by remember(component){mutableStateOf(component.name)}
    val portion=remember(component){CapturedReferenceForm(component.portion)}
    var link by remember(component){mutableStateOf(component.reference)}
    var hasFacts by remember(component){mutableStateOf(component.facts!=null)}
    val energy=LocalEnergyFormatter.current
    val facts=remember(component,energy){CapturedNutritionForm(component.facts ?: NutritionFacts(),energy)}
    var confirmed by remember(component){mutableStateOf<List<String?>?>(null)}
    var failed by remember(component){mutableStateOf(false)}
    fun signature()=listOf(name,portion.amount,portion.unitText,portion.text,portion.metric,portion.metricUnit?.name)
    fun save() {
        val captured=if(hasFacts)facts.candidate()else null
        val changed=name!=component.name || portion.candidate()!=component.portion
        if(name.isBlank() || name.length>512 || !portion.isValid || hasFacts && (captured==null || changed && confirmed!=signature())) {failed=true;return}
        onSave(component.copy(name=name.trim(),portion=portion.candidate(),facts=captured,reference=link))
    }
    Scaffold(topBar={TopAppBar(title={Text(stringResource(Res.string.captured_component_edit), modifier = Modifier.uiName("CapturedComponentEditor.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "CapturedComponentEditor"))},navigationIcon={ArrowBackIconButton(onCancel, modifier = Modifier.uiName("CapturedComponentEditor.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "CapturedComponentEditor"))},actions={
        TextButton(onClick=onCancel, modifier = Modifier.uiName("CapturedComponentEditor.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "CapturedComponentEditor")){Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("CapturedComponentEditor.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "CapturedComponentEditor"))}
        TextButton(onClick=::save,modifier=(Modifier.testTag("captured-component-apply")).uiName("CapturedComponentEditor.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "CapturedComponentEditor")){Text(stringResource(Res.string.meals_apply_changes), modifier = Modifier.uiName("CapturedComponentEditor.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "CapturedComponentEditor"))}
    }, modifier = Modifier.uiName("CapturedComponentEditor.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "CapturedComponentEditor"))}, modifier = Modifier.uiName("CapturedComponentEditor.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "CapturedComponentEditor")) { padding ->
        LazyColumn((Modifier.fillMaxSize().testTag("captured-component-editor")).uiName("CapturedComponentEditor.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "CapturedComponentEditor"),contentPadding=PaddingValues(16.dp,padding.calculateTopPadding()+8.dp,16.dp,padding.calculateBottomPadding()+16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            if(failed)item{Text(stringResource(Res.string.captured_invalid),color=MaterialTheme.colorScheme.error, modifier = Modifier.uiName("CapturedComponentEditor.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "CapturedComponentEditor"))}
            item{CapturedTextField(name,{name=it},stringResource(Res.string.captured_component_name),"captured-component-name")}
            item{CapturedReferenceFields(portion,"captured-component-portion-")}
            item {
                if(link!=null) {
                    Text(stringResource(if(link?.kind==DefinitionKind.PRODUCT)Res.string.captured_linked_food else Res.string.captured_linked_recipe), modifier = Modifier.uiName("CapturedComponentEditor.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "CapturedComponentEditor"))
                    TextButton(onClick={link=null}, modifier = Modifier.uiName("CapturedComponentEditor.TextButton.3", "androidx.compose.material3.TextButton", uiInspectionSource, "CapturedComponentEditor")){Text(stringResource(Res.string.captured_link_remove), modifier = Modifier.uiName("CapturedComponentEditor.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "CapturedComponentEditor"))}
                }
                CapturedMetadataView(component.metadata)
                Row((Modifier.fillMaxWidth().toggleable(hasFacts,role=Role.Checkbox,onValueChange={hasFacts=it})).uiName("CapturedComponentEditor.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "CapturedComponentEditor")) {
                    Checkbox(hasFacts,null, modifier = Modifier.uiName("CapturedComponentEditor.Checkbox.1", "androidx.compose.material3.Checkbox", uiInspectionSource, "CapturedComponentEditor"));Text(stringResource(Res.string.captured_component_facts),(Modifier.padding(top=12.dp)).uiName("CapturedComponentEditor.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "CapturedComponentEditor"))
                }
                if(hasFacts)Row((Modifier.fillMaxWidth().toggleable(confirmed==signature(),role=Role.Checkbox,onValueChange={confirmed=if(it)signature()else null})).uiName("CapturedComponentEditor.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "CapturedComponentEditor")) {
                    Checkbox(confirmed==signature(),null, modifier = Modifier.uiName("CapturedComponentEditor.Checkbox.2", "androidx.compose.material3.Checkbox", uiInspectionSource, "CapturedComponentEditor"));Text(stringResource(Res.string.captured_confirm_reference),(Modifier.padding(top=12.dp)).uiName("CapturedComponentEditor.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "CapturedComponentEditor"))
                }
            }
            if(hasFacts)capturedNutritionFields(facts,"captured-component-nutrient-")
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.recipe",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/recipe/CapturedComponentEditor.kt",
)
