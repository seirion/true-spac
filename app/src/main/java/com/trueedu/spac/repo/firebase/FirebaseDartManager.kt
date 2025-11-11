package com.trueedu.spac.repo.firebase

import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.spac.dart.model.DartListResponse
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.util.toDateTimeCompactString
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseRealtimeDatabase 에서 dart 데이터 처리
 */
@Singleton
class FirebaseDartManager @Inject constructor(
): FirebaseDatabaseBase() {
    companion object {
        private const val BASE_PATH = "dart"
        private const val CHILD_PATH = "list"
    }

    /**
     * yyyyMMddHHmm 형식의 Long 타입 반환
     */
    suspend fun lastUpdatedAt(): Long {
        if (firebaseCurrentUser() == null) {
            logD("lastUpdatedAt() failed: currentUser null")
            return 0L
        }
        val snapshot = database.getReference("meta").get().await()
        val lastUpdatedAt = snapshot.child("dartLastUpdatedAt").getValue(Long::class.java)
        return lastUpdatedAt ?: 0L
    }

    suspend fun loadDartList(): List<DartListResponse> {
        logD("loadDartList()")
        if (firebaseCurrentUser() == null) {
            logD("loadDartList() failed: currentUser null")
            return emptyList()
        }
        val ref = database.getReference(BASE_PATH)
        val snapshot = ref.child(CHILD_PATH)
        val list = snapshot.get().await()
            .getValue(object : GenericTypeIndicator<List<DartListResponse>>() {})
        return list ?: emptyList()
    }

    suspend fun writeDartList(list: List<DartListResponse>): Boolean {
        logD("writeDartList(): ${list.size}")
        if (firebaseCurrentUser() == null) {
            logD("writeDartList() failed: currentUser null")
            return false
        }
        return try {
            val ref = database.getReference(BASE_PATH)
            val snapshot = ref.child(CHILD_PATH)
            snapshot.setValue(list).await()

            val metaRef = database.getReference("meta")
            metaRef.child("dartLastUpdatedAt").setValue(
                LocalDateTime.now()
                    .toDateTimeCompactString()
                    .dropLast(2) // ss 제거 하여 yyyyMMddHHmm 으로 변환
                    .toLong()
            ).await()
            true
        } catch (e: Exception) {
            logD("writeDartList() failed: ${e.message}")
            false
        }
    }
}
