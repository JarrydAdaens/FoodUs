package com.maksimowiczm.foodyou.app.ui.language

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TranslateButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = (modifier.height(96.dp)).uiName("TranslateButton.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "TranslateButton"),
        shapes = ButtonDefaults.shapes(),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        contentPadding = PaddingValues(horizontal = 48.dp),
    ) {
        Row(
            modifier = (Modifier.fillMaxWidth()).uiName("TranslateButton.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "TranslateButton"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = null,
                modifier = (Modifier.size(32.dp)).uiName("TranslateButton.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "TranslateButton"),
            )
            Column (modifier = Modifier.uiName("TranslateButton.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "TranslateButton")) {
                Text(
                    text = stringResource(Res.string.action_translate),
                    style = MaterialTheme.typography.headlineSmall, modifier = Modifier.uiName("TranslateButton.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "TranslateButton")
                )
                Text(
                    text = stringResource(Res.string.neutral_help_translating_the_app),
                    style = MaterialTheme.typography.bodyLarge, modifier = Modifier.uiName("TranslateButton.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "TranslateButton")
                )
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.language",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/language/TranslateButton.kt",
)
