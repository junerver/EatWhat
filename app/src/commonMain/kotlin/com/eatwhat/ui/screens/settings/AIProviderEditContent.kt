package com.eatwhat.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eatwhat.domain.model.AIConfig
import com.eatwhat.domain.model.AIProviderEditData
import com.eatwhat.domain.model.ConnectionTestResult
import com.eatwhat.ui.components.AppToolbar
import com.eatwhat.ui.components.PaletteConfirmDialog
import com.eatwhat.ui.components.StyledTextField
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.PrimaryOrange
import com.eatwhat.ui.theme.SoftBlue
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useTimeoutFn
import xyz.junerver.compose.palette.components.alert.AlertType
import xyz.junerver.compose.palette.components.alert.PAlert
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.container.PContainer
import xyz.junerver.compose.palette.components.loading.PLoading
import xyz.junerver.compose.palette.components.scaffold.PScaffold
import xyz.junerver.compose.palette.components.scaffold.ScaffoldDefaults
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagColors
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.tag.TagVariant
import xyz.junerver.compose.palette.components.text.PText
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AIProviderEditContent(
    initialData: AIProviderEditData?,
    isEditing: Boolean,
    onNavigateUp: () -> Unit,
    onSaveProvider: suspend (AIProviderEditData) -> Unit,
    onDeleteProvider: suspend () -> Unit,
    onFetchModels: suspend (AIConfig) -> Result<List<String>>,
    onTestConnection: suspend (AIConfig) -> Result<ConnectionTestResult>
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val (name, setName) = useGetState(default = "")
    val (baseUrl, setBaseUrl) = useGetState(default = "")
    val (apiKey, setApiKey) = useGetState(default = "")
    val (model, setModel) = useGetState(default = "")
    val (isActive, setIsActive) = useGetState(default = false)

    val (availableModels, setAvailableModels) = useGetState(default = emptyList<String>())
    val (isFetchingModels, setIsFetchingModels) = useGetState(default = false)
    val (testResult, setTestResult) = useGetState(default = "")
    val (isTesting, setIsTesting) = useGetState(default = false)
    val (testSuccess, setTestSuccess) = useGetState(default = false)
    val (isApiKeyVisible, setApiKeyVisible) = useGetState(default = false)
    val (showDeleteDialog, setShowDeleteDialog) = useGetState(default = false)

    val isDark = LocalDarkTheme.current
    val pageBackground = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF5F5F5)
    val inputBackground = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF8F8F8)
    val textColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color.Black
    val subTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray

    useEffect(initialData) {
        initialData?.let { provider ->
            setName(provider.name)
            setBaseUrl(provider.baseUrl)
            setApiKey(provider.apiKey)
            setModel(provider.model)
            setIsActive(provider.isActive)
        }
    }

    useTimeoutFn({
        if (testSuccess.value && testResult.value.isNotBlank()) {
            setTestResult("")
            setTestSuccess(false)
        }
    }, 2.seconds)

    fun currentFormData(): AIProviderEditData {
        return AIProviderEditData(
            id = initialData?.id,
            name = name.value.ifBlank { "Default" },
            baseUrl = baseUrl.value,
            apiKey = apiKey.value,
            model = model.value,
            isActive = isActive.value
        )
    }

    fun fetchModels() {
        if (baseUrl.value.isNotBlank() && apiKey.value.isNotBlank()) {
            setIsFetchingModels(true)
            setTestResult("")
            setTestSuccess(false)
            scope.launch {
                val config = AIConfig(baseUrl.value, apiKey.value, model.value)
                val result = onFetchModels(config)
                setIsFetchingModels(false)
                result.onSuccess {
                    setAvailableModels(it)
                }.onFailure {
                    setTestResult("获取模型列表失败: ${it.message}")
                    setTestSuccess(false)
                }
            }
        }
    }

    fun testConnection() {
        if (baseUrl.value.isNotBlank() && apiKey.value.isNotBlank()) {
            setIsTesting(true)
            setTestResult("")
            setTestSuccess(false)
            scope.launch {
                val config = AIConfig(baseUrl.value, apiKey.value, model.value)
                val result = onTestConnection(config)
                setIsTesting(false)
                result.onSuccess {
                    if (it.isSuccess) {
                        setTestResult("Success! Latency: ${it.latencyMs}ms\nResponse: ${it.message.take(50)}...")
                        setTestSuccess(true)
                    } else {
                        setTestResult("Test failed: ${it.message}")
                        setTestSuccess(false)
                    }
                }.onFailure {
                    setTestResult("Test failed: ${it.message}")
                    setTestSuccess(false)
                }
            }
        }
    }

    fun saveProvider() {
        scope.launch {
            onSaveProvider(currentFormData())
            onNavigateUp()
        }
    }

    PScaffold(
        topBar = {
            AppToolbar(
                title = if (isEditing) "编辑模型供应商" else "添加模型供应商",
                containerColor = Color.Transparent,
                onNavigateUp = onNavigateUp,
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { setShowDeleteDialog(true) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                    IconButton(onClick = { saveProvider() }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF4CAF50))
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PCard(
                modifier = Modifier.fillMaxWidth(),
                variant = CardVariant.Elevated
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryOrange.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = PrimaryOrange
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        PText(
                            text = "供应商配置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    StyledTextField(
                        value = name.value,
                        onValueChange = { setName(it) },
                        label = "供应商名称",
                        placeholder = "例如: OpenAI, DeepSeek...",
                        backgroundColor = inputBackground,
                        textColor = textColor,
                        placeholderColor = subTextColor
                    )

                    StyledTextField(
                        value = baseUrl.value,
                        onValueChange = { setBaseUrl(it) },
                        label = "API Base URL",
                        placeholder = "https://api.openai.com/v1",
                        backgroundColor = inputBackground,
                        textColor = textColor,
                        placeholderColor = subTextColor
                    )

                    StyledTextField(
                        value = apiKey.value,
                        onValueChange = { setApiKey(it) },
                        label = "API Key",
                        placeholder = "sk-...",
                        isPassword = !isApiKeyVisible.value,
                        backgroundColor = inputBackground,
                        textColor = textColor,
                        placeholderColor = subTextColor,
                        trailingIcon = {
                            Icon(
                                imageVector = if (isApiKeyVisible.value) {
                                    Icons.Default.Visibility
                                } else {
                                    Icons.Default.VisibilityOff
                                },
                                contentDescription = if (isApiKeyVisible.value) "Hide API Key" else "Show API Key",
                                tint = subTextColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { setApiKeyVisible(!isApiKeyVisible.value) }
                            )
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StyledTextField(
                            value = model.value,
                            onValueChange = { setModel(it) },
                            label = "模型名称",
                            placeholder = "gpt-3.5-turbo",
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                if (isFetchingModels.value) {
                                    PLoading(
                                        size = 20.dp,
                                        color = PrimaryOrange
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            focusManager.clearFocus()
                                            fetchModels()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Fetch Models",
                                            tint = subTextColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            backgroundColor = inputBackground,
                            textColor = textColor,
                            placeholderColor = subTextColor
                        )

                        PContainer(
                            onClick = { testConnection() },
                            shape = RoundedCornerShape(12.dp),
                            color = inputBackground,
                            enabled = !isTesting.value && baseUrl.value.isNotBlank() && apiKey.value.isNotBlank(),
                            modifier = Modifier
                                .height(56.dp)
                                .width(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isTesting.value) {
                                    PLoading(
                                        size = 24.dp,
                                        color = PrimaryOrange
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = "Test Connection",
                                        tint = if (baseUrl.value.isNotBlank() && apiKey.value.isNotBlank()) {
                                            PrimaryOrange
                                        } else {
                                            subTextColor
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = testResult.value.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                PAlert(
                    message = if (testSuccess.value) "连接成功" else "连接失败",
                    description = testResult.value,
                    type = if (testSuccess.value) AlertType.Success else AlertType.Error
                )
            }

            AnimatedVisibility(
                visible = availableModels.value.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                PCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = CardVariant.Elevated
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SoftBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = SoftBlue
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                PText(
                                    text = "可用模型列表",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                PText(
                                    text = "共 ${availableModels.value.size} 个模型",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = subTextColor
                                )
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableModels.value.forEach { modelName ->
                                val selected = model.value == modelName
                                PTag(
                                    text = modelName,
                                    size = TagSize.Large,
                                    variant = if (selected) TagVariant.Outlined else TagVariant.Default,
                                    onClick = { setModel(modelName) },
                                    colors = TagColors(
                                        containerColor = if (selected) {
                                            SoftBlue.copy(alpha = 0.15f)
                                        } else {
                                            inputBackground
                                        },
                                        contentColor = if (selected) SoftBlue else textColor,
                                        borderColor = if (selected) SoftBlue else Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        if (showDeleteDialog.value) {
            PaletteConfirmDialog(
                title = "确认删除",
                message = "确定要删除此模型供应商吗？此操作无法撤销。",
                confirmText = "删除",
                confirmType = ButtonType.DANGER,
                icon = Icons.Default.Delete,
                iconTint = Color.Red,
                onDismiss = { setShowDeleteDialog(false) },
                onConfirm = {
                    setShowDeleteDialog(false)
                    scope.launch {
                        onDeleteProvider()
                        onNavigateUp()
                    }
                }
            )
        }
    }
}
