package com.trueedu.spac.api.model.dto.firebase

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * AI 채팅 메시지. /chat/{uid}/{pushId} 에 저장된다.
 *
 * 사용자 질문은 앱이 쓰고, 답변은 로컬에서 도는 워커가 쓴다.
 * 워커가 남기는 answered/tools 같은 필드는 앱에서 쓰지 않으므로
 * [IgnoreExtraProperties] 로 무시한다. (tools 는 원본 인자까지 담은 디버깅용이고,
 * 화면에는 사람이 읽을 문구인 sources 를 쓴다.)
 */
@IgnoreExtraProperties
data class ChatMessage(
    val role: String = "",
    val text: String = "",
    val ts: Long = 0L,
    val status: String = "",   // assistant 만 사용
    val replyTo: String = "",  // assistant 만 사용
    /**
     * 답변의 근거. 워커가 어떤 조회를 했는지 사람이 읽을 문구로 남긴다.
     * 예: ["스팩 정보 조회 (KB제27호스팩)", "전자공시 목록 조회 (…)"]
     *
     * 답변보다 먼저 채워진다 — 조회 중에도 무엇을 하고 있는지 보여줄 수 있다.
     */
    val sources: List<String> = emptyList(),
) {
    /** RTDB 의 push key. 스냅샷에서 채워 넣는다. */
    @get:Exclude
    var id: String = ""

    @get:Exclude
    val isUser: Boolean
        get() = role == ROLE_USER

    /** 답변 생성 중. 본문이 비어 있으면 "생각 중", 차 있으면 그대로 보여준다. */
    @get:Exclude
    val isPending: Boolean
        get() = role == ROLE_ASSISTANT && status == STATUS_PENDING

    /** 아직 한 글자도 오지 않은 상태. 이때만 "생각 중" 을 띄운다. */
    @get:Exclude
    val isThinking: Boolean
        get() = isPending && text.isBlank()

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
