package com.trueedu.spac.util.formatter

/**
 * yyyyMMdd 형식의 스트링을 yyyy.MM.dd 형식으로 변환한다.
 */
fun dateFormat(s: String?): String {
    return if (s?.length == 8) {
        listOf(s.substring(0, 4), s.substring(4, 6), s.substring(6, 8)).joinToString(".")
    } else {
        s ?: ""
    }
}

fun dateFormat1(s: String?): String {
    return if (s?.length == 8) {
        listOf(s.substring(0, 4), s.substring(4, 6), s.substring(6, 8)).joinToString("-")
    } else {
        s ?: ""
    }
}

/**
 * api 를 통해서 받은 숫자 형식의 스트링 값을 숫자 포맷으로 변경한 스트링 반환
 */
fun numberFormatString(s: String?): String {
    if (s.isNullOrEmpty()) return "0"
    if (s.first() == '-') return "-" + numberFormatString(s.substring(1))

    val digits = s.dropWhile { it == '0' }
    if (digits.isEmpty()) return "0"
    try {
        return intFormatter.format(digits.toDouble())
    } catch (e: NumberFormatException) {
        return "0"
    }
}

fun String?.safeLong(): Long {
    if (this.isNullOrEmpty()) return 0L
    if (this.first() == '-') return -1 * this.substring(1).safeLong()

    val digits = this.dropWhile { it == '0' }
    if (digits.isEmpty()) return 0L
    try {
        return digits.toLong()
    } catch (e: NumberFormatException) {
        return 0L
    }
}

fun String?.safeDouble(): Double {
    if (this.isNullOrEmpty()) return 0.0
    if (this.first() == '-') return -1 * this.substring(1).safeDouble()

    val digits = this.dropWhile { it == '0' }
    if (digits.isEmpty()) return 0.0
    try {
        return digits.toDouble()
    } catch (e: NumberFormatException) {
        return 0.0
    }
}

fun Boolean.toYnString() = if (this) "Y" else "N"
