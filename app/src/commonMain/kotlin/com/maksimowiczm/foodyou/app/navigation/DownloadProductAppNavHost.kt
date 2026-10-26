package com.maksimowiczm.foodyou.app.navigation

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.maksimowiczm.foodyou.app.navigation.DownloadProductAppNavHost.CreateProduct
import com.maksimowiczm.foodyou.app.navigation.DownloadProductAppNavHost.OpenFoodFactsLogin
import com.maksimowiczm.foodyou.app.navigation.DownloadProductAppNavHost.UsdaApiKey
import com.maksimowiczm.foodyou.app.ui.database.externaldatabases.OpenFoodFactsLoginDialog
import com.maksimowiczm.foodyou.app.ui.database.externaldatabases.UpdateUsdaApiKeyDialog
import com.maksimowiczm.foodyou.app.ui.food.product.CreateProductScreen
import kotlinx.serialization.Serializable

@Composable
fun DownloadProductAppNavHost(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    url: String,
    modifier: Modifier = Modifier.Companion,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CreateProduct(url),
        modifier = modifier,
    ) {
        dialog<UsdaApiKey> {
            UpdateUsdaApiKeyDialog(
                onDismissRequest = { navController.popBackStackInclusive<UsdaApiKey>() },
                onSave = { navController.popBackStackInclusive<UsdaApiKey>() }, modifier = Modifier.uiName("DownloadProductAppNavHost.UpdateUsdaApiKeyDialog.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.UpdateUsdaApiKeyDialog", uiInspectionSource, "DownloadProductAppNavHost")
            )
        }
        dialog<OpenFoodFactsLogin> {
            OpenFoodFactsLoginDialog(
                onDismissRequest = { navController.popBackStackInclusive<OpenFoodFactsLogin>() },
                onSave = { navController.popBackStackInclusive<OpenFoodFactsLogin>() }, modifier = Modifier.uiName("DownloadProductAppNavHost.OpenFoodFactsLoginDialog.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.OpenFoodFactsLoginDialog", uiInspectionSource, "DownloadProductAppNavHost")
            )
        }
        forwardBackwardComposable<CreateProduct> {
            val (url) = it.toRoute<CreateProduct>()

            UiSourceScope(uiInspectionSource.element("DownloadProductAppNavHost.CreateProductScreen.1", "com.maksimowiczm.foodyou.app.ui.food.product.CreateProductScreen", "DownloadProductAppNavHost")) { CreateProductScreen(
                onBack = onBack,
                onCreate = { onCreate() },
                onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                onUpdateOpenFoodFactsCredentials = {
                    navController.navigateSingleTop(OpenFoodFactsLogin)
                },
                url = url,
            ) }
        }
    }
}

private object DownloadProductAppNavHost {
    @Serializable object UsdaApiKey

    @Serializable data class CreateProduct(val url: String)

    @Serializable object OpenFoodFactsLogin
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.navigation",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/navigation/DownloadProductAppNavHost.kt",
)
