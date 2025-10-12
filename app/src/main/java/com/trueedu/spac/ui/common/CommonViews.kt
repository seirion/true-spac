package com.trueedu.spac.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Margin(space: Int) {
    Spacer(modifier = Modifier.size(space.dp))
}
