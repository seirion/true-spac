package com.trueedu.spac.ui.home.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.common.CustomTopBar
import com.trueedu.spac.ui.common.Margin
import com.trueedu.spac.ui.components.TouchIcon24
import com.trueedu.spac.ui.components.TouchIcon32
import com.trueedu.spac.ui.components.TrueText


@Preview(showBackground = true)
@Composable
fun FollowingEditTopBar(
    title: String = "타이틀",
    onBack: () -> Unit = {},
    onTitleChanged: (String) -> Unit = {},
) {
    var editMode by remember { mutableStateOf(false) }
    var titleEditable by remember(title) { mutableStateOf(title) }

    CustomTopBar(
        navigationIcon = {
            TouchIcon32(
                icon = Icons.Filled.ChevronLeft,
                onClick = {
                    editMode = false
                    onBack()
                },
            )
        },
        titleView = {
            if (editMode) {
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                TextField(
                    value = titleEditable,
                    onValueChange = {
                        titleEditable = it.take(12)
                    },
                    maxLines = 1,
                    textStyle = MaterialTheme.typography.titleMedium, // 16sp 크기의 텍스트 스타일 적용
                    colors = defaultTextColors()
                        .copy(
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                    modifier = Modifier.focusRequester(focusRequester),
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        editMode = true
                    }
                ) {
                    TrueText(
                        s = titleEditable,
                        fontSize = 20,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Margin(4)
                    Icon(
                        modifier = Modifier.size(12.dp),
                        imageVector = Icons.Filled.Edit,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "icon"
                    )
                }
            }
        },
        actionsView = {
            if (editMode) {
                TouchIcon24(
                    icon = Icons.Filled.Save,
                    onClick = {
                        val name = titleEditable.trim()
                        if (name.isNotEmpty()) {
                            editMode = false
                            onTitleChanged(name)
                        }
                    },
                )
            }
        }
    )
}

@Composable
fun defaultTextColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.primary,
        unfocusedTextColor = MaterialTheme.colorScheme.primary,
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    )
}
