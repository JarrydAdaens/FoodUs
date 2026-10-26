package com.maksimowiczm.foodyou.app.ui.sponsor



import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Hail
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.toShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalAppConfig
import com.maksimowiczm.foodyou.common.compose.component.StatusBarProtection
import com.maksimowiczm.foodyou.common.compose.component.StatusBarProtectionDefaults
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.common.compose.utility.LocalClipboardManager
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.common.extension.now
import com.maksimowiczm.foodyou.sponsorship.domain.entity.AvailableSponsorMethod
import com.maksimowiczm.foodyou.sponsorship.domain.entity.CryptoSponsorMethod
import com.maksimowiczm.foodyou.sponsorship.domain.entity.LinkSponsorMethod
import foodyou.app.generated.resources.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SponsorScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<SponsorViewModel>()

    val privacyAccepted by viewModel.privacyAccepted.collectAsStateWithLifecycle()

    if (privacyAccepted) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        UiSourceScope(uiInspectionSource.element("SponsorScreen.SponsorScreen.1", "com.maksimowiczm.foodyou.app.ui.sponsor.SponsorScreen", "SponsorScreen")) { SponsorScreen(
            uiState = uiState,
            onBack = onBack,
            onOrderChange = viewModel::changeMessagesOrder,
            onNextMonth = viewModel::nextMonth,
            onPreviousMonth = viewModel::previousMonth,
            onRefresh = viewModel::refresh,
            modifier = modifier,
        ) }
    } else {
        UiSourceScope(uiInspectionSource.element("SponsorScreen.SponsorPrivacyScreen.1", "com.maksimowiczm.foodyou.app.ui.sponsor.SponsorPrivacyScreen", "SponsorScreen")) { SponsorPrivacyScreen(
            onBack = onBack,
            onAllow = { viewModel.setPrivacyAccepted(true) },
            modifier = modifier,
        ) }
    }
}

@Composable
private fun SponsorPrivacyScreen(
    onBack: () -> Unit,
    onAllow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = (modifier).uiName("SponsorPrivacyScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "SponsorPrivacyScreen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_sponsor), modifier = Modifier.uiName("SponsorPrivacyScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SponsorPrivacyScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("SponsorPrivacyScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "SponsorPrivacyScreen")) },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("SponsorPrivacyScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "SponsorPrivacyScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                (Modifier.fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .padding(horizontal = 16.dp)).uiName("SponsorPrivacyScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "SponsorPrivacyScreen"),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.uiName("SponsorPrivacyScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "SponsorPrivacyScreen")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PrivacyTip,
                        contentDescription = null,
                        modifier = (Modifier.size(64.dp)).uiName("SponsorPrivacyScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "SponsorPrivacyScreen"),
                    )
                    Text(
                        text = stringResource(Res.string.description_sponsors_privacy),
                        style = MaterialTheme.typography.bodyLarge, modifier = Modifier.uiName("SponsorPrivacyScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SponsorPrivacyScreen")
                    )
                    Button(
                        onClick = onAllow,
                        shapes = ButtonDefaults.shapesFor(ButtonDefaults.LargeContainerHeight),
                        modifier = (Modifier.height(ButtonDefaults.LargeContainerHeight)).uiName("SponsorPrivacyScreen.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "SponsorPrivacyScreen"),
                        contentPadding =
                            ButtonDefaults.contentPaddingFor(ButtonDefaults.LargeContainerHeight),
                    ) {
                        Text(
                            text = stringResource(Res.string.action_allow),
                            style = ButtonDefaults.textStyleFor(ButtonDefaults.LargeContainerHeight), modifier = Modifier.uiName("SponsorPrivacyScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "SponsorPrivacyScreen")
                        )
                    }
                }
            }

            item { Methods( modifier = Modifier.uiName("SponsorPrivacyScreen.Methods.1", "com.maksimowiczm.foodyou.app.ui.sponsor.Methods", uiInspectionSource, "SponsorPrivacyScreen")) }

            item { ContactCard( modifier = Modifier.uiName("SponsorPrivacyScreen.ContactCard.1", "com.maksimowiczm.foodyou.app.ui.sponsor.ContactCard", uiInspectionSource, "SponsorPrivacyScreen")) }
        }
    }
}

