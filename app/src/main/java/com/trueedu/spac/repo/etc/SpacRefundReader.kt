package com.trueedu.spac.repo.etc

import com.trueedu.spac.api.model.dto.firebase.SpacRefund
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * text 파일을 읽어서 SpacRefund 객체로 변환
 */
suspend fun readSpacRefund(): Map<String, SpacRefund> = withContext(Dispatchers.IO) {
    return@withContext try {
        val url = "https://raw.githubusercontent.com/true-education/true-education.github.io/refs/heads/main/data/v1.txt"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000 // 10초
        connection.readTimeout = 10_000 // 10초

        val text = connection.inputStream.bufferedReader().use { it.readText() }

        text.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val refund = SpacRefund.from(line)
                    refund.code?.let { code -> code to refund }
                } catch (e: Exception) {
                    logE("Failed to parse SpacRefund line: $line", e)
                    null
                }
            }
            .toMap()
    } catch (e: Exception) {
        logE("Failed to read SpacRefund data", e)
        emptyMap()
    }
}
