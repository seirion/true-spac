package com.trueedu.spac.worker

import com.trueedu.spac.data.stocks.PriceManager.PriceFetchFailure
import com.trueedu.spac.data.stocks.PriceManager.PriceFetchResult
import kotlinx.coroutines.CancellationException

/**
 * 주기적 시세 업데이트(StockPriceWorker)의 분석 이벤트.
 *
 * 워커가 끝나는 모든 경로에서 다음 중 하나가 남도록 한다.
 * - [COMPLETED]: Firebase 저장 성공 (num: 저장한 종목 수, total: 조회 대상 종목 수)
 * - [FAILED]: 시세를 갱신하지 못함. reason 으로 원인을 구분한다.
 * - [SKIPPED]: 갱신 대상 시간이 아니라서 의도적으로 건너뜀
 */
object PriceUpdateEvents {
    const val COMPLETED = "price__write_completed"
    const val FAILED = "price__write_failed"
    const val SKIPPED = "price__skipped"

    // FAILED 의 reason 값
    const val REASON_INVALID_USER_KEY = "invalid_user_key"
    const val REASON_NO_STOCKS = "no_stocks"
    const val REASON_ALL_FETCH_FAILED = "all_fetch_failed"
    const val REASON_FETCH_EXCEPTION = "fetch_exception"
    const val REASON_EMPTY_PRICE_MAP = "empty_price_map"
    const val REASON_FIREBASE_WRITE = "firebase_write_failed"
    const val REASON_EXCEPTION = "exception"
    const val REASON_CANCELLED = "cancelled"

    // SKIPPED 의 reason 값
    const val REASON_NOT_UPDATE_TIME = "not_update_time"

    /** 시세 조회 결과가 비어 있을 때의 FAILED 파라미터 */
    fun emptyResultParams(result: PriceFetchResult, spacReady: Boolean): Map<String, Any> {
        val reason = when (result.failure) {
            PriceFetchFailure.INVALID_USER_KEY -> REASON_INVALID_USER_KEY
            PriceFetchFailure.NO_STOCKS -> REASON_NO_STOCKS
            PriceFetchFailure.ALL_FETCH_FAILED -> REASON_ALL_FETCH_FAILED
            PriceFetchFailure.EXCEPTION -> REASON_FETCH_EXCEPTION
            null -> REASON_EMPTY_PRICE_MAP
        }
        return buildMap {
            put("reason", reason)
            put("num", 0)
            put("total", result.requested)
            put("spac_ready", spacReady)
            result.errorName?.let { put("error", it) }
        }
    }

    /** Firebase 저장이 false 를 돌려준 경우의 FAILED 파라미터 */
    fun writeFailedParams(num: Int, total: Int): Map<String, Any> =
        mapOf("reason" to REASON_FIREBASE_WRITE, "num" to num, "total" to total)

    /** 워커에서 예외가 난 경우의 FAILED 파라미터 (취소는 별도 reason) */
    fun exceptionParams(e: Throwable): Map<String, Any> =
        mapOf(
            "reason" to if (e is CancellationException) REASON_CANCELLED else REASON_EXCEPTION,
            "error" to e.javaClass.simpleName,
        )

    fun completedParams(num: Int, total: Int): Map<String, Any> =
        mapOf("num" to num, "total" to total)

    fun skippedParams(reason: String): Map<String, Any> = mapOf("reason" to reason)
}
