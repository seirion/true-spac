package com.trueedu.spac.api.model.dto.firebase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StockInfoKosdaqTest {

    private fun isSpac(name: String) = StockInfoKosdaq("000000", name, "").isSpac

    @Test
    fun `isSpac - 스팩 이름 패턴은 true`() {
        assertTrue(isSpac("IBKS제24호스팩"))
        assertTrue(isSpac("유진스팩10호"))
        assertTrue(isSpac("하나33호스팩"))
        assertTrue(isSpac("엘에스스팩1호"))
        assertTrue(isSpac("테스트스팩"))
        assertTrue(isSpac("에스케이증권제14호기업인수목적"))
    }

    @Test
    fun `isSpac - 이름 중간에 스팩이 들어간 일반 종목은 false`() {
        assertFalse(isSpac("아인스팩씨엔에스"))
    }
}
