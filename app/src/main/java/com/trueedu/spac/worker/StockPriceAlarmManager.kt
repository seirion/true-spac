package com.trueedu.spac.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.data.stocks.PriceManager
import com.trueedu.spac.di.ApplicationScope
import com.trueedu.spac.util.TradingTimeHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 주식 시세 업데이트를 위한 AlarmManager 관리 클래스
 * 거래 시간 중 5분 간격으로 실행
 */
@Singleton
class StockPriceAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val ALARM_REQUEST_CODE = 1001
        private const val INTERVAL_MINUTES = 5L // 5분 간격

        // 재시도 전략: AlarmManager가 INTERVAL_MINUTES 마다 자동으로 재시도
        // WorkManager는 재시도하지 않고 실패 시 다음 스케줄에서 재시도
    }

    /**
     * 거래 시간 중 주기적 알람 시작
     */
    fun startTradingTimeAlarm() {
        if (!TradingTimeHelper.isTradingTime()) {
            logD("거래 시간이 아닙니다. 다음 거래 시작 시간에 알람을 예약합니다")
            scheduleNextTradingStart()
            return
        }

        startAlarmNow()
    }

    /**
     * 거래 시간 체크 없이 알람을 바로 시작 (테스트용)
     */
    fun startAlarmNow() {
        logD("시세 업데이트 알람 시작 (${INTERVAL_MINUTES}분 간격)")

        val pendingIntent = createPendingIntent()
        val intervalMillis = INTERVAL_MINUTES * 60 * 1000

        // Android 12+ 에서 정확한 알람 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                logD("⚠️ Cannot schedule exact alarms - using inexact alarm. 설정에서 정확한 알람 권한을 허용해주세요.")
                scheduleInexactRepeating(pendingIntent, intervalMillis)
                return
            } else {
                logD("✅ 정확한 알람 권한이 허용되었습니다")
            }
        }

        // 즉시 한 번 실행
        triggerImmediately()

        // 정확한 반복 알람 설정 (5분 후부터 반복)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis,
            intervalMillis,
            pendingIntent
        )
        logD("✅ 알람이 성공적으로 설정되었습니다 - 즉시 실행 후 ${INTERVAL_MINUTES}분마다 반복")

        // 거래 종료 시간에 알람 중지 스케줄링 (거래 시간일 경우에만)
        if (TradingTimeHelper.isTradingTime()) {
            scheduleTradingEnd()
        }
    }

    /**
     * 즉시 Worker 실행
     */
    private fun triggerImmediately() {
        logD("🚀 시세 업데이트 Worker를 즉시 실행합니다")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<StockPriceWorker>()
            .setConstraints(constraints)
            .build()

        // REPLACE 정책으로 중복 실행 방지
        // 실패 시 재시도하지 않고 다음 5분 스케줄에서 재시도
        WorkManager.getInstance(context).enqueueUniqueWork(
            StockPriceWorker.WORK_NAME,
            androidx.work.ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * 알람 중지
     */
    fun stopAlarm() {
        logD("시세 업데이트 알람 중지")
        val pendingIntent = createPendingIntent()
        alarmManager.cancel(pendingIntent)
    }

    /**
     * 다음 거래 시작 시간에 알람 시작 스케줄링
     */
    private fun scheduleNextTradingStart() {
        // Android 12+ 에서 정확한 알람 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                logD("정확한 알람 권한이 없어 거래 시작 알람을 예약할 수 없습니다")
                return
            }
        }

        val millisUntilStart = TradingTimeHelper.getMillisUntilTradingStart()
        val startIntent = Intent(context, TradingStartReceiver::class.java)
        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE + 1,
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + millisUntilStart,
            startPendingIntent
        )

        logD("거래 시작 알람이 ${millisUntilStart / 1000 / 60}분 후로 예약되었습니다")
    }

    /**
     * 거래 종료 시간에 알람 중지 스케줄링
     */
    private fun scheduleTradingEnd() {
        // Android 12+ 에서 정확한 알람 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                logD("정확한 알람 권한이 없어 거래 종료 알람을 예약할 수 없습니다")
                return
            }
        }

        val millisUntilEnd = TradingTimeHelper.getMillisUntilTradingEnd()
        val endIntent = Intent(context, TradingEndReceiver::class.java)
        val endPendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE + 2,
            endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + millisUntilEnd,
            endPendingIntent
        )

        logD("거래 종료 알람이 ${millisUntilEnd / 1000 / 60}분 후로 예약되었습니다")
    }

    /**
     * 정확하지 않은 반복 알람 (Android 12+ 권한 없을 때)
     */
    private fun scheduleInexactRepeating(pendingIntent: PendingIntent, intervalMillis: Long) {
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis,
            intervalMillis,
            pendingIntent
        )
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, StockPriceAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * 알람이 현재 설정되어 있는지 확인
     * @return true: 알람이 활성화되어 있음, false: 알람이 중단된 상태
     */
    fun isAlarmScheduled(): Boolean {
        val intent = Intent(context, StockPriceAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }

    /**
     * 알람 상태 진단 정보 반환
     */
    fun getAlarmDiagnostics(): String {
        val isScheduled = isAlarmScheduled()
        val isTradingTime = TradingTimeHelper.isTradingTime()
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        val nextTradingStart = TradingTimeHelper.getNextTradingStartTime()
        val nextTradingEnd = TradingTimeHelper.getNextTradingEndTime()

        return buildString {
            appendLine("📊 알람 진단 정보")
            appendLine("━━━━━━━━━━━━━━━━")
            appendLine("알람 상태: ${if (isScheduled) "✅ 활성화" else "❌ 중단됨"}")
            appendLine("현재 거래 시간: ${if (isTradingTime) "✅ YES" else "❌ NO"}")
            appendLine("정확한 알람 권한: ${if (canScheduleExact) "✅ 허용됨" else "❌ 거부됨"}")
            appendLine("다음 거래 시작: $nextTradingStart")
            appendLine("다음 거래 종료: $nextTradingEnd")
            appendLine("━━━━━━━━━━━━━━━━")
            if (!isScheduled) {
                appendLine("\n⚠️ 알람이 중단된 이유:")
                if (!isTradingTime) {
                    appendLine("• 현재 거래 시간이 아님")
                    appendLine("  → 앱이 거래 시간 외에 실행됨")
                }
                if (!canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    appendLine("• 정확한 알람 권한 없음")
                    appendLine("  → 설정 > 앱 > 권한에서 알람 권한 허용 필요")
                }
                appendLine("\n💡 해결 방법:")
                appendLine("• '알람 재시작' 버튼을 눌러주세요")
                appendLine("  (거래 시간 체크를 우회하여 즉시 시작)")
            }
        }
    }
}

