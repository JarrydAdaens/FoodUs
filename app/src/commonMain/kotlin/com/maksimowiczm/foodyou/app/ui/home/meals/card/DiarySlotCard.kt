package com.maksimowiczm.foodyou.app.ui.home.meals.card

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.MoveToInbox
import androidx.compose.material.icons.outlined.FileCopy
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
    onBulk: (DiaryBulkIntent) -> Unit = {},
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

    FoodYouHomeCard(modifier = (modifier).uiName("DiarySlotCard.FoodYouHomeCard.1", "com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard", uiInspectionSource, "DiarySlotCard"), onClick = onAddFood, onLongClick = onLongClick) {
        Column(modifier = (Modifier.fillMaxWidth().padding(16.dp)).uiName("app.ui.home.meals.card.DiarySlotCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DiarySlotCard")) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.uiName("app.ui.home.meals.card.DiarySlotCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DiarySlotCard")) {
                Column(modifier = (Modifier.weight(1f)).uiName("app.ui.home.meals.card.DiarySlotCard.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DiarySlotCard")) {
                    Text(
                        text = diarySlot.name,
                        style = MaterialTheme.typography.headlineMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.uiName("app.ui.home.meals.card.DiarySlotCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotCard")
                    )
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("app.ui.home.meals.card.DiarySlotCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotCard")
                    )
                }

                MealMenu(
                    onBulk = onBulk,
                    canSave = diarySlot.foods.isNotEmpty(),
                    onSaveClick = { showSaveMealDialog = true },
                )
            }

            Spacer(Modifier.height(16.dp))

            FoodContainer(
                foods = diarySlot.foods,
                onEditEntry = onEditEntry,
                onDeleteEntry = onDeleteEntry,
                modifier =
                    (Modifier.fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec())).uiName("DiarySlotCard.FoodContainer.1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.FoodContainer", uiInspectionSource, "DiarySlotCard"),
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
                    ), modifier = Modifier.uiName("app.ui.home.meals.card.DiarySlotCard.AnimatedVisibility.1", "androidx.compose.animation.AnimatedVisibility", uiInspectionSource, "DiarySlotCard")
            ) {
                Spacer(Modifier.height(16.dp))
            }

            com.maksimowiczm.foodyou.app.ui.common.component.NutritionColumns(
                modifier = (Modifier.fillMaxWidth().padding(horizontal = 16.dp)).uiName("DiarySlotCard.NutritionColumns.phone1", "com.maksimowiczm.foodyou.app.ui.common.component.NutritionColumns", uiInspectionSource, "DiarySlotCard"),
                calories = { ValueColumn(energyFormatter.suffix(), energyFormatter.formatEnergy(diarySlot.energy, withSuffix = false), null, MaterialTheme.colorScheme.onSurface, modifier = Modifier.uiName("DiarySlotCard.ValueColumn.phone1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.ValueColumn", uiInspectionSource, "DiarySlotCard")) },
                proteins = { ValueColumn(stringResource(Res.string.nutriment_proteins_short), diarySlot.proteins.formatClipZeros("%.1f"), stringResource(Res.string.unit_gram_short), nutrientsPalette.proteinsOnSurfaceContainer, modifier = Modifier.uiName("DiarySlotCard.ValueColumn.phone2", "com.maksimowiczm.foodyou.app.ui.home.meals.card.ValueColumn", uiInspectionSource, "DiarySlotCard")) },
                carbohydrates = { ValueColumn(stringResource(Res.string.nutriment_carbohydrates_short), diarySlot.carbohydrates.formatClipZeros("%.1f"), stringResource(Res.string.unit_gram_short), nutrientsPalette.carbohydratesOnSurfaceContainer, modifier = Modifier.uiName("DiarySlotCard.ValueColumn.phone3", "com.maksimowiczm.foodyou.app.ui.home.meals.card.ValueColumn", uiInspectionSource, "DiarySlotCard")) },
                fats = { ValueColumn(stringResource(Res.string.nutriment_fats_short), diarySlot.fats.formatClipZeros("%.1f"), stringResource(Res.string.unit_gram_short), nutrientsPalette.fatsOnSurfaceContainer, modifier = Modifier.uiName("DiarySlotCard.ValueColumn.phone4", "com.maksimowiczm.foodyou.app.ui.home.meals.card.ValueColumn", uiInspectionSource, "DiarySlotCard")) },
            )

            Spacer(Modifier.height(16.dp))

            // Five direct actions for this slot; loading a saved Meal uses the existing apply flow.
            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("app.ui.home.meals.card.DiarySlotCard.Row.3", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DiarySlotCard"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconButton(
                    onClick = onAddFood,
                    shapes =
                        IconButtonDefaults.shapes(
                            MaterialTheme.shapes.medium,
                            MaterialTheme.shapes.extraSmall,
                        ), modifier = Modifier.uiName("app.ui.home.meals.card.DiarySlotCard.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "DiarySlotCard")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(Res.string.action_search), modifier = Modifier.uiName("app.ui.home.meals.card.DiarySlotCard.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotCard")
                    )
                }
                FilledTonalIconButton(
                    onClick = onQuickAdd,
                    shapes =
                        IconButtonDefaults.shapes(
                            MaterialTheme.shapes.medium,
                            MaterialTheme.shapes.extraSmall,
                        ), modifier = Modifier.uiName("DiarySlotCard.FilledTonalIconButton.1", "androidx.compose.material3.FilledTonalIconButton", uiInspectionSource, "DiarySlotCard")
                ) {
                    Icon(imageVector = Icons.Outlined.Bolt, contentDescription = null, modifier = Modifier.uiName("app.ui.home.meals.card.DiarySlotCard.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotCard"))
                }
                FilledTonalIconButton(
                    onClick = onAiScan,
                    shapes =
                        IconButtonDefaults.shapes(
                            MaterialTheme.shapes.medium,
                            MaterialTheme.shapes.extraSmall,
                        ), modifier = Modifier.uiName("DiarySlotCard.FilledTonalIconButton.2", "androidx.compose.material3.FilledTonalIconButton", uiInspectionSource, "DiarySlotCard")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = stringResource(Res.string.headline_ai_scanning), modifier = Modifier.uiName("app.ui.home.meals.card.DiarySlotCard.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotCard")
                    )
                }
                FilledTonalIconButton(
                    onClick = onFastText,
                    shapes =
                        IconButtonDefaults.shapes(
                            MaterialTheme.shapes.medium,
                            MaterialTheme.shapes.extraSmall,
                        ), modifier = Modifier.uiName("DiarySlotCard.FilledTonalIconButton.3", "androidx.compose.material3.FilledTonalIconButton", uiInspectionSource, "DiarySlotCard")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = stringResource(Res.string.headline_fast_text), modifier = Modifier.uiName("DiarySlotCard.Icon.4", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotCard")
                    )
                }
                FilledTonalIconButton(onClick = { showApplyMealSheet = true }, enabled = meals.isNotEmpty(), modifier = Modifier.uiName("DiarySlotCard.FilledTonalIconButton.phone4", "androidx.compose.material3.FilledTonalIconButton", uiInspectionSource, "DiarySlotCard")) {
                    Icon(Icons.Outlined.ContentCopy, stringResource(Res.string.action_load_meal), modifier = Modifier.uiName("DiarySlotCard.Icon.phone5", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotCard"))
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
    Column(modifier = (modifier).uiName("FoodContainer.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "FoodContainer"), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        foods.forEachIndexed { i, entry ->
            val key =
                remember(entry) {
                    when (entry) {
                        is FoodDiarySlotEntryModel -> "food:${entry.id.value}"
                        is ManualDiarySlotEntryModel -> "manual:${entry.id.value}"
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
                    shape = shape, modifier = Modifier.uiName("FoodContainer.FoodContainerItem.1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.FoodContainerItem", uiInspectionSource, "FoodContainer")
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

        UiSourceScope(uiInspectionSource.element("FoodContainerItem.ModalBottomSheet.1", "androidx.compose.material3.ModalBottomSheet", "FoodContainerItem")) { ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState, modifier = Modifier.uiName("FoodContainerItem.ModalBottomSheet.1.root", "androidx.compose.material3.ModalBottomSheet", uiInspectionSource, "FoodContainerItem")) {
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
                }, modifier = Modifier.uiName("FoodContainerItem.BottomSheetContent.1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.BottomSheetContent", uiInspectionSource, "FoodContainerItem")
            )
        } }
    }

    DiarySlotFoodListItem(
        entry = entry,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = shape,
        modifier = (modifier.clickable { showBottomSheet = true }).uiName("FoodContainerItem.DiarySlotFoodListItem.1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.DiarySlotFoodListItem", uiInspectionSource, "FoodContainerItem"),
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
        modifier = (modifier).uiName("ValueColumn.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ValueColumn"),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides color,
            LocalTextStyle provides MaterialTheme.typography.labelMedium,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.uiName("ValueColumn.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ValueColumn"))

            Text(
                text =
                    if (value == "0") {
                        stringResource(Res.string.em_dash)
                    } else {
                        value + (suffix?.let { " $suffix" } ?: "")
                    }, modifier = Modifier.uiName("ValueColumn.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ValueColumn")
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

    Column(modifier = (modifier).uiName("BottomSheetContent.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "BottomSheetContent")) {
        DiarySlotFoodListItem(
            entry = entry,
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RectangleShape, modifier = Modifier.uiName("BottomSheetContent.DiarySlotFoodListItem.1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.DiarySlotFoodListItem", uiInspectionSource, "BottomSheetContent")
        )
        HorizontalDivider((Modifier.padding(horizontal = 16.dp)).uiName("BottomSheetContent.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "BottomSheetContent"))
        ListItem(
            headlineContent = { Text(stringResource(Res.string.action_edit_entry), modifier = Modifier.uiName("BottomSheetContent.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "BottomSheetContent")) },
            modifier = (Modifier.clickable { onEdit() }).uiName("BottomSheetContent.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "BottomSheetContent"),
            leadingContent = { Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.uiName("BottomSheetContent.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "BottomSheetContent")) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        ListItem(
            headlineContent = { Text(stringResource(Res.string.action_delete_entry), modifier = Modifier.uiName("BottomSheetContent.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "BottomSheetContent")) },
            modifier = (Modifier.clickable { showDeleteDialog = true }).uiName("BottomSheetContent.ListItem.2", "androidx.compose.material3.ListItem", uiInspectionSource, "BottomSheetContent"),
            leadingContent = {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.uiName("BottomSheetContent.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "BottomSheetContent"))
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
    UiSourceScope(uiInspectionSource.element("app.ui.home.meals.card.DeleteDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "DeleteDialog")) { AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onDeleteEntry,
                colors =
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error), modifier = Modifier.uiName("app.ui.home.meals.card.DeleteDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "DeleteDialog")
            ) {
                Text(stringResource(Res.string.action_delete), modifier = Modifier.uiName("app.ui.home.meals.card.DeleteDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DeleteDialog"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, modifier = Modifier.uiName("app.ui.home.meals.card.DeleteDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "DeleteDialog")) {
                Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("app.ui.home.meals.card.DeleteDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DeleteDialog"))
            }
        },
        title = { Text(stringResource(Res.string.action_delete_entry), modifier = Modifier.uiName("app.ui.home.meals.card.DeleteDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "DeleteDialog")) },
        text = { Text(stringResource(Res.string.description_delete_product_entry), modifier = Modifier.uiName("app.ui.home.meals.card.DeleteDialog.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "DeleteDialog")) }, modifier = Modifier.uiName("app.ui.home.meals.card.DeleteDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "DeleteDialog")
    ) }
}

/**
 * Diary-slot actions, including reusable Meals and bulk entry operations.
 * Empty slots keep the menu available for Copy From.
 */
@Composable
private fun MealMenu(
    canSave: Boolean,
    onSaveClick: () -> Unit,
    onBulk: (DiaryBulkIntent) -> Unit,
) {

    var expanded by rememberSaveable { mutableStateOf(false) }

    Box (modifier = Modifier.uiName("MealMenu.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "MealMenu")) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.uiName("MealMenu.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "MealMenu")) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(Res.string.action_saved_meal_menu), modifier = Modifier.uiName("MealMenu.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "MealMenu")
            )
        }
        UiSourceScope(uiInspectionSource.element("MealMenu.DropdownMenu.1", "androidx.compose.material3.DropdownMenu", "MealMenu")) { DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.uiName("MealMenu.DropdownMenu.1.root", "androidx.compose.material3.DropdownMenu", uiInspectionSource, "MealMenu")) {
            DiaryBulkIntent.entries.forEach { intent ->
                DropdownMenuItem(
                    text = { Text(stringResource(intent.labelResource()), modifier = Modifier.uiName("MealMenu.BulkText.1", "androidx.compose.material3.Text", uiInspectionSource, "MealMenu")) },
                    onClick = { expanded = false; onBulk(intent) },
                    enabled = canSave || intent == DiaryBulkIntent.CopyFrom,
                    leadingIcon = {
                        Icon(when (intent) {
                            DiaryBulkIntent.DeleteAll -> Icons.Default.Delete
                            DiaryBulkIntent.MoveTo -> Icons.Outlined.MoveToInbox
                            DiaryBulkIntent.CopyTo -> Icons.Outlined.ContentCopy
                            DiaryBulkIntent.CopyFrom -> Icons.Outlined.FileCopy
                        }, contentDescription = null,
                            modifier = Modifier.uiName("MealMenu.Icon.bulk", "androidx.compose.material3.Icon", uiInspectionSource, "MealMenu"))
                    },
                    modifier = Modifier.uiName("MealMenu.BulkItem.1", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "MealMenu"),
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_save_as_meal), modifier = Modifier.uiName("MealMenu.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "MealMenu")) },
                onClick = {
                    expanded = false
                    onSaveClick()
                },
                enabled = canSave,
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = null, modifier = Modifier.uiName("MealMenu.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "MealMenu"))
                }, modifier = Modifier.uiName("MealMenu.DropdownMenuItem.1", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "MealMenu")
            )

        } }
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
    UiSourceScope(uiInspectionSource.element("SaveMealDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "SaveMealDialog")) { AlertDialog(
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
            }, enabled = name.isNotBlank() && !busy, modifier = Modifier.uiName("SaveMealDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "SaveMealDialog")) { Text(stringResource(Res.string.action_save), modifier = Modifier.uiName("SaveMealDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SaveMealDialog")) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, enabled = !busy, modifier = Modifier.uiName("SaveMealDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "SaveMealDialog")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("SaveMealDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SaveMealDialog")) }
        },
        icon = { Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = null, modifier = Modifier.uiName("SaveMealDialog.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "SaveMealDialog")) },
        title = { Text(stringResource(Res.string.action_save_as_meal), modifier = Modifier.uiName("SaveMealDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "SaveMealDialog")) },
        text = {
            Column (modifier = Modifier.uiName("SaveMealDialog.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "SaveMealDialog")) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.label_meal_name), modifier = Modifier.uiName("SaveMealDialog.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "SaveMealDialog")) }, singleLine = true, modifier = Modifier.uiName("SaveMealDialog.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "SaveMealDialog"))
                if (failed) Text(stringResource(Res.string.meals_save_failed), color = MaterialTheme.colorScheme.error, modifier = Modifier.uiName("SaveMealDialog.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "SaveMealDialog"))
            }
        }, modifier = Modifier.uiName("SaveMealDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "SaveMealDialog")
    ) }
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
    UiSourceScope(uiInspectionSource.element("ApplyMealSheet.ModalBottomSheet.1", "androidx.compose.material3.ModalBottomSheet", "ApplyMealSheet")) { ModalBottomSheet(onDismissRequest = { if (!busy) onDismissRequest() }, sheetState = sheetState, modifier = Modifier.uiName("ApplyMealSheet.ModalBottomSheet.1.root", "androidx.compose.material3.ModalBottomSheet", uiInspectionSource, "ApplyMealSheet")) {
        Column (modifier = Modifier.uiName("ApplyMealSheet.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ApplyMealSheet")) {
            Text(stringResource(Res.string.action_load_meal), style = MaterialTheme.typography.titleLarge,
                modifier = (Modifier.padding(horizontal = 16.dp, vertical = 8.dp)).uiName("ApplyMealSheet.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ApplyMealSheet"))
            if (failedLoad || failedDelete) Text(
                stringResource(if (failedDelete) Res.string.meals_delete_failed else Res.string.meals_load_failed),
                color = MaterialTheme.colorScheme.error, modifier = (Modifier.padding(16.dp)).uiName("ApplyMealSheet.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ApplyMealSheet"),
            )
            meals.forEach { meal ->
                ListItem(
                    headlineContent = { Text(meal.name, modifier = Modifier.uiName("ApplyMealSheet.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ApplyMealSheet")) },
                    modifier = (Modifier.clickable(enabled = !busy) {
                        busy = true
                        onApply(meal.id) { success ->
                            busy = false
                            if (success) onDismissRequest() else { failedLoad = true; failedDelete = false }
                        }
                    }).uiName("ApplyMealSheet.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "ApplyMealSheet"),
                    supportingContent = { Text(stringResource(Res.string.neutral_meal_item_count, meal.itemCount), modifier = Modifier.uiName("ApplyMealSheet.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "ApplyMealSheet")) },
                    trailingContent = {
                        IconButton(enabled = !busy, onClick = {
                            busy = true
                            onDelete(meal.id) { success ->
                                busy = false
                                if (!success) { failedDelete = true; failedLoad = false }
                            }
                        }, modifier = Modifier.uiName("ApplyMealSheet.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "ApplyMealSheet")) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.meals_delete), modifier = Modifier.uiName("ApplyMealSheet.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ApplyMealSheet"))
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    } }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.meals.card",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/meals/card/DiarySlotCard.kt",
)
