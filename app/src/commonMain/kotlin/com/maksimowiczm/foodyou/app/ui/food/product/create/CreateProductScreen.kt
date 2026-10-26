package com.maksimowiczm.foodyou.app.ui.food.product.create

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog
import com.maksimowiczm.foodyou.app.ui.food.product.ProductForm
import com.maksimowiczm.foodyou.app.ui.food.product.ProductFormState
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CreateProductScreen(
    state: ProductFormState,
    onBack: () -> Unit,
    onCreate: (ProductFormState) -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    val handleBack = {
        if (!state.isModified) {
            onBack()
        } else {
            showDiscardDialog = true
        }
    }
    NavigationEventHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = state.isModified,
        onBackCompleted = { showDiscardDialog = true },
    )
    if (showDiscardDialog) {
        DiscardDialog(
            onDismissRequest = { showDiscardDialog = false },
            onDiscard = {
                showDiscardDialog = false
                onBack()
            }, modifier = Modifier.uiName("CreateProductScreen.DiscardDialog.1", "com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog", uiInspectionSource, "CreateProductScreen")
        ) {
            Text(stringResource(Res.string.question_discard_product), modifier = Modifier.uiName("CreateProductScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "CreateProductScreen"))
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = (modifier).uiName("CreateProductScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "CreateProductScreen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_create_product), modifier = Modifier.uiName("CreateProductScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "CreateProductScreen")) },
                navigationIcon = { ArrowBackIconButton(handleBack, modifier = Modifier.uiName("CreateProductScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "CreateProductScreen")) },
                actions = {
                    FilledIconButton(onClick = { onCreate(state) }, enabled = state.isValid, modifier = Modifier.uiName("CreateProductScreen.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "CreateProductScreen")) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = stringResource(Res.string.action_save), modifier = Modifier.uiName("CreateProductScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "CreateProductScreen")
                        )
                    }
                },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("CreateProductScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "CreateProductScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                (Modifier.fillMaxSize()
                    .imePadding()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("CreateProductScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "CreateProductScreen"),
            contentPadding = paddingValues,
        ) {
            item {
                AssistChip(
                    onClick = onDownload,
                    label = { Text(stringResource(Res.string.action_download_product), modifier = Modifier.uiName("CreateProductScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "CreateProductScreen")) },
                    modifier = (Modifier.padding(horizontal = 16.dp)).uiName("CreateProductScreen.AssistChip.1", "androidx.compose.material3.AssistChip", uiInspectionSource, "CreateProductScreen"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = (Modifier.size(AssistChipDefaults.IconSize)).uiName("CreateProductScreen.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "CreateProductScreen"),
                        )
                    },
                )
            }

            item { ProductForm(state = state, contentPadding = PaddingValues(horizontal = 16.dp), modifier = Modifier.uiName("CreateProductScreen.ProductForm.1", "com.maksimowiczm.foodyou.app.ui.food.product.ProductForm", uiInspectionSource, "CreateProductScreen")) }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.product.create",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/product/create/CreateProductScreen.kt",
)
