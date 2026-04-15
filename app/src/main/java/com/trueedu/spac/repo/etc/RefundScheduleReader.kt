package com.trueedu.spac.repo.etc

import com.trueedu.spac.api.model.dto.firebase.RefundSchedule
import com.trueedu.spac.data.log.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

private val json = Json { ignoreUnknownKeys = true }

/**
 * GitHub raw URL에서 refund.json 을 읽어서 RefundSchedule 목록으로 변환
 * JSON 포맷: { "종목코드": { code, nameKr, refundAmount, date, fixed }, ... }
 */
suspend fun readRefundSchedules(): List<RefundSchedule> = withContext(Dispatchers.IO) {
    return@withContext try {
        val url = "https://raw.githubusercontent.com/true-education/true-education.github.io/refs/heads/main/data/refund.json"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        val text = connection.inputStream.bufferedReader().use { it.readText() }

        val map = json.decodeFromString(
            MapSerializer(String.serializer(), RefundSchedule.serializer()),
            text
        )
        map.values.sortedBy { it.date }
    } catch (e: Exception) {
        logE("Failed to read refund schedules", e)
        emptyList()
    }
}
