package com.trueedu.spac.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * 날짜/시간 파싱 유틸리티
 */
object DateTimeFormat {
    /**
     * yyyy-MM-dd 형식
     * 예: "2025-10-12"
     */
    val DATE_DASH: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * yyyyMMdd 형식
     * 예: "20251012"
     */
    val DATE_COMPACT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    /**
     * yyyy-MM-dd HH:mm:ss 형식
     * 예: "2025-10-12 01:30:45"
     */
    val DATETIME_DASH: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * yyyyMMddHHmmss 형식
     * 예: "20251012013045"
     */
    val DATETIME_COMPACT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    /**
     * HH:mm:ss 형식
     * 예: "01:30:45"
     */
    val TIME_COLON: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    /**
     * HHmmss 형식
     * 예: "013045"
     */
    val TIME_COMPACT: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")
}

/**
 * String을 LocalDate로 변환
 * 지원 형식:
 * - yyyy-MM-dd (예: "2025-10-12")
 * - yyyyMMdd (예: "20251012")
 *
 * @return 변환된 LocalDate 또는 null (파싱 실패 시)
 */
fun String.toLocalDate(): LocalDate? {
    if (this.isBlank()) return null

    return try {
        when {
            this.contains("-") -> LocalDate.parse(this, DateTimeFormat.DATE_DASH)
            this.length == 8 -> LocalDate.parse(this, DateTimeFormat.DATE_COMPACT)
            else -> null
        }
    } catch (e: DateTimeParseException) {
        null
    }
}

/**
 * String을 LocalDateTime으로 변환
 * 지원 형식:
 * - yyyy-MM-dd HH:mm:ss (예: "2025-10-12 01:30:45")
 * - yyyyMMddHHmmss (예: "20251012013045")
 *
 * @return 변환된 LocalDateTime 또는 null (파싱 실패 시)
 */
fun String.toLocalDateTime(): LocalDateTime? {
    if (this.isBlank()) return null

    return try {
        when {
            this.contains(" ") -> LocalDateTime.parse(this, DateTimeFormat.DATETIME_DASH)
            this.length == 14 -> LocalDateTime.parse(this, DateTimeFormat.DATETIME_COMPACT)
            else -> null
        }
    } catch (e: DateTimeParseException) {
        null
    }
}

/**
 * String을 LocalTime으로 변환
 * 지원 형식:
 * - HH:mm:ss (예: "01:30:45")
 * - HHmmss (예: "013045")
 *
 * @return 변환된 LocalTime 또는 null (파싱 실패 시)
 */
fun String.toLocalTime(): LocalTime? {
    if (this.isBlank()) return null

    return try {
        when {
            this.contains(":") -> LocalTime.parse(this, DateTimeFormat.TIME_COLON)
            this.length == 6 -> LocalTime.parse(this, DateTimeFormat.TIME_COMPACT)
            else -> null
        }
    } catch (e: DateTimeParseException) {
        null
    }
}

/**
 * LocalDate를 yyyy-MM-dd 형식 문자열로 변환
 */
fun LocalDate.toDateString(): String = this.format(DateTimeFormat.DATE_DASH)

/**
 * LocalDate를 yyyyMMdd 형식 문자열로 변환
 */
fun LocalDate.toDateCompactString(): String = this.format(DateTimeFormat.DATE_COMPACT)

/**
 * LocalDateTime을 yyyy-MM-dd HH:mm:ss 형식 문자열로 변환
 */
fun LocalDateTime.toDateTimeString(): String = this.format(DateTimeFormat.DATETIME_DASH)

/**
 * LocalDateTime을 yyyyMMddHHmmss 형식 문자열로 변환
 */
fun LocalDateTime.toDateTimeCompactString(): String = this.format(DateTimeFormat.DATETIME_COMPACT)

/**
 * LocalTime을 HH:mm:ss 형식 문자열로 변환
 */
fun LocalTime.toTimeString(): String = this.format(DateTimeFormat.TIME_COLON)

/**
 * LocalTime을 HHmmss 형식 문자열로 변환
 */
fun LocalTime.toTimeCompactString(): String = this.format(DateTimeFormat.TIME_COMPACT)

