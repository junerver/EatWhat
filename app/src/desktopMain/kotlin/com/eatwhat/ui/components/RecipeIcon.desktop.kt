package com.eatwhat.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.util.Base64
import org.jetbrains.skia.Image

@Composable
actual fun rememberRecipeImageBitmap(imageBase64: String): ImageBitmap? {
  return remember(imageBase64) {
    try {
      val bytes = Base64.getDecoder().decode(imageBase64)
      Image.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (e: Exception) {
      null
    }
  }
}
