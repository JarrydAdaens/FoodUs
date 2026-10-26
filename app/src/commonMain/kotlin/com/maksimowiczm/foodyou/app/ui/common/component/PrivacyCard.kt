package com.maksimowiczm.foodyou.app.ui.common.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalAppConfig
import com.maksimowiczm.foodyou.app.ui.database.externaldatabases.OpenFoodFactsLoginDialog
import com.maksimowiczm.foodyou.app.ui.database.externaldatabases.UpdateUsdaApiKeyDialog
import com.maksimowiczm.foodyou.food.domain.repository.OpenFoodFactsCredentialsRepository
import foodyou.app.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun PrivacyCard(
    title: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    val inner =
        @Composable {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                Column((Modifier.padding(contentPadding)).uiName("PrivacyCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "PrivacyCard")) {
                    title()
                    Spacer(Modifier.height(8.dp))
                    content()
                }
            }
        }

    Surface(
        onClick = onClick,
        modifier = (modifier).uiName("PrivacyCard.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "PrivacyCard"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        content = inner,
    )
}

@Composable
fun OpenFoodFactsPrivacyCard(
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    termsOfUseUri: String = LocalAppConfig.current.openFoodFactsTermsOfUseUri,
    privacyPolicyUri: String = LocalAppConfig.current.openFoodFactsPrivacyPolicyUri,
    websiteUri: String = LocalAppConfig.current.openFoodFactsWebsiteUri,
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val credentialsRepository: OpenFoodFactsCredentialsRepository = koinInject()

    val hasCredentials by
        remember { credentialsRepository.hasCredentials() }.collectAsStateWithLifecycle(false)

    var showLoginDialog by rememberSaveable { mutableStateOf(false) }
    if (showLoginDialog) {
        OpenFoodFactsLoginDialog(
            onDismissRequest = { showLoginDialog = false },
            onSave = { showLoginDialog = false }, modifier = Modifier.uiName("OpenFoodFactsPrivacyCard.OpenFoodFactsLoginDialog.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.OpenFoodFactsLoginDialog", uiInspectionSource, "OpenFoodFactsPrivacyCard")
        )
    }

    PrivacyCard(
        title = {
            Row(
                modifier = (Modifier.fillMaxSize()).uiName("OpenFoodFactsPrivacyCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "OpenFoodFactsPrivacyCard"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(modifier = (Modifier.size(48.dp)).uiName("OpenFoodFactsPrivacyCard.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "OpenFoodFactsPrivacyCard"), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(Res.drawable.openfoodfacts_logo),
                        contentDescription = null,
                        modifier = (Modifier.size(32.dp)).uiName("OpenFoodFactsPrivacyCard.Image.1", "androidx.compose.foundation.Image", uiInspectionSource, "OpenFoodFactsPrivacyCard"),
                    )
                }
                Text(
                    text = stringResource(Res.string.headline_open_food_facts),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = (Modifier.weight(1f)).uiName("OpenFoodFactsPrivacyCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsPrivacyCard"),
                )
                Box(modifier = (Modifier.size(48.dp)).uiName("OpenFoodFactsPrivacyCard.Box.2", "androidx.compose.foundation.layout.Box", uiInspectionSource, "OpenFoodFactsPrivacyCard"), contentAlignment = Alignment.Center) {
                    Checkbox(checked = selected, onCheckedChange = null, modifier = Modifier.uiName("OpenFoodFactsPrivacyCard.Checkbox.1", "androidx.compose.material3.Checkbox", uiInspectionSource, "OpenFoodFactsPrivacyCard"))
                }
            }
        },
        modifier = (modifier).uiName("OpenFoodFactsPrivacyCard.PrivacyCard.1", "com.maksimowiczm.foodyou.app.ui.common.component.PrivacyCard", uiInspectionSource, "OpenFoodFactsPrivacyCard"),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
        onClick = { onSelectedChange(!selected) },
    ) {
        Column (modifier = Modifier.uiName("OpenFoodFactsPrivacyCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "OpenFoodFactsPrivacyCard")) {
            Text(
                text = stringResource(Res.string.description_open_food_facts),
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("OpenFoodFactsPrivacyCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsPrivacyCard")
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("OpenFoodFactsPrivacyCard.FlowRow.1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "OpenFoodFactsPrivacyCard")) {
                WebsiteChip(onClick = { uriHandler.openUri(websiteUri) }, modifier = Modifier.uiName("OpenFoodFactsPrivacyCard.WebsiteChip.1", "com.maksimowiczm.foodyou.app.ui.common.component.WebsiteChip", uiInspectionSource, "OpenFoodFactsPrivacyCard"))
                TermsOfUseChip(onClick = { uriHandler.openUri(termsOfUseUri) }, modifier = Modifier.uiName("OpenFoodFactsPrivacyCard.TermsOfUseChip.1", "com.maksimowiczm.foodyou.app.ui.common.component.TermsOfUseChip", uiInspectionSource, "OpenFoodFactsPrivacyCard"))
                PrivacyPolicyChip(onClick = { uriHandler.openUri(privacyPolicyUri) }, modifier = Modifier.uiName("OpenFoodFactsPrivacyCard.PrivacyPolicyChip.1", "com.maksimowiczm.foodyou.app.ui.common.component.PrivacyPolicyChip", uiInspectionSource, "OpenFoodFactsPrivacyCard"))
                AssistChip(
                    onClick = {
                        if (hasCredentials) scope.launch { credentialsRepository.clear() }
                        else showLoginDialog = true
                    },
                    leadingIcon = {
                        if (hasCredentials) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = null,
                                modifier = (Modifier.size(AssistChipDefaults.IconSize)).uiName("OpenFoodFactsPrivacyCard.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "OpenFoodFactsPrivacyCard"),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Login,
                                contentDescription = null,
                                modifier = (Modifier.size(AssistChipDefaults.IconSize)).uiName("OpenFoodFactsPrivacyCard.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "OpenFoodFactsPrivacyCard"),
                            )
                        }
                    },
                    label = {
                        if (hasCredentials) {
                            Text(stringResource(Res.string.action_sign_out), modifier = Modifier.uiName("OpenFoodFactsPrivacyCard.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsPrivacyCard"))
                        } else {
                            Text(stringResource(Res.string.action_sign_in), modifier = Modifier.uiName("OpenFoodFactsPrivacyCard.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "OpenFoodFactsPrivacyCard"))
                        }
                    }, modifier = Modifier.uiName("OpenFoodFactsPrivacyCard.AssistChip.1", "androidx.compose.material3.AssistChip", uiInspectionSource, "OpenFoodFactsPrivacyCard")
                )
            }
        }
    }
}

@Composable
fun UsdaPrivacyCard(
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    privacyPolicyUri: String = LocalAppConfig.current.foodDataCentralPrivacyPolicyUri,
    websiteUri: String = LocalAppConfig.current.foodDataCentralWebsiteUri,
) {
    val uriHandler = LocalUriHandler.current
    var showApiKeyDialog by rememberSaveable { mutableStateOf(false) }
    if (showApiKeyDialog) {
        UpdateUsdaApiKeyDialog(
            onDismissRequest = { showApiKeyDialog = false },
            onSave = { showApiKeyDialog = false }, modifier = Modifier.uiName("UsdaPrivacyCard.UpdateUsdaApiKeyDialog.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.UpdateUsdaApiKeyDialog", uiInspectionSource, "UsdaPrivacyCard")
        )
    }

    PrivacyCard(
        title = {
            Row(
                modifier = (Modifier.fillMaxSize()).uiName("UsdaPrivacyCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "UsdaPrivacyCard"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(modifier = (Modifier.size(48.dp)).uiName("UsdaPrivacyCard.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "UsdaPrivacyCard"), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(Res.drawable.usda_logo),
                        contentDescription = null,
                        modifier = (Modifier.size(32.dp)).uiName("UsdaPrivacyCard.Image.1", "androidx.compose.foundation.Image", uiInspectionSource, "UsdaPrivacyCard"),
                    )
                }
                Text(
                    text = stringResource(Res.string.headline_food_data_central_usda),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = (Modifier.weight(1f)).uiName("UsdaPrivacyCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "UsdaPrivacyCard"),
                )
                Box(modifier = (Modifier.size(48.dp)).uiName("UsdaPrivacyCard.Box.2", "androidx.compose.foundation.layout.Box", uiInspectionSource, "UsdaPrivacyCard"), contentAlignment = Alignment.Center) {
                    Checkbox(checked = selected, onCheckedChange = null, modifier = Modifier.uiName("UsdaPrivacyCard.Checkbox.1", "androidx.compose.material3.Checkbox", uiInspectionSource, "UsdaPrivacyCard"))
                }
            }
        },
        modifier = (modifier).uiName("UsdaPrivacyCard.PrivacyCard.1", "com.maksimowiczm.foodyou.app.ui.common.component.PrivacyCard", uiInspectionSource, "UsdaPrivacyCard"),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
        onClick = { onSelectedChange(!selected) },
    ) {
        Column (modifier = Modifier.uiName("UsdaPrivacyCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "UsdaPrivacyCard")) {
            Text(
                text = stringResource(Res.string.description_food_data_central_usda),
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("UsdaPrivacyCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "UsdaPrivacyCard")
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("UsdaPrivacyCard.FlowRow.1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "UsdaPrivacyCard")) {
                WebsiteChip(onClick = { uriHandler.openUri(websiteUri) }, modifier = Modifier.uiName("UsdaPrivacyCard.WebsiteChip.1", "com.maksimowiczm.foodyou.app.ui.common.component.WebsiteChip", uiInspectionSource, "UsdaPrivacyCard"))
                PrivacyPolicyChip(onClick = { uriHandler.openUri(privacyPolicyUri) }, modifier = Modifier.uiName("UsdaPrivacyCard.PrivacyPolicyChip.1", "com.maksimowiczm.foodyou.app.ui.common.component.PrivacyPolicyChip", uiInspectionSource, "UsdaPrivacyCard"))
                AssistChip(
                    onClick = { showApiKeyDialog = true },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Key,
                            contentDescription = null,
                            modifier = (Modifier.size(AssistChipDefaults.IconSize)).uiName("UsdaPrivacyCard.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "UsdaPrivacyCard"),
                        )
                    },
                    label = { Text(stringResource(Res.string.headline_api_key), modifier = Modifier.uiName("UsdaPrivacyCard.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "UsdaPrivacyCard")) }, modifier = Modifier.uiName("UsdaPrivacyCard.AssistChip.1", "androidx.compose.material3.AssistChip", uiInspectionSource, "UsdaPrivacyCard")
                )
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/PrivacyCard.kt",
)
