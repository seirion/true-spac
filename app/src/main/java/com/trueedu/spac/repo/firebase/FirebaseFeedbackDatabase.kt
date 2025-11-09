package com.trueedu.spac.repo.firebase

import com.trueedu.spac.api.model.dto.firebase.UserFeedback
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.util.toDateTimeCompactString
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseRealtimeDatabase 에서 사용자 피드백 데이터 처리
 */
@Singleton
class FirebaseFeedbackDatabase @Inject constructor(
): FirebaseDatabaseBase() {
    companion object {
        private const val BASE_PATH = "feedback"
    }

    suspend fun writeFeedback(feedback: UserFeedback): Boolean {
        logD("writeFeedback()")
        return try {
            val currentUser = firebaseCurrentUser()
            if (currentUser == null) {
                logD("writeFeedback() failed: currentUser null")
                return false
            }

            val timestamp = LocalDateTime.now()
                .toDateTimeCompactString()
                .toLong()

            val feedbackWithMetadata = feedback.copy(
                timestamp = timestamp,
                userId = currentUser.uid,
                userEmail = currentUser.email ?: ""
            )

            val ref = database.getReference(BASE_PATH)
            val newFeedbackRef = ref.push()
            newFeedbackRef.setValue(feedbackWithMetadata).await()

            logD("writeFeedback() success: timestamp=$timestamp")
            true
        } catch (e: Exception) {
            logD("writeFeedback() failed: ${e.message}")
            false
        }
    }
}