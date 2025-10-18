package com.trueedu.spac.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.repo.firebase.FirebaseRealtimeDatabase
import com.trueedu.spac.util.TradingTimeHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 주식 시세를 주기적으로 가져와서 Firebase에 저장하는 Worker
 * 거래 시간에만 실행됩니다.
 */
@HiltWorker
class StockPriceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firebaseDatabase: FirebaseRealtimeDatabase,
    // TODO: KIS API 또는 시세 API 서비스 주입
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            logD("StockPriceWorker started")

            // 거래 시간 체크
            if (!TradingTimeHelper.isTradingTime()) {
                logD("Not trading time, skipping price update")
                return Result.success()
            }

            // 현재 시간 (yyyyMMddHHmm 형식)
            val now = LocalDateTime.now()
            val timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")).toLong()

            // TODO: 여기에 실제 시세 데이터를 가져오는 로직 추가
            // 예시:
            // val prices = kisApiService.getCurrentPrices()
            // val stockInfoMap = prices.map { it.toStockInfo() }.associateBy { it.code }

            // 임시 예제 - 실제로는 API에서 가져온 데이터 사용
            val stockInfoMap = emptyMap<String, com.trueedu.spac.api.model.dto.firebase.StockInfo>()

            // Firebase에 저장
            if (stockInfoMap.isNotEmpty()) {
                firebaseDatabase.writeStockInfo(timestamp, stockInfoMap)
                logD("Stock prices updated successfully: ${stockInfoMap.size} stocks")
            } else {
                logD("No stock price data to update")
            }

            Result.success()
        } catch (e: Exception) {
            logE(e, "StockPriceWorker failed")
            // 재시도 필요한 경우 Result.retry() 반환
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "stock_price_work"
    }
}