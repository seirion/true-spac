package com.trueedu.spac.ui.admin

import android.app.AlarmManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
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
import com.trueedu.spac.data.master.MasterFileDownloader
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.util.FcmPushSender
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
class AdminViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val local: Local,
    private val tracker: WorkerExecutionTracker,
    private val stockPriceAlarmManager: StockPriceAlarmManager,
    private val workManagerHelper: WorkManagerHelper,
    private val masterFileDownloader: MasterFileDownloader,
) : ViewModel() {

    // Worker 상태 State
    private val _lastMasterFileUpdate = mutableStateOf("")
    val lastMasterFileUpdate: State<String> = _lastMasterFileUpdate

    private val _lastMasterFileUpdate2 = mutableStateOf("")
    val lastMasterFileUpdate2: State<String> = _lastMasterFileUpdate2

    private val _masterFileExecutionCount = mutableStateOf(0)
    val masterFileExecutionCount: State<Int> = _masterFileExecutionCount

    private val _lastPriceUpdate = mutableStateOf("")
    val lastPriceUpdate: State<String> = _lastPriceUpdate

    private val _lastPriceUpdate2 = mutableStateOf("")
    val lastPriceUpdate2: State<String> = _lastPriceUpdate2

    private val _priceExecutionCount = mutableStateOf(0)
    val priceExecutionCount: State<Int> = _priceExecutionCount

    private val _isAlarmScheduled = mutableStateOf(false)
    val isAlarmScheduled: State<Boolean> = _isAlarmScheduled

    private val _alarmDiagnostics = mutableStateOf("")
    val alarmDiagnostics: State<String> = _alarmDiagnostics

    private val _canScheduleExactAlarms = mutableStateOf(true)
    val canScheduleExactAlarms: State<Boolean> = _canScheduleExactAlarms

    private val _isBatteryOptimizationIgnored = mutableStateOf(true)
    val isBatteryOptimizationIgnored: State<Boolean> = _isBatteryOptimizationIgnored

    private val _isDownloadingUsMaster = mutableStateOf(false)
    val isDownloadingUsMaster: State<Boolean> = _isDownloadingUsMaster

    private val _usMasterDownloadMessage = mutableStateOf("")
    val usMasterDownloadMessage: State<String> = _usMasterDownloadMessage

    // FCM 푸시 테스트 관련 State
    private val _fcmToken = mutableStateOf("")
    val fcmToken: State<String> = _fcmToken

    private val _pushTitle = mutableStateOf("")
    val pushTitle: State<String> = _pushTitle

    private val _pushBody = mutableStateOf("")
    val pushBody: State<String> = _pushBody

    private val _pushDeepLink = mutableStateOf("")
    val pushDeepLink: State<String> = _pushDeepLink

    private val _isSendingPush = mutableStateOf(false)
    val isSendingPush: State<Boolean> = _isSendingPush

    private val _pushResultMessage = mutableStateOf("")
    val pushResultMessage: State<String> = _pushResultMessage

    init {
        // 초기 데이터 로드
        refreshWorkerStats()
        // Worker 실행 통계 로그 출력
        logWorkerStats()
        // Worker 스케줄링 상태 확인
        checkWorkerSchedulingStatus()
    }

    // Worker 상태 조회 메서드
    fun isAdminMode(): Boolean = local.getUserKey().isValid()

    /**
     * Worker 통계 새로고침
     */
    fun refreshWorkerStats() {
        _lastMasterFileUpdate.value = tracker.getLastMasterFileUpdate()
        _lastMasterFileUpdate2.value = tracker.getLastMasterFileUpdate2()
        _masterFileExecutionCount.value = tracker.getMasterFileExecutionCount()
        _lastPriceUpdate.value = tracker.getLastPriceUpdate()
        _lastPriceUpdate2.value = tracker.getLastPriceUpdate2()
        _priceExecutionCount.value = tracker.getPriceExecutionCount()
        _isAlarmScheduled.value = stockPriceAlarmManager.isAlarmScheduled()
        _alarmDiagnostics.value = stockPriceAlarmManager.getAlarmDiagnostics()

        // 정확한 알람 권한 체크
        _canScheduleExactAlarms.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        // 배터리 최적화 제외 여부 체크
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        _isBatteryOptimizationIgnored.value = powerManager.isIgnoringBatteryOptimizations(context.packageName)

        // 알람 진단 정보 출력
        logD(stockPriceAlarmManager.getAlarmDiagnostics())
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
        logD("🧪 수동으로 시세 업데이트 Worker 실행")

        val workRequest = OneTimeWorkRequestBuilder<StockPriceWorker>()
            .setConstraints(createNetworkConstraints())
            .build()

        // REPLACE 정책으로 중복 실행 방지
        // 실패 시 재시도하지 않고 다음 5분 스케줄에서 재시도
        WorkManager.getInstance(context).enqueueUniqueWork(
            StockPriceWorker.WORK_NAME,
            androidx.work.ExistingWorkPolicy.REPLACE,
            workRequest
        )
        logD("✅ 시세 업데이트 Worker가 큐에 추가되었습니다 (REPLACE 정책)")
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

    /**
     * 알람 권한 설정 페이지로 이동
     * Android 12+ 에서만 동작
     */
    fun openAlarmPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                logD("⚙️ 알람 권한 설정 페이지로 이동")
            } catch (e: Exception) {
                logD("⚠️ 알람 권한 설정 페이지 열기 실패: ${e.message}")
            }
        } else {
            logD("ℹ️ Android 12 미만에서는 알람 권한이 필요하지 않습니다")
        }
    }

    /**
     * 배터리 최적화 제외 설정 페이지로 이동
     */
    fun openBatteryOptimizationSettings() {
        try {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            logD("⚙️ 배터리 최적화 설정 페이지로 이동")
        } catch (e: Exception) {
            logD("⚠️ 배터리 최적화 설정 페이지 열기 실패: ${e.message}")
        }
    }

    /**
     * 미국 주식 마스터 파일 다운로드 (테스트용)
     */
    fun downloadUsMasterFile() {
        if (_isDownloadingUsMaster.value) {
            logD("⚠️ 이미 다운로드 중입니다")
            return
        }

        viewModelScope.launch {
            try {
                _isDownloadingUsMaster.value = true
                _usMasterDownloadMessage.value = "다운로드 중..."
                logD("🇺🇸 미국 주식 마스터 파일 다운로드 시작")

                masterFileDownloader.downloadUsMasterFile()

                _usMasterDownloadMessage.value = "다운로드 완료!"
                logD("✅ 미국 주식 마스터 파일 다운로드 완료")
            } catch (e: Exception) {
                _usMasterDownloadMessage.value = "다운로드 실패: ${e.message}"
                logD("❌ 미국 주식 마스터 파일 다운로드 실패: ${e.message}")
            } finally {
                _isDownloadingUsMaster.value = false
            }
        }
    }

    /**
     * FCM 토큰 업데이트
     */
    fun updateFcmToken(token: String) {
        _fcmToken.value = token
    }

    /**
     * 클립보드에서 텍스트를 가져와 FCM 토큰에 붙여넣기
     */
    fun pasteFromClipboard() {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString() ?: ""
                if (text.isNotEmpty()) {
                    _fcmToken.value = text
                    logD("📋 클립보드에서 토큰 붙여넣기 완료")
                } else {
                    logD("⚠️ 클립보드가 비어있습니다")
                }
            } else {
                logD("⚠️ 클립보드에 데이터가 없습니다")
            }
        } catch (e: Exception) {
            logD("❌ 클립보드 읽기 실패: ${e.message}", e)
        }
    }

    /**
     * 푸시 결과 메시지를 클립보드에 복사
     */
    fun copyResultMessageToClipboard() {
        try {
            val message = _pushResultMessage.value
            if (message.isNotEmpty()) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = android.content.ClipData.newPlainText("푸시 결과", message)
                clipboard.setPrimaryClip(clip)
                logD("📋 결과 메시지를 클립보드에 복사했습니다: $message")
            }
        } catch (e: Exception) {
            logD("❌ 클립보드 복사 실패: ${e.message}", e)
        }
    }

    /**
     * 푸시 제목 업데이트
     */
    fun updatePushTitle(title: String) {
        _pushTitle.value = title
    }

    /**
     * 푸시 내용 업데이트
     */
    fun updatePushBody(body: String) {
        _pushBody.value = body
    }

    /**
     * 푸시 딥링크 업데이트
     */
    fun updatePushDeepLink(deepLink: String) {
        _pushDeepLink.value = deepLink
    }

    /**
     * FCM 푸시 전송 (테스트용)
     */
    fun sendTestPush() {
        if (_isSendingPush.value) {
            logD("⚠️ 이미 푸시 전송 중입니다")
            return
        }

        val token = _fcmToken.value.trim()
        val title = _pushTitle.value.trim()
        val body = _pushBody.value.trim()
        val deepLink = _pushDeepLink.value.trim().ifEmpty { null }

        // 입력 값 검증
        if (token.isEmpty()) {
            _pushResultMessage.value = "❌ FCM 토큰을 입력해주세요"
            return
        }
        if (title.isEmpty()) {
            _pushResultMessage.value = "❌ 푸시 제목을 입력해주세요"
            return
        }
        if (body.isEmpty()) {
            _pushResultMessage.value = "❌ 푸시 내용을 입력해주세요"
            return
        }

        viewModelScope.launch {
            try {
                _isSendingPush.value = true
                _pushResultMessage.value = "전송 중..."
                logD("📤 FCM 푸시 전송 시작${deepLink?.let { " (딥링크: $it)" } ?: ""}")

                val result = FcmPushSender.sendPush(
                    context = context,
                    token = token,
                    title = title,
                    body = body,
                    data = mapOf(
                        "type" to "test",
                        "timestamp" to System.currentTimeMillis().toString()
                    ),
                    deepLink = deepLink
                )

                result.onSuccess { response ->
                    _pushResultMessage.value = "✅ 푸시 전송 성공!"
                    logD("✅ FCM 푸시 전송 성공: $response")
                }.onFailure { error ->
                    _pushResultMessage.value = "❌ 전송 실패: ${error.message}"
                    logD("❌ FCM 푸시 전송 실패: ${error.message}", error)
                }
            } catch (e: Exception) {
                _pushResultMessage.value = "❌ 전송 오류: ${e.message}"
                logD("❌ FCM 푸시 전송 오류: ${e.message}", e)
            } finally {
                _isSendingPush.value = false
            }
        }
    }

    /**
     * 푸시 테스트 필드 초기화
     */
    fun clearPushTestFields() {
        _fcmToken.value = ""
        _pushTitle.value = ""
        _pushBody.value = ""
        _pushDeepLink.value = ""
        _pushResultMessage.value = ""
    }
}

