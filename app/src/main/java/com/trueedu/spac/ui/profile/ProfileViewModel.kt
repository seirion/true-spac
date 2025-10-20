package com.trueedu.spac.ui.profile

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.user.UserCycle
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.worker.PeriodicSyncWorker
import com.trueedu.spac.worker.StockPriceAlarmManager
import com.trueedu.spac.worker.StockPriceWorker
import com.trueedu.spac.worker.WorkerExecutionTracker
import com.trueedu.spac.worker.WorkManagerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userCycle: UserCycle,
    private val local: Local,
    private val tracker: WorkerExecutionTracker,
    private val stockPriceAlarmManager: StockPriceAlarmManager,
    private val workManagerHelper: WorkManagerHelper,
) : ViewModel() {
    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    // Worker 상태 State
    private val _lastMasterFileUpdate = mutableStateOf("")
    val lastMasterFileUpdate: State<String> = _lastMasterFileUpdate

    private val _masterFileExecutionCount = mutableStateOf(0)
    val masterFileExecutionCount: State<Int> = _masterFileExecutionCount

    private val _lastPriceUpdate = mutableStateOf("")
    val lastPriceUpdate: State<String> = _lastPriceUpdate

    private val _priceExecutionCount = mutableStateOf(0)
    val priceExecutionCount: State<Int> = _priceExecutionCount

    init {
        // 초기 데이터 로드
        refreshWorkerStats()
        // Worker 실행 통계 로그 출력
        logWorkerStats()
        // Worker 스케줄링 상태 확인
        checkWorkerSchedulingStatus()
    }

    fun email() = userCycle.email.value
    fun profileImageUrl() = userCycle.profileImageUrl.value

    // Worker 상태 조회 메서드
    fun isAdminMode(): Boolean = local.getUserKey().isValid()

    /**
     * Worker 통계 새로고침
     */
    fun refreshWorkerStats() {
        _lastMasterFileUpdate.value = tracker.getLastMasterFileUpdate()
        _masterFileExecutionCount.value = tracker.getMasterFileExecutionCount()
        _lastPriceUpdate.value = tracker.getLastPriceUpdate()
        _priceExecutionCount.value = tracker.getPriceExecutionCount()
        logD("🔄 Worker 통계 새로고침 완료")
    }

    /**
     * Worker 통계 초기화
     */
    fun resetWorkerStats() {
        tracker.resetStats()
        refreshWorkerStats() // 초기화 후 자동으로 새로고침
        logD("Worker 통계가 초기화되었습니다")
    }

    /**
     * 네트워크 연결이 필요한 Worker 작업을 위한 제약 조건 생성
     */
    private fun createNetworkConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * 시세 업데이트 Worker를 수동으로 실행 (테스트용)
     */
    fun manuallyTriggerPriceUpdate() {
        logD("수동으로 시세 업데이트 Worker 실행")

        val workRequest = OneTimeWorkRequestBuilder<StockPriceWorker>()
            .setConstraints(createNetworkConstraints())
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        logD("시세 업데이트 Worker가 큐에 추가되었습니다")
    }

    /**
     * 마스터 파일 업데이트 Worker를 수동으로 실행 (테스트용)
     */
    fun manuallyTriggerMasterFileUpdate() {
        logD("🧪 수동으로 마스터 파일 업데이트 Worker 실행")

        val workRequest = OneTimeWorkRequestBuilder<PeriodicSyncWorker>()
            .setConstraints(createNetworkConstraints())
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        logD("✅ 마스터 파일 업데이트 Worker가 큐에 추가되었습니다")
    }

    /**
     * 알람을 다시 시작 (테스트용 - 거래 시간 체크 우회)
     */
    fun restartStockPriceAlarm() {
        logD("🔧 시세 업데이트 알람 재시작 (거래 시간 체크 우회)")
        stockPriceAlarmManager.stopAlarm()
        stockPriceAlarmManager.startAlarmNow() // 거래 시간 체크 없이 바로 시작
        logD("✅ 시세 업데이트 알람이 재시작되었습니다 - 5분마다 실행됩니다")
    }

    /**
     * Worker 실행 통계를 로그로 출력
     * 프로필 화면 진입 시 백그라운드 작업이 정상적으로 실행되었는지 확인 가능
     */
    private fun logWorkerStats() {
        logD(tracker.getExecutionStats())
    }

    /**
     * Worker 스케줄링 상태 확인
     */
    private fun checkWorkerSchedulingStatus() {
        viewModelScope.launch {
            val isScheduled = workManagerHelper.isWorkScheduled(PeriodicSyncWorker.WORK_NAME)
            if (isScheduled) {
                logD("✅ PeriodicSyncWorker가 스케줄링되어 있습니다")
            } else {
                logD("❌ PeriodicSyncWorker가 스케줄링되어 있지 않습니다!")
                if (isAdminMode()) {
                    logD("⚠️ 관리자 모드인데 Worker가 없습니다. 재등록이 필요할 수 있습니다.")
                }
            }
        }
    }

    /**
     * PeriodicSyncWorker를 취소하고 다시 등록
     */
    fun reschedulePeriodicSyncWorker() {
        logD("🔄 PeriodicSyncWorker 재등록 시작...")

        // 1. 기존 Worker 취소
        workManagerHelper.cancelPeriodicWork()
        logD("기존 Worker 취소 완료")

        // 2. 새로운 Worker 등록
        val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(createNetworkConstraints())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PeriodicSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE, // KEEP 대신 REPLACE 사용
            periodicWorkRequest
        )

        logD("✅ PeriodicSyncWorker 재등록 완료")

        // 3. 재등록 확인
        checkWorkerSchedulingStatus()
    }
}
