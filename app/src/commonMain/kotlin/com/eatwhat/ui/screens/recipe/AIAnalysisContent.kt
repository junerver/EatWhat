package com.eatwhat.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatwhat.domain.model.AIProviderSummary
import com.eatwhat.ui.components.AppToolbar
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftPurple
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.button.ButtonColors
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardColors
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.container.PContainer
import xyz.junerver.compose.palette.components.popup.PPopup
import xyz.junerver.compose.palette.components.radio.PRadio
import xyz.junerver.compose.palette.components.scaffold.PScaffold
import xyz.junerver.compose.palette.components.scaffold.ScaffoldDefaults
import xyz.junerver.compose.palette.components.text.PText

@Composable
fun AIAnalysisContent(
    prompt: String,
    selectedImageBase64: String?,
    activeProvider: AIProviderSummary?,
    providers: List<AIProviderSummary>,
    isLoading: Boolean,
    displayError: String?,
    onPromptChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onAnalyze: () -> Unit,
    onNavigateUp: () -> Unit,
    onConfigureAI: () -> Unit,
    onProviderSelected: (Long) -> Unit,
    selectedImagePreview: @Composable (String) -> Unit
) {
    var showModelSelector by useState(false)
    val isDark = LocalDarkTheme.current

    PScaffold(
        topBar = {
            AppToolbar(
                title = "AI 菜谱分析",
                onNavigateUp = onNavigateUp
            )
        },
        colors = ScaffoldDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ModelSelectorCard(
                    activeProvider = activeProvider,
                    hasProviders = providers.isNotEmpty(),
                    onClick = {
                        if (providers.isNotEmpty()) {
                            showModelSelector = true
                        } else {
                            onConfigureAI()
                        }
                    }
                )
            }

            item {
                PCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = CardVariant.Elevated,
                    colors = CardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PText(
                            "菜谱描述",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        PromptTextField(
                            prompt = prompt,
                            isDark = isDark,
                            onPromptChange = onPromptChange
                        )

                        PText(
                            "添加图片 (可选)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        if (selectedImageBase64 != null) {
                            SelectedImageCard(
                                imageBase64 = selectedImageBase64,
                                onRemoveImage = onRemoveImage,
                                selectedImagePreview = selectedImagePreview
                            )
                        } else {
                            ImagePickerPlaceholder(
                                isDark = isDark,
                                onPickImage = onPickImage
                            )
                        }

                        if (displayError != null) {
                            PText(
                                displayError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                PButton(
                    text = if (isLoading) "分析中..." else "开始分析",
                    modifier = Modifier.fillMaxWidth(),
                    disabled = prompt.isBlank() && selectedImageBase64 == null,
                    loading = isLoading,
                    colors = ButtonColors(
                        containerColor = PrimaryOrange,
                        contentColor = Color.White
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = onAnalyze
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    PPopup(
        visible = showModelSelector,
        onClose = { showModelSelector = false },
        containerColor = MaterialTheme.colorScheme.surface,
        contentPadding = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            PText(
                "选择 AI 模型",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            ModelSheetDivider()

            providers.forEach { provider ->
                val isSelected = provider.id == activeProvider?.id
                PRadio(
                    label = provider.name,
                    description = provider.model,
                    checked = isSelected,
                    checkedColor = PrimaryOrange,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    descriptionColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    onClick = {
                        onProviderSelected(provider.id)
                        showModelSelector = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ModelSelectorCard(
    activeProvider: AIProviderSummary?,
    hasProviders: Boolean,
    onClick: () -> Unit
) {
    PCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated,
        onClick = onClick,
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoftPurple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = SoftPurple,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (activeProvider != null) {
                    PText(
                        activeProvider.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    PText(
                        activeProvider.model,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    PText(
                        "未配置模型",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                    PText(
                        if (hasProviders) "点击选择模型" else "点击前往设置",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "选择模型",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PromptTextField(
    prompt: String,
    isDark: Boolean,
    onPromptChange: (String) -> Unit
) {
    PContainer(
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF8F8F8)
    ) {
        BasicTextField(
            value = prompt,
            onValueChange = onPromptChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (prompt.isEmpty()) {
                        PText(
                            "例如：\n西红柿炒鸡蛋\n需要两个西红柿和三个鸡蛋\n先炒鸡蛋后加西红柿...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            lineHeight = 24.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun SelectedImageCard(
    imageBase64: String,
    onRemoveImage: () -> Unit,
    selectedImagePreview: @Composable (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        selectedImagePreview(imageBase64)

        IconButton(
            onClick = onRemoveImage,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove image",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ImagePickerPlaceholder(
    isDark: Boolean,
    onPickImage: () -> Unit
) {
    PContainer(
        onClick = onPickImage,
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF8F8F8),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.AddAPhoto,
                contentDescription = null,
                tint = PrimaryOrange,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PText(
                "点击上传图片",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModelSheetDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    )
}
