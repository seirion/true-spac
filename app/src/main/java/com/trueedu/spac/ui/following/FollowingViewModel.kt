package com.trueedu.spac.ui.following

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.price.PriceResponse
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.stocks.FollowingManager
import com.trueedu.spac.data.stocks.StockPool
import com.trueedu.spac.data.user.UserCycle
import com.trueedu.spac.util.formatter.safeDouble
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowingViewModel @Inject constructor(
    private val trueAnalytics: TrueAnalytics,
    private val userCycle: UserCycle,
    private val followingManager: FollowingManager,
    val stockPool: StockPool,
) : ViewModel() {

    val loading = mutableStateOf(true)
    val currentPage = mutableStateOf<Int?>(null)

    /**
     * 실시간 가격을 받기 전까지 사용함
     */
    val basePrices = mutableStateMapOf<String, PriceResponse?>()

    var job: Job? = null

    fun pageCount() = FollowingManager.MAX_GROUP_SIZE

    fun init() {
        if (!userCycle.loggedIn()) {
            loading.value = false
            return
        }

        job = viewModelScope.launch {
            launch {
                combine(
                    snapshotFlow { followingManager.list.value },
                    snapshotFlow { currentPage.value }
                ) { list, page ->
                    if (list.isEmpty() || page == null) {
                        emptyList()
                    } else {
                        list[page]
                    }
                }
                    .distinctUntilChanged()
                    .collect { list ->
                        if (list.isEmpty()) return@collect
                        logD("followingManager: $list")
                        loading.value = false
                    }
            }
        }
    }

    fun onStop() {
        job?.cancel()
        job = null
    }

    fun groupName(page: Int?): String {
        if (page == null) return "관심 그룹"
        return followingManager.groupNames.value.getOrNull(page) ?: "관심 그룹 $page"
    }

    fun getItems(index: Int): List<String> {
        return followingManager.get(index)
    }

    fun getStock(code: String): StockInfo? {
        return stockPool.get(code)
    }

    /**
     * 관심 종목을 다른 그룹으로 이동하기
     */
    fun moveTo(index: Int, to: Int) {
        trueAnalytics.clickButton("following__move__click")
        if (currentPage.value != null) {
            val code = followingManager.get(currentPage.value!!).getOrNull(index) ?: return
            followingManager.removeAt(currentPage.value!!, index)
            followingManager.add(to, code)
        }
    }

    fun removeStock(index: Int) {
        trueAnalytics.clickButton("following__remove__click")
        if (currentPage.value != null) {
            followingManager.removeAt(currentPage.value!!, index)
        }
    }

    fun hasDisclosure(code: String): Boolean {
        // TODO
        //return dartManager.hasDisclosure(code)
        return false
    }

    // 현재 페이지의 관심 종목에 대해서 실시간 가격 요청하기
    private fun requestRealtimePrice() {
        if (loading.value || currentPage.value == null) return
        // TODO
    }

    private fun cancelRealtimePrice() {
        // TODO
    }

    fun prevPrice(code: String): Double {
        return getStock(code)?.prevPrice.safeDouble()
    }
}
