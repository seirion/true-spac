package com.trueedu.spac.ui.edit.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.components.TrueText

@Preview(showBackground = true)
@Composable
fun BottomBar(
    text: String = "확인",
    buttonEnabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 16.dp)
            .height(56.dp)
            .padding(horizontal = 24.dp),
        enabled = buttonEnabled,
    ) {
        val buttonColor = if (buttonEnabled) {
            MaterialTheme.colorScheme.inversePrimary
        } else {
            MaterialTheme.colorScheme.surface
        }
        TrueText(s = text, fontSize = 20, color = buttonColor)
    }
}