/**
 * 주기적 알람을 받아서 WorkManager 작업 실행
 */
@AndroidEntryPoint
class StockPriceAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val currentTime = java.time.LocalDateTime.now()
        logD("⏰ 시세 업데이트 알람 트리거: $currentTime")

        // 거래 시간 체크 (경고만 하고 계속 진행 - 테스트 목적)
        if (!TradingTimeHelper.isTradingTime()) {
            logD("⚠️ 거래 시간이 아닙니다 (테스트 모드에서는 계속 진행)")
            // 테스트 목적으로 거래 시간이 아니어도 계속 진행
            // 프로덕션 배포 시 아래 return 주석을 해제하세요
            // return
        } else {
            logD("✅ 거래 시간 확인됨")
        }

        // WorkManager로 실제 작업 실행
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<StockPriceWorker>()
            .setConstraints(constraints)
            .build()

        // REPLACE 정책: 이전 작업이 실행 중이면 취소하고 새 작업 실행
        // 중복 실행 방지 및 최신 데이터 우선
        // 실패 시 재시도하지 않고 다음 5분 스케줄에서 재시도
        WorkManager.getInstance(context).enqueueUniqueWork(
            StockPriceWorker.WORK_NAME,
            androidx.work.ExistingWorkPolicy.REPLACE,
            workRequest
        )
        logD("📋 시세 업데이트 Worker가 큐에 추가되었습니다 (REPLACE 정책)")
    }
}

/**
 * 거래 시작 시간 알람 수신
 */
@AndroidEntryPoint
class TradingStartReceiver : BroadcastReceiver() {

    @Inject
    lateinit var stockPriceAlarmManager: StockPriceAlarmManager

    override fun onReceive(context: Context, intent: Intent) {
        logD("거래 시작 시간 도달 - 알람 시작")
        stockPriceAlarmManager.startTradingTimeAlarm()
    }
}

/**
 * 거래 종료 시간 알람 수신
 */
@AndroidEntryPoint
class TradingEndReceiver : BroadcastReceiver() {

    @Inject
    lateinit var stockPriceAlarmManager: StockPriceAlarmManager

    @Inject
    lateinit var priceManager: PriceManager

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        logD("거래 종료 시간 도달 - 종가 업데이트 후 알람 중지")

        // BroadcastReceiver의 goAsync()를 사용하여 비동기 작업 수행
        val pendingResult = goAsync()

        // Application 스코프에서 종가 업데이트
        applicationScope.launch {
            try {
                // 오늘 날짜의 장 종료 시각(15:30)으로 종가 업데이트
                val marketCloseTime = TradingTimeHelper.getTodayMarketCloseTime()

                logD("📊 장 종가 업데이트 시작 (시각: $marketCloseTime)")

                // KIS API에서 현재 시세 가져오기
                val priceMap = priceManager.getPriceMap(forceRefresh = true)

                // Firebase에 15:30 시각으로 저장
                if (priceMap.isNotEmpty()) {
                    priceManager.writePriceToFirebase(priceMap, marketCloseTime)
                    logD("✅ 장 종가 업데이트 완료: ${priceMap.size}개 종목")
                } else {
                    logD("⚠️ 종가 업데이트할 데이터가 없습니다")
                }
            } catch (e: Exception) {
                logE(e, "❌ 장 종가 업데이트 실패")
            } finally {
                // 알람 중지
                stockPriceAlarmManager.stopAlarm()

                // 다음날 거래 시작 시간 스케줄링
                stockPriceAlarmManager.startTradingTimeAlarm()

                // BroadcastReceiver 완료
                pendingResult.finish()
            }
        }
    }
}