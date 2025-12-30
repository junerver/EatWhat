package com.eatwhat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StyledTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  placeholder: String = "",
  isPassword: Boolean = false,
  modifier: Modifier = Modifier,
  trailingIcon: @Composable (() -> Unit)? = null,
  backgroundColor: Color = Color(0xFFF8F8F8),
  textColor: Color = Color.Black,
  placeholderColor: Color = Color.Gray
) {
  Column(modifier = modifier) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = placeholderColor,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = backgroundColor
    ) {
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
          fontSize = 16.sp,
          color = textColor
        ),
        singleLine = true,
        cursorBrush = SolidColor(textColor),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        decorationBox = { innerTextField ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(modifier = Modifier.weight(1f)) {
              if (value.isEmpty()) {
                Text(
                  text = placeholder,
                  color = placeholderColor.copy(alpha = 0.5f),
                  fontSize = 16.sp
                )
              }
              innerTextField()
            }

            if (value.isNotEmpty()) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear",
                tint = placeholderColor,
                modifier = Modifier
                  .size(20.dp)
                  .clickable { onValueChange("") }
              )
            }

            if (trailingIcon != null) {
              Spacer(modifier = Modifier.width(8.dp))
              trailingIcon()
            }
          }
        }
      )
    }
  }
}