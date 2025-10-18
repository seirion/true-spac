package com.trueedu.spac.repo.kis.services

import com.trueedu.spac.api.model.dto.price.DailyPriceResponse
import com.trueedu.spac.api.model.dto.price.PriceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap

interface PriceService {
    @GET("uapi/domestic-stock/v1/quotations/inquire-price")
    suspend fun currentPrice(
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>
    ): Response<PriceResponse>

    @GET("uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
    suspend fun dailyPrice(
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>
    ): Response<DailyPriceResponse>
}
