package com.trueedu.spac.ui.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.user.UserCycle
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.worker.WorkerExecutionTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userCycle: UserCycle,
    private val local: Local,
    private val tracker: WorkerExecutionTracker,
) : ViewModel() {
    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    init {
        // Worker 실행 통계 로그 출력
        logWorkerStats()
    }

    fun email() = userCycle.email.value
    fun profileImageUrl() = userCycle.profileImageUrl.value

    // Worker 상태 조회 메서드
    fun isAdminMode(): Boolean = local.getUserKey().isValid()
    fun getLastMasterFileUpdate(): String = tracker.getLastMasterFileUpdate()
    fun getMasterFileExecutionCount(): Int = tracker.getMasterFileExecutionCount()
    fun getLastPriceUpdate(): String = tracker.getLastPriceUpdate()
    fun getPriceExecutionCount(): Int = tracker.getPriceExecutionCount()

    /**
     * Worker 통계 초기화
     */
    fun resetWorkerStats() {
        tracker.resetStats()
        logD("Worker 통계가 초기화되었습니다")
    }

    /**
     * Worker 실행 통계를 로그로 출력
     * 프로필 화면 진입 시 백그라운드 작업이 정상적으로 실행되었는지 확인 가능
     */
    private fun logWorkerStats() {
        logD(tracker.getExecutionStats())

        // 개별 정보도 출력
        logD("마스터 파일 - 마지막 실행: ${tracker.getLastMasterFileUpdate()}, 총 ${tracker.getMasterFileExecutionCount()}회")
        logD("시세 업데이트 - 마지막 실행: ${tracker.getLastPriceUpdate()}, 총 ${tracker.getPriceExecutionCount()}회")
    }
}
