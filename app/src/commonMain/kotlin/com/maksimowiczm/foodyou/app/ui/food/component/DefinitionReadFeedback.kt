package com.maksimowiczm.foodyou.app.ui.food.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.developer.inspection.*
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DefinitionReadFeedback(loading: Boolean, onRetry: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(modifier = (modifier).uiName("DefinitionReadFeedback.Scaffold.library1", "androidx.compose.material3.Scaffold", uiInspectionSource, "DefinitionReadFeedback"), topBar = { TopAppBar(title = {}, navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("DefinitionReadFeedback.ArrowBackIconButton.library1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "DefinitionReadFeedback")) }, modifier = Modifier.uiName("DefinitionReadFeedback.TopAppBar.library1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "DefinitionReadFeedback")) }) { padding ->
        Column(modifier = (Modifier.fillMaxSize().padding(padding).padding(24.dp)).uiName("DefinitionReadFeedback.Column.library1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DefinitionReadFeedback")) {
            Text(stringResource(if(loading) Res.string.library_loading else Res.string.library_unavailable), modifier = Modifier.uiName("DefinitionReadFeedback.Text.library1", "androidx.compose.material3.Text", uiInspectionSource, "DefinitionReadFeedback"))
            if(!loading) TextButton(onClick = onRetry, modifier = Modifier.uiName("DefinitionReadFeedback.TextButton.library1", "androidx.compose.material3.TextButton", uiInspectionSource, "DefinitionReadFeedback")) { Text(stringResource(Res.string.meals_retry), modifier = Modifier.uiName("DefinitionReadFeedback.Text.library2", "androidx.compose.material3.Text", uiInspectionSource, "DefinitionReadFeedback")) }
        }
    }
}
private val uiInspectionSource = UiSourceFile("com.maksimowiczm.foodyou.app.ui.food.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/component/DefinitionReadFeedback.kt")
