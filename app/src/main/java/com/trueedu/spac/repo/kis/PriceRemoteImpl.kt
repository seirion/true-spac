package com.trueedu.spac.repo.kis

import com.trueedu.spac.di.NormalService
import com.trueedu.spac.network.apiCallFlow
import com.trueedu.spac.repo.kis.services.PriceService


class PriceRemoteImpl(
    @NormalService
    private val priceService: PriceService
): PriceRemote {
    override fun currentPrice(code: String) = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "FHKST01010100",
            "custtype" to "P",
        )
        val queries = mapOf(
            "FID_COND_MRKT_DIV_CODE" to "J", // 주식, ETF, ETN
            "FID_INPUT_ISCD" to code,
        )
        priceService.currentPrice(headers, queries)
    }

    override fun dailyPrice(
        code: String,
        from: String, // yyyyMMdd
        to: String, // yyyyMMdd
    ) = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "FHKST03010100",
            "custtype" to "P",
        )
        val queries = mapOf(
            "FID_COND_MRKT_DIV_CODE" to "J", // 주식, ETF, ETN
            "FID_INPUT_ISCD" to code,
            "FID_INPUT_DATE_1" to from,
            "FID_INPUT_DATE_2" to to,
            "FID_PERIOD_DIV_CODE" to "D", // D:일봉, W:주봉, M:월봉, Y:년봉
            "FID_ORG_ADJ_PRC" to "1", // 	0:수정주가 1:원주가
        )
        priceService.dailyPrice(headers, queries)
    }
}
