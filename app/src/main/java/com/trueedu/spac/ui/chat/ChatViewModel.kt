package com.trueedu.spac.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.api.model.dto.firebase.ChatMessage
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.repo.firebase.FirebaseChatDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val firebaseChatDatabase: FirebaseChatDatabase,
    private val trueAnalytics: TrueAnalytics,
) : ViewModel() {

    var input by mutableStateOf("")

    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set

    var isSending by mutableStateOf(false)
        private set

    /** 답변을 기다리는 중. 마지막 메시지가 아직 pending 인 답변이면 참. */
    val isWaiting: Boolean
        get() = messages.lastOrNull()?.isPending == true

    init {
        viewModelScope.launch {
            firebaseChatDatabase.observeMessages()
                .collect { messages = it }
        }
    }

    fun send(onFail: () -> Unit) {
        val text = input.trim()
        if (text.isBlank() || isSending) return

        isSending = true
        input = ""

        viewModelScope.launch {
            try {
                if (firebaseChatDatabase.sendMessage(text)) {
                    trueAnalytics.log("chat_send_success")
                } else {
                    trueAnalytics.log("chat_send_fail")
                    input = text        // 실패하면 입력을 되돌려 다시 보낼 수 있게 한다
                    onFail()
                }
            } catch (e: Exception) {
                logD("메시지 전송 실패: ${e.message}")
                trueAnalytics.log("chat_send_error", mapOf("error" to (e.message ?: "unknown")))
                input = text
                onFail()
            } finally {
                isSending = false
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            trueAnalytics.log("chat_clear")
            firebaseChatDatabase.clear()
        }
    }
}
