package com.trueedu.spac.repo.etc

import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.ui.merge.model.MergeSchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

private const val DEFAULT_MERGE_SCHEDULE_URL =
    "https://raw.githubusercontent.com/true-education/true-education.github.io/refs/heads/main/data/merge.txt"

/**
 * URL이 가리키는 텍스트 파일(JSON 문자열)을 읽어서 [List]<[MergeSchedule]> 로 파싱합니다.
 *
 * 파일 포맷 예시:
 * - `[ { ... }, { ... } ]`
 */
suspend fun readMergeSchedule(
    url: String = DEFAULT_MERGE_SCHEDULE_URL,
    okHttpClient: OkHttpClient = OkHttpClient(),
    json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    },
): List<MergeSchedule> = withContext(Dispatchers.IO) {
    val request = Request.Builder()
        .url(url)
        .get()
        .build()

    try {
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                logD("Failed to read merge schedule. code=${response.code}, message=${response.message}")
                return@withContext emptyList()
            }

            val bodyString = response.body?.string()?.trim().orEmpty()
            if (bodyString.isBlank()) {
                logD("Merge schedule content is empty.")
                return@withContext emptyList()
            }

            return@withContext try {
                json.decodeFromString<List<MergeSchedule>>(bodyString)
            } catch (e: SerializationException) {
                logE(e, "Failed to parse merge schedule JSON.")
                emptyList()
            }
        }
    } catch (e: Exception) {
        logE(e, "Failed to fetch merge schedule.")
        emptyList()
    }
}
