package com.maksimowiczm.foodyou.app.ui.food.recipe
import androidx.compose.ui.Modifier

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RecipeSaveErrorDialog(onDismiss: () -> Unit) {
    UiSourceScope(uiInspectionSource.element("RecipeSaveErrorDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "RecipeSaveErrorDialog")) { AlertDialog(onDismissRequest = onDismiss,
        text = { Text(stringResource(Res.string.error_recipe_save), modifier = Modifier.uiName("RecipeSaveErrorDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "RecipeSaveErrorDialog")) },
        confirmButton = { TextButton(onDismiss, modifier = Modifier.uiName("RecipeSaveErrorDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "RecipeSaveErrorDialog")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("RecipeSaveErrorDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "RecipeSaveErrorDialog")) } }, modifier = Modifier.uiName("RecipeSaveErrorDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "RecipeSaveErrorDialog")) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.recipe",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/recipe/RecipeSaveErrorDialog.kt",
)
