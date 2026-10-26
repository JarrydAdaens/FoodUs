package com.maksimowiczm.foodyou.app.ui.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem
import com.maksimowiczm.foodyou.settings.domain.repository.TranslationRepository
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
internal fun LanguageSettingsListItem(
    onClick: () -> Unit,
    shape: Shape,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val translationRepository: TranslationRepository = koinInject()
    val language = translationRepository.observeCurrent().collectAsStateWithLifecycle(null).value

    SettingsListItem(
        icon = { Icon(Icons.Outlined.Translate, null, modifier = Modifier.uiName("LanguageSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "LanguageSettingsListItem")) },
        label = { Text(stringResource(Res.string.headline_language), modifier = Modifier.uiName("LanguageSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "LanguageSettingsListItem")) },
        supportingContent = { language?.languageName?.let { Text(it, modifier = Modifier.uiName("LanguageSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "LanguageSettingsListItem")) } },
        onClick = onClick,
        modifier = (modifier).uiName("LanguageSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "LanguageSettingsListItem"),
        shape = shape,
        color = color,
        contentColor = contentColor,
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/LanguageSettingsListItem.kt",
)
