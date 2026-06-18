package com.eatwhat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.palette.components.text.PText

/**
 * Composable to display a recipe icon (emoji or image).
 */
@Composable
fun RecipeIcon(
    emoji: String,
    imageBase64: String?,
    modifier: Modifier = Modifier,
    size: IconSize = IconSize.MEDIUM
) {
    val textStyle = when (size) {
        IconSize.SMALL -> MaterialTheme.typography.titleMedium
        IconSize.MEDIUM -> MaterialTheme.typography.headlineMedium
        IconSize.LARGE -> MaterialTheme.typography.displaySmall
    }

    val imageModifier = when (size) {
        IconSize.SMALL -> modifier.size(32.dp)
        IconSize.MEDIUM -> modifier.size(48.dp)
        IconSize.LARGE -> modifier.size(80.dp)
    }

    val imageBitmap = imageBase64?.let { rememberRecipeImageBitmap(it) }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "菜谱图片",
            modifier = imageModifier.clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        PText(
            text = emoji,
            style = textStyle,
            modifier = modifier
        )
    }
}

@Composable
expect fun rememberRecipeImageBitmap(imageBase64: String): ImageBitmap?

enum class IconSize {
    SMALL, MEDIUM, LARGE
}
