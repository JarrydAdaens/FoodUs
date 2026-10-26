package com.maksimowiczm.foodyou.app.ui.login

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.InteractiveLogo
import com.maksimowiczm.foodyou.app.ui.common.component.PrivacyPolicyChip
import com.maksimowiczm.foodyou.app.ui.common.theme.brandTypography
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalAppConfig
import com.maksimowiczm.foodyou.common.auth.Session
import com.maksimowiczm.foodyou.common.auth.SessionRepository
import com.maksimowiczm.foodyou.common.compose.extension.add
import foodyou.app.generated.resources.*
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun LoginScreen(onBack: () -> Unit, onLoginSuccess: () -> Unit, modifier: Modifier = Modifier) {
    val sessionRepository: SessionRepository = koinInject()
    val appConfig = LocalAppConfig.current
    val uriHandler = LocalUriHandler.current

    val session by sessionRepository.observeSession().collectAsStateWithLifecycle(null)

    if (session != null) {
        Text(text = session.toString(), modifier = (Modifier.safeContentPadding()).uiName("LoginScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "LoginScreen"))
        return
    }

    var isLoading by rememberSaveable { mutableStateOf(false) }
    var isError by rememberSaveable { mutableStateOf(false) }
    val onLogin =
        rememberOnLogin(
            onLoginSuccess = {
                isLoading = false
                isError = false
                onLoginSuccess()
            },
            onLoginError = {
                isError = true
                isLoading = false
                // TODO
                //  show error details?
                //  report via some analytics service?
            },
            onCancel = { isLoading = false },
        )

    UiSourceScope(uiInspectionSource.element("LoginScreen.LoginScreen.1", "com.maksimowiczm.foodyou.app.ui.login.LoginScreen", "LoginScreen")) { LoginScreen(
        isLoading = isLoading,
        isError = isError,
        onBack = onBack,
        onLogin = {
            if (!isLoading) {
                isLoading = true
                isError = false
                onLogin()
            }
        },
        onPrivacyPolicy = { uriHandler.openUri(appConfig.privacyPolicyUri) },
        modifier = modifier,
    ) }
}

@Composable
internal fun rememberOnLogin(
    onLoginSuccess: () -> Unit,
    onLoginError: (Throwable) -> Unit,
    onCancel: () -> Unit,
): () -> Unit {
    val sessionRepository: SessionRepository = koinInject()
    val coroutineScope = rememberCoroutineScope()

    return {
        coroutineScope.launch {
            delay(500)

            val testSession =
                Session(
                    userId = "test-user-id",
                    userEmail = "test@test.com",
                    accessToken = "test",
                    expiresAt = Instant.DISTANT_FUTURE,
                )
            sessionRepository.saveSession(testSession)

            onLoginSuccess()
        }
    }
}

@Composable
private fun LoginScreen(
    isLoading: Boolean,
    isError: Boolean,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fabHeight = remember { ButtonDefaults.LargeContainerHeight }

    Scaffold(
        modifier = (modifier).uiName("LoginScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "LoginScreen"),
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Button(
                onClick = { if (!isLoading) onLogin() },
                enabled = !isLoading,
                shapes = ButtonDefaults.shapesFor(fabHeight),
                contentPadding = ButtonDefaults.contentPaddingFor(fabHeight), modifier = Modifier.uiName("LoginScreen.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "LoginScreen")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Login,
                    contentDescription = null,
                    modifier = (Modifier.size(ButtonDefaults.iconSizeFor(fabHeight))).uiName("LoginScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "LoginScreen"),
                )
                Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(fabHeight)))
                Text(
                    text = stringResource(Res.string.action_sign_in),
                    style = ButtonDefaults.textStyleFor(fabHeight), modifier = Modifier.uiName("LoginScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "LoginScreen")
                )
            }
        },
    ) { paddingValues ->
        // Padding according to the Material Design App bars guidelines
        // https://m3.material.io/components/app-bars/specs
        val insets = TopAppBarDefaults.windowInsets
        val padding = PaddingValues(top = 8.dp, start = 4.dp)

        Box(
            modifier =
                (Modifier.windowInsetsPadding(insets)
                    .consumeWindowInsets(insets)
                    .padding(padding)
                    .zIndex(100f)).uiName("LoginScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "LoginScreen")
        ) {
            FilledIconButton(
                onClick = onBack,
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ), modifier = Modifier.uiName("LoginScreen.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "LoginScreen")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.action_go_back), modifier = Modifier.uiName("LoginScreen.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "LoginScreen")
                )
            }
        }

        LazyColumn(
            modifier = (Modifier.fillMaxSize()).uiName("LoginScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "LoginScreen"),
            contentPadding = paddingValues.add(bottom = 16.dp + fabHeight),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item { InteractiveLogo( modifier = Modifier.uiName("LoginScreen.InteractiveLogo.1", "com.maksimowiczm.foodyou.app.ui.common.component.InteractiveLogo", uiInspectionSource, "LoginScreen")) }

            item {
                Text(text = stringResource(Res.string.app_name), style = brandTypography.brandName, modifier = Modifier.uiName("LoginScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "LoginScreen"))
            }

            item {
                Column(modifier = (Modifier.fillMaxWidth()).uiName("LoginScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "LoginScreen")) {
                    FlowRow((Modifier.padding(horizontal = 32.dp)).uiName("LoginScreen.FlowRow.1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "LoginScreen")) {
                        Spacer(Modifier.width(8.dp))
                        PrivacyPolicyChip(onPrivacyPolicy, modifier = Modifier.uiName("LoginScreen.PrivacyPolicyChip.1", "com.maksimowiczm.foodyou.app.ui.common.component.PrivacyPolicyChip", uiInspectionSource, "LoginScreen"))
                    }

                    Spacer(Modifier.height(8.dp))

                    if (isLoading) {
                        LinearWavyProgressIndicator((Modifier.fillMaxWidth()).uiName("LoginScreen.LinearWavyProgressIndicator.1", "androidx.compose.material3.LinearWavyProgressIndicator", uiInspectionSource, "LoginScreen"))
                        Spacer(Modifier.height(16.dp))
                    } else {
                        Spacer(Modifier.height(26.dp))
                    }

                    AnimatedVisibility(
                        visible = isError,
                        modifier = (Modifier.padding(horizontal = 32.dp)).uiName("LoginScreen.AnimatedVisibility.1", "androidx.compose.animation.AnimatedVisibility", uiInspectionSource, "LoginScreen"),
                    ) {
                        Column (modifier = Modifier.uiName("LoginScreen.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "LoginScreen")) {
                            Spacer(Modifier.height(8.dp))
                            Card(
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                    ), modifier = Modifier.uiName("LoginScreen.Card.1", "androidx.compose.material3.Card", uiInspectionSource, "LoginScreen")
                            ) {
                                Row(
                                    modifier = (Modifier.fillMaxWidth().padding(16.dp)).uiName("LoginScreen.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "LoginScreen"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = null, modifier = Modifier.uiName("LoginScreen.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "LoginScreen")
                                    )
                                    Text(
                                        text =
                                            stringResource(
                                                Res.string.error_there_was_an_error_while_signing_in
                                            ),
                                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("LoginScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "LoginScreen")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.login",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/login/LoginScreen.kt",
)
