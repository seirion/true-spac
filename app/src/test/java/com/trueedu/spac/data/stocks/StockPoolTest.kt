package com.trueedu.spac.data.stocks

import com.trueedu.spac.api.model.dao.StockInfoLocal
import com.trueedu.spac.api.model.dto.firebase.StockInfoKosdaq
import com.trueedu.spac.api.model.dto.firebase.StockInfoKospi
import com.trueedu.spac.repo.firebase.FirebaseRealtimeDatabase
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.repo.local.StockLocal
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class StockPoolTest {
    private val local = mockk<Local>(relaxed = true)
    private val stockLocal = mockk<StockLocal>(relaxed = true)
    private val firebase = mockk<FirebaseRealtimeDatabase>(relaxed = true)
    private val downloader = mockk<StockInfoDownloader>()

    private fun pool() = StockPool(local, stockLocal, mockk(relaxed = true), firebase, downloader)

    private suspend fun StockPool.awaitLoaded() = withTimeout(5_000) {
        status.first { it != StockPool.Status.LOADING }
    }

    @Test
    fun `로딩 중 loadStockInfo 를 여러 번 호출해도 다운로드는 한 번만 한다`() = runBlocking {
        coEvery { downloader.getKrStockInfoList() } coAnswers {
            delay(300)
            listOf(
                StockInfoKospi("000020", "동화약품", ""),
                StockInfoKosdaq("469480", "IBKS제24호스팩", ""),
            )
        }
        val pool = pool()

        repeat(3) { pool.loadStockInfo() }

        assertEquals(StockPool.Status.SUCCESS, pool.awaitLoaded())
        coVerify(exactly = 1) { downloader.getKrStockInfoList() }
        assertEquals(1, pool.search { it.isKosdaq && it.isSpac }.size)
    }

    @Test
    fun `다운로드가 실패하면 로컬 데이터를 쓰고 갱신 시각을 남기지 않는다`() = runBlocking {
        coEvery { downloader.getKrStockInfoList() } throws IOException("kosdaq_code.mst.zip 다운로드 실패")
        coEvery { stockLocal.getAllStocks() } returns listOf(
            StockInfoLocal("000020", "동화약품", "", true),
            StockInfoLocal("469480", "IBKS제24호스팩", "", false),
        )
        val pool = pool()

        pool.loadStockInfo()

        assertEquals(StockPool.Status.SUCCESS, pool.awaitLoaded())
        verify(exactly = 0) { local.stockUpdatedAt = any() }
        coVerify(exactly = 0) { stockLocal.setAllStocks(any()) }
        assertEquals(1, pool.search { it.isKosdaq && it.isSpac }.size)
    }

    @Test
    fun `관리자 다운로드가 실패해도 이미 로딩된 종목이 있으면 SUCCESS 를 유지한다`() = runBlocking {
        coEvery { stockLocal.getAllStocks() } returns listOf(StockInfoLocal("000020", "동화약품", "", true))
        every { local.stockUpdatedAt } returns 202611271919L // 업데이트 불필요 → 로컬 사용
        val pool = pool()
        pool.loadStockInfo()
        pool.awaitLoaded()

        coEvery { downloader.getKrStockInfoList() } throws IOException("fail")
        runCatching { pool.downloadMasterFiles() }

        assertEquals(StockPool.Status.SUCCESS, pool.status.value)
    }
}
