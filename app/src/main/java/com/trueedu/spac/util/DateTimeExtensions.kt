package com.trueedu.spac.util

import com.trueedu.spac.api.model.dto.auth.TokenResponse
import com.trueedu.spac.api.model.dto.order.OrderResponse
import com.trueedu.spac.api.model.dto.price.DailyPrice
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * DTO 확장 함수들
 * 문자열로 되어 있는 날짜/시간 필드를 LocalDate/LocalDateTime/LocalTime으로 변환
 */

/**
 * TokenResponse 확장 함수
 */
fun TokenResponse.getExpiredDateTime(): LocalDateTime? {
    return accessTokenTokenExpired.toLocalDateTime()
}

/**
 * DailyPrice 확장 함수
 */
fun DailyPrice.getDate(): LocalDate? {
    return date?.toLocalDate()
}

/**
 * OrderDetail 확장 함수
 */
fun com.trueedu.spac.api.model.dto.order.OrderDetail.getOrderTime(): LocalTime? {
    return orderTime.toLocalTime()
}

