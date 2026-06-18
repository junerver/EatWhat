package com.eatwhat.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.database.entities.AIProviderEntity
import com.eatwhat.data.repository.AIProviderRepository
import com.eatwhat.domain.model.AIConfig
import com.eatwhat.domain.model.AIProviderSummary
import com.eatwhat.domain.model.ProviderTestState
import com.eatwhat.domain.service.AIService
import com.eatwhat.domain.service.OpenAIService
import com.eatwhat.navigation.Destinations
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useMap

@Composable
fun AIProviderListScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database by useCreation { EatWhatDatabase.getInstance(context) }
    val repository by useCreation { AIProviderRepository(database.aiProviderDao()) }
    val openAIService: AIService by useCreation { OpenAIService() }

    val providers by repository.allProviders.collectAsState(initial = emptyList())
    val providerSummaries = providers.map { it.toSummary() }
    val testStates = useMap<Long, ProviderTestState>()
    val (isBatchTesting, setIsBatchTesting) = useGetState(default = false)

    fun updateTesting(providerId: Long) {
        testStates[providerId] =
            testStates.getOrDefault(providerId, ProviderTestState()).copy(isTesting = true)
    }

    suspend fun testProvider(provider: AIProviderEntity) {
        updateTesting(provider.id)
        val config = AIConfig(provider.baseUrl, provider.apiKey, provider.model)
        val result = openAIService.testConnection(config)

        result.onSuccess {
            testStates[provider.id] = ProviderTestState(
                isTesting = false,
                isSuccess = it.isSuccess,
                latency = it.latencyMs,
                message = if (it.isSuccess) "Connected" else it.message,
                lastTestTime = System.currentTimeMillis()
            )
        }.onFailure {
            testStates[provider.id] = ProviderTestState(
                isTesting = false,
                isSuccess = false,
                message = it.message ?: "Unknown error",
                lastTestTime = System.currentTimeMillis()
            )
        }
    }

    AIProviderListContent(
        providers = providerSummaries,
        testStates = testStates,
        isBatchTesting = isBatchTesting.value,
        onNavigateUp = { navController.popBackStack() },
        onAddProvider = { navController.navigate(Destinations.AIProviderEdit.createRoute()) },
        onEditProvider = { providerId ->
            navController.navigate(Destinations.AIProviderEdit.createRoute(providerId))
        },
        onActivateProvider = { providerId ->
            scope.launch { repository.setActive(providerId) }
        },
        onTestProvider = { providerId ->
            providers.firstOrNull { it.id == providerId }?.let { provider ->
                scope.launch { testProvider(provider) }
            }
        },
        onBatchTest = {
            if (!isBatchTesting.value && providers.isNotEmpty()) {
                setIsBatchTesting(true)
                scope.launch {
                    providers.map { provider ->
                        async { testProvider(provider) }
                    }.awaitAll()
                    setIsBatchTesting(false)
                }
            }
        }
    )
}

private fun AIProviderEntity.toSummary(): AIProviderSummary {
    return AIProviderSummary(
        id = id,
        name = name,
        baseUrl = baseUrl,
        model = model,
        isActive = isActive
    )
}
