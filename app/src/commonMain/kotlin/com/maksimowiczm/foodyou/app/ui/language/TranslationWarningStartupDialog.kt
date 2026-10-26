package com.maksimowiczm.foodyou.app.ui.language

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TranslationWarningStartupDialog(modifier: Modifier = Modifier) {
    val viewModel: LanguageViewModel = koinViewModel()

    val showTranslationWarning by viewModel.showTranslationWarning.collectAsStateWithLifecycle()
    val translation by viewModel.translation.collectAsStateWithLifecycle()

    if (showTranslationWarning && !translation.isVerified) {
        LanguageWarningDialog(
            onDismissRequest = {},
            onConfirm = viewModel::hideTranslationWarning,
            modifier = (modifier).uiName("TranslationWarningStartupDialog.LanguageWarningDialog.1", "com.maksimowiczm.foodyou.app.ui.language.LanguageWarningDialog", uiInspectionSource, "TranslationWarningStartupDialog"),
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.language",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/language/TranslationWarningStartupDialog.kt",
)
