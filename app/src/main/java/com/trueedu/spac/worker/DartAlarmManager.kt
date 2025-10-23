package com.trueedu.spac.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.util.DartUpdateTimeHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DART 공시 업데이트를 위한 AlarmManager 관리 클래스
 * 평일 9:00-23:00 사이에 30분 간격으로 실행
 */
@Singleton
class DartAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val ALARM_REQUEST_CODE = 1002
        private const val INTERVAL_MINUTES = 30L // 30분 간격
    }

    /**
     * 업데이트 시간 중 주기적 알람 시작
     */
    fun startUpdateTimeAlarm() {
        // Android 12+ 정확한 알람 권한 체크 및 로깅
        checkExactAlarmPermission()

        if (!DartUpdateTimeHelper.isUpdateTime()) {
            logD("업데이트 시간이 아닙니다. 다음 업데이트 시작 시간에 알람을 예약합니다")
            scheduleNextUpdateStart()
            return
        }

        startAlarmNow()
    }

    /**
     * 정확한 알람 권한 체크
     * Android 12+ 에서 정확한 알람 권한이 필요한 경우 확인
     */
    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                logD("⚠️ 정확한 알람 권한이 없습니다.")
                logD("💡 설정 > 앱 > SPAC > 알람 및 리마인더 권한을 활성화해주세요.")
                logD("   이 권한이 없으면 알람이 정확한 시간에 실행되지 않을 수 있습니다.")
            } else {
                logD("✅ 정확한 알람 권한 확인됨")
            }
        }
    }

    /**
     * 업데이트 시간 체크 없이 알람을 바로 시작 (테스트용)
     */
    fun startAlarmNow() {
        logD("DART 업데이트 알람 시작 (${INTERVAL_MINUTES}분 간격)")

        val intent = Intent(context, DartAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + (INTERVAL_MINUTES * 60 * 1000)

        // 정확한 알람 설정 시도
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    INTERVAL_MINUTES * 60 * 1000,
                    pendingIntent
                )
                logD("✅ 정확한 반복 알람 설정 완료")
            } else {
                logD("⚠️ 정확한 알람 권한 없음 - 대략적 알람 사용")
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    AlarmManager.INTERVAL_HALF_HOUR,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                INTERVAL_MINUTES * 60 * 1000,
                pendingIntent
            )
            logD("✅ 반복 알람 설정 완료")
        }

        scheduleUpdateEnd()
    }

    /**
     * 모든 알람 중지
     */
    fun stopAlarm() {
        logD("DART 업데이트 알람 중지")

        val intent = Intent(context, DartAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        logD("✅ 알람 취소 완료")

        cancelUpdateStart()
        cancelUpdateEnd()
    }

    /**
     * 다음 업데이트 시작 시간에 알람 예약
     */
    private fun scheduleNextUpdateStart() {
        val triggerAtMillis = DartUpdateTimeHelper.getMillisUntilUpdateStart() + System.currentTimeMillis()

        val intent = Intent(context, DartUpdateStartReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        logD("✅ 다음 업데이트 시작 시간 알람 예약: ${java.time.LocalDateTime.now().plusNanos((triggerAtMillis - System.currentTimeMillis()) * 1_000_000)}")
    }

    /**
     * 업데이트 종료 시간에 알람 예약
     */
    private fun scheduleUpdateEnd() {
        val triggerAtMillis = DartUpdateTimeHelper.getMillisUntilUpdateEnd() + System.currentTimeMillis()

        val intent = Intent(context, DartUpdateEndReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE + 2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        logD("✅ 업데이트 종료 시간 알람 예약: ${java.time.LocalDateTime.now().plusNanos((triggerAtMillis - System.currentTimeMillis()) * 1_000_000)}")
    }

    /**
     * 업데이트 시작 알람 취소
     */
    private fun cancelUpdateStart() {
        val intent = Intent(context, DartUpdateStartReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * 업데이트 종료 알람 취소
     */
    private fun cancelUpdateEnd() {
        val intent = Intent(context, DartUpdateEndReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE + 2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * 알람이 예약되어 있는지 확인
     */
    fun isAlarmScheduled(): Boolean {
        val intent = Intent(context, DartAlarmReceiver::class.java)
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
        val isUpdateTime = DartUpdateTimeHelper.isUpdateTime()
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        val nextUpdateStart = DartUpdateTimeHelper.getNextUpdateStartTime()
        val nextUpdateEnd = DartUpdateTimeHelper.getNextUpdateEndTime()

        return buildString {
            appendLine("=== DART 업데이트 알람 진단 정보 ===")
            appendLine("알람 예약 상태: ${if (isScheduled) "예약됨" else "예약 안됨"}")
            appendLine("현재 업데이트 시간: ${if (isUpdateTime) "예" else "아니오"}")
            appendLine("정확한 알람 권한: ${if (canScheduleExact) "있음" else "없음"}")
            appendLine("다음 업데이트 시작: $nextUpdateStart")
            appendLine("다음 업데이트 종료: $nextUpdateEnd")
            appendLine("===============================")
        }
    }
}

/**
 * DART 업데이트 알람 수신
 */
@AndroidEntryPoint
class DartAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // BroadcastReceiver가 완료되기 전까지 시스템이 대기하도록 함
        val pendingResult = goAsync()

        try {
            val currentTime = java.time.LocalDateTime.now()
            logD("⏰ DART 업데이트 알람 트리거: $currentTime")

            // 업데이트 시간 체크
            if (!DartUpdateTimeHelper.isUpdateTime()) {
                logD("⚠️ 업데이트 시간이 아닙니다")
            } else {
                logD("✅ 업데이트 시간 확인됨")

                // WorkManager로 실제 작업 실행
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<DartWorker>()
                    .setConstraints(constraints)
                    .build()

                // REPLACE 정책: 이전 작업이 실행 중이면 취소하고 새 작업 실행
                WorkManager.getInstance(context).enqueueUniqueWork(
                    DartWorker.WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
                logD("📋 DART 업데이트 Worker가 큐에 추가되었습니다 (REPLACE 정책)")
            }
        } finally {
            // 작업 완료를 시스템에 알림
            pendingResult.finish()
        }
    }
}

/**
 * 업데이트 시작 시간 알람 수신
 */
@AndroidEntryPoint
class DartUpdateStartReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dartAlarmManager: DartAlarmManager

    override fun onReceive(context: Context, intent: Intent) {
        // BroadcastReceiver가 완료되기 전까지 시스템이 대기하도록 함
        val pendingResult = goAsync()

        try {
            logD("업데이트 시작 시간 도달 - 알람 시작")
            dartAlarmManager.startUpdateTimeAlarm()
        } finally {
            // 작업 완료를 시스템에 알림
            pendingResult.finish()
        }
    }
}

/**
 * 업데이트 종료 시간 알람 수신
 */
@AndroidEntryPoint
class DartUpdateEndReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dartAlarmManager: DartAlarmManager

    override fun onReceive(context: Context, intent: Intent) {
        // BroadcastReceiver가 완료되기 전까지 시스템이 대기하도록 함
        val pendingResult = goAsync()

        try {
            logD("업데이트 종료 시간 도달 - 알람 중지")
            dartAlarmManager.stopAlarm()
            // 다음 날 시작 시간 예약
            dartAlarmManager.startUpdateTimeAlarm()
        } finally {
            // 작업 완료를 시스템에 알림
            pendingResult.finish()
        }
    }
}
