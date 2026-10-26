package com.maksimowiczm.foodyou.app.ui.common.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun SomethingWentWrongScreen(onBack: () -> Unit, message: String?, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = (modifier).uiName("SomethingWentWrongScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "SomethingWentWrongScreen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_something_went_wrong), modifier = Modifier.uiName("SomethingWentWrongScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SomethingWentWrongScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("SomethingWentWrongScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "SomethingWentWrongScreen")) }, modifier = Modifier.uiName("SomethingWentWrongScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "SomethingWentWrongScreen")
            )
        },
    ) { paddingValues ->
        Column(
            modifier = (Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)).uiName("SomethingWentWrongScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "SomethingWentWrongScreen"),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = (Modifier.size(68.dp)).uiName("SomethingWentWrongScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "SomethingWentWrongScreen"),
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message ?: stringResource(Res.string.error_unknown_error),
                style = MaterialTheme.typography.bodyLarge, modifier = Modifier.uiName("SomethingWentWrongScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SomethingWentWrongScreen")
            )
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/SomethingWentWrongScreen.kt",
)
