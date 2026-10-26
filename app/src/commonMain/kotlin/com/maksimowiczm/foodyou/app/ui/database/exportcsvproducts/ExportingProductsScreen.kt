package com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import foodyou.app.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExportingProductsScreen(count: Int, modifier: Modifier = Modifier) {
    val coroutinesScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val pleaseWaitMessage = stringResource(Res.string.headline_please_wait)

    NavigationEventHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = {
            coroutinesScope.launch { snackbarHostState.showSnackbar(pleaseWaitMessage) }
        },
    )

    Scaffold(modifier = (modifier).uiName("ExportingProductsScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "ExportingProductsScreen"), snackbarHost = { SnackbarHost(hostState = snackbarHostState, modifier = Modifier.uiName("ExportingProductsScreen.SnackbarHost.1", "androidx.compose.material3.SnackbarHost", uiInspectionSource, "ExportingProductsScreen")) }) {
        paddingValues ->
        Column(
            modifier = (Modifier.fillMaxSize().padding(paddingValues)).uiName("ExportingProductsScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ExportingProductsScreen"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularWavyProgressIndicator(modifier = (Modifier.size(68.dp)).uiName("ExportingProductsScreen.CircularWavyProgressIndicator.1", "androidx.compose.material3.CircularWavyProgressIndicator", uiInspectionSource, "ExportingProductsScreen"))

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.notification_exporting_products),
                modifier = (Modifier.fillMaxWidth()).uiName("ExportingProductsScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ExportingProductsScreen"),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.description_please_wait_while_exporting_products),
                modifier = (Modifier.fillMaxWidth()).uiName("ExportingProductsScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ExportingProductsScreen"),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(8.dp))

            Text(text = count.toString(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("ExportingProductsScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ExportingProductsScreen"))
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/exportcsvproducts/ExportingProductsScreen.kt",
)
