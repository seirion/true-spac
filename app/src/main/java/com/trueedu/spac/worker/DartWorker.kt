package com.trueedu.spac.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.data.stocks.DartManager
import com.trueedu.spac.data.stocks.SpacManager
import com.trueedu.spac.repo.firebase.FirebaseAdminDatabase
import com.trueedu.spac.util.DartUpdateTimeHelper
import com.trueedu.spac.util.FcmPushSender
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    private val firebaseAdminDatabase: FirebaseAdminDatabase,
    private val trueAnalytics: TrueAnalytics,
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
            val newDisclosureCount = coroutineScope {
                with(dartManager) {
                    syncListToFirebase(spacList.map { it.code })
                }
            }

            val dartSize = dartManager.getSize()
            if (newDisclosureCount > 0) {
                logD("✅ DART 업데이트 완료: ${dartSize}개 종목 (새로운 공시: ${newDisclosureCount}개)")

                // 새로운 공시가 있으면 푸시 전송
                sendPushNotification(newDisclosureCount)
            } else {
                logD("✅ DART 업데이트 완료: ${dartSize}개 종목 (새로운 공시 없음)")
            }

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

    /**
     * 새로운 공시가 있는 경우 푸시 알림 전송
     */
    private suspend fun sendPushNotification(newDisclosureCount: Int) = coroutineScope {
        logD("📤 새로운 공시 ${newDisclosureCount}개에 대한 푸시 전송 준비")

        // 푸시 수신 대상 사용자 로드
        val userConfigs = firebaseAdminDatabase.loadAllUserConfigs()
        val eligibleUsers = userConfigs.filter { (_, config) ->
            config.notificationEnabled && !config.pushToken.isNullOrBlank()
        }

        if (eligibleUsers.isEmpty()) {
            logD("📤 푸시 수신 대상 사용자 없음")
            return@coroutineScope
        }

        logD("📤 푸시 전송 대상: ${eligibleUsers.size}명")

        // 푸시 제목
        val title = "${newDisclosureCount}개의 새로운 공시가 있습니다"

        // 푸시 내용
        val body = "SPAC 공시 정보가 업데이트 되었습니다."

        val deepLink = "truespac://app/dart"

        // FCM Rate Limiting을 고려하여 배치로 처리
        val batchSize = 100  // 한 번에 처리할 최대 사용자 수
        val delayBetweenBatches = 100L  // 배치 간 지연 시간 (ms)

        // 각 사용자에게 병렬로 푸시 전송
        val results = eligibleUsers.chunked(batchSize).mapIndexed { batchIndex, batch ->
            // 배치 간 지연
            if (batchIndex > 0) {
                delay(delayBetweenBatches)
            }

            // 배치 내에서는 병렬 처리
            batch.map { (userId, config) ->
                async {
                    val pushToken = config.pushToken ?: return@async null

                    try {
                        val result = FcmPushSender.sendPush(
                            context = applicationContext,
                            token = pushToken,
                            title = title,
                            body = body,
                            deepLink = deepLink
                        )

                        if (result.isSuccess) {
                            logD("✅ 푸시 전송 성공: userId=$userId")
                            true
                        } else {
                            logE("❌ 푸시 전송 실패: userId=$userId, error=${result.exceptionOrNull()?.message}")
                            false
                        }
                    } catch (e: Exception) {
                        logE("❌ 푸시 전송 오류: userId=$userId, error=${e.message}")
                        false
                    }
                }
            }.awaitAll()
        }.flatten()

        val successCount = results.count { it == true }
        val failCount = results.count { it == false }

        logD("📤 푸시 전송 완료: 성공 ${successCount}건, 실패 ${failCount}건")

        // Analytics 로깅
        trueAnalytics.log(
            "dart__push_sent",
            mapOf(
                "newDisclosureCount" to newDisclosureCount,
                "targetUserCount" to eligibleUsers.size,
                "successCount" to successCount,
                "failCount" to failCount
            )
        )
    }
}