@Composable
private fun SponsorScreen(
    uiState: SponsorScreenUiState,
    onBack: () -> Unit,
    onOrderChange: (MessagesOrder) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val scrolledOffset = rememberSaveable { mutableFloatStateOf(0f) }
    val nestedScrollConnection =
        StatusBarProtectionDefaults.scrollConnection { scrolledOffset.value -= it.y }

    Scaffold(
        modifier = (modifier).uiName("SponsorScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "SponsorScreen"),
        topBar = {
            TopBar(
                onBack = onBack,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onRefresh = onRefresh,
                yearMonth = uiState.yearMonth,
                isError = uiState.isError,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)).uiName("SponsorScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "SponsorScreen"),
            state = lazyListState,
            contentPadding = paddingValues.add(bottom = 8.dp),
        ) {
            item {
                if (uiState.isLoading) {
                    LinearWavyProgressIndicator((Modifier.fillMaxWidth()).uiName("SponsorScreen.LinearWavyProgressIndicator.1", "androidx.compose.material3.LinearWavyProgressIndicator", uiInspectionSource, "SponsorScreen"))
                } else {
                    Spacer(Modifier.height(10.dp))
                }
            }

            item { Spacer(Modifier.height(24.dp)) }

            item {
                ThisMonth(
                    uiState = uiState,
                    onProgressBarClick = {
                        coroutineScope.launch { lazyListState.animateScrollToItem(6) }
                    },
                    modifier = (Modifier.padding(horizontal = 16.dp)).uiName("SponsorScreen.ThisMonth.1", "com.maksimowiczm.foodyou.app.ui.sponsor.ThisMonth", uiInspectionSource, "SponsorScreen"),
                )
            }

            item { Spacer(Modifier.height(24.dp)) }

            item { Methods(modifier = (Modifier.padding(horizontal = 16.dp)).uiName("SponsorScreen.Methods.1", "com.maksimowiczm.foodyou.app.ui.sponsor.Methods", uiInspectionSource, "SponsorScreen")) }

            item { Spacer(Modifier.height(24.dp)) }

            item {
                Goals(
                    goals = uiState.goals,
                    remainingForNextGoal = uiState.remainingForNextGoal?.formatClipZeros("%.2f"),
                    modifier = (Modifier.padding(horizontal = 16.dp)).uiName("SponsorScreen.Goals.1", "com.maksimowiczm.foodyou.app.ui.sponsor.Goals", uiInspectionSource, "SponsorScreen"),
                )
            }

            item { Spacer(Modifier.height(24.dp)) }

            sponsorsList(
                messages = uiState.messages,
                order = uiState.messagesOrder,
                onOrderChange = onOrderChange,
                contentPadding = PaddingValues(horizontal = 16.dp),
            )

            item { Spacer(Modifier.height(24.dp)) }

            item { ContactCard(modifier = (Modifier.padding(horizontal = 16.dp)).uiName("SponsorScreen.ContactCard.1", "com.maksimowiczm.foodyou.app.ui.sponsor.ContactCard", uiInspectionSource, "SponsorScreen")) }
        }
    }

    val density = LocalDensity.current
    StatusBarProtection(
        progress = {
            if (lazyListState.canScrollBackward)
                lerp(0f, 1f, scrolledOffset.value / with(density) { 56.dp.toPx() })
            else 0f
        }
    )
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRefresh: () -> Unit,
    yearMonth: YearMonth,
    isError: Boolean,
) {
    val startYearMonth = remember { YearMonth(2025, 6) }
    val currentYearMonth = remember { LocalDate.now().yearMonth }

    TopAppBar(
        title = {},
        navigationIcon = {
            FilledIconButton(
                onClick = onBack,
                shapes = IconButtonDefaults.shapes(),
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ), modifier = Modifier.uiName("TopBar.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "TopBar")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(Res.string.action_go_back), modifier = Modifier.uiName("TopBar.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "TopBar")
                )
            }
        },
        actions = {
            ButtonGroup(
                overflowIndicator = {},
                horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.uiName("TopBar.ButtonGroup.1", "androidx.compose.material3.ButtonGroup", uiInspectionSource, "TopBar")
            ) {
                if (isError) {
                    customItem(
                        buttonGroupContent = {
                            FilledIconButton(
                                onClick = onRefresh,
                                shapes = IconButtonDefaults.shapes(),
                                colors =
                                    IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                    ), modifier = Modifier.uiName("TopBar.FilledIconButton.2", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "TopBar")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = stringResource(Res.string.action_try_again), modifier = Modifier.uiName("TopBar.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "TopBar")
                                )
                            }
                        },
                        menuContent = {},
                    )
                }
                customItem(
                    buttonGroupContent = {
                        FilledIconButton(
                            onClick = onPreviousMonth,
                            shapes = IconButtonDefaults.shapes(),
                            enabled = yearMonth > startYearMonth,
                            colors =
                                IconButtonDefaults.filledIconButtonColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ), modifier = Modifier.uiName("TopBar.FilledIconButton.3", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "TopBar")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                                contentDescription =
                                    stringResource(Res.string.action_previous_month), modifier = Modifier.uiName("TopBar.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "TopBar")
                            )
                        }
                    },
                    menuContent = {},
                )
                customItem(
                    buttonGroupContent = {
                        FilledIconButton(
                            onClick = onNextMonth,
                            shapes = IconButtonDefaults.shapes(),
                            enabled = yearMonth < currentYearMonth,
                            colors =
                                IconButtonDefaults.filledIconButtonColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ), modifier = Modifier.uiName("TopBar.FilledIconButton.4", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "TopBar")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = stringResource(Res.string.action_next_month), modifier = Modifier.uiName("TopBar.Icon.4", "androidx.compose.material3.Icon", uiInspectionSource, "TopBar")
                            )
                        }
                    },
                    menuContent = {},
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ), modifier = Modifier.uiName("TopBar.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "TopBar")
    )
}

