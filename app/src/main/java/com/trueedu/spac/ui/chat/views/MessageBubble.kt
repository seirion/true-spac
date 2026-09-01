package com.trueedu.spac.ui.chat.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

    val bubbleModifier = Modifier
        .widthIn(max = 300.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(background)
        .padding(horizontal = 12.dp, vertical = 8.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        if (message.isThinking) {
            TrueText(
                s = "생각 중…",
                fontSize = 14,
                color = textColor,
                maxLines = Int.MAX_VALUE,
                modifier = bubbleModifier,
            )
        } else if (isUser) {
            // 사용자 입력은 원문 그대로 보여준다 — 마크다운으로 다시 해석하지 않는다.
            TrueText(
                s = message.text,
                fontSize = 14,
                color = textColor,
                maxLines = Int.MAX_VALUE,
                modifier = bubbleModifier,
            )
        } else {
            MarkdownMessageText(
                text = message.text,
                color = textColor,
                modifier = bubbleModifier,
            )
        }
    }

        // 무엇을 조회해서 나온 답인지 밝힌다. 답변보다 먼저 채워지므로
        // 생성 중에도 보인다.
        if (!isUser && message.sources.isNotEmpty()) {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                message.sources.forEach { source ->
                    TrueText(
                        s = "🔎 $source",
                        fontSize = 11,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        maxLines = Int.MAX_VALUE,
                    )
                }
            }
        }
    }
}
