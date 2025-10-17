package com.trueedu.spac.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.trueedu.spac.ui.components.TrueText

@Composable
fun ActionDialog(
    title: String,
    description: String,
    confirmText: String = "Delete",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            TrueText(
                s = title,
                fontSize = 17,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.primary,
                maxLines = Int.MAX_VALUE,
            )
        },
        text = {
            TrueText(
                s = description,
                fontSize = 13,
                fontWeight = FontWeight.W400,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = Int.MAX_VALUE,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                TrueText(
                    s = confirmText,
                    fontSize = 17,
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                TrueText(
                    s = dismissText,
                    fontSize = 17,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    )
}

@Preview(showBackground = true)
@Composable
private fun ActionDialogPreview() {
    ActionDialog(
        title = "Delete account",
        description = "Are you sure you want to delete account?",
        confirmText = "Delete",
        dismissText = "Cancel",
    )
}
