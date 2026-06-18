package com.eatwhat.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import xyz.junerver.compose.palette.components.toolbar.PToolbar
import xyz.junerver.compose.palette.components.toolbar.ToolbarColors

@Composable
fun AppToolbar(
    title: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onNavigateUp: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    PToolbar(
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
        title = title,
        colors = ToolbarColors(
            backgroundColor = containerColor,
            contentColor = contentColor
        ),
        navigationIcon = onNavigateUp?.let { navigateUp ->
            {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = contentColor
                    )
                }
            }
        },
        actions = actions
    )
}
