package com.eatwhat.ui.screens.prep

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.domain.model.RollResult
import com.eatwhat.domain.usecase.GeneratePrepListUseCase
import com.eatwhat.domain.usecase.PrepListItem
import xyz.junerver.compose.hooks.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrepScreen(
    navController: NavController,
    rollResult: RollResult
) {
    val useCase = remember { GeneratePrepListUseCase() }
    val initialPrepList = remember { useCase(rollResult.recipes) }
    val (prepList, setPrepList) = useState(initialPrepList)

    val checkedCount = prepList.count { it.isChecked }
    val totalCount = prepList.size
    val progress = if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("备菜清单") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress indicator
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "准备进度",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$checkedCount / $totalCount",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Prep list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(prepList, key = { "${it.name}_${it.unit}" }) { item ->
                        IngredientCheckItem(
                            item = item,
                            onCheckedChange = { checked ->
                                setPrepList(
                                    prepList.map {
                                        if (it.name == item.name && it.unit == item.unit) {
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
            }

            // Start cooking button
            Button(
                onClick = {
                    // TODO: Navigate to HistoryDetailScreen after saving history
                    // For now, just navigate back
                    navController.navigateUp()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("开始做菜", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun IngredientCheckItem(
    item: PrepListItem,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange
            )

            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                color = if (item.isChecked) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        Text(
            text = "${item.amount} ${
                when (item.unit) {
                    "G" -> "g"
                    "ML" -> "ml"
                    "PIECE" -> "个"
                    "SPOON" -> "勺"
                    "MODERATE" -> ""
                    else -> item.unit
                }
            }",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
