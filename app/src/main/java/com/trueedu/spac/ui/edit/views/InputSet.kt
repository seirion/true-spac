package com.trueedu.spac.ui.edit.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.components.DigitInput
import com.trueedu.spac.ui.components.TouchIcon24
import com.trueedu.spac.ui.components.TrueText

@Preview(showBackground = true)
@Composable
fun InputSet(
    label: String = "가격",
    input: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue("1000")),
    increase: () -> Unit = {},
    decrease: () -> Unit = {},
) {
    Row {
        InputLabel(label)
        Spacer(modifier = Modifier.width(40.dp))
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        TouchIcon24(Icons.Outlined.RemoveCircleOutline, onClick = decrease)
        DigitInput(input, Modifier.weight(1f))
        TouchIcon24(Icons.Outlined.AddCircleOutline, onClick = increase)
    }
}

@Composable
fun InputLabel(label: String) {
    TrueText(
        s = label,
        fontSize = 14,
        color = MaterialTheme.colorScheme.secondary,
    )
}
