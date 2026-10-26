package com.maksimowiczm.foodyou.app.ui.changelog

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PreviewReleaseDialog(modifier: Modifier = Modifier) {
    val viewModel: ChangelogViewModel = koinViewModel()
    val showDialog by viewModel.showDialog.collectAsStateWithLifecycle()

    if (showDialog)
        UiSourceScope(uiInspectionSource.element("PreviewReleaseDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "PreviewReleaseDialog")) { AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Row (modifier = Modifier.uiName("PreviewReleaseDialog.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "PreviewReleaseDialog")) {
                    TextButton(onClick = viewModel::dontShowAgain, modifier = Modifier.uiName("PreviewReleaseDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "PreviewReleaseDialog")) {
                        Text(stringResource(Res.string.action_dont_show_again), modifier = Modifier.uiName("PreviewReleaseDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "PreviewReleaseDialog"))
                    }
                    TextButton(onClick = viewModel::dismissDialog, modifier = Modifier.uiName("PreviewReleaseDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "PreviewReleaseDialog")) {
                        Text(stringResource(Res.string.positive_ok), modifier = Modifier.uiName("PreviewReleaseDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "PreviewReleaseDialog"))
                    }
                }
            },
            modifier = (modifier).uiName("PreviewReleaseDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "PreviewReleaseDialog"),
            icon = { Icon(imageVector = Icons.Outlined.Warning, contentDescription = null, modifier = Modifier.uiName("PreviewReleaseDialog.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "PreviewReleaseDialog")) },
            title = { Text(stringResource(Res.string.headline_preview_release), modifier = Modifier.uiName("PreviewReleaseDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "PreviewReleaseDialog")) },
            text = { Text(stringResource(Res.string.description_preview_release), modifier = Modifier.uiName("PreviewReleaseDialog.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "PreviewReleaseDialog")) },
        ) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.changelog",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/changelog/PreviewReleaseDialog.kt",
)
