package com.eatwhat.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.palette.components.loading.PLoading

/**
 * 兼容旧调用点的 Palette 加载指示器。
 */
@Composable
fun SimpleCircularProgressIndicator(
  modifier: Modifier = Modifier,
  color: Color = Color.Blue,
  strokeWidth: Dp = 4.dp
) {
  Box(modifier = modifier) {
    PLoading(
      size = 48.dp,
      color = color
    )
  }
}
