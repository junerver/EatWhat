package com.eatwhat.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.eatwhat.util.ImageUtils
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.loading.PLoading
import xyz.junerver.compose.palette.components.segmented.PSegmented
import xyz.junerver.compose.palette.components.segmented.SegmentedOption
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagColors
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.tag.TagVariant
import xyz.junerver.compose.palette.components.text.PText

/**
 * Recipe icon picker component
 * Allows user to select an emoji or upload a custom image
 *
 * @param selectedEmoji Currently selected emoji (used when no image is set)
 * @param selectedImageBase64 Currently selected image in Base64 format
 * @param recipeType Current recipe type for emoji suggestions
 * @param onEmojiSelected Callback when an emoji is selected
 * @param onImageSelected Callback when an image is selected (Base64 encoded)
 * @param onImageCleared Callback when the image is cleared
 * @param modifier Modifier for the component
 */
@Composable
fun RecipeIconPicker(
    selectedEmoji: String,
    selectedImageBase64: String?,
    recipeType: String = "MEAT",
    onEmojiSelected: (String) -> Unit,
    onImageSelected: (String) -> Unit,
    onImageCleared: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
  var showEmojiPicker by useState(false)
  var isProcessingImage by useState(false)
  var errorMessage by _useState<String?>(null)

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isProcessingImage = true
            errorMessage = null

            // Process image in background
            val result = ImageUtils.processImageToBase64(context, it)
            when (result) {
                is ImageUtils.ImageProcessingResult.Success -> {
                    onImageSelected(result.base64)
                }
                is ImageUtils.ImageProcessingResult.Error -> {
                    errorMessage = result.message
                }
            }
            isProcessingImage = false
        }
    }

    Column(modifier = modifier) {
        PText(
            text = "菜谱图标",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current icon preview
            IconPreview(
                emoji = selectedEmoji,
                imageBase64 = selectedImageBase64,
                isLoading = isProcessingImage,
                modifier = Modifier.size(80.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Upload image button
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessingImage
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    PText(if (selectedImageBase64 != null) "更换图片" else "上传图片")
                }

                // Emoji picker button or clear image button
                if (selectedImageBase64 != null) {
                    OutlinedButton(
                        onClick = onImageCleared,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        PText("清除图片")
                    }
                } else {
                    OutlinedButton(
                        onClick = { showEmojiPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.EmojiEmotions,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        PText("选择图标")
                    }
                }
            }
        }

        // Error message
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            PText(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Suggested emojis row
        if (selectedImageBase64 == null) {
            Spacer(modifier = Modifier.height(12.dp))
            PText(
                text = "推荐图标",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = FoodEmojis.getSuggestionsForType(recipeType)
                items(suggestions) { emoji ->
                    EmojiChip(
                        emoji = emoji,
                        isSelected = emoji == selectedEmoji,
                        onClick = { onEmojiSelected(emoji) }
                    )
                }
            }
        }
    }

    // Emoji picker dialog
    if (showEmojiPicker) {
        EmojiPickerDialog(
            selectedEmoji = selectedEmoji,
            onEmojiSelected = {
                onEmojiSelected(it)
                showEmojiPicker = false
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}

/**
 * Preview of the current icon (emoji or image)
 */
@Composable
private fun IconPreview(
    emoji: String,
    imageBase64: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp)
          ),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                PLoading(size = 32.dp)
            }
            imageBase64 != null -> {
                val bitmap = remember(imageBase64) {
                    try {
                        val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "菜谱图片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: PText(
                    text = emoji,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            else -> {
                PText(
                    text = emoji,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}

/**
 * Small emoji chip for suggestions
 */
@Composable
private fun EmojiChip(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        PTag(
            text = emoji,
            size = TagSize.Large,
            variant = if (isSelected) TagVariant.Outlined else TagVariant.Default,
            onClick = onClick,
            colors = emojiTagColors(isSelected)
        )
    }
}

/**
 * Full emoji picker dialog with categories
 */
@Composable
private fun EmojiPickerDialog(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
  var selectedCategory by useState(FoodEmojis.categories.first())

    Dialog(onDismissRequest = onDismiss) {
        PCard(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PText(
                        text = "选择图标",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category tabs
                Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState())
                ) {
                    PSegmented(
                        options = FoodEmojis.categories.map { category ->
                            SegmentedOption(
                                value = category.name,
                                label = category.name
                            )
                        },
                        value = selectedCategory.name,
                        onValueChange = { selectedName ->
                            selectedCategory = FoodEmojis.categories.first { it.name == selectedName }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Emoji grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                      .fillMaxWidth()
                      .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(selectedCategory.emojis) { emoji ->
                        EmojiGridItem(
                            emoji = emoji,
                            isSelected = emoji == selectedEmoji,
                            onClick = { onEmojiSelected(emoji) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Grid item for emoji in the picker dialog
 */
@Composable
private fun EmojiGridItem(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
          .aspectRatio(1f)
          .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        PTag(
            text = emoji,
            size = TagSize.Large,
            variant = if (isSelected) TagVariant.Outlined else TagVariant.Default,
            onClick = onClick,
            colors = emojiTagColors(isSelected)
        )
    }
}

@Composable
private fun emojiTagColors(isSelected: Boolean): TagColors {
    return TagColors(
        containerColor = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        borderColor = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        }
    )
}

