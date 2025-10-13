package com.trueedu.spac.ui.following

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
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
    val followingManager: FollowingManager,
    val stockPool: StockPool,
) : ViewModel() {

    val loading = mutableStateOf(true)
    val currentPage = mutableStateOf<Int?>(null)

    val editMode = mutableStateOf(false)

    /**
     * 실시간 가격을 받기 전까지 사용함
     */
    val basePrices = mutableStateMapOf<String, PriceResponse?>()

    private var lifecycleJob: Job? = null
    private var isStarted = false

    var job: Job? = null

    fun pageCount() = FollowingManager.MAX_GROUP_SIZE

    fun onStart() {
        if (isStarted) return
        isStarted = true

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
        if (!isStarted) return
        isStarted = false

        job?.cancel()
        job = null
    }

    fun observeLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleJob?.cancel()

        lifecycleJob = viewModelScope.launch {
            lifecycleOwner.lifecycle.currentStateFlow
                .collect { state ->
                    when (state) {
                        Lifecycle.State.STARTED, Lifecycle.State.RESUMED -> {
                            logD("Lifecycle STARTED/RESUMED: onStart()")
                            onStart()
                        }
                        Lifecycle.State.CREATED -> {
                            logD("Lifecycle CREATED: onStop()")
                            onStop()
                        }
                        Lifecycle.State.DESTROYED -> {
                            logD("Lifecycle DESTROYED: onStop()")
                            onStop()
                        }
                        else -> {}
                    }
                }
        }
    }

    fun groupName(page: Int?): String {
        if (page == null) return "관심 그룹"
        return followingManager.groupNames.value.getOrNull(page) ?: "관심 그룹 $page"
    }

    fun getItems(index: Int): List<StockInfo> {
        return followingManager.get(index)
            .mapNotNull { stockPool.get(it) }
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

    fun updateGroupName(page: Int, name: String) {
        followingManager.updateGroupName(page, name)
    }

    fun updateStocks(page: Int, stockIds: List<String>) {
        followingManager.replace(page, stockIds)
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

    override fun onCleared() {
        super.onCleared()
        lifecycleJob?.cancel()
        job?.cancel()
    }
}
