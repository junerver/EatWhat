package com.eatwhat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import xyz.junerver.compose.palette.components.dialog.DialogDefaults
import xyz.junerver.compose.palette.components.text.PText

@Composable
fun RowScope.PaletteDialogAction(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .weight(1f)
            .height(DialogDefaults.buttonHeight())
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        PText(
            text = text,
            color = color,
            style = DialogDefaults.buttonTextStyle()
        )
    }
}

@Composable
fun RowScope.PaletteDialogCancelAction(
    onClick: () -> Unit,
    text: String = "取消"
) {
    PaletteDialogAction(
        text = text,
        color = DialogDefaults.cancelColor(),
        onClick = onClick
    )
}

@Composable
fun RowScope.PaletteDialogConfirmAction(
    text: String = "确定",
    color: Color? = null,
    onClick: () -> Unit
) {
    PaletteDialogAction(
        text = text,
        color = color ?: DialogDefaults.okColor(),
        onClick = onClick
    )
}

@Composable
fun RowScope.PaletteDialogActionDivider() {
    Box(
        modifier = Modifier
            .size(DialogDefaults.dividerWidth(), DialogDefaults.buttonHeight())
            .background(DialogDefaults.dividerColor())
    )
}
