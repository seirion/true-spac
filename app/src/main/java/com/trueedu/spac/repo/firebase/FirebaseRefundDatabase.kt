package com.trueedu.spac.repo.firebase

import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.spac.api.model.dto.firebase.RefundSchedule
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.util.toDateTimeCompactString
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseRealtimeDatabase 에서 환급 일정 데이터 처리
 */
@Singleton
class FirebaseRefundDatabase @Inject constructor(
): FirebaseDatabaseBase() {
    companion object {
        private const val BASE_PATH = "spac"
        private const val CHILD_PATH = "refund"
    }

    suspend fun loadRefundSchedule(): List<RefundSchedule> {
        logD("loadRefundSchedule()")
        return try {
            val ref = database.getReference(BASE_PATH)
            val snapshot = ref.child(CHILD_PATH)
            val list = snapshot.get().await()
                .getValue(object : GenericTypeIndicator<List<RefundSchedule>>() {})
            list ?: emptyList()
        } catch (e: Exception) {
            logD("loadRefundSchedule() failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun writeRefundSchedule(list: List<RefundSchedule>) {
        logD("writeRefundSchedule(): ${list.size}")
        val currentUser = firebaseCurrentUser() ?: run {
            logD("writeRefundSchedule() failed: currentUser null")
            return
        }
        val ref = database.getReference(BASE_PATH)
        val snapshot = ref.child(CHILD_PATH)
        snapshot.setValue(list).await()

        val metaRef = database.getReference("meta")
        metaRef.child("refundLastUpdatedAt").setValue(
            LocalDateTime.now()
                .toDateTimeCompactString()
                .dropLast(2) // ss 제거 하여 yyyyMMddHHmm 으로 변환
                .toLong()
        ).await()
    }
}
