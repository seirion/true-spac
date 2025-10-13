package com.trueedu.spac.ui.components

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.trueedu.spac.ui.common.Margin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun <T : Any> dragDropColumn(
    items: List<T>,
    onSwap: (Int, Int) -> Boolean,
    modifier: Modifier = Modifier,
    keyValue: (item: T, position: Int) -> String,
    onEditMoveCallback: (Boolean) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    itemContent: @Composable (item: T, position: Int) -> Unit
): ItemDrags {

    val listState: LazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val itemDragState by remember {
        mutableStateOf(ItemDrags(listState, scope) { from, to -> onSwap(from, to) })
    }

    Scaffold(
        modifier = modifier.onSizeChanged { itemDragState.canvasSize.value = it.height },
        containerColor = MaterialTheme.colorScheme.background,
    ) { _ ->
        CompositionLocalProvider(
            LocalOverscrollConfiguration provides null
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                    //.padding(innerPadding),
                state = listState,
                contentPadding = contentPadding,
            ) {
                item {
                    Box( // 맨 처음 허상으로 넣어서, 아이템을 화면상의 첫 아이템으로 교체할 때, 말려올라가지 않도록 방어함.
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                    )
                }
                items(items.size, key = { position ->
                    try {
                        if (position < items.size) keyValue.invoke(items[position], position)
                        else "$position"
                    } catch (out: Throwable) {
                        "$position"
                    }
                }) { position ->
                    if (position < items.size) {
                        val item = items[position]
                        Card(
                            modifier = Modifier
                                .animateItem()
                                .alpha(if (itemDragState.currentIndex.value == position) 0f else 1f),
                            shape = RectangleShape,
                            colors = CardColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = Color.Transparent,
                            ),
                        ) {
                            itemContent(item, position)
                        }
                    } else Margin(0)
                }

                if (listState.isScrollInProgress) {
                    if (itemDragState.draggingYOffset.value < -1 && itemDragState.dragFlag.value) {
                        itemDragState.upsideMove()
                    } else if (itemDragState.dragFlag.value && itemDragState.draggingYOffset.value + itemDragState.cardSize.value > itemDragState.canvasSize.value) {
                        itemDragState.checkingHoverItem(itemDragState.draggingYOffset.value)
                    }
                }
            }
        }

        // 편집모드에서는 핸들링 영역의 터치를 가장 우선 처리함.
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier
                .fillMaxHeight()
                .width(60.dp) // 오른쪽 핸들링 영역으로 컨트롤
                .align(Alignment.TopEnd)
                .pointerInput(itemDragState) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            itemDragState.initStartPosition(offset.y)
                            onEditMoveCallback.invoke(false)
                        },
                        onDrag = { _, dragAmount -> itemDragState.dragging(dragAmount.y) },
                        onDragCancel = {
                            itemDragState.dragOff()
                        },
                        onDragEnd = {
                            itemDragState.dragOff()
                            onEditMoveCallback.invoke(true)
                        }
                    )
                }
                .pointerInteropFilter {
                    if (it.action == MotionEvent.ACTION_DOWN) {
                        itemDragState.initStartPosition(it.y)
                    } else if (it.action == MotionEvent.ACTION_UP) {
                        itemDragState.dragOff()
                    }
                    true
                }
            ) { /*ignore*/ }
        }

        if (itemDragState.dragFlag.value && itemDragState.currentIndex.value < items.size) {
            Card(
                modifier = Modifier
                    .zIndex(1f)
                    .graphicsLayer {
                        translationY = itemDragState.draggingYOffset.value.takeIf { it >= 0 } ?: 0f
                    }
                    .onSizeChanged { itemDragState.cardSize.value = it.height },
                shape = RectangleShape,
            ) {
                itemContent.invoke(
                    items[itemDragState.currentIndex.value],
                    itemDragState.currentIndex.value
                )
            }
        }

    }
    return itemDragState
}

