package com.trueedu.spac.ui.feedback

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.api.model.dto.firebase.UserFeedback
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.repo.firebase.FirebaseFeedbackDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val firebaseFeedbackDatabase: FirebaseFeedbackDatabase,
    private val trueAnalytics: TrueAnalytics,
) : ViewModel() {

    var title by mutableStateOf("")
    var email by mutableStateOf("")
    var content by mutableStateOf("")
    var isSubmitting by mutableStateOf(false)

    fun submitFeedback(
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        if (isSubmitting) {
            return
        }

        if (title.isBlank() || content.isBlank()) {
            logD("제목과 내용은 필수입니다")
            onFail()
            return
        }

        isSubmitting = true

        viewModelScope.launch {
            try {
                val feedback = UserFeedback(
                    title = title.trim(),
                    email = email.trim(),
                    content = content.trim()
                )

                val success = firebaseFeedbackDatabase.writeFeedback(feedback)

                if (success) {
                    trueAnalytics.log("feedback_submit_success")
                    onSuccess()
                } else {
                    trueAnalytics.log("feedback_submit_fail")
                    onFail()
                }
            } catch (e: Exception) {
                logD("피드백 전송 실패: ${e.message}")
                trueAnalytics.log("feedback_submit_error", mapOf("error" to (e.message ?: "unknown")))
                onFail()
            } finally {
                isSubmitting = false
            }
        }
    }
}