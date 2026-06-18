package com.eatwhat.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

@Composable
actual fun rememberRecipeImageBitmap(imageBase64: String): ImageBitmap? {
  return remember(imageBase64) {
    try {
      val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
      BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (e: Exception) {
      null
    }
  }
}
