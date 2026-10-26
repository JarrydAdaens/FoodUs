package com.maksimowiczm.foodyou.app.ui.developer.inspection

import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.settings.domain.entity.Settings
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
internal fun UiNameBarSettingsListItem() {
    val repository: UserPreferencesRepository<Settings> = koinInject(named(Settings::class.qualifiedName!!))
    val enabled by remember(repository) { repository.observe().map { it.uiNameBarEnabled } }
        .collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()
    val toggle: (Boolean) -> Unit = { value ->
        scope.launch { repository.update { copy(uiNameBarEnabled = value) } }
    }
    SettingsListItem(
        modifier = Modifier.uiName("developer.uiNameBar.setting", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", source, "UiNameBarSettingsListItem"),
        label = { Text(stringResource(Res.string.ui_name_bar_setting), modifier = Modifier.uiName("developer.uiNameBar.label", "androidx.compose.material3.Text", source, "UiNameBarSettingsListItem")) },
        supportingContent = { Text(stringResource(Res.string.ui_name_bar_description), modifier = Modifier.uiName("developer.uiNameBar.description", "androidx.compose.material3.Text", source, "UiNameBarSettingsListItem")) },
        onClick = { toggle(!enabled) },
        trailingContent = { Switch(checked = enabled, onCheckedChange = toggle,
            modifier = Modifier.uiName("developer.uiNameBar.switch", "androidx.compose.material3.Switch", source, "UiNameBarSettingsListItem")) },
    )
}

private val source = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.developer.inspection",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/developer/inspection/UiNameBarSettingsListItem.kt",
)
