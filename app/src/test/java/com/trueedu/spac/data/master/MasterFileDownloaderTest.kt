package com.trueedu.spac.data.master

import com.trueedu.spac.api.model.dto.firebase.UsStockInfo
import com.trueedu.spac.data.stocks.StockInfoDownloader
import com.trueedu.spac.repo.firebase.FirebaseUsStockDatabase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class MasterFileDownloaderTest {

    private lateinit var stockInfoDownloader: StockInfoDownloader
    private lateinit var firebaseUsStockDatabase: FirebaseUsStockDatabase
    private lateinit var masterFileDownloader: MasterFileDownloader

    @Before
    fun setUp() {
        stockInfoDownloader = mockk()
        firebaseUsStockDatabase = mockk()
        masterFileDownloader = MasterFileDownloader(
            stockInfoDownloader,
            firebaseUsStockDatabase
        )
    }

    @Test
    fun `downloadUsMasterFile - 정상적으로 다운로드하고 Firebase에 업로드`() = runTest {
        // given: 유효한 주식 데이터
        val stocks = listOf(
            createUsStockInfo("AAPL", "애플"),
            createUsStockInfo("TSLA", "테슬라"),
            createUsStockInfo("GOOGL", "구글")
        )
        coEvery { stockInfoDownloader.getUsStockInfoList() } returns stocks
        coEvery { firebaseUsStockDatabase.writeStocks(any()) } returns true

        // when
        masterFileDownloader.downloadUsMasterFile()

        // then: StockInfoDownloader가 호출되었는지 확인
        coVerify(exactly = 1) { stockInfoDownloader.getUsStockInfoList() }

        // then: Firebase에 데이터가 저장되었는지 확인
        coVerify(exactly = 1) {
            firebaseUsStockDatabase.writeStocks(
                match { map ->
                    map.size == 3 &&
                    map.containsKey("AAPL") &&
                    map.containsKey("TSLA") &&
                    map.containsKey("GOOGL")
                }
            )
        }
    }

    @Test
    fun `downloadUsMasterFile - 빈 리스트면 Firebase에 저장하지 않음`() = runTest {
        // given: 빈 주식 리스트
        coEvery { stockInfoDownloader.getUsStockInfoList() } returns emptyList()

        // when
        masterFileDownloader.downloadUsMasterFile()

        // then: StockInfoDownloader는 호출됨
        coVerify(exactly = 1) { stockInfoDownloader.getUsStockInfoList() }

        // then: Firebase에는 저장되지 않음
        coVerify(exactly = 0) { firebaseUsStockDatabase.writeStocks(any()) }
    }

    @Test
    fun `downloadUsMasterFile - 다운로드 실패 시 예외 발생`() = runTest {
        // given: 다운로드 실패
        val exception = RuntimeException("Network error")
        coEvery { stockInfoDownloader.getUsStockInfoList() } throws exception

        // when & then: 예외가 발생해야 함
        try {
            masterFileDownloader.downloadUsMasterFile()
            fail("예외가 발생해야 함")
        } catch (e: Exception) {
            assertEquals("Network error", e.message)
        }

        // then: Firebase에 저장되지 않음
        coVerify(exactly = 0) { firebaseUsStockDatabase.writeStocks(any()) }
    }

    @Test
    fun `downloadUsMasterFile - Firebase 저장 실패해도 예외 발생하지 않음`() = runTest {
        // given: 다운로드는 성공하지만 Firebase 저장 실패
        val stocks = listOf(
            createUsStockInfo("AAPL", "애플")
        )
        coEvery { stockInfoDownloader.getUsStockInfoList() } returns stocks
        coEvery { firebaseUsStockDatabase.writeStocks(any()) } returns false

        // when: 예외가 발생하지 않아야 함
        masterFileDownloader.downloadUsMasterFile()

        // then: Firebase 저장 시도는 됨
        coVerify(exactly = 1) { firebaseUsStockDatabase.writeStocks(any()) }
    }

    @Test
    fun `downloadUsMasterFile - 대량의 주식 데이터 처리`() = runTest {
        // given: 많은 주식 데이터
        val stocks = (1..1000).map { index ->
            createUsStockInfo("STOCK$index", "종목$index")
        }
        coEvery { stockInfoDownloader.getUsStockInfoList() } returns stocks
        coEvery { firebaseUsStockDatabase.writeStocks(any()) } returns true

        // when
        masterFileDownloader.downloadUsMasterFile()

        // then: 모든 데이터가 Map으로 변환되어 저장됨
        coVerify(exactly = 1) {
            firebaseUsStockDatabase.writeStocks(
                match { map ->
                    map.size == 1000
                }
            )
        }
    }

    @Test
    fun `downloadUsMasterFile - 중복된 종목 코드가 있으면 마지막 것만 유지`() = runTest {
        // given: 중복된 종목 코드
        val stocks = listOf(
            createUsStockInfo("AAPL", "애플1"),
            createUsStockInfo("AAPL", "애플2"),
            createUsStockInfo("TSLA", "테슬라")
        )
        coEvery { stockInfoDownloader.getUsStockInfoList() } returns stocks
        coEvery { firebaseUsStockDatabase.writeStocks(any()) } returns true

        // when
        masterFileDownloader.downloadUsMasterFile()

        // then: 중복 제거되어 2개만 저장됨
        coVerify(exactly = 1) {
            firebaseUsStockDatabase.writeStocks(
                match { map ->
                    map.size == 2 &&
                    map["AAPL"]?.nameKr == "애플2"  // 마지막 값으로 덮어씌워짐
                }
            )
        }
    }

    private fun createUsStockInfo(code: String, nameKr: String): UsStockInfo {
        // 실제 마스터 파일은 탭 구분이다. 고정폭으로 만들면 UsStockInfo.from 이
        // 필드를 1개로 보고 통째로 거른다.
        val data = listOf(
            "US",           // ncod 국가코드
            "22",           // exid 거래소ID
            "NAS",          // excd 거래소코드
            "나스닥",        // exnm 거래소명
            code,           // symb 심볼
            "NAS$code",     // rsym 실시간심볼
            nameKr,         // knam 한글명
            "$code INC",    // enam 영문명
            "2",            // stis 증권타입
            "USD",          // curr 통화
            "4",            // zdiv 소수점자리
            "",             // ztyp 데이터타입
            "100.0000",     // base 기준가
            "1",            // bnit 매수호가단위
            "1",            // anit 매도호가단위
            "930",          // mstm 시장시작시간
            "1600",         // metm 시장종료시간
            "N",            // isdr DR여부
            "",             // drcd DR국가코드
            "730",          // icod 업종분류코드
            "0",            // sjong 지수구성종목여부
            "0",            // ttyp 호가단위타입
            "",             // etyp ETP타입
            "",             // ttyp_sb 호가단위타입상세
        ).joinToString("\t")
        return UsStockInfo.from(data)
    }
}
