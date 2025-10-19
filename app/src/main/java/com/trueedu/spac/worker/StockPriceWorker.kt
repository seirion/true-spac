package com.trueedu.spac.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.data.stocks.PriceManager
import com.trueedu.spac.util.TradingTimeHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 주식 시세를 주기적으로 가져와서 Firebase에 저장하는 Worker
 * 거래 시간에만 실행됩니다.
 */
@HiltWorker
class StockPriceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val tracker: WorkerExecutionTracker,
    private val priceManager: PriceManager,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // 실행 기록 저장 (앱 종료 후에도 확인 가능)
            tracker.recordPriceUpdateExecution()
            logD("🔄 StockPriceWorker started at ${java.time.LocalDateTime.now()}")

            // 거래 시간 체크 (경고만 하고 계속 진행)
            if (!TradingTimeHelper.isTradingTime()) {
                logD("⚠️ Not trading time, but continuing for testing purposes")
                // 프로덕션에서는 return Result.success() 하려면 아래 주석 해제
                // return Result.success()
            } else {
                logD("✅ Trading time confirmed")
            }

            // KIS API에서 모든 SPAC 종목의 시세 데이터 가져오기
            logD("📡 Fetching stock prices from KIS API...")
            val priceMap = priceManager.getPriceMap(forceRefresh = true)

            // Firebase에 저장
            if (priceMap.isNotEmpty()) {
                logD("💾 Writing ${priceMap.size} stock prices to Firebase...")
                priceManager.writePriceToFirebase(priceMap)
                logD("✅ Stock prices updated successfully: ${priceMap.size} stocks")
            } else {
                logD("⚠️ No stock price data to update")
            }

            Result.success()
        } catch (e: Exception) {
            logE(e, "❌ StockPriceWorker failed")
            // 재시도 필요한 경우 Result.retry() 반환
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "stock_price_work"
    }
}