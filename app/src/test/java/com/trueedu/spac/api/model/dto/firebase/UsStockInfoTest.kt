package com.trueedu.spac.api.model.dto.firebase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsStockInfoTest {

    /**
     * AAPL (애플) 종목 데이터로 테스트
     * 실제 데이터 예시:
     * US	22	NAS	나스닥	AAPL	NASAAPL	애플	APPLE INC	2	USD	4		267.4600	1	1	930	1600	N		730	0	0
     *
     * 탭으로 구분된 24개 필드 형식
     */
    @Test
    fun `from - AAPL 데이터를 정확히 파싱`() {
        // given: AAPL 데이터 (탭으로 구분)
        val aaplData = listOf(
            "US",               // 0: 국가코드
            "22",               // 1: 거래소ID
            "NAS",              // 2: 거래소코드
            "나스닥",            // 3: 거래소명
            "AAPL",             // 4: 심볼
            "NASAAPL",          // 5: 실시간심볼
            "애플",              // 6: 한글명
            "APPLE INC",        // 7: 영문명
            "2",                // 8: 증권타입 (2=Stock)
            "USD",              // 9: 통화
            "4",                // 10: 소수점자리
            "",                 // 11: 데이터타입
            "267.4600",         // 12: 기준가
            "1",                // 13: 매수호가단위
            "1",                // 14: 매도호가단위
            "930",              // 15: 시장시작시간
            "1600",             // 16: 시장종료시간
            "N",                // 17: DR여부
            "",                 // 18: DR국가코드
            "730",              // 19: 업종분류코드
            "0",                // 20: 지수구성종목여부
            "0",                // 21: 호가단위타입
            "   ",              // 22: ETP타입
            "   "               // 23: 호가단위타입상세
        ).joinToString("\t")

        // when
        val result = UsStockInfo.from(aaplData)

        // then - 기본 정보
        assertNotNull(result)
        assertEquals("AAPL", result.code)
        assertEquals("애플", result.nameKr)

        // then - 미국 주식 특화 속성
        assertEquals("US", result.nationalCode)
        assertEquals("22", result.exchangeId)
        assertEquals("NAS", result.exchangeCode)
        assertEquals("나스닥", result.exchangeName)
        assertEquals("NASAAPL", result.realtimeSymbol)
        assertEquals("APPLE INC", result.englishName)

        // then - 증권 타입
        assertEquals("2", result.securityType)
        assertTrue("주식이어야 함", result.isStock)
        assertFalse("지수가 아니어야 함", result.isIndex)
        assertFalse("ETF가 아니어야 함", result.isEtf)

        // then - 거래 정보
        assertEquals("USD", result.currency)
        assertEquals("4", result.floatPosition)
        assertEquals("267.4600", result.basePrice)
        assertEquals("930", result.marketStartTime)
        assertEquals("1600", result.marketEndTime)

        // then - 기타 정보
        assertFalse("DR이 아니어야 함", result.isDr)
        assertEquals("730", result.industryCode)
        assertFalse("지수구성종목이 아니어야 함", result.hasIndexConstituents)
    }

    @Test
    fun `from - TSLA 데이터 파싱`() {
        // given: TSLA 데이터 (탭으로 구분)
        val tslaData = listOf(
            "US",               // 0: 국가코드
            "22",               // 1: 거래소ID
            "NAS",              // 2: 거래소코드
            "나스닥",            // 3: 거래소명
            "TSLA",             // 4: 심볼
            "NASTSLA",          // 5: 실시간심볼
            "테슬라",            // 6: 한글명
            "TESLA INC",        // 7: 영문명
            "2",                // 8: 증권타입
            "USD",              // 9: 통화
            "4",                // 10: 소수점자리
            "",                 // 11: 데이터타입
            "350.2500",         // 12: 기준가
            "1",                // 13: 매수호가단위
            "1",                // 14: 매도호가단위
            "930",              // 15: 시장시작시간
            "1600",             // 16: 시장종료시간
            "N",                // 17: DR여부
            "",                 // 18: DR국가코드
            "820",              // 19: 업종분류코드
            "0",                // 20: 지수구성종목여부
            "0",                // 21: 호가단위타입
            "   ",              // 22: ETP타입
            "   "               // 23: 호가단위타입상세
        ).joinToString("\t")

        // when
        val result = UsStockInfo.from(tslaData)

        // then
        assertEquals("TSLA", result.code)
        assertEquals("테슬라", result.nameKr)
        assertEquals("TESLA INC", result.englishName)
        assertEquals("NAS", result.exchangeCode)
        assertEquals("820", result.industryCode)
        assertTrue(result.isStock)
    }

    @Test
    fun `from - NYSE 상장 종목 파싱`() {
        // given: NYSE 상장 종목 (탭으로 구분)
        val nyseData = listOf(
            "US",                           // 0: 국가코드
            "11",                           // 1: 거래소ID
            "NYS",                          // 2: 거래소코드
            "뉴욕증권거래소",                 // 3: 거래소명
            "IBM",                          // 4: 심볼
            "NYSIBM",                       // 5: 실시간심볼
            "IBM",                          // 6: 한글명
            "INTL BUSINESS MACHINES CORP",  // 7: 영문명
            "2",                            // 8: 증권타입
            "USD",                          // 9: 통화
            "4",                            // 10: 소수점자리
            "",                             // 11: 데이터타입
            "185.5000",                     // 12: 기준가
            "1",                            // 13: 매수호가단위
            "1",                            // 14: 매도호가단위
            "930",                          // 15: 시장시작시간
            "1600",                         // 16: 시장종료시간
            "N",                            // 17: DR여부
            "",                             // 18: DR국가코드
            "740",                          // 19: 업종분류코드
            "0",                            // 20: 지수구성종목여부
            "0",                            // 21: 호가단위타입
            "   ",                          // 22: ETP타입
            "   "                           // 23: 호가단위타입상세
        ).joinToString("\t")

        // when
        val result = UsStockInfo.from(nyseData)

        // then
        assertEquals("IBM", result.code)
        assertEquals("IBM", result.nameKr)
        assertEquals("NYS", result.exchangeCode)
        assertEquals("뉴욕증권거래소", result.exchangeName)
        assertTrue(result.isStock)
    }

    @Test
    fun `from - S&P500 지수 데이터 파싱`() {
        // given: S&P500 지수 (탭으로 구분)
        val indexData = listOf(
            "US",               // 0: 국가코드
            "22",               // 1: 거래소ID
            "IDX",              // 2: 거래소코드
            "지수",              // 3: 거래소명
            "SPX",              // 4: 심볼
            "SPX",              // 5: 실시간심볼
            "S&P500지수",        // 6: 한글명
            "S&P 500 INDEX",    // 7: 영문명
            "1",                // 8: 증권타입 (1=지수)
            "USD",              // 9: 통화
            "2",                // 10: 소수점자리
            "",                 // 11: 데이터타입
            "4500.00",          // 12: 기준가
            "0",                // 13: 매수호가단위
            "0",                // 14: 매도호가단위
            "930",              // 15: 시장시작시간
            "1600",             // 16: 시장종료시간
            "N",                // 17: DR여부
            "",                 // 18: DR국가코드
            "",                 // 19: 업종분류코드
            "1",                // 20: 지수구성종목여부 (1=구성종목있음)
            "0",                // 21: 호가단위타입
            "   ",              // 22: ETP타입
            "   "               // 23: 호가단위타입상세
        ).joinToString("\t")

        // when
        val result = UsStockInfo.from(indexData)

        // then
        assertEquals("SPX", result.code)
        assertEquals("S&P500지수", result.nameKr)
        assertEquals("1", result.securityType)
        assertTrue("지수여야 함", result.isIndex)
        assertFalse("주식이 아니어야 함", result.isStock)
        assertTrue("지수구성종목이 있어야 함", result.hasIndexConstituents)
    }

    @Test
    fun `from - ETF 데이터 파싱`() {
        // given: ETF 데이터 (탭으로 구분)
        val etfData = listOf(
            "US",                   // 0: 국가코드
            "22",                   // 1: 거래소ID
            "NAS",                  // 2: 거래소코드
            "나스닥",                // 3: 거래소명
            "QQQ",                  // 4: 심볼
            "NASQQQ",               // 5: 실시간심볼
            "QQQ ETF",              // 6: 한글명
            "INVESCO QQQ TRUST",    // 7: 영문명
            "3",                    // 8: 증권타입 (3=ETP/ETF)
            "USD",                  // 9: 통화
            "4",                    // 10: 소수점자리
            "",                     // 11: 데이터타입
            "380.5000",             // 12: 기준가
            "1",                    // 13: 매수호가단위
            "1",                    // 14: 매도호가단위
            "930",                  // 15: 시장시작시간
            "1600",                 // 16: 시장종료시간
            "N",                    // 17: DR여부
            "",                     // 18: DR국가코드
            "",                     // 19: 업종분류코드
            "0",                    // 20: 지수구성종목여부
            "0",                    // 21: 호가단위타입
            "001",                  // 22: ETP타입 (001=ETF)
            "   "                   // 23: 호가단위타입상세
        ).joinToString("\t")

        // when
        val result = UsStockInfo.from(etfData)

        // then
        assertEquals("QQQ", result.code)
        assertEquals("3", result.securityType)
        assertTrue("ETF여야 함", result.isEtf)
        assertEquals("001", result.etpType)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `from - 필드 개수가 부족하면 예외 발생`() {
        // given: 24개 필드 미만의 데이터
        val invalidData = listOf("US", "22", "NAS", "AAPL").joinToString("\t")

        // when & then: 예외 발생
        UsStockInfo.from(invalidData)
    }

    @Test
    fun `no-arg constructor - Firebase 호환성`() {
        // when: 인자 없는 생성자로 객체 생성
        val stockInfo = UsStockInfo()

        // then: 기본값으로 초기화됨
        assertEquals("", stockInfo.code)
        assertEquals("", stockInfo.nameKr)
        assertEquals("", stockInfo.attributes)
    }
}

