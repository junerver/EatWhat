package com.eatwhat.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.text.PText
import xyz.junerver.compose.palette.components.textfield.BorderTextField

/**
 * 项目输入框兼容层，底层使用 Palette BorderTextField。
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
  val focusManager = LocalFocusManager.current
  val density = LocalDensity.current
  val imeInsets = WindowInsets.ime
  var isKeyboardVisible by useState(false)

  LaunchedEffect(Unit) {
    snapshotFlow { imeInsets.getBottom(density) }.collect { imeBottom ->
      val newState = imeBottom > 0
      if (isKeyboardVisible && !newState) {
        focusManager.clearFocus()
      }
      isKeyboardVisible = newState
    }
  }

  val labelColor = if (placeholderColor != Color.Unspecified) {
    placeholderColor
  } else {
    MaterialTheme.colorScheme.onSurfaceVariant
  }

  Column(modifier = modifier) {
    PText(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = labelColor,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
    BorderTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = placeholder,
      leadingIcon = leadingIcon,
      trailingIcon = trailingIcon,
      clearable = true,
      singleLine = minLines == 1,
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
      visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )
  }
}
