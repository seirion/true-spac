package com.trueedu.spac.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.data.stocks.StockPool
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 주기적으로 실행되는 백그라운드 작업을 처리하는 Worker
 * 앱이 꺼진 상태에서도 스케줄된 시간에 실행됩니다.
 */
@HiltWorker
class PeriodicSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val stockPool: StockPool,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            logD("PeriodicSyncWorker started")

            // 여기에 주기적으로 수행할 작업을 추가합니다
            // 예: 데이터 동기화, 알림 확인 등

            // 예제: StockPool 데이터 로드
            stockPool.loadStockInfo()

            logD("PeriodicSyncWorker completed successfully")
            Result.success()
        } catch (e: Exception) {
            logE(e, "PeriodicSyncWorker failed")
            // 재시도가 필요한 경우 Result.retry() 반환
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "periodic_sync_work"
    }
}