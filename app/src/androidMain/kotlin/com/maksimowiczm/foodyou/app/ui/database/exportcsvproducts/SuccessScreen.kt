package com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SuccessScreen(count: Int, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold((modifier).uiName("SuccessScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "SuccessScreen")) { paddingValues ->
        Box(modifier = (Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)).uiName("SuccessScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "SuccessScreen")) {
            Column(
                modifier = (Modifier.align(Alignment.Center)).uiName("SuccessScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "SuccessScreen"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = (Modifier.size(68.dp)).uiName("SuccessScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "SuccessScreen"),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text =
                        pluralStringResource(
                            Res.plurals.neutral_exported_x_food_products,
                            count,
                            count,
                        ),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.uiName("SuccessScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SuccessScreen")
                )
            }

            Button(
                onClick = onBack,
                modifier = (Modifier.height(56.dp).align(Alignment.BottomCenter)).uiName("SuccessScreen.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "SuccessScreen"),
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(stringResource(Res.string.action_go_back), modifier = Modifier.uiName("SuccessScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SuccessScreen"))
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts",
    "app/src/androidMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/exportcsvproducts/SuccessScreen.kt",
)
