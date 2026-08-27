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

    /**
     * 답변을 기다리는 중.
     *
     * 두 구간이 있다. 질문만 올라가 있고 답변 말풍선이 아직 안 생긴 구간과,
     * 답변 말풍선이 pending 인 구간. 앞쪽은 답변을 만드는 쪽이 질문을 아직
     * 집어가지 않은 상태라 화면에 아무 변화가 없다.
     */
    val isWaiting: Boolean
        get() = messages.lastOrNull()?.let { it.isUser || it.isPending } == true

    /** 질문만 올라가 있고 답변 말풍선조차 없는 상태. */
    val isUnclaimed: Boolean
        get() = messages.lastOrNull()?.isUser == true

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
