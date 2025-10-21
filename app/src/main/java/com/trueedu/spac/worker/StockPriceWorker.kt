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

    companion object {
        const val WORK_NAME = "stock_price_work"
    }

    override suspend fun doWork(): Result {
        return try {
            // 실행 기록 저장 (앱 종료 후에도 확인 가능)
            tracker.recordPriceUpdateExecution()
            logD("🔄 시세 업데이트 Worker 시작: ${java.time.LocalDateTime.now()}")

            // 거래 시간 체크 (경고만 하고 계속 진행)
            if (!TradingTimeHelper.isTradingTime()) {
                logD("⚠️ 거래 시간이 아니지만 테스트 목적으로 계속 진행합니다")
                // 프로덕션에서는 return Result.success() 하려면 아래 주석 해제
                // return Result.success()
            } else {
                logD("✅ 거래 시간 확인됨")
            }

            // KIS API에서 모든 SPAC 종목의 시세 데이터 가져오기
            logD("📡 KIS API에서 시세 데이터를 가져옵니다...")
            val priceMap = priceManager.getPriceMap(forceRefresh = true)

            // Firebase에 저장
            if (priceMap.isNotEmpty()) {
                logD("💾 Firebase에 ${priceMap.size}개 종목 시세를 저장합니다...")
                priceManager.writePriceToFirebase(priceMap)
                logD("✅ 시세 업데이트 완료: ${priceMap.size}개 종목")
            } else {
                logD("⚠️ 업데이트할 시세 데이터가 없습니다")
            }

            Result.success()
        } catch (e: Exception) {
            logE(e, "❌ 시세 업데이트 Worker 실패")
            // 실패를 명시적으로 표시하여 모니터링 가능하도록 함
            // 단, 5분 후 AlarmManager가 다시 트리거하므로 WorkManager 자체 재시도는 하지 않음
            Result.failure()
        }
    }
}