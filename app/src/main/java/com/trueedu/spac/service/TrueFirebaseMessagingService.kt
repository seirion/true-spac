package com.trueedu.spac.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.user.RemoteConfig
import com.trueedu.spac.repo.local.Local
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Firebase Cloud Messaging (FCM) 서비스
 * 푸시 알림 수신 및 토큰 관리를 담당합니다.
 */
@AndroidEntryPoint
class TrueFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var local: Local

    @Inject
    lateinit var remoteConfig: RemoteConfig

    /**
     * 새로운 FCM 토큰이 생성되었을 때 호출됩니다.
     * 앱 재설치, 앱 데이터 삭제, 또는 토큰 갱신 시 호출됩니다.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        logD("🔑 새로운 FCM 토큰 수신: $token")

        // 토큰을 로컬에 저장
        local.notificationToken = token

        // RemoteConfig에도 저장 (Firebase Realtime Database에 자동 동기화)
        remoteConfig.updatePushToken(token)

        logD("✅ FCM 토큰 저장 완료 (로컬 + 원격)")
    }

    /**
     * FCM 메시지를 수신했을 때 호출됩니다.
     * 앱이 포그라운드 상태일 때만 호출됩니다.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        logD("📬 FCM 메시지 수신: ${remoteMessage.from}")

        // 데이터 페이로드 처리
        if (remoteMessage.data.isNotEmpty()) {
            logD("📦 데이터 페이로드: ${remoteMessage.data}")
            // TODO: 데이터 메시지 처리
        }

        // 알림 페이로드 처리
        remoteMessage.notification?.let {
            logD("📢 알림 제목: ${it.title}")
            logD("📢 알림 내용: ${it.body}")
            // TODO: 커스텀 알림 표시 (필요시)
        }
    }

    /**
     * 메시지 전송이 삭제되었을 때 호출됩니다.
     * 서버에 너무 많은 메시지가 대기 중일 때 발생할 수 있습니다.
     */
    override fun onDeletedMessages() {
        super.onDeletedMessages()
        logD("⚠️ FCM 메시지가 삭제되었습니다")
    }
}