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
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealId
import com.maksimowiczm.foodyou.settings.domain.entity.NutrientsOrder
import foodyou.app.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DiarySlotCard(
    diarySlot: DiarySlotModel,
    onAddFood: () -> Unit,
    onQuickAdd: () -> Unit,
    onAiScan: () -> Unit,
    onFastText: () -> Unit,
    onEditEntry: (DiarySlotEntryModel) -> Unit,
    onDeleteEntry: (DiarySlotEntryModel) -> Unit,
    onLongClick: () -> Unit,
    meals: List<MealModel>,
    onSaveMeal: (name: String, onResult: (Boolean) -> Unit) -> Unit,
    onApplyMeal: (MealId, onResult: (Boolean) -> Unit) -> Unit,
    onDeleteMeal: (MealId, onResult: (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nutrientsPalette = LocalNutrientsPalette.current
    val nutrientsOrder = LocalNutrientsOrder.current
    val dateFormatter = LocalDateFormatter.current
    val energyFormatter = LocalEnergyFormatter.current
    val enDash = stringResource(Res.string.en_dash)
    val allDayString = stringResource(Res.string.headline_all_day)

    val timeString =
        remember(dateFormatter, diarySlot, enDash, allDayString) {
            if (diarySlot.isAllDay) {
                allDayString
            } else {
                buildString {
                    append(dateFormatter.formatTime(diarySlot.from))
                    append(" $enDash ")
                    append(dateFormatter.formatTime(diarySlot.to))
                }
            }
        }

    var showSaveMealDialog by rememberSaveable { mutableStateOf(false) }
    if (showSaveMealDialog) {
        SaveMealDialog(
            onDismissRequest = { showSaveMealDialog = false },
            onConfirm = onSaveMeal,
        )
    }

    var showApplyMealSheet by rememberSaveable { mutableStateOf(false) }
    if (showApplyMealSheet) {
        ApplyMealSheet(
            meals = meals,
            onApply = onApplyMeal,
            onDelete = onDeleteMeal,
            onDismissRequest = { showApplyMealSheet = false },
        )
    }

    FoodYouHomeCard(modifier = modifier, onClick = onAddFood, onLongClick = onLongClick) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = diarySlot.name,
                        style = MaterialTheme.typography.headlineMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                MealMenu(
                    canSave = diarySlot.foods.isNotEmpty(),
                    canApply = meals.isNotEmpty(),
                    onSaveClick = { showSaveMealDialog = true },
                    onApplyClick = { showApplyMealSheet = true },
                )
            }

            Spacer(Modifier.height(16.dp))

            FoodContainer(
                foods = diarySlot.foods,
                onEditEntry = onEditEntry,
                onDeleteEntry = onDeleteEntry,
                modifier =
                    Modifier.fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec()),
            )

            AnimatedVisibility(
                visible = diarySlot.foods.isNotEmpty(),
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
                    value = energyFormatter.formatEnergy(diarySlot.energy, withSuffix = false),
                    suffix = null,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                nutrientsOrder.forEach { field ->
                    when (field) {
                        NutrientsOrder.Proteins ->
                            ValueColumn(
                                label = stringResource(Res.string.nutriment_proteins_short),
                                value = diarySlot.proteins.formatClipZeros("%.1f"),
                                suffix = stringResource(Res.string.unit_gram_short),
                                color = nutrientsPalette.proteinsOnSurfaceContainer,
                            )

                        NutrientsOrder.Carbohydrates ->
                            ValueColumn(
                                label = stringResource(Res.string.nutriment_carbohydrates_short),
                                value = diarySlot.carbohydrates.formatClipZeros("%.1f"),
                                suffix = stringResource(Res.string.unit_gram_short),
                                color = nutrientsPalette.carbohydratesOnSurfaceContainer,
                            )

                        NutrientsOrder.Fats ->
                            ValueColumn(
                                label = stringResource(Res.string.nutriment_fats_short),
                                value = diarySlot.fats.formatClipZeros("%.1f"),
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

            // Four self-describing add paths for this diarySlot: search, quick add, AI scan, fast text.
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
    foods: List<DiarySlotEntryModel>,
    onEditEntry: (DiarySlotEntryModel) -> Unit,
    onDeleteEntry: (DiarySlotEntryModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        foods.forEachIndexed { i, entry ->
            val key =
                remember(entry) {
                    when (entry) {
                        is FoodDiarySlotEntryModel -> entry.id.toString()
                        is ManualDiarySlotEntryModel -> entry.id.toString()
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
    entry: DiarySlotEntryModel,
    onEditEntry: (DiarySlotEntryModel) -> Unit,
    onDeleteEntry: (DiarySlotEntryModel) -> Unit,
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

    DiarySlotFoodListItem(
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
    entry: DiarySlotEntryModel,
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
        DiarySlotFoodListItem(
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
 * Overflow menu hosting the reusable-Meal actions (Milestone 2, Story 13). Hidden entirely when
 * there is nothing to do — no entries to save and no Meals to apply — so the card never shows a
 * dead control.
 */
@Composable
private fun MealMenu(
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
                contentDescription = stringResource(Res.string.action_saved_meal_menu),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_save_as_meal)) },
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
                text = { Text(stringResource(Res.string.action_load_meal)) },
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
private fun SaveMealDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String, (Boolean) -> Unit) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismissRequest() },
        confirmButton = {
            TextButton(onClick = {
                if (!busy) {
                    busy = true
                    onConfirm(name) { success ->
                        busy = false
                        if (success) onDismissRequest() else failed = true
                    }
                }
            }, enabled = name.isNotBlank() && !busy) { Text(stringResource(Res.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, enabled = !busy) { Text(stringResource(Res.string.action_cancel)) }
        },
        icon = { Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = null) },
        title = { Text(stringResource(Res.string.action_save_as_meal)) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.label_meal_name)) }, singleLine = true)
                if (failed) Text(stringResource(Res.string.meals_save_failed), color = MaterialTheme.colorScheme.error)
            }
        },
    )
}

@Composable
private fun ApplyMealSheet(
    meals: List<MealModel>,
    onApply: (MealId, (Boolean) -> Unit) -> Unit,
    onDelete: (MealId, (Boolean) -> Unit) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var busy by remember { mutableStateOf(false) }
    var failedDelete by remember { mutableStateOf(false) }
    var failedLoad by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = { if (!busy) onDismissRequest() }, sheetState = sheetState) {
        Column {
            Text(stringResource(Res.string.action_load_meal), style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            if (failedLoad || failedDelete) Text(
                stringResource(if (failedDelete) Res.string.meals_delete_failed else Res.string.meals_load_failed),
                color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp),
            )
            meals.forEach { meal ->
                ListItem(
                    headlineContent = { Text(meal.name) },
                    modifier = Modifier.clickable(enabled = !busy) {
                        busy = true
                        onApply(meal.id) { success ->
                            busy = false
                            if (success) onDismissRequest() else { failedLoad = true; failedDelete = false }
                        }
                    },
                    supportingContent = { Text(stringResource(Res.string.neutral_meal_item_count, meal.itemCount)) },
                    trailingContent = {
                        IconButton(enabled = !busy, onClick = {
                            busy = true
                            onDelete(meal.id) { success ->
                                busy = false
                                if (!success) { failedDelete = true; failedLoad = false }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.meals_delete))
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
