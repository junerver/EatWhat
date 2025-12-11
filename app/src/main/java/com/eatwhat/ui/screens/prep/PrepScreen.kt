package com.eatwhat.ui.screens.prep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eatwhat.domain.usecase.GeneratePrepListUseCase
import com.eatwhat.domain.usecase.PrepListItem
import xyz.junerver.compose.hooks.*

@Composable
fun PrepScreen(
    navController: NavController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as com.eatwhat.EatWhatApplication
    val rollResult = app.currentRollResult

    // If no roll result, navigate back
    if (rollResult == null) {
        LaunchedEffect(Unit) {
            navController.navigateUp()
        }
        return
    }

    val useCase = remember { GeneratePrepListUseCase() }
    val initialPrepList = remember { useCase(rollResult.recipes) }
    val (prepList, setPrepList) = useState(initialPrepList)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 60.dp, bottom = 100.dp)
        ) {
            // 返回按钮
            TextButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "← 返回",
                    fontSize = 24.sp,
                    color = Color(0xFF6750A4)
                )
            }

            // 标题
            Text(
                text = "准备食材",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 食材清单
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(prepList, key = { _, item -> "${item.name}_${item.unit}" }) { index, item ->
                    IngredientCheckItem(
                        item = item,
                        onCheckedChange = { checked ->
                            setPrepList(
                                prepList.mapIndexed { i, it ->
                                    if (i == index) {
                                        it.copy(isChecked = checked)
                                    } else {
                                        it
                                    }
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 开始做菜按钮
            Button(
                onClick = {
                    // TODO: Navigate to HistoryDetailScreen after saving history
                    // For now, just navigate back
                    navController.navigateUp()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6750A4)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "开始做菜",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun IngredientCheckItem(
    item: PrepListItem,
    onCheckedChange: (Boolean) -> Unit
) {
    val backgroundColor = if (item.isChecked) Color(0xFFE8DEF8) else Color(0xFFF5F5F5)
    val textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
    val opacity = if (item.isChecked) 0.6f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!item.isChecked) }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(20.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF6750A4),
                uncheckedColor = Color(0xFF79747E)
            )
        )

        Text(
            text = "${item.name} ${item.amount}${
                when (item.unit) {
                    "G" -> "g"
                    "ML" -> "ml"
                    "PIECE" -> "个"
                    "SPOON" -> "勺"
                    "MODERATE" -> ""
                    else -> item.unit
                }
            }",
            fontSize = 16.sp,
            color = Color(0xFF1C1B1F).copy(alpha = opacity),
            textDecoration = textDecoration,
            modifier = Modifier.weight(1f)
        )
    }
}
