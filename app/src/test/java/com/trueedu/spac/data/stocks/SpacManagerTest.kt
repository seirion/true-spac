package com.trueedu.spac.data.stocks

import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.data.user.RemoteConfig
import com.trueedu.spac.repo.local.Local
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import javax.inject.Provider

@OptIn(ExperimentalCoroutinesApi::class)
class SpacManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var spacManager: SpacManager
    private lateinit var local: Local
    private lateinit var stockPool: StockPool
    private lateinit var remoteConfig: RemoteConfig
    private lateinit var priceManagerProvider: Provider<PriceManager>
    private lateinit var priceManager: PriceManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        local = mockk(relaxed = true)
        stockPool = mockk(relaxed = true)
        remoteConfig = mockk(relaxed = true)
        priceManager = mockk(relaxed = true)
        priceManagerProvider = mockk {
            every { get() } returns priceManager
        }

        every { local.spacAnnualProfit } returns false
        every { remoteConfig.refundPriceVisible } returns true
        every { stockPool.status } returns mockk(relaxed = true)

        spacManager = SpacManager(
            local = local,
            stockPool = stockPool,
            remoteConfig = remoteConfig,
            priceManagerProvider = priceManagerProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search returns filtered stock list by code`() {
        // Given
        val stock1 = mockk<StockInfo> {
            every { code } returns "123456"
            every { nameKr } returns "테스트SPAC1"
        }
        val stock2 = mockk<StockInfo> {
            every { code } returns "234567"
            every { nameKr } returns "테스트SPAC2"
        }
        spacManager.spacList.value = listOf(stock1, stock2)

        // When
        val result = spacManager.search("1234")

        // Then
        assertEquals(1, result.size)
        assertEquals("123456", result[0].code)
    }

    @Test
    fun `search returns filtered stock list by name`() {
        // Given
        val stock1 = mockk<StockInfo> {
            every { code } returns "123456"
            every { nameKr } returns "테스트SPAC1"
        }
        val stock2 = mockk<StockInfo> {
            every { code } returns "234567"
            every { nameKr } returns "샘플SPAC2"
        }
        spacManager.spacList.value = listOf(stock1, stock2)

        // When
        val result = spacManager.search("테스트")

        // Then
        assertEquals(1, result.size)
        assertEquals("테스트SPAC1", result[0].nameKr)
    }

    @Test
    fun `search is case insensitive`() {
        // Given
        val stock = mockk<StockInfo> {
            every { code } returns "ABC123"
            every { nameKr } returns "테스트SPAC"
        }
        spacManager.spacList.value = listOf(stock)

        // When
        val result1 = spacManager.search("abc")
        val result2 = spacManager.search("ABC")

        // Then
        assertEquals(1, result1.size)
        assertEquals(1, result2.size)
        assertEquals(result1[0].code, result2[0].code)
    }

    @Test
    fun `search returns empty list when no match found`() {
        // Given
        val stock = mockk<StockInfo> {
            every { code } returns "123456"
            every { nameKr } returns "테스트SPAC"
        }
        spacManager.spacList.value = listOf(stock)

        // When
        val result = spacManager.search("없는검색어")

        // Then
        assertEquals(0, result.size)
    }
}

