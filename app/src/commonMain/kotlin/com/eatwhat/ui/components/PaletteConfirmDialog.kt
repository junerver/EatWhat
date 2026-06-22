package com.eatwhat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.dialog.DialogDefaults
import xyz.junerver.compose.palette.components.dialog.PDialog
import xyz.junerver.compose.palette.components.dialog.PDialogActionDivider
import xyz.junerver.compose.palette.components.dialog.PDialogCancelAction
import xyz.junerver.compose.palette.components.dialog.PDialogConfirmAction
import xyz.junerver.compose.palette.components.text.PText

@Composable
fun PaletteConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String = "取消",
    confirmType: ButtonType = ButtonType.PRIMARY,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    PDialog(
        modifier = modifier,
        onDismiss = onDismiss,
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(32.dp)
                    )
                }
                PText(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        content = {
            PText(
                text = message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        actions = {
            PDialogCancelAction(
                text = cancelText,
                onClick = onDismiss
            )
            PDialogActionDivider()
            PDialogConfirmAction(
                text = confirmText,
                color = when (confirmType) {
                    ButtonType.DANGER -> MaterialTheme.colorScheme.error
                    ButtonType.PLAIN -> MaterialTheme.colorScheme.onSurfaceVariant
                    ButtonType.PRIMARY,
                    ButtonType.OUTLINED -> DialogDefaults.okColor()
                },
                onClick = onConfirm
            )
        }
    )
}