@Composable
private fun Methods(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = (modifier).uiName("Methods.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Methods"), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(Res.string.headline_sponsor_methods),
            style = sponsorTypography.title, modifier = Modifier.uiName("Methods.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Methods")
        )

        Column (modifier = Modifier.uiName("Methods.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Methods")) {
            Text(
                text = stringResource(Res.string.sponsor_bank_or_card).uppercase(),
                modifier = (Modifier.padding(start = 16.dp)).uiName("Methods.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Methods"),
                style = MaterialTheme.typography.labelLarge,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("Methods.Column.3", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Methods")) {
                AvailableSponsorMethod.fiat.forEach {
                    it.SponsorCard(onClick = { uriHandler.openUri(it.url) }, modifier = Modifier.uiName("Methods.SponsorCard.1", "com.maksimowiczm.foodyou.app.ui.sponsor.SponsorCard", uiInspectionSource, "Methods"))
                }
            }
        }

        AvailableSponsorMethod.crypto.forEach {
            it.SponsorCard(onClick = { clipboardManager.copy("address", it.address) }, modifier = Modifier.uiName("Methods.SponsorCard.2", "com.maksimowiczm.foodyou.app.ui.sponsor.SponsorCard", uiInspectionSource, "Methods"))
        }
    }
}

@Composable
private fun LinkSponsorMethod.SponsorCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SponsorCard(
        label = name,
        leadingIcon = { Icon((Modifier.width(24.dp)).uiName("SponsorCard.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "SponsorCard")) },
        trailingIcon = { Icon(imageVector = Icons.Outlined.Link, contentDescription = null, modifier = Modifier.uiName("SponsorCard.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "SponsorCard")) },
        onClick = onClick,
        modifier = (modifier).uiName("SponsorCard.SponsorCard.2", "com.maksimowiczm.foodyou.app.ui.sponsor.SponsorCard", uiInspectionSource, "SponsorCard"),
    )
}

@Composable
private fun CryptoSponsorMethod.SponsorCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    var clicked by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(clicked) {
        if (clicked) {
            delay(2000)
            clicked = false
        }
    }

    Column((modifier).uiName("SponsorCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "SponsorCard")) {
        Text(
            text = name.uppercase(),
            modifier = (Modifier.padding(start = 16.dp)).uiName("SponsorCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SponsorCard"),
            style = MaterialTheme.typography.labelLarge,
        )
        SponsorCard(
            label = address,
            leadingIcon = { Icon((Modifier.height(24.dp)).uiName("SponsorCard.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "SponsorCard")) },
            trailingIcon = {
                if (clicked) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.uiName("SponsorCard.Icon.4", "androidx.compose.material3.Icon", uiInspectionSource, "SponsorCard")
                    )
                } else {
                    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.uiName("SponsorCard.Icon.5", "androidx.compose.material3.Icon", uiInspectionSource, "SponsorCard"))
                }
            },
            onClick = {
                clicked = true
                onClick()
            }, modifier = Modifier.uiName("SponsorCard.SponsorCard.4", "com.maksimowiczm.foodyou.app.ui.sponsor.SponsorCard", uiInspectionSource, "SponsorCard")
        )
    }
}

@Composable
private fun SponsorCard(
    label: String,
    leadingIcon: @Composable () -> Unit,
    trailingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Surface(
        onClick = onClick,
        modifier = (modifier.fillMaxWidth().heightIn(min = 56.dp)).uiName("SponsorCard.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "SponsorCard"),
        shape = MaterialTheme.shapes.medium,
        color = color,
        contentColor = contentColor,
    ) {
        Row(
            modifier = (Modifier.padding(16.dp)).uiName("SponsorCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "SponsorCard"),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon()
            Text(
                text = label,
                modifier = (Modifier.weight(1f)).uiName("SponsorCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SponsorCard"),
                style = MaterialTheme.typography.bodyMedium,
            )
            trailingIcon()
        }
    }
}

@Composable
private fun ContactCard(modifier: Modifier = Modifier) {
    val appConfig = LocalAppConfig.current
    val uriHandler = LocalUriHandler.current

    Card(
        onClick = { uriHandler.openUri(appConfig.contactEmailUri) },
        modifier = (modifier).uiName("ContactCard.Card.1", "androidx.compose.material3.Card", uiInspectionSource, "ContactCard"),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
    ) {
        Column(
            modifier = (Modifier.fillMaxWidth().padding(16.dp)).uiName("ContactCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ContactCard"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("ContactCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "ContactCard")
            ) {
                Icon(imageVector = Icons.Outlined.Hail, contentDescription = null, modifier = Modifier.uiName("ContactCard.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ContactCard"))
                Text(
                    text = stringResource(Res.string.headline_contact),
                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("ContactCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ContactCard")
                )
            }

            Text(
                text = stringResource(Res.string.description_sponsor_contact),
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("ContactCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ContactCard")
            )
        }
    }
}

@Composable
private fun ThisMonth(
    uiState: SponsorScreenUiState,
    onProgressBarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormatter = LocalDateFormatter.current

    val title = dateFormatter.formatMonthYear(uiState.yearMonth)

    Column((modifier).uiName("ThisMonth.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ThisMonth")) {
        Text(text = title, style = sponsorTypography.title, modifier = Modifier.uiName("ThisMonth.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ThisMonth"))
        Spacer(Modifier.height(8.dp))
        Text(
            text =
                stringResource(
                    Res.string.headline_this_month_we_raised,
                    "€${uiState.amount.formatClipZeros("%.2f")}",
                ),
            style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("ThisMonth.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ThisMonth")
        )
        ProgressBar(
            uiState = uiState,
            modifier =
                (Modifier.fillMaxWidth()
                    .height(32.dp)
                    .clickable(
                        indication = null,
                        interactionSource = null,
                        onClick = onProgressBarClick,
                    )).uiName("ThisMonth.ProgressBar.1", "com.maksimowiczm.foodyou.app.ui.sponsor.ProgressBar", uiInspectionSource, "ThisMonth"),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.description_sponsorship_goals),
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("ThisMonth.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ThisMonth")
        )
    }
}

@Composable
private fun ProgressBar(uiState: SponsorScreenUiState, modifier: Modifier = Modifier) {
    if (uiState.goals.size != 3) {
        return
    }

    val colorScheme = MaterialTheme.colorScheme

    val amount by animateFloatAsState(targetValue = uiState.amount.toFloat())
    val goalAmount = uiState.goals.last().amount.toDouble()

    Canvas((modifier.clip(MaterialTheme.shapes.medium)).uiName("ProgressBar.Canvas.1", "androidx.compose.foundation.Canvas", uiInspectionSource, "ProgressBar")) {
        val spacer = 2.dp.toPx()

        val firstPart = ((uiState.goals[0].amount / goalAmount) * size.width).toFloat()
        val secondPart = ((uiState.goals[1].amount / goalAmount) * size.width).toFloat()
        val thirdPart = ((uiState.goals[2].amount / goalAmount) * size.width).toFloat()
        val progress = ((amount / goalAmount) * size.width).toFloat()

        drawRoundRect(
            color = colorScheme.surfaceContainerHighest,
            size = Size(width = firstPart, height = size.height),
            cornerRadius = CornerRadius(4.dp.toPx()),
        )
        drawRoundRect(
            color = colorScheme.surfaceContainerHighest,
            topLeft = Offset(x = firstPart + spacer, y = 0f),
            size = Size(width = secondPart - firstPart - spacer, height = size.height),
            cornerRadius = CornerRadius(4.dp.toPx()),
        )
        drawRoundRect(
            color = colorScheme.surfaceContainerHighest,
            topLeft = Offset(x = secondPart + spacer, y = 0f),
            size = Size(width = thirdPart - secondPart - 2 * spacer, height = size.height),
            cornerRadius = CornerRadius(4.dp.toPx()),
        )
        clipRect(right = progress.coerceAtMost(size.width)) {
            drawRoundRect(
                color = colorScheme.tertiary,
                size = Size(width = firstPart, height = size.height),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )
            drawRoundRect(
                color = colorScheme.secondary,
                topLeft = Offset(x = firstPart + spacer, y = 0f),
                size = Size(width = secondPart - firstPart - spacer, height = size.height),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )
            drawRoundRect(
                color = colorScheme.primary,
                topLeft = Offset(x = secondPart + spacer, y = 0f),
                size = Size(width = thirdPart - secondPart - 2 * spacer, height = size.height),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )
        }
    }
}

@Composable
private fun Goals(
    goals: List<GoalUiModel>,
    remainingForNextGoal: String?,
    modifier: Modifier = Modifier,
) {
    Column((modifier).uiName("Goals.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Goals")) {
        Text(
            text = stringResource(Res.string.headline_sponsorship_goals),
            style = sponsorTypography.title, modifier = Modifier.uiName("Goals.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Goals")
        )
        Spacer(Modifier.height(8.dp))

        for (i in goals.indices) {
            val goal = goals[i]
            val fulfilled = goal.fulfilled
            val previousGoal = goals.getOrNull(i - 1)
            val previousFulfilled = previousGoal?.fulfilled
            val nextGoal = goals.getOrNull(i + 1)
            val nextFulfilled = nextGoal?.fulfilled

            val color =
                when (i) {
                    0 -> MaterialTheme.colorScheme.tertiary
                    1 -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.primary
                }

            val shape =
                when (i) {
                    0 -> MaterialShapes.Cookie6Sided
                    1 -> MaterialShapes.Cookie12Sided
                    else -> MaterialShapes.VerySunny
                }.toShape()

            val containerShape =
                when {
                    // First item
                    i == 0 && fulfilled && nextFulfilled == false ->
                        RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp)

                    i == 0 && previousFulfilled == true && !fulfilled ->
                        RoundedCornerShape(16.dp, 16.dp, 4.dp, 4.dp)

                    i == 0 -> RoundedCornerShape(16.dp, 16.dp, 4.dp, 4.dp)

                    // Last item
                    i == goals.size - 1 && fulfilled && nextFulfilled == false ->
                        RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp)

                    i == goals.size - 1 && previousFulfilled == true && !fulfilled ->
                        RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp)

                    i == goals.size - 1 -> RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp)

                    // Middle items
                    fulfilled && nextFulfilled == false ->
                        RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp)

                    previousFulfilled == true && !fulfilled ->
                        RoundedCornerShape(16.dp, 16.dp, 4.dp, 4.dp)

                    else -> RoundedCornerShape(4.dp)
                }

            Goal(
                goal = goal,
                shape = shape,
                shapeColor = color,
                animate = fulfilled,
                modifier = (Modifier.clip(containerShape)).uiName("Goals.Goal.1", "com.maksimowiczm.foodyou.app.ui.sponsor.Goal", uiInspectionSource, "Goals"),
            )
            if (fulfilled && nextFulfilled == false) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text =
                        stringResource(
                            Res.string.description_x_away_from_next_goal,
                            "€$remainingForNextGoal",
                        ),
                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("Goals.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Goals")
                )
                Spacer(Modifier.height(2.dp))
            } else if (i < goals.size - 1) {
                Spacer(Modifier.height(2.dp))
            }
        }
    }
}

