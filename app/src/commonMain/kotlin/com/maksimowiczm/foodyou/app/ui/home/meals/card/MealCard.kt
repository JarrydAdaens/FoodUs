package com.maksimowiczm.foodyou.app.ui.home.meals.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.theme.LocalNutrientsPalette
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalNutrientsOrder
import com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplateId
import com.maksimowiczm.foodyou.settings.domain.entity.NutrientsOrder
import foodyou.app.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MealCard(
    meal: MealModel,
    onAddFood: () -> Unit,
    onQuickAdd: () -> Unit,
    onAiScan: () -> Unit,
    onFastText: () -> Unit,
    onEditEntry: (MealEntryModel) -> Unit,
    onDeleteEntry: (MealEntryModel) -> Unit,
    onLongClick: () -> Unit,
    templates: List<MealTemplateModel>,
    onSaveTemplate: (name: String) -> Unit,
    onApplyTemplate: (MealTemplateId) -> Unit,
    onDeleteTemplate: (MealTemplateId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nutrientsPalette = LocalNutrientsPalette.current
    val nutrientsOrder = LocalNutrientsOrder.current
    val dateFormatter = LocalDateFormatter.current
    val energyFormatter = LocalEnergyFormatter.current
    val enDash = stringResource(Res.string.en_dash)
    val allDayString = stringResource(Res.string.headline_all_day)

    val timeString =
        remember(dateFormatter, meal, enDash, allDayString) {
            if (meal.isAllDay) {
                allDayString
            } else {
                buildString {
                    append(dateFormatter.formatTime(meal.from))
                    append(" $enDash ")
                    append(dateFormatter.formatTime(meal.to))
                }
            }
        }

    var showSaveTemplateDialog by rememberSaveable { mutableStateOf(false) }
    if (showSaveTemplateDialog) {
        SaveTemplateDialog(
            onDismissRequest = { showSaveTemplateDialog = false },
            onConfirm = { name ->
                onSaveTemplate(name)
                showSaveTemplateDialog = false
            },
        )
    }

    var showApplyTemplateSheet by rememberSaveable { mutableStateOf(false) }
    if (showApplyTemplateSheet) {
        ApplyTemplateSheet(
            templates = templates,
            onApply = { id ->
                onApplyTemplate(id)
                showApplyTemplateSheet = false
            },
            onDelete = onDeleteTemplate,
            onDismissRequest = { showApplyTemplateSheet = false },
        )
    }

    FoodYouHomeCard(modifier = modifier, onClick = onAddFood, onLongClick = onLongClick) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.headlineMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                MealTemplateMenu(
                    canSave = meal.foods.isNotEmpty(),
                    canApply = templates.isNotEmpty(),
                    onSaveClick = { showSaveTemplateDialog = true },
                    onApplyClick = { showApplyTemplateSheet = true },
                )
            }

            Spacer(Modifier.height(16.dp))

            FoodContainer(
                foods = meal.foods,
                onEditEntry = onEditEntry,
                onDeleteEntry = onDeleteEntry,
                modifier =
                    Modifier.fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec()),
            )

            AnimatedVisibility(
                visible = meal.foods.isNotEmpty(),
                enter =
                    expandVertically(
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                    ),
                exit =
                    shrinkVertically(
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                    ),
            ) {
                Spacer(Modifier.height(16.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ValueColumn(
                    label = energyFormatter.suffix(),
                    value = energyFormatter.formatEnergy(meal.energy, withSuffix = false),
                    suffix = null,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                nutrientsOrder.forEach { field ->
                    when (field) {
                        NutrientsOrder.Proteins ->
                            ValueColumn(
                                label = stringResource(Res.string.nutriment_proteins_short),
                                value = meal.proteins.formatClipZeros("%.1f"),
                                suffix = stringResource(Res.string.unit_gram_short),
                                color = nutrientsPalette.proteinsOnSurfaceContainer,
                            )

                        NutrientsOrder.Carbohydrates ->
                            ValueColumn(
                                label = stringResource(Res.string.nutriment_carbohydrates_short),
                                value = meal.carbohydrates.formatClipZeros("%.1f"),
                                suffix = stringResource(Res.string.unit_gram_short),
                                color = nutrientsPalette.carbohydratesOnSurfaceContainer,
                            )

                        NutrientsOrder.Fats ->
                            ValueColumn(
                                label = stringResource(Res.string.nutriment_fats_short),
                                value = meal.fats.formatClipZeros("%.1f"),
                                suffix = stringResource(Res.string.unit_gram_short),
                                color = nutrientsPalette.fatsOnSurfaceContainer,
                            )

                        NutrientsOrder.Other,
                        NutrientsOrder.Vitamins,
                        NutrientsOrder.Minerals -> Unit
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Four self-describing add paths for this meal: search, quick add, AI scan, fast text.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconButton(
                    onClick = onAddFood,
                    shapes =
                        IconButtonDefaults.shapes(
                            MaterialTheme.shapes.medium,
                            MaterialTheme.shapes.extraSmall,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(Res.string.action_search),
                    )
                }
                FilledTonalIconButton(
                    onClick = onQuickAdd,
                    shapes =
                        IconButtonDefaults.shapes(
                            MaterialTheme.shapes.medium,
                            MaterialTheme.shapes.extraSmall,
                        ),
                ) {
                    Icon(imageVector = Icons.Outlined.Bolt, contentDescription = null)
                }
                FilledTonalIconButton(
                    onClick = onAiScan,
                    shapes =
                        IconButtonDefaults.shapes(
                            MaterialTheme.shapes.medium,
                            MaterialTheme.shapes.extraSmall,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = stringResource(Res.string.headline_ai_scanning),
                    )
                }
                FilledTonalIconButton(
                    onClick = onFastText,
                    shapes =
                        IconButtonDefaults.shapes(
                            MaterialTheme.shapes.medium,
                            MaterialTheme.shapes.extraSmall,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = stringResource(Res.string.headline_fast_text),
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodContainer(
    foods: List<MealEntryModel>,
    onEditEntry: (MealEntryModel) -> Unit,
    onDeleteEntry: (MealEntryModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        foods.forEachIndexed { i, entry ->
            val key =
                remember(entry) {
                    when (entry) {
                        is FoodMealEntryModel -> entry.id.toString()
                        is ManualMealEntryModel -> entry.id.toString()
                    }
                }

            key(key) {
                val topStart = animateTopCornerRadius(i)
                val topEnd = animateTopCornerRadius(i)
                val bottomStart = foods.animateBottomCornerRadius(i)
                val bottomEnd = foods.animateBottomCornerRadius(i)
                val shape = RoundedCornerShape(topStart, topEnd, bottomStart, bottomEnd)

                FoodContainerItem(
                    entry = entry,
                    onEditEntry = onEditEntry,
                    onDeleteEntry = onDeleteEntry,
                    shape = shape,
                )
            }
        }
    }
}

@Composable
private fun animateTopCornerRadius(index: Int, defaultRadius: Dp = 12.dp): Dp =
    animateDpAsState(
            targetValue =
                when (index) {
                    0 -> defaultRadius
                    else -> 0.dp
                },
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        )
        .value
        .coerceAtLeast(0.dp)

@Composable
private fun <T> List<T>.animateBottomCornerRadius(index: Int, defaultRadius: Dp = 12.dp): Dp =
    animateDpAsState(
            targetValue =
                when (index) {
                    lastIndex -> defaultRadius
                    else -> 0.dp
                },
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        )
        .value
        .coerceAtLeast(0.dp)

@Composable
private fun FoodContainerItem(
    entry: MealEntryModel,
    onEditEntry: (MealEntryModel) -> Unit,
    onDeleteEntry: (MealEntryModel) -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (showBottomSheet) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
            BottomSheetContent(
                entry = entry,
                onEdit = {
                    coroutineScope.launch {
                        onEditEntry(entry)
                        sheetState.hide()
                        showBottomSheet = false
                    }
                },
                onDelete = {
                    coroutineScope.launch {
                        sheetState.hide()
                        onDeleteEntry(entry)
                        showBottomSheet = false
                    }
                },
            )
        }
    }

    MealFoodListItem(
        entry = entry,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = shape,
        modifier = modifier.clickable { showBottomSheet = true },
    )
}

@Composable
private fun ValueColumn(
    label: String,
    value: String,
    suffix: String?,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides color,
            LocalTextStyle provides MaterialTheme.typography.labelMedium,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)

            Text(
                text =
                    if (value == "0") {
                        stringResource(Res.string.em_dash)
                    } else {
                        value + (suffix?.let { " $suffix" } ?: "")
                    }
            )
        }
    }
}

@Composable
private fun BottomSheetContent(
    entry: MealEntryModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteDialog(
            onDismissRequest = { showDeleteDialog = false },
            onDeleteEntry = {
                onDelete()
                showDeleteDialog = false
            },
        )
    }

    Column(modifier = modifier) {
        MealFoodListItem(
            entry = entry,
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RectangleShape,
        )
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        ListItem(
            headlineContent = { Text(stringResource(Res.string.action_edit_entry)) },
            modifier = Modifier.clickable { onEdit() },
            leadingContent = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        ListItem(
            headlineContent = { Text(stringResource(Res.string.action_delete_entry)) },
            modifier = Modifier.clickable { showDeleteDialog = true },
            leadingContent = {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            },
            colors =
                ListItemDefaults.colors(
                    headlineColor = MaterialTheme.colorScheme.error,
                    leadingIconColor = MaterialTheme.colorScheme.error,
                    containerColor = Color.Transparent,
                ),
        )
    }
}

@Composable
private fun DeleteDialog(onDismissRequest: () -> Unit, onDeleteEntry: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onDeleteEntry,
                colors =
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(stringResource(Res.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
        title = { Text(stringResource(Res.string.action_delete_entry)) },
        text = { Text(stringResource(Res.string.description_delete_product_entry)) },
    )
}

/**
 * Overflow menu hosting the reusable-template actions (Milestone 2, Story 13). Hidden entirely when
 * there is nothing to do — no entries to save and no templates to apply — so the card never shows a
 * dead control.
 */
@Composable
private fun MealTemplateMenu(
    canSave: Boolean,
    canApply: Boolean,
    onSaveClick: () -> Unit,
    onApplyClick: () -> Unit,
) {
    if (!canSave && !canApply) return

    var expanded by rememberSaveable { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(Res.string.action_meal_template_menu),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_save_as_template)) },
                onClick = {
                    expanded = false
                    onSaveClick()
                },
                enabled = canSave,
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = null)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_apply_template)) },
                onClick = {
                    expanded = false
                    onApplyClick()
                },
                enabled = canApply,
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = null)
                },
            )
        }
    }
}

@Composable
private fun SaveTemplateDialog(onDismissRequest: () -> Unit, onConfirm: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text(stringResource(Res.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
        icon = { Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = null) },
        title = { Text(stringResource(Res.string.action_save_as_template)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.label_template_name)) },
                singleLine = true,
            )
        },
    )
}

@Composable
private fun ApplyTemplateSheet(
    templates: List<MealTemplateModel>,
    onApply: (MealTemplateId) -> Unit,
    onDelete: (MealTemplateId) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        Column {
            Text(
                text = stringResource(Res.string.action_apply_template),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            templates.forEach { template ->
                ListItem(
                    headlineContent = { Text(template.name) },
                    modifier = Modifier.clickable { onApply(template.id) },
                    supportingContent = {
                        Text(
                            stringResource(
                                Res.string.neutral_template_item_count,
                                template.itemCount,
                            )
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { onDelete(template.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.action_delete),
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
