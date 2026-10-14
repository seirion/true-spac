package com.trueedu.spac.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.data.stocks.PriceManager
import com.trueedu.spac.util.TradingTimeHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 주식 시세를 주기적으로 가져와서 Firebase에 저장하는 Worker
 * 시세 업데이트 허용 시간(09:00~20:10, 애프터마켓 포함)에만 실행됩니다.
 */
@HiltWorker
class StockPriceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val tracker: WorkerExecutionTracker,
    private val priceManager: PriceManager,
    private val trueAnalytics: TrueAnalytics,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "stock_price_work"
    }

    override suspend fun doWork(): Result {
        return try {
            // 실행 기록 저장 (앱 종료 후에도 확인 가능)
            tracker.recordPriceUpdateExecution()
            logD("🔄 시세 업데이트 Worker 시작: ${java.time.LocalDateTime.now()}")

            // 업데이트 시간 체크 (20:10까지 허용)
            if (!TradingTimeHelper.isPriceUpdateTime()) {
                logD("⏭️ 업데이트 시간이 아닙니다. 작업을 종료합니다")
                trueAnalytics.log(
                    PriceUpdateEvents.SKIPPED,
                    PriceUpdateEvents.skippedParams(PriceUpdateEvents.REASON_NOT_UPDATE_TIME)
                )
                return Result.success()
            } else {
                logD("✅ 업데이트 시간 확인됨")
            }

            // 알람/워커가 앱 프로세스를 새로 띄운 경우 종목 목록이 비어 있으므로 먼저 로딩을 보장한다
            val spacReady = priceManager.ensureSpacListReady()
            if (!spacReady) {
                logD("⚠️ 종목 목록이 준비되지 않았습니다")
            }

            // KIS API에서 모든 SPAC 종목의 시세 데이터 가져오기
            logD("📡 KIS API에서 시세 데이터를 가져옵니다...")
            val result = priceManager.fetchPrices(forceRefresh = true)
            val priceMap = result.prices

            if (priceMap.isEmpty()) {
                // 쓰기를 시도하기 전에 끝나는 경우도 실패 이벤트를 남겨 원인을 추적할 수 있게 한다
                trueAnalytics.log(
                    PriceUpdateEvents.FAILED,
                    PriceUpdateEvents.emptyResultParams(result, spacReady)
                )
                logD("⚠️ 업데이트할 시세 데이터가 없습니다 (reason=${result.failure})")
                return Result.success()
            }

            // Firebase에 저장
            logD("💾 Firebase에 ${priceMap.size}개 종목 시세를 저장합니다...")
            val success = priceManager.writePriceToFirebase(priceMap)
            if (success) {
                trueAnalytics.log(
                    PriceUpdateEvents.COMPLETED,
                    PriceUpdateEvents.completedParams(priceMap.size, result.requested)
                )
                logD("✅ 시세 업데이트 완료: ${priceMap.size}/${result.requested}개 종목")
            } else {
                trueAnalytics.log(
                    PriceUpdateEvents.FAILED,
                    PriceUpdateEvents.writeFailedParams(priceMap.size, result.requested)
                )
                logD("❌ 시세 저장 실패: ${priceMap.size}개 종목")
            }

            Result.success()
        } catch (e: Exception) {
            logE(e, "❌ 시세 업데이트 Worker 실패")
            // 예외로 끝나는 경우에도 실패 이벤트를 남긴다 (분석 로깅 자체가 예외를 던져도 무시)
            runCatching { trueAnalytics.log(PriceUpdateEvents.FAILED, PriceUpdateEvents.exceptionParams(e)) }
            // 실패를 명시적으로 표시하여 모니터링 가능하도록 함
            // 단, 5분 후 AlarmManager가 다시 트리거하므로 WorkManager 자체 재시도는 하지 않음
            Result.failure()
        }
    }
}