@Composable
private fun Goal(
    goal: GoalUiModel,
    shape: Shape,
    shapeColor: Color,
    animate: Boolean,
    modifier: Modifier = Modifier,
) {
    val color by
        animateColorAsState(
            if (goal.fulfilled) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceContainer
        )
    val contentColor by
        animateColorAsState(
            if (goal.fulfilled) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurface
        )

    val rotation by
        rememberInfiniteTransition()
            .animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec =
                    InfiniteRepeatableSpec(
                        animation = tween(easing = LinearEasing, durationMillis = 2 * 60 * 1000),
                        repeatMode = RepeatMode.Restart,
                    ),
            )

    Surface(modifier = (modifier).uiName("Goal.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "Goal"), color = color) {
        Row(
            modifier = (Modifier.fillMaxWidth().padding(16.dp)).uiName("Goal.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "Goal"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.uiName("Goal.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "Goal")) {
                Canvas(
                    (Modifier.size(150.dp)
                        .padding(16.dp)
                        .graphicsLayer { rotationZ = if (animate) rotation else 0f }
                        .clip(shape)).uiName("Goal.Canvas.1", "androidx.compose.foundation.Canvas", uiInspectionSource, "Goal")
                ) {
                    drawRect(shapeColor)
                }
                Text(
                    text = "€${goal.amount}",
                    style = sponsorTypography.goal,
                    color = contentColorFor(shapeColor), modifier = Modifier.uiName("Goal.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Goal")
                )
            }
            Column (modifier = Modifier.uiName("Goal.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Goal")) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor, modifier = Modifier.uiName("Goal.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Goal")
                )
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor, modifier = Modifier.uiName("Goal.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "Goal")
                )
            }
        }
    }
}

