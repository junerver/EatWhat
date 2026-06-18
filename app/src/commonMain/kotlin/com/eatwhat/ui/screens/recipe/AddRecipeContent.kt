package com.eatwhat.ui.screens.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.eatwhat.domain.model.Difficulty
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.domain.model.Unit as IngredientUnit
import com.eatwhat.ui.components.AppToolbar
import com.eatwhat.ui.components.StyledTextField
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.MeatRed
import com.eatwhat.ui.theme.OtherPurple
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftBlue
import com.eatwhat.ui.theme.SoftGreen
import com.eatwhat.ui.theme.SoupBlue
import com.eatwhat.ui.theme.StapleOrange
import com.eatwhat.ui.theme.StepCardBackground
import com.eatwhat.ui.theme.VegGreen
import com.eatwhat.ui.theme.WarmYellow
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.palette.components.button.ButtonColors
import xyz.junerver.compose.palette.components.button.ButtonSize
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.container.PContainer
import xyz.junerver.compose.palette.components.scaffold.PScaffold
import xyz.junerver.compose.palette.components.scaffold.ScaffoldDefaults
import xyz.junerver.compose.palette.components.select.PSelect
import xyz.junerver.compose.palette.components.select.SelectDefaults
import xyz.junerver.compose.palette.components.select.SelectOption
import xyz.junerver.compose.palette.components.tag.PEditableTagGroup
import xyz.junerver.compose.palette.components.tag.TagDefaults
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.text.PText
import xyz.junerver.compose.palette.core.spec.ComponentSize
import xyz.junerver.compose.palette.core.spec.ComponentStatus

