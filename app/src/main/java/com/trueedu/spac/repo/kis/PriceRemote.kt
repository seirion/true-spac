package com.trueedu.spac.repo.kis

import com.trueedu.spac.api.model.dto.price.DailyPriceResponse
import com.trueedu.spac.api.model.dto.price.PriceResponse
import kotlinx.coroutines.flow.Flow

/**
 * 각종 시세 관련 API
 */
interface PriceRemote {
    fun currentPrice(code: String): Flow<PriceResponse>

    /**
     * 일 별 시세 - 최대 30건
     */
    fun dailyPrice(code: String, from: String, to: String): Flow<DailyPriceResponse>
}
