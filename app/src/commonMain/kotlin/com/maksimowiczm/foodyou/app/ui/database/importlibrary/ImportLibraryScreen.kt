package com.maksimowiczm.foodyou.app.ui.database.importlibrary

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.common.domain.food.DefinitionKind
import com.maksimowiczm.foodyou.importexport.structuredlibrary.domain.LibraryErrorCode
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable expect fun ImportLibraryScreen(onBack:()->Unit,onFinish:()->Unit,modifier:Modifier=Modifier)

@Composable
internal fun ImportLibraryContent(state:LibraryImportState,onChoose:()->Unit,onConfirm:()->Unit,onCancel:()->Unit,
    onBack:()->Unit,onFinish:()->Unit,modifier:Modifier=Modifier) {
    Scaffold(modifier=(modifier).uiName("ImportLibraryContent.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "ImportLibraryContent"),topBar={TopAppBar(title={Text(stringResource(Res.string.library_import_title), modifier = Modifier.uiName("ImportLibraryContent.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))},navigationIcon={ArrowBackIconButton(onBack, modifier = Modifier.uiName("ImportLibraryContent.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "ImportLibraryContent"))}, modifier = Modifier.uiName("ImportLibraryContent.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "ImportLibraryContent"))}) { padding ->
        Column((Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp).testTag("library-import")).uiName("ImportLibraryContent.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ImportLibraryContent"),
            verticalArrangement=Arrangement.spacedBy(12.dp)) {
            when(state) {
                LibraryImportState.Waiting -> {
                    Text(stringResource(Res.string.library_import_description), modifier = Modifier.uiName("ImportLibraryContent.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    Button(onClick=onChoose,modifier=(Modifier.testTag("library-select")).uiName("ImportLibraryContent.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "ImportLibraryContent")){Text(stringResource(Res.string.library_choose_file), modifier = Modifier.uiName("ImportLibraryContent.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))}
                }
                LibraryImportState.Reading,LibraryImportState.Importing -> {
                    CircularProgressIndicator( modifier = Modifier.uiName("ImportLibraryContent.CircularProgressIndicator.1", "androidx.compose.material3.CircularProgressIndicator", uiInspectionSource, "ImportLibraryContent"))
                    Text(stringResource(if(state==LibraryImportState.Reading)Res.string.library_reading else Res.string.library_importing), modifier = Modifier.uiName("ImportLibraryContent.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    TextButton(onClick=onCancel, modifier = Modifier.uiName("ImportLibraryContent.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "ImportLibraryContent")){Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("ImportLibraryContent.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))}
                }
                is LibraryImportState.Preview -> {
                    val preview=state.summary
                    Text(stringResource(Res.string.library_preview_title),style=MaterialTheme.typography.titleLarge, modifier = Modifier.uiName("ImportLibraryContent.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    Text(stringResource(Res.string.library_preview_foods,preview.products), modifier = Modifier.uiName("ImportLibraryContent.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    Text(stringResource(Res.string.library_preview_recipes,preview.recipes), modifier = Modifier.uiName("ImportLibraryContent.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    Text(stringResource(Res.string.library_preview_meals,preview.meals), modifier = Modifier.uiName("ImportLibraryContent.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    Text(stringResource(Res.string.library_preview_skipped,preview.skipped), modifier = Modifier.uiName("ImportLibraryContent.Text.10", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    if(preview.metricUnavailable>0)Text(stringResource(Res.string.library_metric_unavailable,preview.metricUnavailable), modifier = Modifier.uiName("ImportLibraryContent.Text.11", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    if(preview.nutritionUnavailable>0)Text(stringResource(Res.string.library_nutrition_unavailable,preview.nutritionUnavailable), modifier = Modifier.uiName("ImportLibraryContent.Text.12", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    Text(stringResource(Res.string.library_legacy_overlap), modifier = Modifier.uiName("ImportLibraryContent.Text.13", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    if(preview.conflicts>0)Text(stringResource(Res.string.library_conflicts,preview.conflicts),color=MaterialTheme.colorScheme.error, modifier = Modifier.uiName("ImportLibraryContent.Text.14", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    Button(onClick=onConfirm,enabled=preview.conflicts==0,modifier=(Modifier.testTag("library-confirm")).uiName("ImportLibraryContent.Button.2", "androidx.compose.material3.Button", uiInspectionSource, "ImportLibraryContent")){Text(stringResource(Res.string.library_confirm), modifier = Modifier.uiName("ImportLibraryContent.Text.15", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))}
                    TextButton(onClick=onCancel, modifier = Modifier.uiName("ImportLibraryContent.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "ImportLibraryContent")){Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("ImportLibraryContent.Text.16", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))}
                }
                is LibraryImportState.Complete -> {
                    Text(stringResource(Res.string.library_complete),style=MaterialTheme.typography.titleLarge, modifier = Modifier.uiName("ImportLibraryContent.Text.17", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    Text(stringResource(Res.string.library_added_foods,state.result.products), modifier = Modifier.uiName("ImportLibraryContent.Text.18", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    Text(stringResource(Res.string.library_added_recipes,state.result.recipes), modifier = Modifier.uiName("ImportLibraryContent.Text.19", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    Text(stringResource(Res.string.library_added_meals,state.result.meals), modifier = Modifier.uiName("ImportLibraryContent.Text.20", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    Text(stringResource(Res.string.library_preview_skipped,state.result.skipped), modifier = Modifier.uiName("ImportLibraryContent.Text.21", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    Button(onClick=onFinish,modifier=(Modifier.testTag("library-done")).uiName("ImportLibraryContent.Button.3", "androidx.compose.material3.Button", uiInspectionSource, "ImportLibraryContent")){Text(stringResource(Res.string.library_done), modifier = Modifier.uiName("ImportLibraryContent.Text.22", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))}
                }
                is LibraryImportState.Failed -> {
                    Text(stringResource(state.code.message()),color=MaterialTheme.colorScheme.error,modifier=(Modifier.testTag("library-error")).uiName("ImportLibraryContent.Text.23", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    if(state.kind!=null && state.index!=null) {
                        val kind=stringResource(when(state.kind){DefinitionKind.PRODUCT->Res.string.library_kind_food;DefinitionKind.RECIPE->Res.string.library_kind_recipe;DefinitionKind.MEAL->Res.string.library_kind_meal})
                        Text(stringResource(Res.string.library_error_record,kind,state.index+1), modifier = Modifier.uiName("ImportLibraryContent.Text.24", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
                    }
                    Button(onClick=onChoose, modifier = Modifier.uiName("ImportLibraryContent.Button.4", "androidx.compose.material3.Button", uiInspectionSource, "ImportLibraryContent")){Text(stringResource(Res.string.library_choose_file), modifier = Modifier.uiName("ImportLibraryContent.Text.25", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))}
                }
                LibraryImportState.PlatformUnavailable -> Text(stringResource(Res.string.library_android_only), modifier = Modifier.uiName("ImportLibraryContent.Text.26", "androidx.compose.material3.Text", uiInspectionSource, "ImportLibraryContent"))
            }
        }
    }
}

private fun LibraryErrorCode.message()=when(this) {
    LibraryErrorCode.CONFLICT -> Res.string.library_error_conflict
    LibraryErrorCode.VERSION -> Res.string.library_error_version
    LibraryErrorCode.FILE_TOO_LARGE,LibraryErrorCode.LIMIT -> Res.string.library_error_limit
    LibraryErrorCode.FILE_UNAVAILABLE -> Res.string.library_error_file
    LibraryErrorCode.PERSISTENCE -> Res.string.library_error_persistence
    LibraryErrorCode.NUMBER,LibraryErrorCode.UNIT,LibraryErrorCode.BASIS -> Res.string.library_error_nutrition
    else -> Res.string.library_error_invalid
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.database.importlibrary",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/importlibrary/ImportLibraryScreen.kt",
)
