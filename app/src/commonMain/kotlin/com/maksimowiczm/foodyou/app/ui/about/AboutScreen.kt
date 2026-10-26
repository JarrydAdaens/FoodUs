package com.maksimowiczm.foodyou.app.ui.about

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.maksimowiczm.foodyou.app.ui.changelog.ChangelogModalBottomSheet
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.InteractiveLogo
import com.maksimowiczm.foodyou.app.ui.common.theme.brandTypography
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalAppConfig
import com.maksimowiczm.foodyou.common.compose.component.StatusBarProtection
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutScreen(onBack: () -> Unit, onSponsor: () -> Unit, modifier: Modifier = Modifier) {
    val appConfig = LocalAppConfig.current
    val uriHandler = LocalUriHandler.current

    val scrollState = rememberScrollState()
    val systemTopBarHeight = WindowInsets.systemBars.getTop(LocalDensity.current)

    var showChangelog by rememberSaveable { mutableStateOf(false) }
    if (showChangelog) {
        ChangelogModalBottomSheet(onDismissRequest = { showChangelog = false }, modifier = Modifier.uiName("AboutScreen.ChangelogModalBottomSheet.1", "com.maksimowiczm.foodyou.app.ui.changelog.ChangelogModalBottomSheet", uiInspectionSource, "AboutScreen"))
    }

    Box((modifier).uiName("AboutScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "AboutScreen")) {
        // Padding according to the Material Design App bars guidelines
        // https://m3.material.io/components/app-bars/specs
        val insets = TopAppBarDefaults.windowInsets
        val padding = PaddingValues(top = 8.dp, start = 4.dp)

        Box(
            modifier =
                (Modifier.windowInsetsPadding(insets)
                    .consumeWindowInsets(insets)
                    .padding(padding)
                    .zIndex(100f)).uiName("AboutScreen.Box.2", "androidx.compose.foundation.layout.Box", uiInspectionSource, "AboutScreen")
        ) {
            ArrowBackIconButton(onBack, modifier = Modifier.uiName("AboutScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "AboutScreen"))
        }

        Column(
            modifier = (Modifier.fillMaxSize().verticalScroll(scrollState)).uiName("AboutScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "AboutScreen"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
            InteractiveLogo(
                (Modifier.padding(horizontal = 32.dp)
                    .widthIn(max = 350.dp)
                    .aspectRatio(1f)
                    .fillMaxSize()).uiName("AboutScreen.InteractiveLogo.1", "com.maksimowiczm.foodyou.app.ui.common.component.InteractiveLogo", uiInspectionSource, "AboutScreen")
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.app_name),
                modifier = (Modifier.padding(horizontal = 16.dp)).uiName("AboutScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "AboutScreen"),
                textAlign = TextAlign.Center,
                style = brandTypography.brandName,
            )
            Text(
                text =
                    buildString {
                        append(stringResource(Res.string.headline_version))
                        append(" ")
                        append(appConfig.forkVersionName)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("AboutScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "AboutScreen")
            )
            Text(
                text =
                    stringResource(
                        Res.string.description_derived_from_food_you,
                        appConfig.versionName,
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("AboutScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "AboutScreen")
            )
            TextButton(onClick = { uriHandler.openUri(appConfig.upstreamAuthorUri) }, modifier = Modifier.uiName("AboutScreen.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "AboutScreen")) {
                Text(text = stringResource(Res.string.action_original_creator_on_github), modifier = Modifier.uiName("AboutScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "AboutScreen"))
            }
            Text(
                text = icons8stringResource(MaterialTheme.typography.bodyMedium),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("AboutScreen.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "AboutScreen")
            )
            Spacer(Modifier.height(64.dp))
            AboutButtons(
                onSourceCode = { uriHandler.openUri(appConfig.sourceCodeUri) },
                onChangelog = { showChangelog = true },
                onIdea = { uriHandler.openUri(appConfig.issueTrackerUri) },
                onEmail = { uriHandler.openUri(appConfig.contactEmailUri) }, modifier = Modifier.uiName("AboutScreen.AboutButtons.1", "com.maksimowiczm.foodyou.app.ui.about.AboutButtons", uiInspectionSource, "AboutScreen")
            )
            Spacer(Modifier.height(24.dp))
            SponsorButton(
                onClick = onSponsor,
                modifier = (Modifier.padding(horizontal = 16.dp)).uiName("AboutScreen.SponsorButton.1", "com.maksimowiczm.foodyou.app.ui.about.SponsorButton", uiInspectionSource, "AboutScreen"),
            )
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
        }
    }
    StatusBarProtection(
        progress = {
            lerp(start = 0f, stop = 1f, fraction = scrollState.value.toFloat() / systemTopBarHeight)
        }
    )
}

@Composable
private fun AboutButtons(
    onSourceCode: () -> Unit,
    onChangelog: () -> Unit,
    onIdea: () -> Unit,
    onEmail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonHeight = 56.dp

    Row(
        modifier = (modifier).uiName("AboutButtons.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "AboutButtons"),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        OutlinedButton(
            onClick = onSourceCode,
            shapes = ButtonDefaults.shapesFor(buttonHeight),
            modifier = (Modifier.height(buttonHeight)).uiName("AboutButtons.OutlinedButton.1", "androidx.compose.material3.OutlinedButton", uiInspectionSource, "AboutButtons"),
            contentPadding = ButtonDefaults.contentPaddingFor(buttonHeight),
        ) {
            Icon(
                imageVector = Icons.Outlined.Code,
                contentDescription = stringResource(Res.string.headline_source_code),
                modifier = (Modifier.size(ButtonDefaults.iconSizeFor(buttonHeight))).uiName("AboutButtons.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "AboutButtons"),
            )
        }
        OutlinedButton(
            onClick = onChangelog,
            shapes = ButtonDefaults.shapesFor(buttonHeight),
            modifier = (Modifier.height(buttonHeight)).uiName("AboutButtons.OutlinedButton.2", "androidx.compose.material3.OutlinedButton", uiInspectionSource, "AboutButtons"),
            contentPadding = ButtonDefaults.contentPaddingFor(buttonHeight),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                contentDescription = stringResource(Res.string.headline_changelog),
                modifier = (Modifier.size(ButtonDefaults.iconSizeFor(buttonHeight))).uiName("AboutButtons.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "AboutButtons"),
            )
        }
        OutlinedButton(
            onClick = onIdea,
            shapes = ButtonDefaults.shapesFor(buttonHeight),
            modifier = (Modifier.height(buttonHeight)).uiName("AboutButtons.OutlinedButton.3", "androidx.compose.material3.OutlinedButton", uiInspectionSource, "AboutButtons"),
            contentPadding = ButtonDefaults.contentPaddingFor(buttonHeight),
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = stringResource(Res.string.action_feature_request_on_github),
                modifier = (Modifier.size(ButtonDefaults.iconSizeFor(buttonHeight))).uiName("AboutButtons.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "AboutButtons"),
            )
        }
        OutlinedButton(
            onClick = onEmail,
            shapes = ButtonDefaults.shapesFor(buttonHeight),
            modifier = (Modifier.height(buttonHeight)).uiName("AboutButtons.OutlinedButton.4", "androidx.compose.material3.OutlinedButton", uiInspectionSource, "AboutButtons"),
            contentPadding = ButtonDefaults.contentPaddingFor(buttonHeight),
        ) {
            Icon(
                imageVector = Icons.Outlined.Mail,
                contentDescription = stringResource(Res.string.action_write_an_email),
                modifier = (Modifier.size(ButtonDefaults.iconSizeFor(buttonHeight))).uiName("AboutButtons.Icon.4", "androidx.compose.material3.Icon", uiInspectionSource, "AboutButtons"),
            )
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.about",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/about/AboutScreen.kt",
)
