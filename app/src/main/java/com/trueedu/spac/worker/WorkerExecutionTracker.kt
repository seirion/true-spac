package com.trueedu.spac.worker

import android.content.SharedPreferences
import com.trueedu.spac.repo.local.Local
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Worker 실행 이력을 추적하는 클래스
 * 앱이 종료된 상태에서도 Worker가 실행되었는지 확인 가능
 */
@Singleton
class WorkerExecutionTracker @Inject constructor(
    private val preferences: SharedPreferences
) {
    companion object {
        private const val KEY_LAST_MASTER_FILE_UPDATE = "last_master_file_update"
        private const val KEY_LAST_MASTER_FILE_UPDATE_2 = "last_master_file_update_2"
        private const val KEY_MASTER_FILE_EXECUTION_COUNT = "master_file_execution_count"
        private const val KEY_LAST_PRICE_UPDATE = "last_price_update"
        private const val KEY_LAST_PRICE_UPDATE_2 = "last_price_update_2"
        private const val KEY_PRICE_EXECUTION_COUNT = "price_execution_count"

        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    /**
     * 마스터 파일 Worker 실행 기록
     */
    fun recordMasterFileExecution() {
        val now = LocalDateTime.now().format(formatter)
        val count = preferences.getInt(KEY_MASTER_FILE_EXECUTION_COUNT, 0) + 1
        val previousLast = preferences.getString(KEY_LAST_MASTER_FILE_UPDATE, null)

        with(preferences.edit()) {
            putString(KEY_LAST_MASTER_FILE_UPDATE, now)
            if (previousLast != null) {
                putString(KEY_LAST_MASTER_FILE_UPDATE_2, previousLast)
            }
            putInt(KEY_MASTER_FILE_EXECUTION_COUNT, count)
            apply()
        }
    }

    /**
     * 시세 Worker 실행 기록
     */
    fun recordPriceUpdateExecution() {
        val now = LocalDateTime.now().format(formatter)
        val count = preferences.getInt(KEY_PRICE_EXECUTION_COUNT, 0) + 1
        val previousLast = preferences.getString(KEY_LAST_PRICE_UPDATE, null)

        with(preferences.edit()) {
            putString(KEY_LAST_PRICE_UPDATE, now)
            if (previousLast != null) {
                putString(KEY_LAST_PRICE_UPDATE_2, previousLast)
            }
            putInt(KEY_PRICE_EXECUTION_COUNT, count)
            apply()
        }
    }

    /**
     * 마지막 마스터 파일 업데이트 시간
     */
    fun getLastMasterFileUpdate(): String {
        return preferences.getString(KEY_LAST_MASTER_FILE_UPDATE, "Never") ?: "Never"
    }

    /**
     * 마스터 파일 업데이트 실행 횟수
     */
    fun getMasterFileExecutionCount(): Int {
        return preferences.getInt(KEY_MASTER_FILE_EXECUTION_COUNT, 0)
    }

    /**
     * 마지막 시세 업데이트 시간
     */
    fun getLastPriceUpdate(): String {
        return preferences.getString(KEY_LAST_PRICE_UPDATE, "Never") ?: "Never"
    }

    /**
     * 두 번째 마스터 파일 업데이트 시간
     */
    fun getLastMasterFileUpdate2(): String {
        return preferences.getString(KEY_LAST_MASTER_FILE_UPDATE_2, "Never") ?: "Never"
    }

    /**
     * 두 번째 시세 업데이트 시간
     */
    fun getLastPriceUpdate2(): String {
        return preferences.getString(KEY_LAST_PRICE_UPDATE_2, "Never") ?: "Never"
    }

    /**
     * 시세 업데이트 실행 횟수
     */
    fun getPriceExecutionCount(): Int {
        return preferences.getInt(KEY_PRICE_EXECUTION_COUNT, 0)
    }

    /**
     * 실행 통계 정보
     */
    fun getExecutionStats(): String {
        return buildString {
            appendLine("=== Worker 실행 통계 ===")
            appendLine("마스터 파일:")
            appendLine("  최근 실행: ${getLastMasterFileUpdate()}")
            appendLine("  이전 실행: ${getLastMasterFileUpdate2()}")
            appendLine("  총 실행 횟수: ${getMasterFileExecutionCount()}회")
            appendLine()
            appendLine("시세 업데이트:")
            appendLine("  최근 실행: ${getLastPriceUpdate()}")
            appendLine("  이전 실행: ${getLastPriceUpdate2()}")
            appendLine("  총 실행 횟수: ${getPriceExecutionCount()}회")
        }
    }

    /**
     * 통계 초기화
     * 디버깅이나 테스트를 위해 모든 통계 데이터를 삭제
     */
    fun resetStats() {
        preferences.edit()
            .remove(KEY_LAST_MASTER_FILE_UPDATE)
            .remove(KEY_LAST_MASTER_FILE_UPDATE_2)
            .remove(KEY_MASTER_FILE_EXECUTION_COUNT)
            .remove(KEY_LAST_PRICE_UPDATE)
            .remove(KEY_LAST_PRICE_UPDATE_2)
            .remove(KEY_PRICE_EXECUTION_COUNT)
            .apply()
    }
}
