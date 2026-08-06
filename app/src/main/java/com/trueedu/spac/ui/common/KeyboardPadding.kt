package com.trueedu.spac.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt

/**
 * 키보드와 실제로 겹치는 만큼만 하단 패딩을 준다.
 *
 * 각 화면은 하단 탭 바(NavigationSuiteScaffold)만큼 창 하단보다 위에서 끝나는데,
 * imePadding()은 창 하단 기준으로 패딩을 주기 때문에 그대로 쓰면 탭 바 높이만큼 빈 공간이 남는다.
 * 이 modifier는 화면 하단과 창 하단 사이의 간격을 재서 그만큼을 뺀 값을 패딩으로 사용한다.
 *
 * 위치 측정은 패딩이 적용되기 전 노드에서 하므로(패딩을 줘도 크기가 변하지 않는다)
 * 패딩과 측정값이 서로를 바꾸는 일은 없다. 단, 높이가 고정되도록 fillMaxSize() 등의 뒤에 붙여야 한다.
 */
@Composable
fun Modifier.keyboardOverlapPadding(): Modifier {
    val density = LocalDensity.current
    var spaceBelowScreen by remember { mutableIntStateOf(0) }
    val imeHeight = WindowInsets.ime.getBottom(density)
    val overlap = with(density) { (imeHeight - spaceBelowScreen).coerceAtLeast(0).toDp() }

    return this
        .onGloballyPositioned {
            val screenBottom = it.positionInRoot().y + it.size.height
            spaceBelowScreen = (it.findRootCoordinates().size.height - screenBottom).roundToInt()
        }
        .padding(bottom = overlap)
}
