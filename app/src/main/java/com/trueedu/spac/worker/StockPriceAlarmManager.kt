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
import com.trueedu.spac.util.TradingTimeHelper
import dagger.hilt.android.qualifiers.ApplicationContext
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
    }

    /**
     * 거래 시간 중 주기적 알람 시작
     */
    fun startTradingTimeAlarm() {
        if (!TradingTimeHelper.isTradingTime()) {
            logD("Not trading time, scheduling for next trading start")
            scheduleNextTradingStart()
            return
        }

        logD("Starting trading time alarm (${INTERVAL_MINUTES}min interval)")

        val pendingIntent = createPendingIntent()
        val intervalMillis = INTERVAL_MINUTES * 60 * 1000

        // Android 12+ 에서 정확한 알람 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                logD("Cannot schedule exact alarms - using inexact alarm")
                scheduleInexactRepeating(pendingIntent, intervalMillis)
                return
            }
        }

        // 정확한 반복 알람 설정
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis,
            intervalMillis,
            pendingIntent
        )

        // 거래 종료 시간에 알람 중지 스케줄링
        scheduleTradingEnd()
    }

    /**
     * 알람 중지
     */
    fun stopAlarm() {
        logD("Stopping stock price alarm")
        val pendingIntent = createPendingIntent()
        alarmManager.cancel(pendingIntent)
    }

    /**
     * 다음 거래 시작 시간에 알람 시작 스케줄링
     */
    private fun scheduleNextTradingStart() {
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

        logD("Scheduled trading start alarm in ${millisUntilStart / 1000 / 60} minutes")
    }

    /**
     * 거래 종료 시간에 알람 중지 스케줄링
     */
    private fun scheduleTradingEnd() {
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

        logD("Scheduled trading end alarm in ${millisUntilEnd / 1000 / 60} minutes")
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
}

/**
 * 주기적 알람을 받아서 WorkManager 작업 실행
 */
class StockPriceAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        logD("Stock price alarm triggered")

        // 거래 시간 체크
        if (!TradingTimeHelper.isTradingTime()) {
            logD("Not trading time, skipping")
            return
        }

        // WorkManager로 실제 작업 실행
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<StockPriceWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}

/**
 * 거래 시작 시간 알람 수신
 */
class TradingStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        logD("Trading start time - starting alarms")
        val alarmManager = StockPriceAlarmManager(context)
        alarmManager.startTradingTimeAlarm()
    }
}

/**
 * 거래 종료 시간 알람 수신
 */
class TradingEndReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        logD("Trading end time - stopping alarms")
        val alarmManager = StockPriceAlarmManager(context)
        alarmManager.stopAlarm()

        // 다음날 거래 시작 시간 스케줄링
        alarmManager.startTradingTimeAlarm()
    }
}

