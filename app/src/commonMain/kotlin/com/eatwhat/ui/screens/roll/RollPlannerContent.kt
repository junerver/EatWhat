package com.eatwhat.ui.screens.roll

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatwhat.domain.model.RollConfig
import com.eatwhat.ui.theme.DarkGradientEnd
import com.eatwhat.ui.theme.DarkGradientStart
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.MeatRed
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.PrimaryOrangeLight
import com.eatwhat.ui.theme.SoftBlue
import com.eatwhat.ui.theme.SoftGreen
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.palette.components.container.PContainer
import xyz.junerver.compose.palette.components.dialog.PDialog
import xyz.junerver.compose.palette.components.dialog.PDialogConfirmAction
import xyz.junerver.compose.palette.components.text.PText

@Composable
fun RollPlannerContent(
  onRoll: (RollConfig) -> Unit,
  modifier: Modifier = Modifier
) {
  val (totalCount, setTotalCount) = useGetState(default = 0)
  val (meatCount, setMeatCount) = useGetState(default = 0)
  val (vegCount, setVegCount) = useGetState(default = 0)
  val (soupCount, setSoupCount) = useGetState(default = 0)
  val (showDishCountDialog, setShowDishCountDialog) = useGetState(default = false)
  val (showTypeDialog, setShowTypeDialog) = useGetState(default = false)
  val (currentType, setCurrentType) = useGetState(default = "")

    // 检测深色模式
  val isDarkTheme = LocalDarkTheme.current

    // 背景颜色：深色模式使用深色背景渐变，浅色模式使用橙色渐变
    val backgroundBrush = if (isDarkTheme) {
        Brush.linearGradient(
            colors = listOf(DarkGradientStart, DarkGradientEnd),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(PrimaryOrange, PrimaryOrangeLight),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    // Roll 按钮颜色
    val rollButtonColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White
    val rollButtonTextColor = PrimaryOrange

    fun executeRoll() {
        // 如果没有选择菜数，默认1个荤菜
      if (totalCount.value == 0) {
            onRoll(
                RollConfig(
                    meatCount = 1,
                    vegCount = 0,
                    soupCount = 0,
                    stapleCount = 0,
                    randomCount = 0
                )
            )
            return
        }

        // 检查用户已分配的菜数
      val allocated = meatCount.value + vegCount.value + soupCount.value
      val remaining = totalCount.value - allocated

      // 如果用户没有分配任何类型，应用自动均衡策略：荤素搭配，汤0-1
      if (allocated == 0) {
        val total = totalCount.value

        // 策略：汤的数量默认 0 或 1（除非总量太少）
        // 如果总量 > 1，随机决定是否要汤
        val soupTarget = if (total > 1) (0..1).random() else 0

        // 剩余数量分配给荤菜和素菜
        val remaining = total - soupTarget

        // 尽量平均分配，多余的随机给荤或素
        val half = remaining / 2
        val extra = remaining % 2

        // 随机决定谁拿多出来的那一个
        val meatExtra = if (extra > 0 && (0..1).random() == 1) 1 else 0
        val vegExtra = if (extra > 0 && meatExtra == 0) 1 else 0

        val meatTarget = half + meatExtra
        val vegTarget = half + vegExtra

        onRoll(
          RollConfig(
            meatCount = meatTarget,
            vegCount = vegTarget,
            soupCount = soupTarget,
            stapleCount = 0,
            randomCount = 0
          )
        )
        return
      }

      // 使用最终分配的数量，剩余数量作为随机数量
        onRoll(
            RollConfig(
              meatCount = meatCount.value,
              vegCount = vegCount.value,
              soupCount = soupCount.value,
              stapleCount = 0,
              randomCount = remaining
            )
        )
    }

    Box(
        modifier = modifier
          .fillMaxSize()
          .background(brush = backgroundBrush)
          .windowInsetsPadding(WindowInsets.statusBars)
          .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Title
            PText(
                text = "🍜 吃点啥",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            PText(
                text = "今天做什么菜？让我帮你决定",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Main Roll Button
            PContainer(
                modifier = Modifier
                  .size(180.dp)
                  .shadow(16.dp, CircleShape),
                shape = CircleShape,
                color = rollButtonColor,
                onClick = { executeRoll() }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PText(text = "🎲", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    PText(
                        text = "Roll 一下！",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = rollButtonTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Config buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dish count button
                ConfigButton(
                    emoji = "🍽️",
                  label = if (totalCount.value == 0) "选择菜数" else "${totalCount.value}个菜",
                    onClick = { setShowDishCountDialog(true) }
                )

                // Type distribution buttons
              if (totalCount.value > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TypeConfigChip(
                            emoji = "🍗",
                            label = "荤",
                          count = meatCount.value,
                            color = MeatRed,
                            onClick = {
                                setCurrentType("meat")
                                setShowTypeDialog(true)
                            }
                        )
                        TypeConfigChip(
                            emoji = "🥬",
                            label = "素",
                          count = vegCount.value,
                            color = SoftGreen,
                            onClick = {
                                setCurrentType("veg")
                                setShowTypeDialog(true)
                            }
                        )
                        TypeConfigChip(
                            emoji = "🍲",
                            label = "汤",
                          count = soupCount.value,
                            color = SoftBlue,
                            onClick = {
                                setCurrentType("soup")
                                setShowTypeDialog(true)
                            }
                        )
                    }

                    // Show remaining allocation hint
                    val allocated = meatCount.value + vegCount.value + soupCount.value
                    val remaining = totalCount.value - allocated
                    if (remaining > 0) {
                        PText(
                          text = "还有 $remaining 个菜未分配,将自动随机分配",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    // Dish count selector dialog
  if (showDishCountDialog.value) {
        SelectorDialog(
            title = "选择菜数",
            emoji = "🍽️",
          initialValue = totalCount.value,
            onSelect = { count ->
                setTotalCount(count)
                setMeatCount(0)
                setVegCount(0)
                setSoupCount(0)
                setShowDishCountDialog(false)
            },
            onDismiss = { setShowDishCountDialog(false) }
        )
    }

    // Type count selector dialog
  if (showTypeDialog.value) {
    val used = meatCount.value + vegCount.value + soupCount.value - when (currentType.value) {
      "meat" -> meatCount.value
      "veg" -> vegCount.value
      "soup" -> soupCount.value
            else -> 0
        }
    val available = totalCount.value - used

    val (emoji, title, color) = when (currentType.value) {
            "meat" -> Triple("🍗", "选择荤菜数量", MeatRed)
            "veg" -> Triple("🥬", "选择素菜数量", SoftGreen)
            "soup" -> Triple("🍲", "选择汤数量", SoftBlue)
            else -> Triple("", "", Color.Gray)
        }

        TypeSelectorDialog(
            title = title,
            emoji = emoji,
            color = color,
            maxCount = available,
            onSelect = { count ->
              when (currentType.value) {
                    "meat" -> setMeatCount(count)
                    "veg" -> setVegCount(count)
                    "soup" -> setSoupCount(count)
                }
                setShowTypeDialog(false)
            },
            onDismiss = { setShowTypeDialog(false) }
        )
    }
}

@Composable
private fun ConfigButton(
    emoji: String,
    label: String,
    onClick: () -> Unit
) {
    PContainer(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.2f),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PText(emoji, fontSize = 20.sp)
            PText(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun TypeConfigChip(
    emoji: String,
    label: String,
    count: Int,
    color: Color,
    onClick: () -> Unit
) {
    PContainer(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (count > 0) Color.White else Color.White.copy(alpha = 0.2f),
        border = if (count > 0) BorderStroke(2.dp, color) else BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PText(emoji, fontSize = 16.sp)
            PText(
                text = if (count > 0) "$label$count" else label,
                color = if (count > 0) color else Color.White,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun SelectorDialog(
    title: String,
    emoji: String,
    initialValue: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
  val (count, setCount) = useGetState(default = if (initialValue > 0) initialValue else 2)

    PDialog(
      content = {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PText(emoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.height(12.dp))
                PText(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
              Spacer(modifier = Modifier.height(32.dp))

              // Counter Row
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
              ) {
                // Minus Button
                PContainer(
                  onClick = { if (count.value > 1) setCount(count.value - 1) },
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  modifier = Modifier.size(48.dp)
                ) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Remove,
                      contentDescription = "Decrease",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                PText(
                  text = "${count.value}",
                  style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp
                  ),
                  color = PrimaryOrange,
                  modifier = Modifier.padding(horizontal = 32.dp)
                )

                // Plus Button
                PContainer(
                  onClick = { setCount(count.value + 1) },
                  shape = CircleShape,
                  color = PrimaryOrange,
                  modifier = Modifier.size(48.dp)
                ) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Add,
                      contentDescription = "Increase",
                      tint = Color.White
                    )
                  }
                }
              }

                Spacer(modifier = Modifier.height(24.dp))

              // Shortcuts
              Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                listOf(3, 5, 7).forEach { option ->
                  val isSelected = count.value == option
                  PContainer(
                    onClick = { setCount(option) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) PrimaryOrange else MaterialTheme.colorScheme.surfaceVariant.copy(
                      alpha = 0.5f
                    ),
                    modifier = Modifier
                      .weight(1f)
                      .height(48.dp)
                  ) {
                    Box(
                      modifier = Modifier.fillMaxSize(),
                      contentAlignment = Alignment.Center
                    ) {
                      PText(
                        text = "$option",
                        style = MaterialTheme.typography.titleMedium.copy(
                          fontWeight = FontWeight.Bold
                        ),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }
              }

            }
      },
      actions = {
        PDialogConfirmAction(
          onClick = { onSelect(count.value) }
        )
      },
      onDismiss = onDismiss
    )
}

@Composable
private fun TypeSelectorDialog(
    title: String,
    emoji: String,
    color: Color,
    maxCount: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    PDialog(
      content = {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PText(emoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.height(12.dp))
                PText(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Options grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    (0..maxCount).toList().chunked(4).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { count ->
                                PContainer(
                                    onClick = { onSelect(count) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = color.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        PText(
                                            text = count.toString(),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = color,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            // Fill empty slots
                            repeat(4 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
      },
      onDismiss = onDismiss
    )
}
