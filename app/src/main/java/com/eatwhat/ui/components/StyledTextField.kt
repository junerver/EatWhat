package com.eatwhat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatwhat.ui.theme.InputBackground
import com.eatwhat.ui.theme.LocalDarkTheme
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useState

/**
 * 统一的文本输入框组件
 * 支持前置图标、后置图标、密码模式、键盘类型等
 */
@Composable
fun StyledTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  placeholder: String = "",
  isPassword: Boolean = false,
  modifier: Modifier = Modifier,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  keyboardType: KeyboardType = KeyboardType.Text,
  backgroundColor: Color = Color.Unspecified,
  textColor: Color = Color.Unspecified,
  placeholderColor: Color = Color.Unspecified,
  minLines: Int = 1
) {
  val isDark = LocalDarkTheme.current
  val focusManager = LocalFocusManager.current
  val focusRequester by useCreation { FocusRequester() }

  // Monitor keyboard state with real-time detection
  val density = LocalDensity.current
  val imeInsets = WindowInsets.ime
  var isKeyboardVisible by useState(false)

  // Track keyboard visibility changes and clear focus when keyboard is dismissed
  LaunchedEffect(Unit) {
    snapshotFlow {
      imeInsets.getBottom(density)
    }.collect { imeBottom ->
      val newState = imeBottom > 0
      if (isKeyboardVisible && !newState) {
        // Keyboard was visible and now hidden
        focusManager.clearFocus()
      }
      isKeyboardVisible = newState
    }
  }

  val effectiveBackgroundColor = if (backgroundColor != Color.Unspecified) {
    backgroundColor
  } else {
    if (isDark) MaterialTheme.colorScheme.surfaceVariant else InputBackground
  }

  val effectiveTextColor = if (textColor != Color.Unspecified) {
    textColor
  } else {
    MaterialTheme.colorScheme.onSurface
  }

  val effectivePlaceholderColor = if (placeholderColor != Color.Unspecified) {
    placeholderColor
  } else {
    if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
  }

  Column(modifier = modifier) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = effectivePlaceholderColor,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = effectiveBackgroundColor
    ) {
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
          fontSize = 16.sp,
          color = effectiveTextColor
        ),
        modifier = Modifier.focusRequester(focusRequester),
        singleLine = minLines == 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(effectiveTextColor),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        decorationBox = { innerTextField ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Leading icon (前置图标)
            leadingIcon?.invoke()

            // Text field content
            Box(modifier = Modifier.weight(1f)) {
              if (value.isEmpty()) {
                Text(
                  text = placeholder,
                  color = effectivePlaceholderColor.copy(alpha = 0.5f),
                  fontSize = 16.sp
                )
              }
              innerTextField()
            }

            // Clear button (当有内容且没有 trailing icon 时显示)
            if (value.isNotEmpty() && trailingIcon == null) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear",
                tint = effectivePlaceholderColor,
                modifier = Modifier
                  .size(20.dp)
                  .clickable { onValueChange("") }
              )
            }

            // Trailing icon (后置图标，优先级高于 Clear 按钮)
            trailingIcon?.invoke()
          }
        }
      )
    }
  }
}