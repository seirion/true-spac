package com.trueedu.spac.ui.chat.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.common.Margin
import com.trueedu.spac.ui.components.TrueText

/**
 * 질문을 보냈지만 아직 답변 말풍선이 생기지 않은 구간을 메운다.
 *
 * 답변은 별도 프로세스가 만들어 넣기 때문에, 그 프로세스가 질문을 집어가기 전까지는
 * 화면에 아무 변화가 없다. 그동안 전송이 실패한 것처럼 보이는 걸 막는다.
 */
@Composable
fun WaitingIndicator(delayed: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.Start) {
            TrueText(
                s = "생각 중…",
                fontSize = 14,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }

        if (delayed) {
            Margin(6)
            TrueText(
                s = "응답이 지연되고 있습니다. 잠시 후 다시 확인해 주세요.",
                fontSize = 12,
                color = MaterialTheme.colorScheme.surfaceVariant,
                maxLines = Int.MAX_VALUE,
            )
        }
    }
}
