package com.maksimowiczm.foodyou.app.ui.food.search


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NorthWest
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FoodSearchView(
    appState: FoodSearchAppState,
    uiState: FoodSearchUiState,
    onFill: (String) -> Unit,
    onSearch: (String?) -> Unit,
    onSource: (FoodFilter.Source) -> Unit,
    inputField: @Composable () -> Unit,
) {
    val filters = uiState.sources.filterValues { state -> state.shouldShowFilter }

    UiSourceScope(uiInspectionSource.element("FoodSearchView.ExpandedFullScreenSearchBar.1", "androidx.compose.material3.ExpandedFullScreenSearchBar", "FoodSearchView")) { ExpandedFullScreenSearchBar(state = appState.searchBarState, inputField = inputField, modifier = Modifier.uiName("FoodSearchView.ExpandedFullScreenSearchBar.1.root", "androidx.compose.material3.ExpandedFullScreenSearchBar", uiInspectionSource, "FoodSearchView")) {
        LazyColumn (modifier = Modifier.uiName("FoodSearchView.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "FoodSearchView")) {
            items(uiState.recentSearches.take(3)) {
                FoodSearchItem(
                    search = it,
                    onFill = { onFill(it) },
                    modifier = (Modifier.clickable { onSearch(it) }).uiName("FoodSearchView.FoodSearchItem.1", "com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchItem", uiInspectionSource, "FoodSearchView"),
                )
            }

            item {
                if (uiState.recentSearches.isNotEmpty()) {
                    HorizontalDivider( modifier = Modifier.uiName("FoodSearchView.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "FoodSearchView"))
                }

                Column(
                    modifier = (Modifier.padding(vertical = 8.dp)).uiName("FoodSearchView.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "FoodSearchView"),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.headline_filters),
                        modifier = (Modifier.padding(horizontal = 16.dp)).uiName("FoodSearchView.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchView"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp), modifier = Modifier.uiName("FoodSearchView.LazyRow.1", "androidx.compose.foundation.lazy.LazyRow", uiInspectionSource, "FoodSearchView")
                    ) {
                        items(filters.toList()) { (source, _) ->
                            DatabaseFilterIconButton(
                                selected = uiState.filter.source == source,
                                onClick = { onSource(source) },
                                logo = { source.Icon( modifier = Modifier.uiName("FoodSearchView.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "FoodSearchView")) },
                                label = {
                                    Text(
                                        text = source.stringResource(),
                                        textAlign = TextAlign.Center, modifier = Modifier.uiName("FoodSearchView.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchView")
                                    )
                                }, modifier = Modifier.uiName("FoodSearchView.DatabaseFilterIconButton.1", "com.maksimowiczm.foodyou.app.ui.food.search.DatabaseFilterIconButton", uiInspectionSource, "FoodSearchView")
                            )
                        }
                    }
                }

                HorizontalDivider( modifier = Modifier.uiName("FoodSearchView.HorizontalDivider.2", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "FoodSearchView"))
            }

            items(uiState.recentSearches.drop(3)) {
                FoodSearchItem(
                    search = it,
                    onFill = { onFill(it) },
                    modifier = (Modifier.clickable { onSearch(it) }).uiName("FoodSearchView.FoodSearchItem.2", "com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchItem", uiInspectionSource, "FoodSearchView"),
                )
            }
        }
    } }
}

@Composable
private fun FoodSearchItem(search: String, onFill: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        modifier = (modifier).uiName("FoodSearchItem.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "FoodSearchItem"),
        headlineContent = { Text(search, modifier = Modifier.uiName("FoodSearchItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchItem")) },
        leadingContent = { Icon(imageVector = Icons.Outlined.History, contentDescription = null, modifier = Modifier.uiName("FoodSearchItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "FoodSearchItem")) },
        trailingContent = {
            IconButton(onFill, modifier = Modifier.uiName("FoodSearchItem.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "FoodSearchItem")) {
                Icon(
                    imageVector = Icons.Outlined.NorthWest,
                    contentDescription = stringResource(Res.string.action_insert_suggested_search), modifier = Modifier.uiName("FoodSearchItem.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "FoodSearchItem")
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun DatabaseFilterIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    logo: @Composable () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val containerColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
        )
    val contentColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
        )

    val labelColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
        )

    Column(
        modifier =
            (modifier
                .clickable(
                    onClick = onClick,
                    indication = null,
                    interactionSource = interactionSource,
                )
                .widthIn(min = 64.dp, max = 96.dp)).uiName("DatabaseFilterIconButton.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DatabaseFilterIconButton"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            onClick = onClick,
            modifier = (Modifier.size(48.dp)).uiName("DatabaseFilterIconButton.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "DatabaseFilterIconButton"),
            shape = CircleShape,
            color = containerColor,
            contentColor = contentColor,
            interactionSource = interactionSource,
        ) {
            Box(modifier = (Modifier.padding(12.dp)).uiName("DatabaseFilterIconButton.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "DatabaseFilterIconButton"), contentAlignment = Alignment.Center) { logo() }
        }
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.labelMedium,
            LocalContentColor provides labelColor,
        ) {
            label()
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.search",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/search/FoodSearchView.kt",
)
