package com.trueedu.spac.repo.firebase

import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.spac.api.model.dao.StockPriceDao
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePriceDatabase @Inject constructor(

): FirebaseDatabaseBase() {
    companion object {
        private const val META_KEY = "meta"
        private const val SNAPSHOT_KEY = "spac"
        private const val CHILD_KEY = "price"
    }

    /**
     * Firebase 인증 체크를 수행하고, 인증되지 않은 경우 기본값 반환
     */
    private suspend fun <T> withAuthCheck(
        onFail: T,
        block: suspend () -> T
    ): T {
        return if (firebaseCurrentUser() == null) {
            logD("Firebase auth failed: currentUser null")
            onFail
        } else {
            block()
        }
    }

    /**
     * yyyyMMddHHmm 형식의 Long 타입 반환
     */
    suspend fun lastUpdatedAt(): Long {
        val snapshot = database.getReference(META_KEY).get().await()
        val lastUpdatedAt = snapshot.child("priceLastUpdatedAt").getValue(Long::class.java)
        return lastUpdatedAt ?: 0L
    }

    /**
     * key: stockId, value: StockPriceDao
     */
    suspend fun load(): Map<String, StockPriceDao> {
        logD("load()")

        return withAuthCheck(emptyMap()) {
            try {
                val ref = database.getReference(SNAPSHOT_KEY)
                val snapshot = ref.child(CHILD_KEY)
                val m = snapshot.get().await()
                    .getValue(object : GenericTypeIndicator<Map<String, StockPriceDao>>() {})

                m ?: emptyMap()
            } catch (e: Exception) {
                logE("Failed to load price from Firebase", e)
                emptyMap()
            }
        }
    }

    suspend fun write(m: Map<String, StockPriceDao>, customTimestamp: LocalDateTime? = null) {
        logD("write Price data: ${m.size}")

        withAuthCheck(Unit) {
            try {
                val ref = database.getReference(SNAPSHOT_KEY)
                val snapshot = ref.child(CHILD_KEY)

                // 시세 데이터 저장
                snapshot.setValue(m).await()

                // 마지막 업데이트 시간 기록 (yyyyMMddHHmm 형식)
                val timestamp = customTimestamp ?: LocalDateTime.now()
                val formattedTimestamp = timestamp
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
                    .toLong()

                database.getReference(META_KEY)
                    .child("priceLastUpdatedAt")
                    .setValue(formattedTimestamp)
                    .await()

                logD("write() completed: ${m.size} items, timestamp: $formattedTimestamp")
            } catch (e: Exception) {
                logE("Failed to write price to Firebase", e)
            }
        }
    }
}
