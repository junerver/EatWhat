package com.eatwhat.ui.screens.roll

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.domain.model.RollConfig
import com.eatwhat.domain.model.RollResult
import com.eatwhat.domain.usecase.InsufficientRecipesException
import com.eatwhat.domain.usecase.RollRecipesUseCase
import com.eatwhat.ui.components.RecipeCard
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RollScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val useCase = remember { RollRecipesUseCase(app.rollRepository) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val (config, setConfig) = useState(RollConfig())
    val (result, setResult) = useState<RollResult?>(null)
    val (isLoading, setIsLoading) = useState(false)
    val (errorMessage, setErrorMessage) = useState<String?>(null)

    // Show error message in snackbar
    useEffect(errorMessage ?: "") {
        errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                setErrorMessage(null)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Roll点") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Configuration Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "配置",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Auto balance toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("荤素搭配")
                        Switch(
                            checked = config.autoBalance,
                            onCheckedChange = { setConfig(config.copy(autoBalance = it)) }
                        )
                    }

                    Divider()

                    // Meat counter
                    CounterRow(
                        label = "荤菜 🍗",
                        count = config.meatCount,
                        onIncrement = { setConfig(config.copy(meatCount = config.meatCount + 1)) },
                        onDecrement = {
                            if (config.meatCount > 0) {
                                setConfig(config.copy(meatCount = config.meatCount - 1))
                            }
                        }
                    )

                    // Veg counter
                    CounterRow(
                        label = "素菜 🥬",
                        count = config.vegCount,
                        onIncrement = { setConfig(config.copy(vegCount = config.vegCount + 1)) },
                        onDecrement = {
                            if (config.vegCount > 0) {
                                setConfig(config.copy(vegCount = config.vegCount - 1))
                            }
                        }
                    )

                    // Soup counter
                    CounterRow(
                        label = "汤 🍲",
                        count = config.soupCount,
                        onIncrement = { setConfig(config.copy(soupCount = config.soupCount + 1)) },
                        onDecrement = {
                            if (config.soupCount > 0) {
                                setConfig(config.copy(soupCount = config.soupCount - 1))
                            }
                        }
                    )

                    // Staple counter
                    CounterRow(
                        label = "主食 🍚",
                        count = config.stapleCount,
                        onIncrement = { setConfig(config.copy(stapleCount = config.stapleCount + 1)) },
                        onDecrement = {
                            if (config.stapleCount > 0) {
                                setConfig(config.copy(stapleCount = config.stapleCount - 1))
                            }
                        }
                    )
                }
            }

            // Roll Button
            Button(
                onClick = {
                    // Validation
                    val totalCount = config.meatCount + config.vegCount +
                                   config.soupCount + config.stapleCount
                    if (totalCount == 0) {
                        setErrorMessage("请至少选择一道菜")
                        return@Button
                    }

                    // Execute roll
                    scope.launch {
                        setIsLoading(true)
                        try {
                            val rollResult = useCase(config)
                            rollResult.fold(
                                onSuccess = { setResult(it) },
                                onFailure = { error ->
                                    when (error) {
                                        is InsufficientRecipesException -> {
                                            setErrorMessage(error.errors.joinToString("\n"))
                                        }
                                        else -> {
                                            setErrorMessage("Roll失败: ${error.message}")
                                        }
                                    }
                                }
                            )
                        } finally {
                            setIsLoading(false)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Roll点", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Result Section
            result?.let { rollResult ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "今天吃这些",
                            style = MaterialTheme.typography.titleMedium
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            items(rollResult.recipes) { recipe ->
                                RecipeCard(recipe = recipe)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { setResult(null) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("重新Roll")
                            }

                            Button(
                                onClick = {
                                    // TODO: Navigate to PrepScreen with result
                                    // navController.navigate("prep/${result.toJson()}")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("确认")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterRow(
    label: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDecrement,
                enabled = count > 0
            ) {
                Text("-", style = MaterialTheme.typography.titleLarge)
            }

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.widthIn(min = 24.dp)
            )

            IconButton(onClick = onIncrement) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
