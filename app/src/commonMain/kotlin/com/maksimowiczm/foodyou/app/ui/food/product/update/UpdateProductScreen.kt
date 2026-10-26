package com.maksimowiczm.foodyou.app.ui.food.product.update

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog
import com.maksimowiczm.foodyou.app.ui.food.product.ProductForm
import com.maksimowiczm.foodyou.app.ui.food.product.rememberProductFormState
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UpdateProductScreen(
    onBack: () -> Unit,
    onUpdate: () -> Unit,
    viewModel: UpdateProductViewModel,
    modifier: Modifier = Modifier,
) {
    val latestOnUpdate by rememberUpdatedState(onUpdate)
    LaunchedCollectWithLifecycle(viewModel.events) { event ->
        when (event) {
            UpdateProductEvent.Updated -> latestOnUpdate()
        }
    }

    val product = viewModel.product.collectAsStateWithLifecycle().value

    if (product == null) {
        // TODO loading state
        return
    } else {
        if(product.factBasis==com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis.REFERENCE_SERVING || product.nutritionMetadata?.reference!=null) {
            CapturedProductEditor(product,onBack,onUpdate,(modifier).uiName("UpdateProductScreen.CapturedProductEditor.1", "com.maksimowiczm.foodyou.app.ui.food.product.update.CapturedProductEditor", uiInspectionSource, "UpdateProductScreen"))
            return
        }
        val productForm = rememberProductFormState(product)
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
        val handleBack = {
            if (!productForm.isModified) {
                onBack()
            } else {
                showDiscardDialog = true
            }
        }
        NavigationEventHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = productForm.isModified,
            onBackCompleted = { showDiscardDialog = true },
        )
        if (showDiscardDialog) {
            DiscardDialog(
                onDismissRequest = { showDiscardDialog = false },
                onDiscard = {
                    showDiscardDialog = false
                    onBack()
                }, modifier = Modifier.uiName("UpdateProductScreen.DiscardDialog.1", "com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog", uiInspectionSource, "UpdateProductScreen")
            ) {
                Text(stringResource(Res.string.question_discard_changes), modifier = Modifier.uiName("UpdateProductScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "UpdateProductScreen"))
            }
        }

        Scaffold(
            modifier = (modifier).uiName("UpdateProductScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "UpdateProductScreen"),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.headline_edit_product), modifier = Modifier.uiName("UpdateProductScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "UpdateProductScreen")) },
                    navigationIcon = { ArrowBackIconButton(handleBack, modifier = Modifier.uiName("UpdateProductScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "UpdateProductScreen")) },
                    actions = {
                        FilledIconButton(
                            onClick = { viewModel.updateProduct(productForm) },
                            enabled = productForm.isValid, modifier = Modifier.uiName("UpdateProductScreen.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "UpdateProductScreen")
                        ) {
                            Icon(imageVector = Icons.Outlined.Save, contentDescription = stringResource(Res.string.action_save), modifier = Modifier.uiName("UpdateProductScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "UpdateProductScreen"))
                        }
                    },
                    scrollBehavior = scrollBehavior, modifier = Modifier.uiName("UpdateProductScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "UpdateProductScreen")
                )
            },
        ) { paddingValues ->
            LazyColumn(
                modifier =
                    (Modifier.fillMaxSize()
                        .imePadding()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("UpdateProductScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "UpdateProductScreen"),
                contentPadding = paddingValues,
            ) {
                item {
                    ProductForm(
                        state = productForm,
                        contentPadding = PaddingValues(horizontal = 16.dp), modifier = Modifier.uiName("UpdateProductScreen.ProductForm.1", "com.maksimowiczm.foodyou.app.ui.food.product.ProductForm", uiInspectionSource, "UpdateProductScreen")
                    )
                }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.product.update",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/product/update/UpdateProductScreen.kt",
)
