package com.trueedu.spac.ui.edit.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.home.views.defaultTextColors

@Composable
fun MemoInput(s: MutableState<String>) {
    OutlinedTextField(
        value = s.value,
        onValueChange = {
            s.value = it.take(128)
        },
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 40.dp),
        label = {
            TrueText(
                s = "메모(최대 128자)",
                fontSize = 14,
                color = MaterialTheme.colorScheme.secondary
            )
        },
        maxLines = 8,
        colors = defaultTextColors(),
    )
}
