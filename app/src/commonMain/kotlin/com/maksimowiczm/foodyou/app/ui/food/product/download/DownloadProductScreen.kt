package com.maksimowiczm.foodyou.app.ui.food.product.download


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.node.Ref
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.common.compose.component.unorderedList
import com.maksimowiczm.foodyou.food.domain.usecase.DownloadProductError
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DownloadProductScreen(
    isDownloading: Boolean,
    error: DownloadProductError?,
    textFieldState: TextFieldState,
    onBack: () -> Unit,
    onDownload: () -> Unit,
    onPaste: () -> Unit,
    onOpenFoodFacts: () -> Unit,
    onUsda: () -> Unit,
    onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = (modifier).uiName("DownloadProductScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "DownloadProductScreen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.action_download_product), modifier = Modifier.uiName("DownloadProductScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("DownloadProductScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "DownloadProductScreen")) },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("DownloadProductScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "DownloadProductScreen")
            )
        },
        floatingActionButton = {
            Column(
                modifier =
                    (Modifier.animateFloatingActionButton(
                        visible = !isDownloading,
                        alignment = Alignment.BottomEnd,
                    )).uiName("DownloadProductScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DownloadProductScreen"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
            ) {
                FloatingActionButton(
                    onClick = {
                        if (!isDownloading) {
                            onPaste()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.uiName("DownloadProductScreen.FloatingActionButton.1", "androidx.compose.material3.FloatingActionButton", uiInspectionSource, "DownloadProductScreen")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = stringResource(Res.string.action_paste_url), modifier = Modifier.uiName("DownloadProductScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DownloadProductScreen")
                    )
                }

                LargeExtendedFloatingActionButton(
                    onClick = {
                        if (!isDownloading) {
                            onDownload()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = (Modifier.size(FloatingActionButtonDefaults.LargeIconSize)).uiName("DownloadProductScreen.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "DownloadProductScreen"),
                        )
                    },
                    text = { Text(stringResource(Res.string.action_download), modifier = Modifier.uiName("DownloadProductScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductScreen")) }, modifier = Modifier.uiName("DownloadProductScreen.LargeExtendedFloatingActionButton.1", "androidx.compose.material3.LargeExtendedFloatingActionButton", uiInspectionSource, "DownloadProductScreen")
                )
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("DownloadProductScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "DownloadProductScreen"),
            contentPadding = paddingValues,
        ) {
            stickyHeader {
                AnimatedContent(
                    targetState = isDownloading,
                    modifier = (Modifier.fillMaxWidth().heightIn(min = 12.dp)).uiName("DownloadProductScreen.AnimatedContent.1", "androidx.compose.animation.AnimatedContent", uiInspectionSource, "DownloadProductScreen"),
                    transitionSpec = {
                        fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically()
                    },
                ) {
                    if (it) {
                        LinearWavyProgressIndicator( modifier = Modifier.uiName("DownloadProductScreen.LinearWavyProgressIndicator.1", "androidx.compose.material3.LinearWavyProgressIndicator", uiInspectionSource, "DownloadProductScreen"))
                    } else {
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                Text(
                    text = stringResource(Res.string.description_download_product),
                    modifier = (Modifier.padding(horizontal = 16.dp)).uiName("DownloadProductScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductScreen"),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                OutlinedTextField(
                    state = textFieldState,
                    modifier = (Modifier.padding(horizontal = 16.dp).fillMaxWidth()).uiName("DownloadProductScreen.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "DownloadProductScreen"),
                    enabled = !isDownloading,
                    trailingIcon = {
                        if (textFieldState.text.isNotEmpty()) {
                            IconButton(onClick = textFieldState::clearText, modifier = Modifier.uiName("DownloadProductScreen.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "DownloadProductScreen")) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(Res.string.action_clear), modifier = Modifier.uiName("DownloadProductScreen.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "DownloadProductScreen")
                                )
                            }
                        }
                    },
                    supportingText = {
                        Text(stringResource(Res.string.description_download_product_hint), modifier = Modifier.uiName("DownloadProductScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductScreen"))
                    },
                    placeholder = { Text(stringResource(Res.string.product_link), modifier = Modifier.uiName("DownloadProductScreen.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "DownloadProductScreen")) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    onKeyboardAction = { onDownload() },
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                val ref = remember { Ref<DownloadProductError>() }

                ref.value = error ?: ref.value

                AnimatedVisibility(
                    visible = error != null,
                    modifier = (Modifier.fillMaxWidth().padding(horizontal = 16.dp)).uiName("DownloadProductScreen.AnimatedVisibility.1", "androidx.compose.animation.AnimatedVisibility", uiInspectionSource, "DownloadProductScreen"),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    ref.value?.let {
                        Column (modifier = Modifier.uiName("DownloadProductScreen.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DownloadProductScreen")) {
                            DownloadErrorCard(
                                it,
                                onUpdateUsdaApiKey,
                                onUpdateOpenFoodFactsCredentials, modifier = Modifier.uiName("DownloadProductScreen.DownloadErrorCard.1", "com.maksimowiczm.foodyou.app.ui.food.product.download.DownloadErrorCard", uiInspectionSource, "DownloadProductScreen")
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }

            item { SupportedUrls(modifier = (Modifier.padding(horizontal = 16.dp)).uiName("DownloadProductScreen.SupportedUrls.1", "com.maksimowiczm.foodyou.app.ui.food.product.download.SupportedUrls", uiInspectionSource, "DownloadProductScreen")) }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                ActionChips(
                    isDownloading = isDownloading,
                    onPaste = onPaste,
                    onOpenFoodFacts = onOpenFoodFacts,
                    onUsda = onUsda,
                    modifier = (Modifier.padding(horizontal = 16.dp)).uiName("DownloadProductScreen.ActionChips.1", "com.maksimowiczm.foodyou.app.ui.food.product.download.ActionChips", uiInspectionSource, "DownloadProductScreen"),
                )
            }
        }
    }
}

@Composable
private fun SupportedUrls(modifier: Modifier = Modifier) {
    Column(modifier = (modifier).uiName("SupportedUrls.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "SupportedUrls"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.headline_supported_URLs),
            style = MaterialTheme.typography.labelMedium, modifier = Modifier.uiName("SupportedUrls.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SupportedUrls")
        )

        Text(
            text =
                unorderedList(
                    "Open Food Facts (world.openfoodfacts.org)",
                    "USDA FoodData Central (fdc.nal.usda.gov)",
                ),
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.uiName("SupportedUrls.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SupportedUrls")
        )
    }
}

@Composable
private fun ActionChips(
    isDownloading: Boolean,
    onPaste: () -> Unit,
    onOpenFoodFacts: () -> Unit,
    onUsda: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = (modifier).uiName("ActionChips.FlowRow.1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "ActionChips"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AssistChip(
            onClick = onPaste,
            label = { Text(stringResource(Res.string.action_paste_url), modifier = Modifier.uiName("ActionChips.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ActionChips")) },
            enabled = !isDownloading,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = null,
                    modifier = (Modifier.size(AssistChipDefaults.IconSize)).uiName("ActionChips.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ActionChips"),
                )
            }, modifier = Modifier.uiName("ActionChips.AssistChip.1", "androidx.compose.material3.AssistChip", uiInspectionSource, "ActionChips")
        )
        AssistChip(
            onClick = onOpenFoodFacts,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = (Modifier.size(AssistChipDefaults.IconSize)).uiName("ActionChips.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "ActionChips"),
                )
            },
            label = { Text(stringResource(Res.string.action_browse_open_food_facts), modifier = Modifier.uiName("ActionChips.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ActionChips")) }, modifier = Modifier.uiName("ActionChips.AssistChip.2", "androidx.compose.material3.AssistChip", uiInspectionSource, "ActionChips")
        )
        AssistChip(
            onClick = onUsda,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = (Modifier.size(AssistChipDefaults.IconSize)).uiName("ActionChips.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "ActionChips"),
                )
            },
            label = { Text(stringResource(Res.string.action_browse_usda), modifier = Modifier.uiName("ActionChips.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ActionChips")) }, modifier = Modifier.uiName("ActionChips.AssistChip.3", "androidx.compose.material3.AssistChip", uiInspectionSource, "ActionChips")
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.product.download",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/product/download/DownloadProductScreen.kt",
)
