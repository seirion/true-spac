package com.trueedu.spac.repo.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.trueedu.spac.api.model.dto.firebase.ChatMessage
import com.trueedu.spac.data.log.logD
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseRealtimeDatabase 에서 AI 채팅 메시지 처리
 *
 * 질문을 쓰면 로컬에서 도는 워커가 같은 경로에 답변을 써 넣는다.
 * 앱은 답변을 만들지 않고, 경로를 구독해서 받기만 한다.
 */
@Singleton
class FirebaseChatDatabase @Inject constructor(
): FirebaseDatabaseBase() {
    companion object {
        private const val BASE_PATH = "chat"
    }

    suspend fun sendMessage(text: String): Boolean {
        logD("sendMessage()")
        val currentUser = firebaseCurrentUser() ?: run {
            logD("sendMessage() failed: currentUser null")
            return false
        }

        return try {
            // 워커는 role=="user" 노드가 생기는 즉시 집어간다. 필드를 나눠 쓰면
            // 미완성 상태로 처리되므로 push() 한 번에 완성된 형태로 써야 한다.
            //
            // ts 는 기기 시계를 믿지 않고 서버 시각을 쓴다. ServerValue.TIMESTAMP 는
            // Long 이 아니라 맵이라서 ChatMessage 로는 표현할 수 없어 여기서만 맵을 쓴다.
            val ref = database.getReference("$BASE_PATH/${currentUser.uid}")
            ref.push().setValue(
                mapOf(
                    "role" to ChatMessage.ROLE_USER,
                    "text" to text,
                    "ts" to ServerValue.TIMESTAMP,
                )
            ).await()

            logD("sendMessage() success")
            true
        } catch (e: Exception) {
            logD("sendMessage() failed: ${e.message}")
            false
        }
    }

    /**
     * 대화 전체를 시간순으로 흘려보낸다. 메시지가 추가되거나 답변이 갱신될 때마다
     * 목록 전체가 다시 온다 — 대화 하나 분량이라 부분 갱신을 다룰 만큼 크지 않다.
     */
    fun observeMessages(): Flow<List<ChatMessage>> {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            logD("observeMessages() failed: currentUser null")
            return flowOf(emptyList())
        }

        return callbackFlow {
            val ref = database.getReference("$BASE_PATH/${currentUser.uid}")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // 스키마에 안 맞는 노드(수동 테스트 등으로 남은 것) 하나 때문에
                    // 대화 전체가 못 뜨면 안 되므로, 그 노드만 건너뛴다.
                    val messages = snapshot.children.mapNotNull { child ->
                        try {
                            child.getValue(ChatMessage::class.java)?.apply {
                                id = child.key ?: ""
                            }
                        } catch (e: DatabaseException) {
                            logD("observeMessages() malformed node skip: ${child.key}, ${e.message}")
                            null
                        }
                    }
                    trySend(messages)
                }

                override fun onCancelled(error: DatabaseError) {
                    logD("observeMessages() cancelled: ${error.message}")
                    close(error.toException())
                }
            }

            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }
    }

    suspend fun clear(): Boolean {
        val currentUser = firebaseCurrentUser() ?: return false
        return try {
            database.getReference("$BASE_PATH/${currentUser.uid}").removeValue().await()
            true
        } catch (e: Exception) {
            logD("clear() failed: ${e.message}")
            false
        }
    }

    /**
     * 원격으로 AI 채팅 기능을 껐는지 확인한다. 화면 진입마다 새로 읽는다 —
     * 앱을 켜 둔 채로 서비스가 내려가도 다음 진입에서 바로 반영되도록.
     *
     * 값이 없거나 읽기에 실패하면 켜져 있는 것으로 본다. 이 값은 "끄는" 용도의
     * 킬 스위치라, 못 읽었다고 매번 정상 사용자까지 막는 건 배보다 배꼽이 크다.
     */
    suspend fun isAiAvailable(): Boolean {
        return try {
            val snapshot = database.getReference(FirebasePaths.APP_CONFIG)
                .child("aiAvailable")
                .get()
                .await()
            snapshot.getValue(Boolean::class.java) ?: true
        } catch (e: Exception) {
            logD("isAiAvailable() failed: ${e.message}")
            true
        }
    }
}
