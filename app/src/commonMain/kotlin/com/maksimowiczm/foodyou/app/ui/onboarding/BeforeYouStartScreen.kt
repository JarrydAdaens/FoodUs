package com.maksimowiczm.foodyou.app.ui.onboarding

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.InteractiveLogo
import com.maksimowiczm.foodyou.app.ui.common.component.PrivacyPolicyChip
import com.maksimowiczm.foodyou.app.ui.common.theme.brandTypography
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalAppConfig
import com.maksimowiczm.foodyou.common.compose.extension.add
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun BeforeYouStartScreen(onContinue: () -> Unit, modifier: Modifier = Modifier) {
    val appConfig = LocalAppConfig.current
    val uriHandler = LocalUriHandler.current

    UiSourceScope(uiInspectionSource.element("BeforeYouStartScreen.BeforeYouStartScreen.1", "com.maksimowiczm.foodyou.app.ui.onboarding.BeforeYouStartScreen", "BeforeYouStartScreen")) { BeforeYouStartScreen(
        onContinue = onContinue,
        onPrivacyPolicy = { uriHandler.openUri(appConfig.privacyPolicyUri) },
        modifier = modifier,
    ) }
}

@Composable
private fun BeforeYouStartScreen(
    onContinue: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fabHeight = ButtonDefaults.LargeContainerHeight

    Scaffold(
        modifier = (modifier).uiName("BeforeYouStartScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "BeforeYouStartScreen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.headline_before_you_start),
                        style = MaterialTheme.typography.displaySmall, modifier = Modifier.uiName("BeforeYouStartScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "BeforeYouStartScreen")
                    )
                },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("BeforeYouStartScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "BeforeYouStartScreen")
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Button(
                onClick = onContinue,
                shapes = ButtonDefaults.shapesFor(fabHeight),
                contentPadding = ButtonDefaults.contentPaddingFor(fabHeight), modifier = Modifier.uiName("BeforeYouStartScreen.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "BeforeYouStartScreen")
            ) {
                Text(
                    text = stringResource(Res.string.action_agree_and_continue),
                    style = ButtonDefaults.textStyleFor(fabHeight), modifier = Modifier.uiName("BeforeYouStartScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "BeforeYouStartScreen")
                )
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("BeforeYouStartScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "BeforeYouStartScreen"),
            contentPadding = paddingValues.add(bottom = fabHeight + 16.dp).add(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Column(
                    modifier = (Modifier.fillMaxWidth()).uiName("BeforeYouStartScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "BeforeYouStartScreen"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                ) {
                    InteractiveLogo(
                        modifier =
                            (Modifier.padding(horizontal = 64.dp)
                                .widthIn(max = 350.dp)
                                .aspectRatio(1f)
                                .fillMaxSize()).uiName("BeforeYouStartScreen.InteractiveLogo.1", "com.maksimowiczm.foodyou.app.ui.common.component.InteractiveLogo", uiInspectionSource, "BeforeYouStartScreen")
                    )
                    Text(
                        text = stringResource(Res.string.app_name),
                        style = brandTypography.brandName, modifier = Modifier.uiName("BeforeYouStartScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "BeforeYouStartScreen")
                    )
                }
            }

            item {
                FlowRow(
                    modifier = (Modifier.fillMaxWidth().padding(horizontal = 16.dp)).uiName("BeforeYouStartScreen.FlowRow.1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "BeforeYouStartScreen"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PrivacyPolicyChip(onPrivacyPolicy, modifier = Modifier.uiName("BeforeYouStartScreen.PrivacyPolicyChip.1", "com.maksimowiczm.foodyou.app.ui.common.component.PrivacyPolicyChip", uiInspectionSource, "BeforeYouStartScreen"))
                }
            }
            item {
                Text(
                    text = stringResource(Res.string.onboarding_privacy_tip),
                    modifier = (Modifier.fillMaxWidth().padding(horizontal = 32.dp)).uiName("BeforeYouStartScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "BeforeYouStartScreen"),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.onboarding",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/onboarding/BeforeYouStartScreen.kt",
)
