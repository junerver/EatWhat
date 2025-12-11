package com.eatwhat.ui.screens.roll

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.eatwhat.navigation.Destinations
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

    fun executeRoll() {
        val finalMeatCount = if (totalCount == 0) 1 else meatCount
        val finalVegCount = if (totalCount == 0) 0 else vegCount
        val finalSoupCount = if (totalCount == 0) 0 else soupCount
        val finalStapleCount = 0

        navController.navigate(
            Destinations.RollResult.createRoute(
                meatCount = finalMeatCount,
                vegCount = finalVegCount,
                soupCount = finalSoupCount,
                stapleCount = finalStapleCount
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF6750A4), Color(0xFF7D5FA8)),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "🍜 吃点啥",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            Text(
                text = "今天做什么菜？让我帮你决定",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            Surface(
                modifier = Modifier.size(160.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                onClick = { executeRoll() }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "🎲", fontSize = 48.sp)
                    Text(
                        text = "Roll点！",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF6750A4),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConfigButton(
                    label = if (totalCount == 0) "几个菜" else "${totalCount}个菜",
                    onClick = { setShowDishCountDialog(true) }
                )

                if (totalCount > 0) {
                    ConfigButton(
                        label = "荤${if (meatCount > 0) meatCount else ""}",
                        small = true,
                        onClick = {
                            setCurrentType("meat")
                            setShowTypeDialog(true)
                        }
                    )
                    ConfigButton(
                        label = "素${if (vegCount > 0) vegCount else ""}",
                        small = true,
                        onClick = {
                            setCurrentType("veg")
                            setShowTypeDialog(true)
                        }
                    )
                    ConfigButton(
                        label = "汤${if (soupCount > 0) soupCount else ""}",
                        small = true,
                        onClick = {
                            setCurrentType("soup")
                            setShowTypeDialog(true)
                        }
                    )
                }
            }
        }
    }

    if (showDishCountDialog) {
        SelectorDialog(
            title = "选择菜数",
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

    if (showTypeDialog) {
        val used = meatCount + vegCount + soupCount - when (currentType) {
            "meat" -> meatCount
            "veg" -> vegCount
            "soup" -> soupCount
            else -> 0
        }
        val available = totalCount - used

        TypeSelectorDialog(
            title = when (currentType) {
                "meat" -> "选择荤菜数量"
                "veg" -> "选择素菜数量"
                "soup" -> "选择汤数量"
                else -> ""
            },
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
    label: String,
    small: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.2f),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            color = Color.White,
            style = if (small)
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            else
                MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(
                horizontal = if (small) 16.dp else 24.dp,
                vertical = if (small) 8.dp else 12.dp
            )
        )
    }
}

@Composable
private fun SelectorDialog(
    title: String,
    options: List<Int>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    options.chunked(3).forEach { row ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            row.forEach { option ->
                                Surface(
                                    onClick = { onSelect(option) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF5F5F5),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${option}个菜",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.padding(16.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
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
    maxCount: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    (0..maxCount).chunked(3).forEach { row ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            row.forEach { count ->
                                Surface(
                                    onClick = { onSelect(count) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF5F5F5),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.padding(16.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
