package com.maksimowiczm.foodyou.app.ui.database.importlibrary

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
    Scaffold(modifier=modifier,topBar={TopAppBar(title={Text(stringResource(Res.string.library_import_title))},navigationIcon={ArrowBackIconButton(onBack)})}) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp).testTag("library-import"),
            verticalArrangement=Arrangement.spacedBy(12.dp)) {
            when(state) {
                LibraryImportState.Waiting -> {
                    Text(stringResource(Res.string.library_import_description))
                    Button(onClick=onChoose,modifier=Modifier.testTag("library-select")){Text(stringResource(Res.string.library_choose_file))}
                }
                LibraryImportState.Reading,LibraryImportState.Importing -> {
                    CircularProgressIndicator()
                    Text(stringResource(if(state==LibraryImportState.Reading)Res.string.library_reading else Res.string.library_importing))
                    TextButton(onClick=onCancel){Text(stringResource(Res.string.action_cancel))}
                }
                is LibraryImportState.Preview -> {
                    val preview=state.summary
                    Text(stringResource(Res.string.library_preview_title),style=MaterialTheme.typography.titleLarge)
                    Text(stringResource(Res.string.library_preview_foods,preview.products))
                    Text(stringResource(Res.string.library_preview_recipes,preview.recipes))
                    Text(stringResource(Res.string.library_preview_meals,preview.meals))
                    Text(stringResource(Res.string.library_preview_skipped,preview.skipped))
                    if(preview.metricUnavailable>0)Text(stringResource(Res.string.library_metric_unavailable,preview.metricUnavailable))
                    if(preview.nutritionUnavailable>0)Text(stringResource(Res.string.library_nutrition_unavailable,preview.nutritionUnavailable))
                    Text(stringResource(Res.string.library_legacy_overlap))
                    if(preview.conflicts>0)Text(stringResource(Res.string.library_conflicts,preview.conflicts),color=MaterialTheme.colorScheme.error)
                    Button(onClick=onConfirm,enabled=preview.conflicts==0,modifier=Modifier.testTag("library-confirm")){Text(stringResource(Res.string.library_confirm))}
                    TextButton(onClick=onCancel){Text(stringResource(Res.string.action_cancel))}
                }
                is LibraryImportState.Complete -> {
                    Text(stringResource(Res.string.library_complete),style=MaterialTheme.typography.titleLarge)
                    Text(stringResource(Res.string.library_added_foods,state.result.products))
                    Text(stringResource(Res.string.library_added_recipes,state.result.recipes))
                    Text(stringResource(Res.string.library_added_meals,state.result.meals))
                    Text(stringResource(Res.string.library_preview_skipped,state.result.skipped))
                    Button(onClick=onFinish,modifier=Modifier.testTag("library-done")){Text(stringResource(Res.string.library_done))}
                }
                is LibraryImportState.Failed -> {
                    Text(stringResource(state.code.message()),color=MaterialTheme.colorScheme.error,modifier=Modifier.testTag("library-error"))
                    if(state.kind!=null && state.index!=null) {
                        val kind=stringResource(when(state.kind){DefinitionKind.PRODUCT->Res.string.library_kind_food;DefinitionKind.RECIPE->Res.string.library_kind_recipe;DefinitionKind.MEAL->Res.string.library_kind_meal})
                        Text(stringResource(Res.string.library_error_record,kind,state.index+1))
                    }
                    Button(onClick=onChoose){Text(stringResource(Res.string.library_choose_file))}
                }
                LibraryImportState.PlatformUnavailable -> Text(stringResource(Res.string.library_android_only))
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
