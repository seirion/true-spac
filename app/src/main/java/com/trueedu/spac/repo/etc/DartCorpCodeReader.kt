package com.trueedu.spac.repo.etc

import com.trueedu.spac.data.log.logE
import com.trueedu.spac.data.stocks.DartCorpCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * text 파일을 읽어서 DartCorpCode 객체로 변환
 */
suspend fun readDartCorpCode(): Map<String, DartCorpCode> = withContext(Dispatchers.IO) {
    return@withContext try {
        val url = "https://raw.githubusercontent.com/true-education/true-education.github.io/refs/heads/main/data/dart.txt"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000 // 10초
        connection.readTimeout = 10_000 // 10초

        try {
            val text = connection.inputStream.bufferedReader().use { it.readText() }

            text.lines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    try {
                        val parts = line.split(" ")
                        if (parts.size != 3) {
                            logE("Invalid DartCorpCode format: $line")
                            return@mapNotNull null
                        }
                        DartCorpCode(
                            corpCode = parts[0],
                            nameKr = parts[1],
                            code = parts[2]
                        )
                    } catch (e: Exception) {
                        logE("Failed to parse DartCorpCode line: $line", e)
                        null
                    }
                }
                .associateBy(DartCorpCode::code)
        } finally {
            connection.disconnect()
        }
    } catch (e: Exception) {
        logE("Failed to read DartCorpCode data", e)
        emptyMap()
    }
}
