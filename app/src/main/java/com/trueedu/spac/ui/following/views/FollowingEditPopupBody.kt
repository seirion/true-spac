package com.trueedu.spac.ui.following.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.ui.common.DashDividerHorizontal
import com.trueedu.spac.ui.common.DividerHorizontal
import com.trueedu.spac.ui.common.Margin
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.following.FollowingViewModel

@Composable
fun FollowingEditPopupBody(
    vm: FollowingViewModel,
    item: StockInfo,
    page: Int,
    index: Int,
    moveTo: (Int, Int) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .background(
                MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        TrueText(
            s = "${item.nameKr} 이동",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
        )
        Margin(8)
        DividerHorizontal()
        Column(modifier = Modifier.fillMaxWidth()) {
            repeat(vm.pageCount()) {
                TrueText(
                    s = vm.groupName(it),
                    fontSize = 16,
                    color = MaterialTheme.colorScheme.primary
                        .copy(alpha = if (it == page) 0.1f else 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (it != page) {
                                moveTo(index, it)
                            }
                        }
                        .padding(vertical = 8.dp),
                )
                DashDividerHorizontal()
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRemove() }
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TrueText(
                s = "관심종목에서 삭제",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        Margin(8)
    }
}
