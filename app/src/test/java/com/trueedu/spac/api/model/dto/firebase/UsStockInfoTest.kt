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
     * US  22  NAS 나스닥  AAPL    NASAAPL 애플    APPLE INC   2   USD 4       267.4600    1   1   930 1600    N       730 0   0
     *
     * mastcode 구조체 기반 242자 고정 길이 문자열 형식
     */
    @Test
    fun `from - AAPL 데이터를 정확히 파싱`() {
        // given: AAPL 데이터
        val aaplData = buildString {
            append("US")                                        // ncod (2자): 국가코드
            append("22 ")                                       // exid (3자): 거래소ID
            append("NAS")                                       // excd (3자): 거래소코드
            append("나스닥".padEnd(16))                          // exnm (16자): 거래소명
            append("AAPL".padEnd(16))                           // symb (16자): 심볼
            append("NASAAPL".padEnd(16))                        // rsym (16자): 실시간심볼
            append("애플".padEnd(64))                            // knam (64자): 한글명
            append("APPLE INC".padEnd(64))                      // enam (64자): 영문명
            append("2")                                          // stis (1자): 증권타입 (2=Stock)
            append("USD ")                                       // curr (4자): 통화
            append("4")                                          // zdiv (1자): 소수점자리
            append(" ")                                          // ztyp (1자): 데이터타입
            append("267.4600".padEnd(12))                       // base (12자): 기준가
            append("1".padEnd(8))                               // bnit (8자): 매수호가단위
            append("1".padEnd(8))                               // anit (8자): 매도호가단위
            append("930 ")                                       // mstm (4자): 시장시작시간
            append("1600")                                       // metm (4자): 시장종료시간
            append("N")                                          // isdr (1자): DR여부
            append("  ")                                         // drcd (2자): DR국가코드
            append("730 ")                                       // icod (4자): 업종분류코드
            append("0")                                          // sjong (1자): 지수구성종목여부
            append("0")                                          // ttyp (1자): 호가단위타입
            append("   ")                                        // etyp (3자): ETP타입
            append("   ")                                        // ttyp_sb (3자): 호가단위타입상세
        }

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
        // given: TSLA 데이터
        val tslaData = buildString {
            append("US")                                        // ncod (2자)
            append("22 ")                                       // exid (3자)
            append("NAS")                                       // excd (3자)
            append("나스닥".padEnd(16))                          // exnm (16자)
            append("TSLA".padEnd(16))                           // symb (16자)
            append("NASTSLA".padEnd(16))                        // rsym (16자)
            append("테슬라".padEnd(64))                          // knam (64자)
            append("TESLA INC".padEnd(64))                      // enam (64자)
            append("2")                                          // stis (1자)
            append("USD ")                                       // curr (4자)
            append("4")                                          // zdiv (1자)
            append(" ")                                          // ztyp (1자)
            append("350.2500".padEnd(12))                       // base (12자)
            append("1".padEnd(8))                               // bnit (8자)
            append("1".padEnd(8))                               // anit (8자)
            append("930 ")                                       // mstm (4자)
            append("1600")                                       // metm (4자)
            append("N")                                          // isdr (1자)
            append("  ")                                         // drcd (2자)
            append("820 ")                                       // icod (4자)
            append("0")                                          // sjong (1자)
            append("0")                                          // ttyp (1자)
            append("   ")                                        // etyp (3자)
            append("   ")                                        // ttyp_sb (3자)
        }

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
        // given: NYSE 상장 종목
        val nyseData = buildString {
            append("US")                                        // ncod (2자)
            append("11 ")                                       // exid (3자)
            append("NYS")                                       // excd (3자)
            append("뉴욕증권거래소".padEnd(16))                   // exnm (16자)
            append("IBM".padEnd(16))                            // symb (16자)
            append("NYSIBM".padEnd(16))                         // rsym (16자)
            append("IBM".padEnd(64))                            // knam (64자)
            append("INTL BUSINESS MACHINES CORP".padEnd(64))    // enam (64자)
            append("2")                                          // stis (1자)
            append("USD ")                                       // curr (4자)
            append("4")                                          // zdiv (1자)
            append(" ")                                          // ztyp (1자)
            append("185.5000".padEnd(12))                       // base (12자)
            append("1".padEnd(8))                               // bnit (8자)
            append("1".padEnd(8))                               // anit (8자)
            append("930 ")                                       // mstm (4자)
            append("1600")                                       // metm (4자)
            append("N")                                          // isdr (1자)
            append("  ")                                         // drcd (2자)
            append("740 ")                                       // icod (4자)
            append("0")                                          // sjong (1자)
            append("0")                                          // ttyp (1자)
            append("   ")                                        // etyp (3자)
            append("   ")                                        // ttyp_sb (3자)
        }

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
        // given: S&P500 지수
        val indexData = buildString {
            append("US")                                        // ncod (2자)
            append("22 ")                                       // exid (3자)
            append("IDX")                                       // excd (3자)
            append("지수".padEnd(16))                            // exnm (16자)
            append("SPX".padEnd(16))                            // symb (16자)
            append("SPX".padEnd(16))                            // rsym (16자)
            append("S&P500지수".padEnd(64))                      // knam (64자)
            append("S&P 500 INDEX".padEnd(64))                  // enam (64자)
            append("1")                                          // stis (1자): 1=지수
            append("USD ")                                       // curr (4자)
            append("2")                                          // zdiv (1자)
            append(" ")                                          // ztyp (1자)
            append("4500.00".padEnd(12))                        // base (12자)
            append("0".padEnd(8))                               // bnit (8자)
            append("0".padEnd(8))                               // anit (8자)
            append("930 ")                                       // mstm (4자)
            append("1600")                                       // metm (4자)
            append("N")                                          // isdr (1자)
            append("  ")                                         // drcd (2자)
            append("    ")                                       // icod (4자)
            append("1")                                          // sjong (1자): 구성종목있음
            append("0")                                          // ttyp (1자)
            append("   ")                                        // etyp (3자)
            append("   ")                                        // ttyp_sb (3자)
        }

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
        // given: ETF 데이터
        val etfData = buildString {
            append("US")                                        // ncod (2자)
            append("22 ")                                       // exid (3자)
            append("NAS")                                       // excd (3자)
            append("나스닥".padEnd(16))                          // exnm (16자)
            append("QQQ".padEnd(16))                            // symb (16자)
            append("NASQQQ".padEnd(16))                         // rsym (16자)
            append("QQQ ETF".padEnd(64))                        // knam (64자)
            append("INVESCO QQQ TRUST".padEnd(64))              // enam (64자)
            append("3")                                          // stis (1자): 3=ETP(ETF)
            append("USD ")                                       // curr (4자)
            append("4")                                          // zdiv (1자)
            append(" ")                                          // ztyp (1자)
            append("380.5000".padEnd(12))                       // base (12자)
            append("1".padEnd(8))                               // bnit (8자)
            append("1".padEnd(8))                               // anit (8자)
            append("930 ")                                       // mstm (4자)
            append("1600")                                       // metm (4자)
            append("N")                                          // isdr (1자)
            append("  ")                                         // drcd (2자)
            append("    ")                                       // icod (4자)
            append("0")                                          // sjong (1자)
            append("0")                                          // ttyp (1자)
            append("001")                                        // etyp (3자): 001=ETF
            append("   ")                                        // ttyp_sb (3자)
        }

        // when
        val result = UsStockInfo.from(etfData)

        // then
        assertEquals("QQQ", result.code)
        assertEquals("3", result.securityType)
        assertTrue("ETF여야 함", result.isEtf)
        assertEquals("001", result.etpType)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `from - 데이터 길이가 부족하면 예외 발생`() {
        // given: 242자 미만의 데이터
        val invalidData = "US" + "AAPL"

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