private fun LazyListScope.sponsorsList(
    messages: List<SponsorMessageUiModel>,
    order: MessagesOrder,
    onOrderChange: (MessagesOrder) -> Unit,
    contentPadding: PaddingValues,
) {
    item {
        Row(modifier = (Modifier.fillMaxWidth().padding(contentPadding)).uiName("sponsorsList.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "sponsorsList")) {
            Text(stringResource(Res.string.headline_sponsors), style = sponsorTypography.title, modifier = Modifier.uiName("sponsorsList.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "sponsorsList"))
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = { onOrderChange(order.toggle()) },
                modifier = (Modifier.height(ButtonDefaults.ExtraSmallContainerHeight)).uiName("sponsorsList.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "sponsorsList"),
                contentPadding =
                    ButtonDefaults.contentPaddingFor(ButtonDefaults.ExtraSmallContainerHeight),
            ) {
                Icon(
                    imageVector = Icons.Outlined.SwapVert,
                    contentDescription = stringResource(Res.string.action_reorder),
                    modifier = (Modifier.size(ButtonDefaults.ExtraSmallIconSize)).uiName("sponsorsList.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "sponsorsList"),
                )
                Spacer(Modifier.width(ButtonDefaults.ExtraSmallIconSpacing))
                Text(
                    text =
                        when (order) {
                            MessagesOrder.NewestFirst -> stringResource(Res.string.headline_latest)
                            MessagesOrder.TopFirst ->
                                stringResource(Res.string.headline_top_sponsorships)
                        },
                    style = ButtonDefaults.textStyleFor(ButtonDefaults.ExtraSmallContainerHeight), modifier = Modifier.uiName("sponsorsList.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "sponsorsList")
                )
            }
        }
    }
    item { Spacer(Modifier.height(8.dp)) }
    if (messages.isEmpty()) {
        item {
            Column(
                modifier = (Modifier.fillMaxWidth().padding(contentPadding)).uiName("sponsorsList.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "sponsorsList"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoneyOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = (Modifier.size(48.dp)).uiName("sponsorsList.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "sponsorsList"),
                )
                Text(
                    text = stringResource(Res.string.description_no_sponsors_this_month),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("sponsorsList.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "sponsorsList")
                )
            }
        }
    } else {
        itemsIndexed(items = messages, key = { _, message -> message.id }) { i, message ->
            val shape =
                when (i) {
                    0 -> RoundedCornerShape(16.dp, 16.dp, 4.dp, 4.dp)
                    messages.size - 1 -> RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp)
                    else -> RoundedCornerShape(4.dp)
                }

            SponsorMessageCard(
                message = message,
                shape = shape,
                modifier = (Modifier.fillMaxWidth().animateItem().padding(contentPadding)).uiName("sponsorsList.SponsorMessageCard.1", "com.maksimowiczm.foodyou.app.ui.sponsor.SponsorMessageCard", uiInspectionSource, "sponsorsList"),
            )

            if (i < messages.size - 1) {
                Spacer(Modifier.height(2.dp))
            }
        }
    }
}

@Composable
private fun SponsorMessageCard(
    message: SponsorMessageUiModel,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val sponsor = message.sponsor ?: stringResource(Res.string.headline_anonymous)
    val amount =
        if (message.currency == "EUR") {
            "€${message.amount}"
        } else {
            "${message.amount} ${message.currency} (€${message.inEuro})"
        }

    Surface(
        modifier = (modifier).uiName("SponsorMessageCard.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "SponsorMessageCard"),
        shape = shape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column((Modifier.fillMaxWidth().padding(16.dp)).uiName("SponsorMessageCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "SponsorMessageCard")) {
            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("SponsorMessageCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "SponsorMessageCard"),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                message.icon()?.let { icon ->
                    icon(Modifier.sizeIn(maxWidth = 24.dp, maxHeight = 24.dp))
                }
                Text(text = sponsor, style = MaterialTheme.typography.titleLargeEmphasized, modifier = Modifier.uiName("SponsorMessageCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SponsorMessageCard"))
                Spacer(Modifier.weight(1f))
                val color = LocalContentColor.current
                BasicText(
                    text = amount,
                    maxLines = 1,
                    autoSize =
                        TextAutoSize.StepBased(
                            minFontSize = MaterialTheme.typography.bodySmall.fontSize,
                            maxFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        ),
                    style = LocalTextStyle.current,
                    color = { color }, modifier = Modifier.uiName("SponsorMessageCard.BasicText.1", "androidx.compose.foundation.text.BasicText", uiInspectionSource, "SponsorMessageCard")
                )
            }

            if (!message.message.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(text = message.message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("SponsorMessageCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SponsorMessageCard"))
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.sponsor",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/sponsor/SponsorScreen.kt",
)
