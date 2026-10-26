package com.maksimowiczm.foodyou.app.ui.changelog

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.changelog.domain.Changelog
import com.maksimowiczm.foodyou.changelog.domain.Version
import com.maksimowiczm.foodyou.common.compose.component.unorderedList
import com.maksimowiczm.foodyou.common.compose.utility.LocalClipboardManager
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import foodyou.app.generated.resources.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChangelogModalBottomSheet(onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    val changelogViewModel: ChangelogViewModel = koinViewModel()
    val changelog = changelogViewModel.changelog.collectAsStateWithLifecycle().value

    if (changelog == null) {
        return
    }

    val sheetState = rememberModalBottomSheetState()

    UiSourceScope(uiInspectionSource.element("ChangelogModalBottomSheet.ModalBottomSheet.1", "androidx.compose.material3.ModalBottomSheet", "ChangelogModalBottomSheet")) { ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = (modifier).uiName("ChangelogModalBottomSheet.ModalBottomSheet.1.root", "androidx.compose.material3.ModalBottomSheet", uiInspectionSource, "ChangelogModalBottomSheet"),
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) },
    ) {
        SheetContent(changelog = changelog, modifier = Modifier.uiName("ChangelogModalBottomSheet.SheetContent.1", "com.maksimowiczm.foodyou.app.ui.changelog.SheetContent", uiInspectionSource, "ChangelogModalBottomSheet"))
    } }
}

@Composable
private fun SheetContent(changelog: Changelog, modifier: Modifier = Modifier) {
    val paddingValues =
        WindowInsets.systemBars
            .only(WindowInsetsSides.Bottom)
            .add(WindowInsets(bottom = 8.dp))
            .asPaddingValues()

    val versions =
        remember(changelog) {
            val currentVersion = changelog.currentVersion

            if (currentVersion?.isPreview == true) {
                changelog.versions
            } else {
                changelog.versions.filterNot { it.isPreview }
            }
        }

    LazyColumn(
        modifier = (modifier).uiName("SheetContent.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "SheetContent"),
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                modifier = (Modifier.fillMaxWidth()).uiName("SheetContent.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SheetContent"),
                text = stringResource(Res.string.headline_whats_new),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        items(items = versions) { version ->
            ChangelogItem(
                version = version,
                isCurrentVersion = version == changelog.currentVersion,
                modifier = (Modifier.padding(horizontal = 16.dp)).uiName("SheetContent.ChangelogItem.1", "com.maksimowiczm.foodyou.app.ui.changelog.ChangelogItem", uiInspectionSource, "SheetContent"),
            )
        }
    }
}

@Composable
private fun ChangelogItem(
    version: Version,
    isCurrentVersion: Boolean,
    modifier: Modifier = Modifier,
) {
    val dateFormatter = LocalDateFormatter.current
    val clipboardManager = LocalClipboardManager.current

    val changelogString = stringResource(Res.string.headline_changelog)
    val changelogText = version.stringResource()
    val coroutineScope = rememberCoroutineScope()
    var copied by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Card(
        modifier = (modifier).uiName("ChangelogItem.Card.1", "androidx.compose.material3.Card", uiInspectionSource, "ChangelogItem"),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = (Modifier.padding(16.dp)).uiName("ChangelogItem.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ChangelogItem"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("ChangelogItem.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "ChangelogItem"),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column (modifier = Modifier.uiName("ChangelogItem.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ChangelogItem")) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("ChangelogItem.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "ChangelogItem")
                    ) {
                        Text(text = version.version, style = MaterialTheme.typography.titleLarge, modifier = Modifier.uiName("ChangelogItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem"))
                        if (isCurrentVersion) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.uiName("ChangelogItem.Badge.1", "androidx.compose.material3.Badge", uiInspectionSource, "ChangelogItem")
                            ) {
                                Text(stringResource(Res.string.headline_installed), modifier = Modifier.uiName("ChangelogItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem"))
                            }
                        }
                    }
                    Text(
                        text = dateFormatter.formatDateShort(version.date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("ChangelogItem.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem")
                    )
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            clipboardManager.copy(label = changelogString, text = changelogText)
                            copied = true
                        }
                    }, modifier = Modifier.uiName("ChangelogItem.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "ChangelogItem")
                ) {
                    Crossfade(targetState = copied, modifier = Modifier.uiName("ChangelogItem.Crossfade.1", "androidx.compose.animation.Crossfade", uiInspectionSource, "ChangelogItem")) {
                        when (it) {
                            false ->
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = stringResource(Res.string.action_copy), modifier = Modifier.uiName("ChangelogItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ChangelogItem")
                                )

                            true ->
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.uiName("ChangelogItem.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "ChangelogItem")
                                )
                        }
                    }
                }
            }

            if (version.newFeatures.isNotEmpty()) {
                Column (modifier = Modifier.uiName("ChangelogItem.Column.3", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ChangelogItem")) {
                    Text(
                        text = stringResource(Res.string.changelog_new_features),
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("ChangelogItem.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem")
                    )
                    Text(
                        text = unorderedList(version.newFeatures),
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("ChangelogItem.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem")
                    )
                }
            }

            if (version.changes.isNotEmpty()) {
                Column (modifier = Modifier.uiName("ChangelogItem.Column.4", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ChangelogItem")) {
                    Text(
                        text = stringResource(Res.string.changelog_changes),
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("ChangelogItem.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem")
                    )
                    Text(
                        text = unorderedList(version.changes),
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("ChangelogItem.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem")
                    )
                }
            }

            if (version.bugFixes.isNotEmpty()) {
                Column (modifier = Modifier.uiName("ChangelogItem.Column.5", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ChangelogItem")) {
                    Text(
                        text = stringResource(Res.string.changelog_bug_fixes),
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("ChangelogItem.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem")
                    )
                    Text(
                        text = unorderedList(version.bugFixes),
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("ChangelogItem.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem")
                    )
                }
            }

            if (version.translations.isNotEmpty()) {
                Column (modifier = Modifier.uiName("ChangelogItem.Column.6", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ChangelogItem")) {
                    Text(
                        text = stringResource(Res.string.changelog_translations),
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("ChangelogItem.Text.10", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem")
                    )
                    Text(
                        text = unorderedList(version.translations),
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("ChangelogItem.Text.11", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem")
                    )
                }
            }

            version.notes?.let { notes ->
                Column (modifier = Modifier.uiName("ChangelogItem.Column.7", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ChangelogItem")) {
                    Text(
                        text = stringResource(Res.string.changelog_notes),
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("ChangelogItem.Text.12", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem")
                    )
                    Text(text = notes, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("ChangelogItem.Text.13", "androidx.compose.material3.Text", uiInspectionSource, "ChangelogItem"))
                }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.changelog",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/changelog/ChangelogModalBottomSheet.kt",
)
