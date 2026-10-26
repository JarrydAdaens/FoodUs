package com.maksimowiczm.foodyou.app.ui.database.externaldatabases

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.food.search.domain.FoodSearchPreferences
import foodyou.app.generated.resources.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun UpdateUsdaApiKeyDialog(
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val foodSearchPreferencesRepository: UserPreferencesRepository<FoodSearchPreferences> =
        koinInject(named(FoodSearchPreferences::class.qualifiedName!!))
    val foodSearchPreferences =
        foodSearchPreferencesRepository.observe().collectAsStateWithLifecycle(null).value

    if (foodSearchPreferences?.usda == null) {
        return
    }

    val textFieldState = rememberTextFieldState(foodSearchPreferences.usda.apiKey ?: "")

    UiSourceScope(uiInspectionSource.element("UpdateUsdaApiKeyDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "UpdateUsdaApiKeyDialog")) { AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    runBlocking {
                        val key = textFieldState.text.toString().takeIf { it.isNotBlank() }
                        foodSearchPreferencesRepository.update {
                            copy(usda = usda.copy(apiKey = key))
                        }
                        onSave()
                    }
                }, modifier = Modifier.uiName("UpdateUsdaApiKeyDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "UpdateUsdaApiKeyDialog")
            ) {
                Text(stringResource(Res.string.action_save), modifier = Modifier.uiName("UpdateUsdaApiKeyDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "UpdateUsdaApiKeyDialog"))
            }
        },
        modifier = (modifier).uiName("UpdateUsdaApiKeyDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "UpdateUsdaApiKeyDialog"),
        dismissButton = {
            TextButton(onDismissRequest, modifier = Modifier.uiName("UpdateUsdaApiKeyDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "UpdateUsdaApiKeyDialog")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("UpdateUsdaApiKeyDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "UpdateUsdaApiKeyDialog")) }
        },
        title = { Text(stringResource(Res.string.action_set_key), modifier = Modifier.uiName("UpdateUsdaApiKeyDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "UpdateUsdaApiKeyDialog")) },
        text = {
            OutlinedTextField(
                state = textFieldState,
                placeholder = { Text(stringResource(Res.string.headline_api_key), modifier = Modifier.uiName("UpdateUsdaApiKeyDialog.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "UpdateUsdaApiKeyDialog")) }, modifier = Modifier.uiName("UpdateUsdaApiKeyDialog.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "UpdateUsdaApiKeyDialog")
            )
        },
    ) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.database.externaldatabases",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/externaldatabases/UpdateUsdaApiKeyDialog.kt",
)
