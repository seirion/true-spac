package com.trueedu.spac.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.api.model.dto.firebase.SpacRefund
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.firebase.UserAsset
import com.trueedu.spac.data.stocks.DartManager
import com.trueedu.spac.data.stocks.FollowingManager
import com.trueedu.spac.data.stocks.PriceManager
import com.trueedu.spac.data.stocks.SpacManager
import com.trueedu.spac.data.stocks.StockPool
import com.trueedu.spac.data.user.ManualAssets
import com.trueedu.spac.data.user.UserCycle
import com.trueedu.spac.ui.stock.views.GroupSelectMode
import com.trueedu.spac.util.formatter.dateFormat
import com.trueedu.spac.util.formatter.numberFormatString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDialogState(
    val mode: GroupSelectMode,
    val availableGroups: List<Int>
)

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val trueAnalytics: TrueAnalytics,
    private val userCycle: UserCycle,
    val followingManager: FollowingManager,
    val manualAssets: ManualAssets,
    private val stockPool: StockPool,
    private val spacManager: SpacManager,
    private val priceManager: PriceManager,
    val dartManager: DartManager,
) : ViewModel() {

    private val _spacRefund = MutableStateFlow<SpacRefund?>(null)
    val spacRefund: StateFlow<SpacRefund?> = _spacRefund.asStateFlow()

    private val _stockInfo = MutableStateFlow<StockInfo?>(null)
    val stockInfo: StateFlow<StockInfo?> = _stockInfo.asStateFlow()

    private val _infoList = MutableStateFlow<List<Pair<String, String?>>>(emptyList())
    val infoList: StateFlow<List<Pair<String, String?>>> = _infoList.asStateFlow()

    // 실시간 가격
    private val _currentPrice = MutableStateFlow<Double?>(null)
    val currentPrice: StateFlow<Double?> = _currentPrice.asStateFlow()

    // 가격 변동
    private val _priceChange = MutableStateFlow<Double?>(null)
    val priceChange: StateFlow<Double?> = _priceChange.asStateFlow()

    // 가격 변동률
    private val _priceChangeRate = MutableStateFlow<Double?>(null)
    val priceChangeRate: StateFlow<Double?> = _priceChangeRate.asStateFlow()

    // 가격 업데이트 수신 Job
    private var priceUpdateJob: Job? = null

    // 관심 그룹 페이지
    private var followingGroupPage: Int? = null

    // 그룹 선택 다이얼로그 표시 여부
    private val _showGroupSelectDialog = MutableStateFlow<GroupDialogState?>(null)
    val showGroupSelectDialog: StateFlow<GroupDialogState?> = _showGroupSelectDialog.asStateFlow()

    fun init(code: String, followingGroupPage: Int? = null) {
        this.followingGroupPage = followingGroupPage
        _stockInfo.value = stockPool.get(code)
        initInfoList()

        // 실시간 가격 업데이트
        updatePriceInfo(code)

        viewModelScope.launch {
            val stockInfo = _stockInfo.value
            if (stockInfo?.isSpac == true) {
                _spacRefund.value = spacManager.spacRefundMap.value[code]
            }
        }

        // 기존 가격 업데이트 Job이 있으면 취소
        priceUpdateJob?.cancel()
        // PriceManager의 가격 업데이트를 수신
        priceUpdateJob = viewModelScope.launch {
            priceManager.priceUpdated.collect {
                updatePriceInfo(code)
            }
        }
    }

    private fun updatePriceInfo(code: String) {
        _currentPrice.value = priceManager.price(code)
        _priceChange.value = priceManager.priceChange(code)
        _priceChangeRate.value = priceManager.priceChangeRate(code)
    }

    fun loggedIn() = userCycle.loggedIn()

    fun isFollowing(): Boolean {
        val code = _stockInfo.value?.code ?: return false
        return if (followingGroupPage != null) {
            followingManager.contains(followingGroupPage!!, code)
        } else {
            followingManager.contains(code)
        }
    }

    fun toggleFollowing() {
        val code = _stockInfo.value?.code ?: return
        val page = followingGroupPage

        if (page != null) {
            if (followingManager.contains(page, code)) {
                followingManager.remove(page, code)
            } else {
                followingManager.add(page, code)
            }
        } else {
            if (followingManager.contains(code)) {
                // 삭제 - 어느 그룹에서 삭제할지 선택
                showGroupSelectDialogForRemove()
            } else {
                // 추가 - 어느 그룹에 추가할지 선택
                showGroupSelectDialogForAdd()
            }
        }
    }

    private fun showGroupSelectDialogForAdd() {
        val availableGroups = (0 until FollowingManager.MAX_GROUP_SIZE).toList()
        _showGroupSelectDialog.value = GroupDialogState(
            mode = GroupSelectMode.ADD,
            availableGroups = availableGroups
        )
    }

    private fun showGroupSelectDialogForRemove() {
        val code = _stockInfo.value?.code ?: return
        // 해당 종목이 포함된 그룹만 표시
        val availableGroups = (0 until FollowingManager.MAX_GROUP_SIZE)
            .filter { followingManager.contains(it, code) }

        if (availableGroups.isEmpty()) {
            // 이론적으로는 isFollowing()이 true일 때만 호출되므로 발생하지 않아야 함
            return
        }

        _showGroupSelectDialog.value = GroupDialogState(
            mode = GroupSelectMode.REMOVE,
            availableGroups = availableGroups
        )
    }

    fun dismissGroupSelectDialog() {
        _showGroupSelectDialog.value = null
    }

    fun addToGroup(groupIndex: Int) {
        val code = _stockInfo.value?.code ?: return
        followingManager.add(groupIndex, code)
        dismissGroupSelectDialog()
    }

    fun removeFromGroup(groupIndex: Int) {
        val code = _stockInfo.value?.code ?: return
        followingManager.remove(groupIndex, code)
        dismissGroupSelectDialog()
    }

    fun getGroupNames(): List<String?> {
        return followingManager.groupNames.value
    }

    fun initInfoList() {
        val stockInfo = _stockInfo.value ?: return
        _infoList.value = listOf(
            "전일가격" to numberFormatString(stockInfo.prevPrice) + "원",
            "전일거래량" to numberFormatString(stockInfo.prevVolume),
            "시가총액" to numberFormatString(stockInfo.marketCap) + "억",
            "상장일자" to dateFormat(stockInfo.listingDate),
//            "상장주수" to numberFormatString(stockInfo.listingShares) + "K",
        )
    }

    fun getManualAsset(): UserAsset? {
        val code = _stockInfo.value?.code ?: return null
        return manualAssets.assets.value.firstOrNull { it.code == code }
    }
}
