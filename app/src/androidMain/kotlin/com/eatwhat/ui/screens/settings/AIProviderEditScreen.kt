package com.eatwhat.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.database.entities.AIProviderEntity
import com.eatwhat.data.repository.AIProviderRepository
import com.eatwhat.domain.model.AIProviderEditData
import com.eatwhat.domain.service.AIService
import com.eatwhat.domain.service.OpenAIService
import kotlinx.coroutines.flow.first
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation

@Composable
fun AIProviderEditScreen(
    navController: NavController,
    providerId: Long? = null
) {
    val context = LocalContext.current
    val database by useCreation { EatWhatDatabase.getInstance(context) }
    val repository by useCreation { AIProviderRepository(database.aiProviderDao()) }
    val openAIService: AIService by useCreation { OpenAIService() }

    val provider by if (providerId != null) {
        repository.getProviderById(providerId).collectAsState(initial = null)
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<AIProviderEntity?>(null) }
    }

    AIProviderEditContent(
        initialData = provider?.toEditData(),
        isEditing = providerId != null,
        onNavigateUp = { navController.popBackStack() },
        onSaveProvider = { form ->
            if (providerId != null) {
                val original = repository.getProviderById(providerId).first()
                if (original != null) {
                    repository.update(
                        original.copy(
                            name = form.name,
                            baseUrl = form.baseUrl,
                            apiKey = form.apiKey,
                            model = form.model,
                            isActive = form.isActive,
                            lastModified = System.currentTimeMillis()
                        )
                    )
                    if (form.isActive) {
                        repository.setActive(providerId)
                    }
                }
            } else {
                val newId = repository.insert(
                    AIProviderEntity(
                        name = form.name,
                        baseUrl = form.baseUrl,
                        apiKey = form.apiKey,
                        model = form.model,
                        isActive = form.isActive
                    )
                )
                val active = repository.activeProvider.first()
                if (form.isActive || active == null) {
                    repository.setActive(newId)
                }
            }
        },
        onDeleteProvider = {
            providerId?.let { repository.delete(it) }
        },
        onFetchModels = { config -> openAIService.fetchModels(config) },
        onTestConnection = { config -> openAIService.testConnection(config) }
    )
}

private fun AIProviderEntity.toEditData(): AIProviderEditData {
    return AIProviderEditData(
        id = id,
        name = name,
        baseUrl = baseUrl,
        apiKey = apiKey,
        model = model,
        isActive = isActive
    )
}
