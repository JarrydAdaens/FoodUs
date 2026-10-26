package com.maksimowiczm.foodyou.app.ui.database.importcsvproducts

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.SomethingWentWrongScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun ImportCsvProductsScreen(onBack: () -> Unit, onFinish: () -> Unit, modifier: Modifier) {
    val viewModel: ImportCsvProductsViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                onBack()
            } else {
                viewModel.handleCsv(uri, context)
            }
        }

    LaunchedEffect(uiState) {
        when (uiState) {
            UiState.WaitingForFile -> launcher.launch(arrayOf("text/csv", "*/*"))
            else -> Unit
        }
    }

    when (uiState) {
        is UiState.WithError ->
            UiSourceScope(uiInspectionSource.element("ImportCsvProductsScreen.SomethingWentWrongScreen.1", "com.maksimowiczm.foodyou.app.ui.common.component.SomethingWentWrongScreen", "ImportCsvProductsScreen")) { SomethingWentWrongScreen(
                onBack = onBack,
                message = uiState.message,
                modifier = modifier,
            ) }

        is UiState.FileOpened ->
            UiSourceScope(uiInspectionSource.element("ImportCsvProductsScreen.ImportCsvProductsScreen.1", "com.maksimowiczm.foodyou.app.ui.database.importcsvproducts.ImportCsvProductsScreen", "ImportCsvProductsScreen")) { ImportCsvProductsScreen(
                header = uiState.header.sorted(),
                onBack = onBack,
                onImport = viewModel::import,
                modifier = modifier,
            ) }

        UiState.WaitingForFile ->
            Scaffold((modifier).uiName("app.ui.database.importcsvproducts.ImportCsvProductsScreen.Scaffold.1.declaration2", "androidx.compose.material3.Scaffold", uiInspectionSource, "ImportCsvProductsScreen")) { paddingValues ->
                Box(
                    modifier =
                        (Modifier.fillMaxSize()
                            .padding(paddingValues)
                            .consumeWindowInsets(paddingValues)).uiName("ImportCsvProductsScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "ImportCsvProductsScreen"),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator( modifier = Modifier.uiName("ImportCsvProductsScreen.LoadingIndicator.1", "androidx.compose.material3.LoadingIndicator", uiInspectionSource, "ImportCsvProductsScreen"))
                }
            }

        is UiState.ImportSuccess -> UiSourceScope(uiInspectionSource.element("ImportCsvProductsScreen.ImportingSuccessScreen.1", "com.maksimowiczm.foodyou.app.ui.database.importcsvproducts.ImportingSuccessScreen", "ImportCsvProductsScreen")) { ImportingSuccessScreen(uiState.count, onBack, modifier) }
        is UiState.Importing -> UiSourceScope(uiInspectionSource.element("ImportCsvProductsScreen.ImportingProductsScreen.1", "com.maksimowiczm.foodyou.app.ui.database.importcsvproducts.ImportingProductsScreen", "ImportCsvProductsScreen")) { ImportingProductsScreen(uiState.count, modifier) }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.database.importcsvproducts",
    "app/src/androidMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/importcsvproducts/ImportCsvProductsScreen.android.kt",
)
