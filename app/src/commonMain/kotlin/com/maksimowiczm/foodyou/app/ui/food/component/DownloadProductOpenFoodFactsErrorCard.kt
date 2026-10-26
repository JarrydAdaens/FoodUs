package com.maksimowiczm.foodyou.app.ui.food.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun DownloadProductOpenFoodFactsUnavailableErrorCard(
    onUpdateCredentials: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val registerUrl = stringResource(Res.string.link_open_food_fats_register)

    Card(
        modifier = (modifier).uiName("DownloadProductOpenFoodFactsUnavailableErrorCard.Card.1", "androidx.compose.material3.Card", uiInspectionSource, "DownloadProductOpenFoodFactsUnavailableErrorCard"),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
    ) {
        Column(
            modifier = (Modifier.padding(16.dp)).uiName("DownloadProductOpenFoodFactsUnavailableErrorCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DownloadProductOpenFoodFactsUnavailableErrorCard"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("DownloadProductOpenFoodFactsUnavailableErrorCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DownloadProductOpenFoodFactsUnavailableErrorCard"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Outlined.ErrorOutline, contentDescription = null, modifier = Modifier.uiName("DownloadProductOpenFoodFactsUnavailableErrorCard.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DownloadProductOpenFoodFactsUnavailableErrorCard"))
                Text(
                    text = stringResource(Res.string.headline_open_food_facts),
                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("DownloadProductOpenFoodFactsUnavailableErrorCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductOpenFoodFactsUnavailableErrorCard")
                )
            }

            Text(
                text = stringResource(Res.string.error_open_food_facts_service_unavailable),
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("DownloadProductOpenFoodFactsUnavailableErrorCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductOpenFoodFactsUnavailableErrorCard")
            )

            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("DownloadProductOpenFoodFactsUnavailableErrorCard.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DownloadProductOpenFoodFactsUnavailableErrorCard"),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                Button(
                    onClick = onUpdateCredentials,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onErrorContainer,
                            contentColor = MaterialTheme.colorScheme.errorContainer,
                        ), modifier = Modifier.uiName("DownloadProductOpenFoodFactsUnavailableErrorCard.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "DownloadProductOpenFoodFactsUnavailableErrorCard")
                ) {
                    Text(stringResource(Res.string.action_sign_in), modifier = Modifier.uiName("DownloadProductOpenFoodFactsUnavailableErrorCard.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductOpenFoodFactsUnavailableErrorCard"))
                }

                Button(
                    onClick = { uriHandler.openUri(registerUrl) },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onErrorContainer,
                            contentColor = MaterialTheme.colorScheme.errorContainer,
                        ), modifier = Modifier.uiName("DownloadProductOpenFoodFactsUnavailableErrorCard.Button.2", "androidx.compose.material3.Button", uiInspectionSource, "DownloadProductOpenFoodFactsUnavailableErrorCard")
                ) {
                    Text(stringResource(Res.string.action_register), modifier = Modifier.uiName("DownloadProductOpenFoodFactsUnavailableErrorCard.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductOpenFoodFactsUnavailableErrorCard"))
                }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/component/DownloadProductOpenFoodFactsErrorCard.kt",
)
