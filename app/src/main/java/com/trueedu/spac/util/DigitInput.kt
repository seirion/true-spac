package com.trueedu.spac.util


/**
 * 양수만 처리한다고 가정함
 */
fun getDigitInput(s: String, maxLength: Int = 12): String {
    val r = s.filter(Char::isDigit)
        .dropWhile { it == '0' }
        .take(maxLength)

    return r.ifEmpty { "0" }
}

/**
 * 한국 주식 호가 한 단계 증가
 * https://www.k-otc.or.kr/public/rule/tradeInfo
 */
fun increasePrice(base: String): String {
    val v = base.toLongOrNull() ?: return base
    val n = increasePrice(v)
    return n.toString()
}

fun decreasePrice(base: String): String {
    val v = base.toLongOrNull() ?: return base
    val n = decreasePrice(v)
    return n.toString()
}

private fun increasePrice(base: Long): Long {
    return when {
        base < 2_000L -> base + 1L
        base < 5_000L-> base + 5L
        base < 20_000L -> base + 10L
        base < 50_000L -> base + 50L
        base < 200_000L -> base + 100L
        base < 500_000L -> base + 500L
        else -> base + 1_000L
    }
}

private fun decreasePrice(base: Long): Long {
    return when {
        base == 0L -> base
        base <= 2_000L -> base - 1L
        base <= 5_000L-> base - 5L
        base <= 20_000L -> base - 10L
        base <= 50_000L -> base - 50L
        base <= 200_000L -> base - 100L
        base <= 500_000L -> base - 500L
        else -> base - 1_000L
    }
}

fun increaseQuantity(base: String): String {
    if (base.isEmpty()) return "1"
    val v = base.toLongOrNull() ?: return base
    return (v + 1).toString()
}

fun decreaseQuantity(base: String): String {
    if (base.isEmpty()) return "0"
    val v = base.toLongOrNull() ?: return base
    return (v - 1).coerceAtLeast(0L).toString()
}
