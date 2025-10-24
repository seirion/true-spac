package com.trueedu.spac.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.trueedu.spac.ui.components.TrueText

@Composable
fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    SettingDialog(
        title = "계정 탈퇴 및 데이터 삭제",
        description = "모든 데이터를 삭제합니다. 삭제된 데이터는 복구할 수 없습니다",
        confirmText = "계정 삭제",
        dismissText = "취소",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Preview(showBackground = true)
@Composable
fun SettingDialog(
    title: String = "Delete account",
    description: String = "Are you sure you want to delete account?",
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
                fontSize = 15,
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
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    )
}
