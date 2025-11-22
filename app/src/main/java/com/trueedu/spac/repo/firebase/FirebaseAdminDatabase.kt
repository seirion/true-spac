package com.trueedu.spac.repo.firebase

import com.trueedu.spac.api.model.dto.firebase.UserRemoteConfig
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.repo.local.Local
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseRealtimeDatabase 에서 관리자 권한이 필요한 데이터 처리
 * 권한이 있는 어드민 사용자만 접근 가능
 */
@Singleton
class FirebaseAdminDatabase @Inject constructor(
    private val local: Local
): FirebaseDatabaseBase() {

    /**
     * 관리자 권한 체크
     * 1. UserKey 유효성 검증 (로컬)
     * 2. Firebase 인증 확인 (원격)
     *
     * @return 두 조건을 모두 만족하면 true
     */
    private suspend fun checkAdminPermission(): Boolean {
        // 1. UserKey 유효성 체크
        if (!local.getUserKey().isValid()) {
            logD("Admin permission denied: UserKey is invalid")
            return false
        }

        // 2. Firebase 인증 상태 체크
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            logD("Admin permission denied: Firebase user not authenticated")
            return false
        }

        return true
    }

    /**
     * 관리자 모드 여부
     * UserKey가 유효하면 관리자로 간주
     */
    val isAdmin: Boolean
        get() = local.getUserKey().isValid()

    /**
     * 모든 사용자의 config를 UserRemoteConfig 형식으로 읽어 리스트로 반환
     * 권한이 없는 경우 빈 리스트 반환
     */
    suspend fun loadAllUserConfigs(): List<Pair<String, UserRemoteConfig>> {
        logD("loadAllUserConfigs()")

        // 어드민 권한 체크 (강화된 보안 체크)
        if (!checkAdminPermission()) {
            logD("loadAllUserConfigs() failed: permission denied")
            return emptyList()
        }

        return try {
            val ref = database.getReference(FirebasePaths.USERS)
            val snapshot = ref.get().await()

            val configs = mutableListOf<Pair<String, UserRemoteConfig>>()

            // 모든 사용자 노드 순회
            for (userSnapshot in snapshot.children) {
                val userId = userSnapshot.key ?: continue
                val configSnapshot = userSnapshot.child(FirebasePaths.USER_CONFIG)

                val config = configSnapshot.getValue(UserRemoteConfig::class.java)
                if (config != null) {
                    configs.add(userId to config)
                    logD("User config loaded: userId=$userId")
                }
            }

            logD("loadAllUserConfigs() success: ${configs.size} configs loaded")
            configs
        } catch (e: Exception) {
            logD("loadAllUserConfigs() failed: ${e.message}")
            emptyList()
        }
    }
}

