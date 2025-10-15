package com.trueedu.spac.ui.stock.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trueedu.spac.api.model.dto.firebase.SpacStatus
import com.trueedu.spac.ui.common.DividerHorizontal
import com.trueedu.spac.ui.components.TrueText

@Composable
fun SpacStatusView(status: SpacStatus.Status?) {

    val text = when (status) {
        SpacStatus.Status.MERGER_REVIEW -> status.description
        SpacStatus.Status.MERGER_APPROVED -> status.description
        else -> null
    }

    if (text == null) return

    Box(
        modifier = Modifier
            .height(56.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceDim,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center,
    ) {
        TrueText(
            s = text,
            fontSize = 14,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(12.dp, 0.dp)
        )
    }
    DividerHorizontal()
}
