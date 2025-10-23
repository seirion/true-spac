package com.trueedu.spac.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.data.stocks.DartManager
import com.trueedu.spac.data.stocks.SpacManager
import com.trueedu.spac.util.DartUpdateTimeHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * DART 공시 정보 업데이트 Worker
 * 30분마다 실행되어 공시 정보를 가져옴
 */
@HiltWorker
class DartWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dartManager: DartManager,
    private val spacManager: SpacManager,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "dart_update_work"
    }

    override suspend fun doWork(): Result {
        return try {
            logD("🔄 DART 업데이트 Worker 시작: ${java.time.LocalDateTime.now()}")

            // 업데이트 시간 체크
            if (!DartUpdateTimeHelper.isUpdateTime()) {
                logD("⚠️ 업데이트 시간이 아닙니다 (평일 9:00-23:00)")
                return Result.success()
            } else {
                logD("✅ 업데이트 시간 확인됨")
            }

            // SpacManager가 로딩 중이면 대기
            var waitCount = 0
            while (spacManager.loading.value && waitCount < 50) { // 최대 10초 대기
                delay(200)
                waitCount++
            }

            if (spacManager.loading.value) {
                logD("⚠️ SpacManager 로딩 타임아웃")
                return Result.retry()
            }

            // DART 공시 정보 로드
            val spacList = spacManager.spacList.value
            if (spacList.isEmpty()) {
                logD("⚠️ SPAC 종목 리스트가 비어있습니다")
                return Result.success()
            }

            logD("📡 ${spacList.size}개 종목의 DART 공시 정보를 가져옵니다...")
            coroutineScope {
                with(dartManager) {
                    loadList(spacList.map { it.code })
                }
            }

            val dartSize = dartManager.getSize()
            logD("✅ DART 업데이트 완료: ${dartSize}개 종목")

            Result.success()
        } catch (e: IOException) {
            // 네트워크 오류: 재시도 가능
            logE(e, "❌ DART 업데이트 네트워크 오류 - 재시도 예약")
            Result.retry()
        } catch (e: SocketTimeoutException) {
            // 타임아웃 오류: 재시도 가능
            logE(e, "❌ DART 업데이트 타임아웃 - 재시도 예약")
            Result.retry()
        } catch (e: UnknownHostException) {
            // DNS 오류: 재시도 가능
            logE(e, "❌ DART 업데이트 연결 실패 - 재시도 예약")
            Result.retry()
        } catch (e: Exception) {
            // 기타 예외: 실패 처리
            logE(e, "❌ DART 업데이트 Worker 실패")
            Result.failure()
        }
    }
}
