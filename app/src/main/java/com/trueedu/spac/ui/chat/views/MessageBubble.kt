package com.trueedu.spac.ui.chat.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.trueedu.spac.api.model.dto.firebase.ChatMessage
import com.trueedu.spac.ui.components.TrueText

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.isUser

    val background = when {
        isUser -> MaterialTheme.colorScheme.primaryContainer
        message.isError -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        isUser -> MaterialTheme.colorScheme.onPrimaryContainer
        message.isError -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        TrueText(
            s = if (message.isPending) "생각 중…" else message.text,
            fontSize = 14,
            color = textColor,
            maxLines = Int.MAX_VALUE,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(background)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}
