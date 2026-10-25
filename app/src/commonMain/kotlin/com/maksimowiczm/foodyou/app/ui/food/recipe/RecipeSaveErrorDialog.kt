package com.maksimowiczm.foodyou.app.ui.food.recipe

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RecipeSaveErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        text = { Text(stringResource(Res.string.error_recipe_save)) },
        confirmButton = { TextButton(onDismiss) { Text(stringResource(Res.string.action_cancel)) } })
}
