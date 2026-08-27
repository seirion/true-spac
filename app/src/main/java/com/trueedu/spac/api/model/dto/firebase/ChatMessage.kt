package com.trueedu.spac.api.model.dto.firebase

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * AI 채팅 메시지. /chat/{uid}/{pushId} 에 저장된다.
 *
 * 사용자 질문은 앱이 쓰고, 답변은 로컬에서 도는 워커가 쓴다.
 * 워커가 남기는 answered/tools 같은 필드는 앱에서 쓰지 않으므로
 * [IgnoreExtraProperties] 로 무시한다.
 */
@IgnoreExtraProperties
data class ChatMessage(
    val role: String = "",
    val text: String = "",
    val ts: Long = 0L,
    val status: String = "",   // assistant 만 사용
    val replyTo: String = "",  // assistant 만 사용
) {
    /** RTDB 의 push key. 스냅샷에서 채워 넣는다. */
    @get:Exclude
    var id: String = ""

    @get:Exclude
    val isUser: Boolean
        get() = role == ROLE_USER

    /** 답변 생성 중. 화면에는 "생각 중" 으로 표시한다. */
    @get:Exclude
    val isPending: Boolean
        get() = role == ROLE_ASSISTANT && status == STATUS_PENDING

    @get:Exclude
    val isError: Boolean
        get() = role == ROLE_ASSISTANT && status == STATUS_ERROR

    companion object {
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"

        const val STATUS_PENDING = "pending"
        const val STATUS_DONE = "done"
        const val STATUS_ERROR = "error"
    }
}
