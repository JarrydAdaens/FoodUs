package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResource
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.NutritionFactsField
import com.maksimowiczm.foodyou.app.ui.food.shared.component.KnownNutrientList
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog
import com.maksimowiczm.foodyou.common.compose.extension.add
import foodyou.app.generated.resources.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QuickAddScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    state: QuickAddFormState = rememberQuickAddFormState(),
    // Story 19 promotion actions. Available on create, edit, and historical edit (all reuse this
    // screen). Enabled only once the fields validate, matching the save action.
    onPromoteToProduct: () -> Unit = {},
    onPromoteToRecipe: () -> Unit = {},
    nutritionDetails: NutritionFacts? = null,
    busy: Boolean = false,
    saveFailed: Boolean = false,
    capturedPortion: com.maksimowiczm.foodyou.common.domain.measurement.Measurement? = null,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        val _ = runCatching { focusRequester.requestFocus() }
    }

    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    val handleBack = {
        if (busy) {
            // Keep the form alive until persistence and its success acknowledgement finish.
        } else if (!state.isModified) {
            onBack()
        } else {
            showDiscardDialog = true
        }
    }
    NavigationEventHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = true,
        onBackCompleted = handleBack,
    )
    if (showDiscardDialog) {
        DiscardDialog(
            onDismissRequest = { showDiscardDialog = false },
            onDiscard = {
                showDiscardDialog = false
                onBack()
            }, modifier = Modifier.uiName("QuickAddScreen.DiscardDialog.1", "com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog", uiInspectionSource, "QuickAddScreen")
        ) {
            Text(stringResource(Res.string.question_discard_changes), modifier = Modifier.uiName("QuickAddScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddScreen"))
        }
    }

    Scaffold(
        modifier = (modifier).uiName("QuickAddScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "QuickAddScreen"),
        bottomBar = {
            if (saveFailed) {
                Text(
                    stringResource(Res.string.error_quick_add_save),
                    color = MaterialTheme.colorScheme.error,
                    modifier = (Modifier.imePadding().navigationBarsPadding().padding(16.dp)).uiName("QuickAddScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddScreen"),
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_quick_add), modifier = Modifier.uiName("QuickAddScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddScreen")) },
                navigationIcon = { ArrowBackIconButton(handleBack, enabled = !busy, modifier = Modifier.uiName("QuickAddScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "QuickAddScreen")) },
                actions = {
                    FilledIconButton(
                        onClick = {
                            if (state.isValid && !busy) {
                                onSave()
                            }
                        },
                        enabled = state.isValid && !busy, modifier = Modifier.uiName("QuickAddScreen.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "QuickAddScreen")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = stringResource(Res.string.action_save), modifier = Modifier.uiName("QuickAddScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "QuickAddScreen")
                        )
                    }
                },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("QuickAddScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "QuickAddScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                (Modifier.fillMaxSize()
                    .imePadding()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("QuickAddScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "QuickAddScreen"),
            contentPadding = paddingValues.add(horizontal = 16.dp, vertical = 8.dp),
        ) {
            item {
                OutlinedButton(
                    onClick = { if (state.isValid && !busy) onPromoteToProduct() },
                    enabled = state.isValid && !busy,
                    modifier = (Modifier.fillMaxWidth()).uiName("QuickAddScreen.OutlinedButton.1", "androidx.compose.material3.OutlinedButton", uiInspectionSource, "QuickAddScreen"),
                ) {
                    Text(stringResource(Res.string.action_promote_to_product), modifier = Modifier.uiName("QuickAddScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddScreen"))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { if (state.isValid && !busy) onPromoteToRecipe() },
                    enabled = state.isValid && !busy,
                    modifier = (Modifier.fillMaxWidth()).uiName("QuickAddScreen.OutlinedButton.2", "androidx.compose.material3.OutlinedButton", uiInspectionSource, "QuickAddScreen"),
                ) {
                    Text(stringResource(Res.string.action_promote_to_recipe), modifier = Modifier.uiName("QuickAddScreen.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddScreen"))
                }
                Spacer(Modifier.height(16.dp))
            }
            item {
                Text(
                    text = stringResource(Res.string.description_quick_add),
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("QuickAddScreen.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddScreen")
                )
                Spacer(Modifier.height(16.dp))
                capturedPortion?.let {
                    Text(it.stringResource(), modifier = Modifier.uiName("QuickAddScreen.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddScreen"))
                    Text(stringResource(Res.string.meals_quantity_help), modifier = Modifier.uiName("QuickAddScreen.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "QuickAddScreen"))
                }
                QuickAddForm(state = state, modifier = (Modifier.focusRequester(focusRequester)).uiName("QuickAddScreen.QuickAddForm.1", "com.maksimowiczm.foodyou.app.ui.food.diary.quickadd.QuickAddForm", uiInspectionSource, "QuickAddScreen"))
                nutritionDetails?.let {
                    KnownNutrientList(it, excludedFields = setOf(
                        NutritionFactsField.Energy, NutritionFactsField.Proteins,
                        NutritionFactsField.Carbohydrates, NutritionFactsField.Fats,
                        NutritionFactsField.DietaryFiber, NutritionFactsField.Sugars,
                    ), modifier = Modifier.uiName("QuickAddScreen.KnownNutrientList.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.KnownNutrientList", uiInspectionSource, "QuickAddScreen"))
                }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.quickadd",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/quickadd/QuickAddScreen.kt",
)
