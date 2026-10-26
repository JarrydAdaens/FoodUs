package com.maksimowiczm.foodyou.app.ui.food.search

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.food.component.DownloadProductOpenFoodFactsUnavailableErrorCard
import com.maksimowiczm.foodyou.app.ui.food.component.DownloadProductUsdaErrorCard
import com.maksimowiczm.foodyou.food.domain.entity.RemoteProductException
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FoodSearchErrorCard(
    error: RemoteProductException,
    onRetry: () -> Unit,
    onUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (error) {
        is RemoteProductException.Unknown ->
            FoodSearchErrorCard(
                message = null,
                detailedMessage = error.message,
                onRetry = onRetry,
                modifier = (modifier).uiName("FoodSearchErrorCard.FoodSearchErrorCard.1", "com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchErrorCard", uiInspectionSource, "FoodSearchErrorCard"),
            )

        is RemoteProductException.OpenFoodFacts.ServiceUnavailable ->
            DownloadProductOpenFoodFactsUnavailableErrorCard(
                onUpdateCredentials = onUpdateOpenFoodFactsCredentials,
                modifier = (modifier).uiName("FoodSearchErrorCard.DownloadProductOpenFoodFactsUnavailableErrorCard.1", "com.maksimowiczm.foodyou.app.ui.food.component.DownloadProductOpenFoodFactsUnavailableErrorCard", uiInspectionSource, "FoodSearchErrorCard"),
            )

        is RemoteProductException.OpenFoodFacts.RateLimit ->
            FoodSearchErrorCard(
                message = stringResource(Res.string.error_open_food_facts_timeout),
                detailedMessage = null,
                onRetry = onRetry,
                modifier = (modifier).uiName("FoodSearchErrorCard.FoodSearchErrorCard.2", "com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchErrorCard", uiInspectionSource, "FoodSearchErrorCard"),
            )

        is RemoteProductException.ProductNotFoundException -> Unit

        is RemoteProductException.USDA.ApiKeyDisabledException,
        is RemoteProductException.USDA.ApiKeyInvalidException,
        is RemoteProductException.USDA.ApiKeyIsMissingException,
        is RemoteProductException.USDA.ApiKeyUnauthorizedException,
        is RemoteProductException.USDA.ApiKeyUnverifiedException,
        is RemoteProductException.USDA.RateLimitException ->
            DownloadProductUsdaErrorCard(
                error = error,
                onUpdateApiKey = onUsdaApiKey,
                modifier = (modifier).uiName("FoodSearchErrorCard.DownloadProductUsdaErrorCard.1", "com.maksimowiczm.foodyou.app.ui.food.component.DownloadProductUsdaErrorCard", uiInspectionSource, "FoodSearchErrorCard"),
            )
    }
}

@Composable
private fun FoodSearchErrorCard(
    message: String?,
    detailedMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDetails by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = (modifier).uiName("FoodSearchErrorCard.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "FoodSearchErrorCard"),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = (Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)).uiName("FoodSearchErrorCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "FoodSearchErrorCard")) {
            Text(
                text = stringResource(Res.string.neutral_an_error_occurred),
                style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("FoodSearchErrorCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchErrorCard")
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message ?: stringResource(Res.string.neutral_remote_database_error),
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("FoodSearchErrorCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchErrorCard")
            )
            if (detailedMessage == null) {
                Spacer(Modifier.height(16.dp))
            } else {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = (Modifier.fillMaxWidth()).uiName("FoodSearchErrorCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "FoodSearchErrorCard"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(
                        onClick = { showDetails = !showDetails },
                        colors =
                            ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ), modifier = Modifier.uiName("FoodSearchErrorCard.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "FoodSearchErrorCard")
                    ) {
                        Text(stringResource(Res.string.action_show_details), modifier = Modifier.uiName("FoodSearchErrorCard.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchErrorCard"))
                    }

                    FilledTonalButton(
                        onClick = { onRetry() },
                        colors =
                            ButtonDefaults.filledTonalButtonColors(
                                contentColor = MaterialTheme.colorScheme.errorContainer,
                                containerColor = MaterialTheme.colorScheme.onErrorContainer,
                            ), modifier = Modifier.uiName("FoodSearchErrorCard.FilledTonalButton.1", "androidx.compose.material3.FilledTonalButton", uiInspectionSource, "FoodSearchErrorCard")
                    ) {
                        Text(stringResource(Res.string.action_retry), modifier = Modifier.uiName("FoodSearchErrorCard.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchErrorCard"))
                    }
                }
                Spacer(Modifier.height(8.dp))
                AnimatedVisibility(showDetails, modifier = Modifier.uiName("FoodSearchErrorCard.AnimatedVisibility.1", "androidx.compose.animation.AnimatedVisibility", uiInspectionSource, "FoodSearchErrorCard")) {
                    Text(
                        text = detailedMessage,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = (Modifier.padding(bottom = 8.dp)).uiName("FoodSearchErrorCard.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchErrorCard"),
                    )
                }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.search",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/search/FoodSearchErrorCard.kt",
)
