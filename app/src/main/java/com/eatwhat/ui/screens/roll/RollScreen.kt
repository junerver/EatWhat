package com.eatwhat.ui.screens.roll

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.eatwhat.navigation.Destinations
import com.eatwhat.ui.theme.*
import xyz.junerver.compose.hooks.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RollScreen(navController: NavController) {
    val (totalCount, setTotalCount) = useState(0)
    val (meatCount, setMeatCount) = useState(0)
    val (vegCount, setVegCount) = useState(0)
    val (soupCount, setSoupCount) = useState(0)
    val (showDishCountDialog, setShowDishCountDialog) = useState(false)
    val (showTypeDialog, setShowTypeDialog) = useState(false)
    val (currentType, setCurrentType) = useState("")

    // 检测深色模式
    val isDarkTheme = isSystemInDarkTheme()

    // 设置状态栏颜色：深色模式使用透明，浅色模式使用橙色
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = if (isDarkTheme) {
            android.graphics.Color.TRANSPARENT
        } else {
            PrimaryOrange.toArgb()
        }
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }

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
        if (totalCount == 0) {
            navController.navigate(
                Destinations.RollResult.createRoute(
                    meatCount = 1,
                    vegCount = 0,
                    soupCount = 0,
                    stapleCount = 0
                )
            )
            return
        }

        // 检查用户已分配的菜数
        val allocated = meatCount + vegCount + soupCount
        val remaining = totalCount - allocated

        // 如果用户没有分配任何类型，自动平均分配
        if (allocated == 0) {
            val autoMeat = totalCount / 2
            val autoVeg = totalCount - autoMeat
            navController.navigate(
                Destinations.RollResult.createRoute(
                    meatCount = autoMeat,
                    vegCount = autoVeg,
                    soupCount = 0,
                    stapleCount = 0
                )
            )
            return
        }

        // 如果有剩余未分配的菜数，自动分配给素菜和汤
        var finalMeatCount = meatCount
        var finalVegCount = vegCount
        var finalSoupCount = soupCount

        if (remaining > 0) {
            // 剩余的菜数平均分配给素菜和汤
            val autoVeg = remaining / 2
            val autoSoup = remaining - autoVeg
            finalVegCount += autoVeg
            finalSoupCount += autoSoup
        }

        // 使用最终分配的数量
        navController.navigate(
            Destinations.RollResult.createRoute(
                meatCount = finalMeatCount,
                vegCount = finalVegCount,
                soupCount = finalSoupCount,
                stapleCount = 0
            )
        )
    }

    Box(
        modifier = Modifier
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
            Text(
                text = "🍜 吃点啥",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            Text(
                text = "今天做什么菜？让我帮你决定",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Main Roll Button
            Surface(
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
                    Text(text = "🎲", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
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
                    label = if (totalCount == 0) "选择菜数" else "${totalCount}个菜",
                    onClick = { setShowDishCountDialog(true) }
                )

                // Type distribution buttons
                if (totalCount > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TypeConfigChip(
                            emoji = "🍗",
                            label = "荤",
                            count = meatCount,
                            color = MeatRed,
                            onClick = {
                                setCurrentType("meat")
                                setShowTypeDialog(true)
                            }
                        )
                        TypeConfigChip(
                            emoji = "🥬",
                            label = "素",
                            count = vegCount,
                            color = SoftGreen,
                            onClick = {
                                setCurrentType("veg")
                                setShowTypeDialog(true)
                            }
                        )
                        TypeConfigChip(
                            emoji = "🍲",
                            label = "汤",
                            count = soupCount,
                            color = SoftBlue,
                            onClick = {
                                setCurrentType("soup")
                                setShowTypeDialog(true)
                            }
                        )
                    }
                    
                    // Show remaining allocation hint
                    val allocated = meatCount + vegCount + soupCount
                    val remaining = totalCount - allocated
                    if (remaining > 0) {
                        Text(
                            text = "还有 $remaining 个菜未分配，将自动随机分配",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    // Dish count selector dialog
    if (showDishCountDialog) {
        SelectorDialog(
            title = "选择菜数",
            emoji = "🍽️",
            options = listOf(2, 3, 4, 5, 6, 7),
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
    if (showTypeDialog) {
        val used = meatCount + vegCount + soupCount - when (currentType) {
            "meat" -> meatCount
            "veg" -> vegCount
            "soup" -> soupCount
            else -> 0
        }
        val available = totalCount - used

        val (emoji, title, color) = when (currentType) {
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
                when (currentType) {
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
    Surface(
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
            Text(emoji, fontSize = 20.sp)
            Text(
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
    Surface(
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
            Text(emoji, fontSize = 16.sp)
            Text(
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
    options: List<Int>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Options grid - 2 columns
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    options.chunked(3).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { option ->
                                Surface(
                                    onClick = { onSelect(option) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = PrimaryOrange.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, PrimaryOrange.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${option}个菜",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = PrimaryOrange,
                                        modifier = Modifier.padding(vertical = 16.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                            // Fill empty slots
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
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
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
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
                                Surface(
                                    onClick = { onSelect(count) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = color.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = color,
                                        modifier = Modifier.padding(vertical = 14.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
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
        }
    }
}
