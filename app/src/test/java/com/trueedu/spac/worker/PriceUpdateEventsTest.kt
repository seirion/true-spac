package com.trueedu.spac.worker

import com.trueedu.spac.data.stocks.PriceManager.PriceFetchFailure
import com.trueedu.spac.data.stocks.PriceManager.PriceFetchResult
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PriceUpdateEventsTest {
    @Test
    fun `빈 시세 결과의 사유가 실패 사유로 구분된다`() {
        fun reason(f: PriceFetchFailure?) =
            PriceUpdateEvents.emptyResultParams(PriceFetchResult(emptyMap(), 5, f), true)["reason"]
        assertEquals("invalid_user_key", reason(PriceFetchFailure.INVALID_USER_KEY))
        assertEquals("no_stocks", reason(PriceFetchFailure.NO_STOCKS))
        assertEquals("all_fetch_failed", reason(PriceFetchFailure.ALL_FETCH_FAILED))
        assertEquals("fetch_exception", reason(PriceFetchFailure.EXCEPTION))
        assertEquals("empty_price_map", reason(null))
    }

    @Test
    fun `빈 결과 파라미터에 조회 대상 수와 종목 준비 여부가 들어간다`() {
        val p = PriceUpdateEvents.emptyResultParams(
            PriceFetchResult(emptyMap(), 0, PriceFetchFailure.NO_STOCKS), spacReady = false
        )
        assertEquals(0, p["num"])
        assertEquals(0, p["total"])
        assertEquals(false, p["spac_ready"])
        assertFalse(p.containsKey("error"))
    }

    @Test
    fun `조회 예외는 예외 이름을 함께 남긴다`() {
        val p = PriceUpdateEvents.emptyResultParams(
            PriceFetchResult(emptyMap(), 3, PriceFetchFailure.EXCEPTION, "IOException"), true
        )
        assertEquals("IOException", p["error"])
    }

    @Test
    fun `쓰기 실패와 완료 파라미터는 저장 수와 조회 대상 수를 담는다`() {
        assertEquals(
            mapOf("reason" to "firebase_write_failed", "num" to 60, "total" to 100),
            PriceUpdateEvents.writeFailedParams(60, 100)
        )
        assertEquals(mapOf("num" to 60, "total" to 100), PriceUpdateEvents.completedParams(60, 100))
    }

    @Test
    fun `워커 예외는 exception 이고 취소는 cancelled`() {
        assertEquals("exception", PriceUpdateEvents.exceptionParams(IllegalStateException("x"))["reason"])
        assertEquals("cancelled", PriceUpdateEvents.exceptionParams(CancellationException("x"))["reason"])
        assertEquals("IllegalStateException", PriceUpdateEvents.exceptionParams(IllegalStateException("x"))["error"])
    }
}
