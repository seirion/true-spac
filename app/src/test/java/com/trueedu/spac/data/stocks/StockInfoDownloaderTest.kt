package com.trueedu.spac.data.stocks

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class StockInfoDownloaderTest {

    private lateinit var context: Context
    private lateinit var downloader: StockInfoDownloader
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        tempDir = createTempDirectory().toFile()

        // Context.cacheDir를 임시 디렉토리로 설정
        every { context.cacheDir } returns tempDir

        downloader = StockInfoDownloader(context)
    }

    @After
    fun tearDown() {
        // 테스트 후 임시 디렉토리 정리
        tempDir.deleteRecursively()
    }

    @Test
    fun `readUsStockFromUnzippedFile - 유효한 데이터 파일을 정확히 파싱`() = runTest {
        // given: 유효한 미국 주식 데이터 파일
        val testFile = File(tempDir, "test_us_stocks.txt")
        testFile.writeText(buildTestData(), charset("CP949"))

        // when: private 메소드를 테스트하기 위해 리플렉션 사용
        val method = StockInfoDownloader::class.java.getDeclaredMethod(
            "readUsStockFromUnzippedFile",
            String::class.java
        )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val result = method.invoke(downloader, testFile.absolutePath) as List<*>

        // then
        assertEquals(3, result.size)

        // 파일이 자동으로 삭제되었는지 확인
        assertTrue("파일이 삭제되어야 함", !testFile.exists())
    }

    @Test
    fun `readUsStockFromUnzippedFile - 파싱 에러가 있어도 나머지는 계속 파싱`() = runTest {
        // given: 일부 잘못된 데이터를 포함한 파일
        val testFile = File(tempDir, "test_with_error.txt")
        val validData1 = buildStockData("AAPL", "애플", "APPLE INC")
        val invalidData = "INVALID_SHORT_DATA"  // 너무 짧은 데이터
        val validData2 = buildStockData("TSLA", "테슬라", "TESLA INC")

        testFile.writeText("$validData1\n$invalidData\n$validData2", charset("CP949"))

        // when
        val method = StockInfoDownloader::class.java.getDeclaredMethod(
            "readUsStockFromUnzippedFile",
            String::class.java
        )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val result = method.invoke(downloader, testFile.absolutePath) as List<*>

        // then: 유효한 2개만 파싱됨
        assertEquals(2, result.size)
    }

    @Test
    fun `readUsStockFromUnzippedFile - 파일이 없으면 빈 리스트 반환`() = runTest {
        // given: 존재하지 않는 파일
        val nonExistentFile = File(tempDir, "non_existent.txt")

        // when
        val method = StockInfoDownloader::class.java.getDeclaredMethod(
            "readUsStockFromUnzippedFile",
            String::class.java
        )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val result = method.invoke(downloader, nonExistentFile.absolutePath) as List<*>

        // then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `readUsStockFromUnzippedFile - 빈 파일은 빈 리스트 반환`() = runTest {
        // given: 빈 파일
        val emptyFile = File(tempDir, "empty.txt")
        emptyFile.writeText("", charset("CP949"))

        // when
        val method = StockInfoDownloader::class.java.getDeclaredMethod(
            "readUsStockFromUnzippedFile",
            String::class.java
        )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val result = method.invoke(downloader, emptyFile.absolutePath) as List<*>

        // then
        assertTrue(result.isEmpty())
    }

    private fun buildTestData(): String {
        val aapl = buildStockData("AAPL", "애플", "APPLE INC")
        val tsla = buildStockData("TSLA", "테슬라", "TESLA INC")
        val googl = buildStockData("GOOGL", "구글", "ALPHABET INC")

        return "$aapl\n$tsla\n$googl"
    }

    private fun buildStockData(symbol: String, nameKr: String, nameEn: String): String {
        // 실제 마스터 파일은 탭 구분이다. 고정폭으로 만들면 UsStockInfo.from 이
        // 필드를 1개로 보고 통째로 거른다.
        return listOf(
            "US",           // ncod 국가코드
            "22",           // exid 거래소ID
            "NAS",          // excd 거래소코드
            "나스닥",        // exnm 거래소명
            symbol,            // symb 심볼
            "NAS$symbol",      // rsym 실시간심볼
            nameKr,            // knam 한글명
            nameEn,       // enam 영문명
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
    }
}

