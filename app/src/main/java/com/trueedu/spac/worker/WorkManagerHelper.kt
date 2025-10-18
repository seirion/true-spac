package com.trueedu.spac.worker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.trueedu.spac.data.log.logD
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.guava.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WorkManager 작업을 관리하기 위한 헬퍼 클래스
 */
@Singleton
class WorkManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * 주기적 작업의 상태를 LiveData로 가져옵니다.
     */
    fun getPeriodicWorkStatus(): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkLiveData(PeriodicSyncWorker.WORK_NAME)
    }

    /**
     * 주기적 작업을 취소합니다.
     */
    fun cancelPeriodicWork() {
        workManager.cancelUniqueWork(PeriodicSyncWorker.WORK_NAME)
        logD("Periodic work cancelled: ${PeriodicSyncWorker.WORK_NAME}")
    }

    /**
     * 모든 작업을 취소합니다.
     */
    fun cancelAllWork() {
        workManager.cancelAllWork()
        logD("All work cancelled")
    }

    /**
     * 작업의 현재 상태를 확인합니다.
     */
    suspend fun isWorkScheduled(workName: String): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(workName).await()
        return workInfos.any { !it.state.isFinished }
    }
}