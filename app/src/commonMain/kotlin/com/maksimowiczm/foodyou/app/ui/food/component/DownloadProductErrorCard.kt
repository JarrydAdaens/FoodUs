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
import com.maksimowiczm.foodyou.food.domain.entity.RemoteProductException
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun DownloadProductUsdaErrorCard(
    error: RemoteProductException.USDA,
    onUpdateApiKey: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val obtainKeyUrl = stringResource(Res.string.link_usda_obtain_key)

    val errorText =
        when (error) {
            is RemoteProductException.USDA.ApiKeyDisabledException,
            is RemoteProductException.USDA.ApiKeyInvalidException,
            is RemoteProductException.USDA.ApiKeyIsMissingException,
            is RemoteProductException.USDA.ApiKeyUnauthorizedException ->
                stringResource(Res.string.error_api_key_is_invalid)

            is RemoteProductException.USDA.ApiKeyUnverifiedException ->
                stringResource(Res.string.error_usda_not_verified)
            is RemoteProductException.USDA.RateLimitException ->
                stringResource(Res.string.error_usda_rate_limit)
        }

    Card(
        modifier = (modifier).uiName("DownloadProductUsdaErrorCard.Card.1", "androidx.compose.material3.Card", uiInspectionSource, "DownloadProductUsdaErrorCard"),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
    ) {
        Column(
            modifier = (Modifier.padding(16.dp)).uiName("DownloadProductUsdaErrorCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DownloadProductUsdaErrorCard"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("DownloadProductUsdaErrorCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DownloadProductUsdaErrorCard"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Outlined.ErrorOutline, contentDescription = null, modifier = Modifier.uiName("DownloadProductUsdaErrorCard.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DownloadProductUsdaErrorCard"))
                Text(
                    text = stringResource(Res.string.headline_food_data_central_usda),
                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("DownloadProductUsdaErrorCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductUsdaErrorCard")
                )
            }

            Text(text = errorText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("DownloadProductUsdaErrorCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductUsdaErrorCard"))

            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("DownloadProductUsdaErrorCard.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DownloadProductUsdaErrorCard"),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                Button(
                    onClick = onUpdateApiKey,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onErrorContainer,
                            contentColor = MaterialTheme.colorScheme.errorContainer,
                        ), modifier = Modifier.uiName("DownloadProductUsdaErrorCard.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "DownloadProductUsdaErrorCard")
                ) {
                    Text(stringResource(Res.string.action_set_key), modifier = Modifier.uiName("DownloadProductUsdaErrorCard.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductUsdaErrorCard"))
                }

                Button(
                    onClick = { uriHandler.openUri(obtainKeyUrl) },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onErrorContainer,
                            contentColor = MaterialTheme.colorScheme.errorContainer,
                        ), modifier = Modifier.uiName("DownloadProductUsdaErrorCard.Button.2", "androidx.compose.material3.Button", uiInspectionSource, "DownloadProductUsdaErrorCard")
                ) {
                    Text(stringResource(Res.string.action_obtain_key), modifier = Modifier.uiName("DownloadProductUsdaErrorCard.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductUsdaErrorCard"))
                }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/component/DownloadProductErrorCard.kt",
)