@Composable
fun AddRecipeContent(
  isEditMode: Boolean,
  name: String,
  type: RecipeType,
  icon: String,
  imageBase64: String?,
  difficulty: Difficulty,
  estimatedTime: String,
  ingredients: List<IngredientInput>,
  steps: List<StepInput>,
  tags: List<String>,
  isSaving: Boolean,
  draggedStepIndex: Int,
  draggedStepOffset: Float,
  onNameChange: (String) -> Unit,
  onTypeChange: (RecipeType) -> Unit,
  onIconChange: (String) -> Unit,
  onImageChange: (String?) -> Unit,
  onDifficultyChange: (Difficulty) -> Unit,
  onEstimatedTimeChange: (String) -> Unit,
  onIngredientsChange: (List<IngredientInput>) -> Unit,
  onStepsChange: (List<StepInput>) -> Unit,
  onTagsChange: (List<String>) -> Unit,
  onDraggedStepIndexChange: (Int) -> Unit,
  onDraggedStepOffsetChange: (Float) -> Unit,
  onNavigateUp: () -> Unit,
  onAIAnalysisClick: () -> Unit,
  onSave: () -> Unit,
  recipeIconPicker: @Composable (
    selectedEmoji: String,
    selectedImageBase64: String?,
    recipeType: String,
    onEmojiSelected: (String) -> Unit,
    onImageSelected: (String) -> Unit,
    onImageCleared: () -> Unit,
    modifier: Modifier
  ) -> Unit
) {
  PScaffold(
    topBar = {
      AppToolbar(
        title = if (isEditMode) "编辑菜谱" else "创建新菜谱",
        onNavigateUp = onNavigateUp,
        actions = {
          IconButton(onClick = onAIAnalysisClick) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = "AI 分析",
              tint = PrimaryOrange,
              modifier = Modifier.size(24.dp)
            )
          }

          PButton(
            text = if (isEditMode) "保存" else "创建",
            modifier = Modifier.padding(end = 8.dp),
            size = ButtonSize.SMALL,
            disabled = false,
            loading = isSaving,
            colors = ButtonColors(
              containerColor = PrimaryOrange,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            leadingIcon = {
              Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
            },
            onClick = onSave
          )
        }
      )
    },
    colors = ScaffoldDefaults.colors(
      containerColor = MaterialTheme.colorScheme.background
    )
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      item {
        SectionCard(
          title = "基本信息",
          icon = Icons.Outlined.Restaurant,
          iconBackgroundColor = PrimaryOrange.copy(alpha = 0.1f),
          iconTint = PrimaryOrange
        ) {
          StyledTextField(
            value = name,
            onValueChange = onNameChange,
            label = "菜名",
            placeholder = "给你的美食起个名字吧",
            leadingIcon = {
              PText("🍳", fontSize = 20.sp)
            }
          )

          Spacer(modifier = Modifier.height(16.dp))

          PText(
            "菜品类型",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            RecipeType.entries.forEach { recipeType ->
              RecipeTypeChip(
                type = recipeType,
                isSelected = type == recipeType,
                onClick = { onTypeChange(recipeType) },
                modifier = Modifier.weight(1f)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          PText(
            "标签",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
          )
          Spacer(modifier = Modifier.height(8.dp))
          PEditableTagGroup(
            tags = tags,
            onTagsChange = onTagsChange,
            placeholder = "标签名",
            size = TagSize.Medium,
            tagColors = { TagDefaults.pastelColors(it) }
          )

          Spacer(modifier = Modifier.height(16.dp))

          recipeIconPicker(
            icon,
            imageBase64,
            type.name,
            onIconChange,
            { onImageChange(it) },
            { onImageChange(null) },
            Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(16.dp))

          PText(
            "难度等级",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Difficulty.entries.forEach { diff ->
              DifficultyChip(
                difficulty = diff,
                isSelected = difficulty == diff,
                onClick = { onDifficultyChange(diff) },
                modifier = Modifier.weight(1f)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          StyledTextField(
            value = estimatedTime,
            onValueChange = onEstimatedTimeChange,
            label = "预计时间",
            placeholder = "30",
            leadingIcon = {
              Icon(
                Icons.Outlined.Timer,
                contentDescription = null,
                tint = PrimaryOrange,
                modifier = Modifier.size(20.dp)
              )
            },
            trailingIcon = {
              PText(
                "分钟",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
              )
            },
            keyboardType = KeyboardType.Number
          )
        }
      }

      item {
        SectionCard(
          title = "食材清单",
          icon = Icons.Outlined.ShoppingCart,
          iconBackgroundColor = SoftGreen.copy(alpha = 0.1f),
          iconTint = SoftGreen,
          action = {
            AddButton(
              onClick = { onIngredientsChange(ingredients + IngredientInput()) },
              color = SoftGreen
            )
          }
        ) {
          ingredients.forEachIndexed { index, ingredient ->
            AnimatedVisibility(
              visible = true,
              enter = fadeIn() + expandVertically(),
              exit = fadeOut() + shrinkVertically()
            ) {
              IngredientInputCard(
                index = index,
                ingredient = ingredient,
                onIngredientChange = { newIngredient ->
                  onIngredientsChange(ingredients.toMutableList().apply {
                    this[index] = newIngredient
                  })
                },
                onDelete = {
                  if (ingredients.size > 1) {
                    onIngredientsChange(ingredients.filterIndexed { i, _ -> i != index })
                  }
                },
                canDelete = ingredients.size > 1
              )
            }
            if (index < ingredients.lastIndex) {
              Spacer(modifier = Modifier.height(12.dp))
            }
          }
        }
      }

      item {
        SectionCard(
          title = "烹饪步骤",
          icon = Icons.AutoMirrored.Outlined.MenuBook,
          iconBackgroundColor = SoftBlue.copy(alpha = 0.1f),
          iconTint = SoftBlue,
          action = {
            AddButton(
              onClick = { onStepsChange(steps + StepInput()) },
              color = SoftBlue
            )
          }
        ) {
          val density = LocalDensity.current

          steps.forEachIndexed { index, step ->
            val isDragging = draggedStepIndex == index
            val currentOffset = if (isDragging) draggedStepOffset else 0f

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, currentOffset.toInt()) }
                .zIndex(if (isDragging) 1f else 0f),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                      brush = Brush.linearGradient(
                        colors = listOf(SoftBlue, SoftBlue.copy(alpha = 0.7f))
                      )
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  PText(
                    "${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }

                if (index < steps.lastIndex) {
                  StepConnectorWithInsert(
                    onInsertStep = {
                      val newList = steps.toMutableList()
                      newList.add(index + 1, StepInput())
                      onStepsChange(newList)
                    }
                  )
                }
              }

              StepContentCard(
                stepNumber = index + 1,
                step = step,
                onStepChange = { newStep ->
                  onStepsChange(steps.toMutableList().apply {
                    this[index] = newStep
                  })
                },
                onDelete = {
                  if (steps.size > 1) {
                    onStepsChange(steps.filterIndexed { i, _ -> i != index })
                  }
                },
                canDelete = steps.size > 1,
                onDragStart = {
                  onDraggedStepIndexChange(index)
                  onDraggedStepOffsetChange(0f)
                },
                onDrag = { delta ->
                  val nextOffset = draggedStepOffset + delta
                  val stepHeight = with(density) { 120.dp.toPx() }
                  val swapThreshold = stepHeight / 2

                  if (nextOffset > swapThreshold && draggedStepIndex < steps.lastIndex) {
                    val newList = steps.toMutableList()
                    val temp = newList[draggedStepIndex]
                    newList[draggedStepIndex] = newList[draggedStepIndex + 1]
                    newList[draggedStepIndex + 1] = temp
                    onStepsChange(newList)
                    onDraggedStepIndexChange(draggedStepIndex + 1)
                    onDraggedStepOffsetChange(nextOffset - stepHeight)
                  } else if (nextOffset < -swapThreshold && draggedStepIndex > 0) {
                    val newList = steps.toMutableList()
                    val temp = newList[draggedStepIndex]
                    newList[draggedStepIndex] = newList[draggedStepIndex - 1]
                    newList[draggedStepIndex - 1] = temp
                    onStepsChange(newList)
                    onDraggedStepIndexChange(draggedStepIndex - 1)
                    onDraggedStepOffsetChange(nextOffset + stepHeight)
                  } else {
                    onDraggedStepOffsetChange(nextOffset)
                  }
                },
                onDragEnd = {
                  onDraggedStepIndexChange(-1)
                  onDraggedStepOffsetChange(0f)
                },
                modifier = Modifier.weight(1f)
              )
            }

            if (index < steps.lastIndex) {
              Spacer(modifier = Modifier.height(12.dp))
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}

@Composable
private fun SectionCard(
  title: String,
  icon: ImageVector,
  iconBackgroundColor: Color,
  iconTint: Color,
  action: (@Composable () -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit
) {
  PCard(
    modifier = Modifier.fillMaxWidth(),
    variant = CardVariant.Elevated,
    colors = CardColors(
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.onSurface
    )
  ) {
    Column(modifier = Modifier.padding(4.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              icon,
              contentDescription = null,
              tint = iconTint,
              modifier = Modifier.size(22.dp)
            )
          }
          PText(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
        }
        action?.invoke()
      }

      Spacer(modifier = Modifier.height(16.dp))

      content()
    }
  }
}

@Composable
private fun AddButton(
  onClick: () -> Unit,
  color: Color
) {
  PContainer(
    onClick = onClick,
    shape = CircleShape,
    color = color.copy(alpha = 0.1f),
    modifier = Modifier.size(36.dp)
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        Icons.Default.Add,
        contentDescription = "添加",
        tint = color,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
private fun RecipeTypeChip(
  type: RecipeType,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val (emoji, label, color) = when (type) {
    RecipeType.MEAT -> Triple("🥩", "荤菜", MeatRed)
    RecipeType.VEG -> Triple("🥬", "素菜", VegGreen)
    RecipeType.SOUP -> Triple("🍲", "汤", SoupBlue)
    RecipeType.STAPLE -> Triple("🍚", "主食", StapleOrange)
    RecipeType.OTHER -> Triple("🥣", "其他", OtherPurple)
  }

  PContainer(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
    border = if (isSelected) BorderStroke(2.dp, color) else null,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      PText(emoji, fontSize = 20.sp)
      PText(
        label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
      )
    }
  }
}

@Composable
private fun DifficultyChip(
  difficulty: Difficulty,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val (label, stars, color) = when (difficulty) {
    Difficulty.EASY -> Triple("简单", 1, SoftGreen)
    Difficulty.MEDIUM -> Triple("中等", 2, WarmYellow)
    Difficulty.HARD -> Triple("困难", 3, MeatRed)
  }

  PContainer(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
    border = if (isSelected) BorderStroke(2.dp, color) else null,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(stars) {
          Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = if (isSelected) color else Color.Gray.copy(alpha = 0.4f),
            modifier = Modifier.size(14.dp)
          )
        }
      }
      PText(
        label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
      )
    }
  }
}

@Composable
private fun IngredientInputCard(
  index: Int,
  ingredient: IngredientInput,
  onIngredientChange: (IngredientInput) -> Unit,
  onDelete: () -> Unit,
  canDelete: Boolean
) {
  PContainer(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = BorderStroke(1.dp, SoftGreen.copy(alpha = 0.2f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(SoftGreen.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
      ) {
        PText(
          "${index + 1}",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = SoftGreen
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        PText(
          "食材",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        BasicTextField(
          value = ingredient.name,
          onValueChange = { onIngredientChange(ingredient.copy(name = it)) },
          textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
          ),
          singleLine = true,
          decorationBox = { innerTextField ->
            Box {
              if (ingredient.name.isEmpty()) {
                PText(
                  "例如：鸡蛋",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
              }
              innerTextField()
            }
          }
        )
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        PContainer(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
          ),
          modifier = Modifier.width(50.dp)
        ) {
          BasicTextField(
            value = ingredient.amount,
            onValueChange = { onIngredientChange(ingredient.copy(amount = it)) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            decorationBox = { innerTextField ->
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
              ) {
                if (ingredient.amount.isEmpty()) {
                  PText(
                    "数量",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                  )
                }
                innerTextField()
              }
            }
          )
        }

        PSelect(
          options = IngredientUnit.entries.map { unit ->
            SelectOption(
              label = unit.getDisplayName(),
              value = unit
            )
          },
          value = ingredient.unit,
          onValueChange = { unit -> onIngredientChange(ingredient.copy(unit = unit)) },
          modifier = Modifier.width(72.dp),
          size = ComponentSize.Small,
          status = ComponentStatus.Success,
          colors = SelectDefaults.colors(
            textColor = SoftGreen,
            placeholderColor = SoftGreen,
            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            containerColor = SoftGreen.copy(alpha = 0.1f),
            dropdownContainerColor = MaterialTheme.colorScheme.surface,
            optionTextColor = MaterialTheme.colorScheme.onSurface,
            selectedOptionTextColor = SoftGreen,
            selectedOptionContainerColor = SoftGreen.copy(alpha = 0.1f),
            disabledOptionTextColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }

      if (canDelete) {
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            Icons.Outlined.Close,
            contentDescription = "删除",
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun StepContentCard(
  stepNumber: Int,
  step: StepInput,
  onStepChange: (StepInput) -> Unit,
  onDelete: () -> Unit,
  canDelete: Boolean,
  onDragStart: () -> Unit = {},
  onDrag: (Float) -> Unit = {},
  onDragEnd: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val isDark = LocalDarkTheme.current
  val stepBackground = if (isDark) MaterialTheme.colorScheme.surfaceVariant else StepCardBackground
  val focusRequester by useCreation { FocusRequester() }

  PContainer(
    shape = RoundedCornerShape(16.dp),
    color = stepBackground,
    border = BorderStroke(1.dp, SoftBlue.copy(alpha = 0.2f)),
    modifier = modifier
      .pointerInput(stepNumber) {
        detectDragGesturesAfterLongPress(
          onDragStart = { onDragStart() },
          onDrag = { _, dragAmount -> onDrag(dragAmount.y) },
          onDragEnd = { onDragEnd() },
          onDragCancel = { onDragEnd() }
        )
      }
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        PText(
          "步骤 $stepNumber",
          style = MaterialTheme.typography.labelMedium,
          color = SoftBlue,
          fontWeight = FontWeight.SemiBold
        )
        if (canDelete) {
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              Icons.Outlined.Close,
              contentDescription = "删除",
              tint = Color.Gray.copy(alpha = 0.5f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      BasicTextField(
        value = step.description,
        onValueChange = { onStepChange(step.copy(description = it)) },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 22.sp
        ),
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(focusRequester),
        decorationBox = { innerTextField ->
          Box {
            if (step.description.isEmpty()) {
              PText(
                "描述这一步的操作...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
              )
            }
            innerTextField()
          }
        }
      )
    }
  }
}

data class IngredientInput(
  val name: String = "",
  val amount: String = "",
  val unit: IngredientUnit = IngredientUnit.G
)

data class StepInput(
  val description: String = ""
)

private fun IngredientUnit.getDisplayName(): String {
  return when (this) {
    IngredientUnit.G -> "克"
    IngredientUnit.ML -> "毫升"
    IngredientUnit.PIECE -> "个"
    IngredientUnit.SPOON -> "勺"
    IngredientUnit.MODERATE -> "适量"
  }
}

@Composable
fun StepConnectorWithInsert(
  onInsertStep: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.width(40.dp)
  ) {
    Box(
      modifier = Modifier
        .width(2.dp)
        .height(24.dp)
        .background(SoftBlue.copy(alpha = 0.3f))
    )

    PContainer(
      onClick = onInsertStep,
      shape = CircleShape,
      color = SoftBlue.copy(alpha = 0.1f),
      modifier = Modifier.size(32.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          Icons.Default.Add,
          contentDescription = "在此处插入步骤",
          tint = SoftBlue,
          modifier = Modifier.size(18.dp)
        )
      }
    }

    Box(
      modifier = Modifier
        .width(2.dp)
        .height(24.dp)
        .background(SoftBlue.copy(alpha = 0.3f))
    )
  }
}
