package com.trueedu.spac.data.stocks

import androidx.compose.runtime.mutableStateOf
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.firebase.StockInfoKospi
import com.trueedu.spac.data.stocks.PriceManager.PriceFetchFailure
import com.trueedu.spac.data.user.TokenKeyManager
import com.trueedu.spac.repo.firebase.FirebasePriceDatabase
import com.trueedu.spac.repo.kis.PriceRemote
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.repo.local.UserKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceManagerFetchTest {
    private val stock = StockInfoKospi("123456", "테스트스팩", "")

    private fun manager(
        userKey: UserKey? = UserKey("k", "s", null, null),
        spacList: List<StockInfo> = emptyList(),
        poolStatus: StockPool.Status = StockPool.Status.LOADING,
        onLoad: (androidx.compose.runtime.MutableState<List<StockInfo>>) -> Unit = {},
        remote: PriceRemote = mockk(relaxed = true),
    ): Triple<PriceManager, StockPool, androidx.compose.runtime.MutableState<List<StockInfo>>> {
        val spacState = mutableStateOf(spacList)
        val spacManager = mockk<SpacManager>()
        every { spacManager.spacList } returns spacState
        val tokenKeyManager = mockk<TokenKeyManager>()
        every { tokenKeyManager.userKey } returns mutableStateOf(userKey)
        val stockPool = mockk<StockPool>(relaxed = true)
        every { stockPool.status } returns MutableStateFlow(poolStatus)
        every { stockPool.loadStockInfo() } answers { onLoad(spacState) }
        val pm = PriceManager(
            local = mockk<Local>(relaxed = true),
            spacManager = spacManager,
            priceRemote = remote,
            firebasePriceManager = mockk<FirebasePriceDatabase>(relaxed = true),
            tokenKeyManager = tokenKeyManager,
            stockPool = stockPool,
            applicationScope = CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined),
        )
        return Triple(pm, stockPool, spacState)
    }

    @Test
    fun `UserKey 가 없으면 INVALID_USER_KEY`() = runBlocking {
        val (pm, _, _) = manager(userKey = null)
        val r = pm.fetchPrices(forceRefresh = true)
        assertTrue(r.prices.isEmpty())
        assertEquals(PriceFetchFailure.INVALID_USER_KEY, r.failure)
    }

    @Test
    fun `종목 목록이 비어 있으면 NO_STOCKS 이고 조회하지 않는다`() = runBlocking {
        val remote = mockk<PriceRemote>(relaxed = true)
        val (pm, _, _) = manager(spacList = emptyList(), remote = remote)
        val r = pm.fetchPrices(forceRefresh = true)
        assertEquals(PriceFetchFailure.NO_STOCKS, r.failure)
        assertEquals(0, r.requested)
        verify(exactly = 0) { remote.currentPrice(any()) }
    }

    @Test
    fun `모든 종목 조회가 실패하면 ALL_FETCH_FAILED 이고 요청 수를 돌려준다`() = runBlocking {
        val remote = mockk<PriceRemote>()
        every { remote.currentPrice(any()) } returns flow { throw RuntimeException("boom") }
        val (pm, _, _) = manager(spacList = listOf(stock), remote = remote)
        val r = pm.fetchPrices(forceRefresh = true)
        assertTrue(r.prices.isEmpty())
        assertEquals(PriceFetchFailure.ALL_FETCH_FAILED, r.failure)
        assertEquals(1, r.requested)
    }

    @Test
    fun `종목 목록이 이미 있으면 로딩을 시작하지 않는다`() = runBlocking {
        val (pm, pool, _) = manager(spacList = listOf(stock))
        assertTrue(pm.ensureSpacListReady(timeoutMs = 200))
        verify(exactly = 0) { pool.loadStockInfo() }
    }

    @Test
    fun `종목 목록이 비어 있으면 로딩을 시작하고 채워지면 준비 완료`() = runBlocking {
        val (pm, pool, _) = manager(onLoad = { it.value = listOf(stock) })
        assertTrue(pm.ensureSpacListReady(timeoutMs = 2_000))
        verify(exactly = 1) { pool.loadStockInfo() }
    }

    @Test
    fun `StockPool 로딩이 실패하면 즉시 준비 실패`() = runBlocking {
        val (pm, _, _) = manager(poolStatus = StockPool.Status.FAIL)
        assertFalse(pm.ensureSpacListReady(timeoutMs = 2_000))
    }

    @Test
    fun `로딩이 끝나지 않으면 타임아웃 후 준비 실패`() = runBlocking {
        val (pm, _, _) = manager(poolStatus = StockPool.Status.LOADING)
        assertFalse(pm.ensureSpacListReady(timeoutMs = 300))
    }

    @Test
    fun `성공 결과에는 실패 사유가 없다`() {
        val r = PriceManager.PriceFetchResult(emptyMap(), 3)
        assertNull(r.failure)
    }
}
