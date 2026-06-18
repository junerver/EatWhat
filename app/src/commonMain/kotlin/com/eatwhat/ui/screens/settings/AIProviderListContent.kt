package com.eatwhat.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eatwhat.domain.model.AIProviderSummary
import com.eatwhat.domain.model.ProviderTestState
import com.eatwhat.ui.components.AppToolbar
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.PrimaryOrange
import xyz.junerver.compose.palette.components.button.ButtonSize
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.empty.PEmpty
import xyz.junerver.compose.palette.components.loading.PLoading
import xyz.junerver.compose.palette.components.scaffold.PScaffold
import xyz.junerver.compose.palette.components.scaffold.ScaffoldDefaults
import xyz.junerver.compose.palette.components.text.PText

@Composable
fun AIProviderListContent(
    providers: List<AIProviderSummary>,
    testStates: Map<Long, ProviderTestState>,
    isBatchTesting: Boolean,
    onNavigateUp: () -> Unit,
    onAddProvider: () -> Unit,
    onEditProvider: (Long) -> Unit,
    onActivateProvider: (Long) -> Unit,
    onTestProvider: (Long) -> Unit,
    onBatchTest: () -> Unit
) {
    val isDark = LocalDarkTheme.current
    val pageBackground = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF5F5F5)
    val textColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color.Black
    val subTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray

    PScaffold(
        topBar = {
            AppToolbar(
                title = "AI 模型供应商",
                containerColor = Color.Transparent,
                onNavigateUp = onNavigateUp,
                actions = {
                    IconButton(
                        onClick = onBatchTest,
                        enabled = !isBatchTesting && providers.isNotEmpty()
                    ) {
                        if (isBatchTesting) {
                            PLoading(
                                size = 24.dp,
                                color = PrimaryOrange
                            )
                        } else {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = "Batch Test",
                                tint = PrimaryOrange
                            )
                        }
                    }
                    IconButton(onClick = onAddProvider) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Provider",
                            tint = PrimaryOrange
                        )
                    }
                }
            )
        },
        colors = ScaffoldDefaults.colors(
            containerColor = pageBackground
        )
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(providers, key = { it.id }) { provider ->
                    AIProviderItem(
                        provider = provider,
                        testState = testStates[provider.id] ?: ProviderTestState(),
                        textColor = textColor,
                        subTextColor = subTextColor,
                        onActivate = { onActivateProvider(provider.id) },
                        onEdit = { onEditProvider(provider.id) },
                        onTest = { onTestProvider(provider.id) }
                    )
                }

                if (providers.isEmpty()) {
                    item {
                        PEmpty(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            icon = {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp)
                                )
                            },
                            title = "暂无配置的供应商",
                            description = "添加一个供应商后即可进行连接测试和模型选择。",
                            iconColor = subTextColor.copy(alpha = 0.5f),
                            titleColor = subTextColor,
                            descriptionColor = subTextColor.copy(alpha = 0.7f),
                            action = {
                                PButton(
                                    text = "添加供应商",
                                    size = ButtonSize.MEDIUM,
                                    onClick = onAddProvider
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AIProviderItem(
    provider: AIProviderSummary,
    testState: ProviderTestState,
    textColor: Color,
    subTextColor: Color,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onTest: () -> Unit
) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        variant = if (provider.isActive) CardVariant.Outlined else CardVariant.Elevated,
        onClick = onActivate
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (provider.isActive) {
                    Icons.Default.RadioButtonChecked
                } else {
                    Icons.Default.RadioButtonUnchecked
                },
                contentDescription = if (provider.isActive) "Active" else "Inactive",
                tint = if (provider.isActive) PrimaryOrange else subTextColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                PText(
                    text = provider.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                PText(
                    text = provider.model,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subTextColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                PText(
                    text = provider.baseUrl,
                    style = MaterialTheme.typography.labelSmall,
                    color = subTextColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                AnimatedVisibility(visible = testState.lastTestTime > 0 || testState.isTesting) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (testState.isTesting) {
                                PLoading(
                                    size = 18.dp,
                                    color = PrimaryOrange
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                PText(
                                    "Testing...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = subTextColor
                                )
                            } else {
                                Icon(
                                    imageVector = if (testState.isSuccess) {
                                        Icons.Default.CheckCircle
                                    } else {
                                        Icons.Default.Error
                                    },
                                    contentDescription = null,
                                    tint = if (testState.isSuccess) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                PText(
                                    text = if (testState.isSuccess) "${testState.latency}ms" else "Failed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (testState.isSuccess) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                )
                            }
                        }
                    }
                }
            }

            Row {
                IconButton(
                    onClick = onTest,
                    enabled = !testState.isTesting
                ) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = "Test",
                        tint = subTextColor
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = subTextColor
                    )
                }
            }
        }
    }
}