class ItemDrags(
    val state: LazyListState,
    private val scope: CoroutineScope,
    val swap: (Int, Int) -> Boolean,
) {
    private var startIndex = mutableStateOf(-1)
    var draggingYOffset = mutableStateOf(-1f)
    var currentIndex = mutableStateOf(-1)

    var dragFlag = mutableStateOf(false)

    val cardSize = mutableStateOf(200)
    val canvasSize = mutableStateOf(0)

    var canDrag: (Int) -> Boolean = { true }
    var checkCondition: (Int) -> Boolean = { true }
    var clickDragArea: (Int) -> Unit = {}

    fun initStartPosition(startOffset: Float): Boolean {
        val clickItem =
            state.layoutInfo.visibleItemsInfo.find { startOffset in it.offset.toFloat()..it.offsetEnd.toFloat() }
        clickItem?.let {
            if (canDrag.invoke(clickItem.index - 1)) {
                draggingYOffset.value = it.offset.toFloat()
                startIndex.value = clickItem.index - 1
                currentIndex.value = clickItem.index - 1

                dragFlag.value = true
            } else {
                clickDragArea.invoke(clickItem.index - 1)
                dragFlag.value = false
            }
        }
        return false
    }

    fun dragging(moveY: Float) {

        if (dragFlag.value) {
            draggingYOffset.value += moveY

            if (draggingYOffset.value < -1) {
                scope.launch {
                    while (draggingYOffset.value < -1f) state.animateScrollBy(
                        -cardSize.value.toFloat(),
                        tween(200, 0, LinearEasing)
                    )
                }
            } else if (draggingYOffset.value + cardSize.value > canvasSize.value) {
                draggingYOffset.value = canvasSize.value - cardSize.value + 15f
                scope.launch {
                    while (draggingYOffset.value + cardSize.value > canvasSize.value) state.animateScrollBy(
                        cardSize.value.toFloat(),
                        tween(200, 0, LinearEasing)
                    )
                }
            } else checkingHoverItem(draggingYOffset.value)
        }
    }

    fun dragOff() {
        draggingYOffset.value = -1f
        startIndex.value = -1
        currentIndex.value = -1
        dragFlag.value = false
    }

    fun upsideMove() {
        val dupPrev = state.layoutInfo.visibleItemsInfo.findLast { it.offset < 0f }
        if (dupPrev != null) {
            scope.launch {
                if (currentIndex.value > 0) {
                    swap(currentIndex.value, dupPrev.index)
                    currentIndex.value = dupPrev.index
                }
            }
        }

    }

    fun checkingHoverItem(hoverTopPosition: Float) {
        val lastItem = state.layoutInfo.visibleItemsInfo.last()

        val dupPrev = state.layoutInfo.visibleItemsInfo.findLast {
            hoverTopPosition in it.offset.toFloat()..((it.offset + it.offsetEnd) / 2f) && currentIndex.value >= it.index
        }

        val dupNext = state.layoutInfo.visibleItemsInfo.find {
            hoverTopPosition in ((it.offset + it.offsetEnd) / 2f)..it.offsetEnd.toFloat() && currentIndex.value + 1 <= it.index
        }

        val swapIfPossible = { targetIndex: Int ->
            if (checkCondition(targetIndex)) {
                swap(currentIndex.value, targetIndex)
                currentIndex.value = targetIndex
            }
        }

        if (dupPrev != null && dupPrev.index > 0) {
            swapIfPossible(dupPrev.index - 1)
        } else if (dupNext != null && currentIndex.value < dupNext.index && dupNext.index < lastItem.index) {
            swapIfPossible(dupNext.index)
        } else {
            // 최 하단으로 드래그 한 경우, 마지막 아이템 위치로 이동할 수 있는 지 체크 한다.
            if (hoverTopPosition > (lastItem.offset + lastItem.offsetEnd) / 2f) {
                swapIfPossible(lastItem.index - 1)
            }
        }
    }

    fun scrollToTop() {
        scope.launch {
            state.animateScrollToItem(0, 0)
        }
    }
}


val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size


fun <T> swap(arr: MutableList<T>, i: Int, j: Int) {
    if (i < arr.size && j < arr.size && i != j) {
        val t = arr[i]
        arr[i] = arr[j]
        arr[j] = t
    }
}

fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
    if (fromIndex == toIndex) return
    val item = removeAt(fromIndex)
    add(toIndex, item)
}
