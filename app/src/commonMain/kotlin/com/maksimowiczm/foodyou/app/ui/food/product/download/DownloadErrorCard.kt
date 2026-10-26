package com.maksimowiczm.foodyou.app.ui.food.product.download

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.food.component.DownloadProductOpenFoodFactsUnavailableErrorCard
import com.maksimowiczm.foodyou.app.ui.food.component.DownloadProductUsdaErrorCard
import com.maksimowiczm.foodyou.food.domain.entity.RemoteProductException
import com.maksimowiczm.foodyou.food.domain.usecase.DownloadProductError
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DownloadErrorCard(
    error: DownloadProductError,
    onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (error) {
        DownloadProductError.UrlNotFound ->
            DownloadErrorCard(
                message = stringResource(Res.string.error_url_not_found),
                modifier = (modifier).uiName("DownloadErrorCard.DownloadErrorCard.1", "com.maksimowiczm.foodyou.app.ui.food.product.download.DownloadErrorCard", uiInspectionSource, "DownloadErrorCard"),
            )

        DownloadProductError.UrlNotSupported ->
            DownloadErrorCard(
                message = stringResource(Res.string.error_url_is_not_supported),
                modifier = (modifier).uiName("DownloadErrorCard.DownloadErrorCard.2", "com.maksimowiczm.foodyou.app.ui.food.product.download.DownloadErrorCard", uiInspectionSource, "DownloadErrorCard"),
            )

        is DownloadProductError.RemoteFoodError ->
            DownloadErrorCard(
                error = error.exception,
                onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials,
                onUpdateUsdaApiKey = onUpdateUsdaApiKey,
                modifier = (modifier).uiName("DownloadErrorCard.DownloadErrorCard.3", "com.maksimowiczm.foodyou.app.ui.food.product.download.DownloadErrorCard", uiInspectionSource, "DownloadErrorCard"),
            )
    }
}

@Composable
private fun DownloadErrorCard(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = (modifier).uiName("DownloadErrorCard.Card.1", "androidx.compose.material3.Card", uiInspectionSource, "DownloadErrorCard"),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
    ) {
        Column(
            modifier = (Modifier.padding(16.dp)).uiName("DownloadErrorCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DownloadErrorCard"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("DownloadErrorCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DownloadErrorCard"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Outlined.ErrorOutline, contentDescription = null, modifier = Modifier.uiName("DownloadErrorCard.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DownloadErrorCard"))
                Text(
                    text = stringResource(Res.string.error_failed_to_download_product),
                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("DownloadErrorCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DownloadErrorCard")
                )
            }

            Text(text = message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("DownloadErrorCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DownloadErrorCard"))
        }
    }
}

@Composable
private fun DownloadErrorCard(
    error: RemoteProductException,
    onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val text =
        when (error) {
            is RemoteProductException.OpenFoodFacts.RateLimit -> error.message

            is RemoteProductException.OpenFoodFacts.ServiceUnavailable ->
                return DownloadProductOpenFoodFactsUnavailableErrorCard(
                    onUpdateCredentials = onUpdateOpenFoodFactsCredentials,
                    modifier = (modifier).uiName("DownloadErrorCard.DownloadProductOpenFoodFactsUnavailableErrorCard.1", "com.maksimowiczm.foodyou.app.ui.food.component.DownloadProductOpenFoodFactsUnavailableErrorCard", uiInspectionSource, "DownloadErrorCard"),
                )

            is RemoteProductException.ProductNotFoundException ->
                stringResource(Res.string.error_product_not_found)

            is RemoteProductException.USDA ->
                return DownloadProductUsdaErrorCard(
                    error = error,
                    onUpdateApiKey = onUpdateUsdaApiKey,
                    modifier = (modifier).uiName("DownloadErrorCard.DownloadProductUsdaErrorCard.1", "com.maksimowiczm.foodyou.app.ui.food.component.DownloadProductUsdaErrorCard", uiInspectionSource, "DownloadErrorCard"),
                )

            is RemoteProductException.Unknown -> error.message
        } ?: stringResource(Res.string.error_unknown_error)

    DownloadErrorCard(message = text, modifier = (modifier).uiName("DownloadErrorCard.DownloadErrorCard.4", "com.maksimowiczm.foodyou.app.ui.food.product.download.DownloadErrorCard", uiInspectionSource, "DownloadErrorCard"))
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.product.download",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/product/download/DownloadErrorCard.kt",
)
