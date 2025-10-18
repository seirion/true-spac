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
 * 관리자 전용: 마스터 파일을 다운로드하고 Firebase에 업로드하는 Worker
 * 앱이 꺼진 상태에서도 주기적으로 실행되어 최신 종목 정보를 유지합니다.
 */
@HiltWorker
class PeriodicSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val stockPool: StockPool,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            logD("PeriodicSyncWorker started - checking if update needed")

            // Firebase 데이터가 오늘 날짜보다 오래되었는지 확인
            if (stockPool.needUpdateMasterFile()) {
                logD("Update needed - downloading master files")
                // 관리자 전용: 마스터 파일 다운로드 + Firebase 업로드
                stockPool.downloadMasterFiles()
                logD("PeriodicSyncWorker completed successfully - master files updated")
            } else {
                logD("PeriodicSyncWorker skipped - Firebase data is already up to date")
            }

            Result.success()
        } catch (e: Exception) {
            logE(e, "PeriodicSyncWorker failed")
            // 재시도가 필요한 경우 Result.retry() 반환
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "periodic_sync_work"
    }
}