package com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts

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
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalAppConfig
import java.time.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun ExportCsvProductsScreen(onBack: () -> Unit, onFinish: () -> Unit, modifier: Modifier) {
    val viewModel: ExportProductsViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val context = LocalContext.current
    LaunchedEffect(viewModel) { viewModel.prepare(context.applicationContext) }
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/csv")
        ) { uri ->
            if (uri == null) {
                onBack()
            } else {
                viewModel.handleCsv(uri, context)
            }
        }

    val appConfig = LocalAppConfig.current
    val fileName = remember {
        "Food You ${appConfig.versionName}-products-${LocalDateTime.now()}.csv"
    }
    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Error,
            is UiState.Exported,
            is UiState.Exporting -> Unit

            UiState.Preparing -> Unit

            UiState.WaitingForFile -> launcher.launch(fileName)
        }
    }

    when (uiState) {
        is UiState.Error ->
            UiSourceScope(uiInspectionSource.element("ExportCsvProductsScreen.SomethingWentWrongScreen.1", "com.maksimowiczm.foodyou.app.ui.common.component.SomethingWentWrongScreen", "ExportCsvProductsScreen")) { SomethingWentWrongScreen(
                onBack = onBack,
                message = stringResource(if (uiState.metricUnavailable) Res.string.captured_csv_unavailable else Res.string.captured_csv_failed),
                modifier = modifier,
            ) }

        is UiState.Exported ->
            UiSourceScope(uiInspectionSource.element("ExportCsvProductsScreen.SuccessScreen.1", "com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts.SuccessScreen", "ExportCsvProductsScreen")) { SuccessScreen(count = uiState.count, onBack = onBack, modifier = modifier) }

        UiState.WaitingForFile, UiState.Preparing ->
            Scaffold((modifier).uiName("ExportCsvProductsScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "ExportCsvProductsScreen")) { paddingValues ->
                Box(
                    modifier =
                        (Modifier.fillMaxSize()
                            .padding(paddingValues)
                            .consumeWindowInsets(paddingValues)).uiName("ExportCsvProductsScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "ExportCsvProductsScreen"),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator( modifier = Modifier.uiName("ExportCsvProductsScreen.LoadingIndicator.1", "androidx.compose.material3.LoadingIndicator", uiInspectionSource, "ExportCsvProductsScreen"))
                }
            }

        is UiState.Exporting -> UiSourceScope(uiInspectionSource.element("ExportCsvProductsScreen.ExportingProductsScreen.1", "com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts.ExportingProductsScreen", "ExportCsvProductsScreen")) { ExportingProductsScreen(count = uiState.count, modifier = modifier) }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts",
    "app/src/androidMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/exportcsvproducts/ExportCsvProductsScreen.android.kt",
)
