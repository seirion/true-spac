package com.trueedu.spac.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.trueedu.spac.MainActivity
import com.trueedu.spac.R
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

    companion object {
        private const val CHANNEL_ID = "truespac_notifications"
        private const val CHANNEL_NAME = "TrueSpac 알림"
    }

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
     * data only 메시지를 사용하므로 앱 상태와 관계없이 항상 호출됩니다.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        logD("📬 FCM 메시지 수신: ${remoteMessage.from}")

        // data 페이로드에서 title, body, deepLink 읽기
        if (remoteMessage.data.isNotEmpty()) {
            logD("📦 데이터 페이로드: ${remoteMessage.data}")

            val title = remoteMessage.data["title"] ?: "TrueSpac"
            val body = remoteMessage.data["body"] ?: ""
            val deepLink = remoteMessage.data["deepLink"]?.let { link ->
                if (isValidDeepLink(link)) {
                    logD("🔗 유효한 딥링크: $link")
                    link
                } else {
                    logD("⚠️ 잘못된 딥링크 형식: $link")
                    null
                }
            }

            logD("📢 알림 제목: $title")
            logD("📢 알림 내용: $body")

            showNotification(title, body, deepLink, remoteMessage.data)
        }
    }

    /**
     * 딥링크의 유효성을 검증합니다
     */
    private fun isValidDeepLink(deepLink: String): Boolean {
        return try {
            val uri = deepLink.toUri()
            val scheme = uri.scheme?.lowercase()
            // truespac:// 스키마 또는 https/http 프로토콜만 허용
            scheme == "truespac" || scheme == "https" || scheme == "http"
        } catch (e: Exception) {
            logD("❌ 딥링크 파싱 실패: ${e.message}")
            false
        }
    }

    /**
     * 알림을 표시합니다
     */
    private fun showNotification(
        title: String,
        body: String,
        deepLink: String?,
        data: Map<String, String>
    ) {
        createNotificationChannel()

        // 고유한 ID 생성 (타임스탬프 사용)
        val notificationId = System.currentTimeMillis().toInt()

        // Intent 생성 (딥링크가 있으면 딥링크 사용, 없으면 기본 앱 실행)
        val intent = if (deepLink != null) {
            Intent(Intent.ACTION_VIEW, deepLink.toUri()).apply {
                setClass(this@TrueFirebaseMessagingService, MainActivity::class.java)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 생성
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())

        logD("✅ 알림 표시 완료 (ID: $notificationId)${deepLink?.let { " (딥링크: $it)" } ?: ""}")
    }

    /**
     * 알림 채널 생성 (Android 8.0 이상)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "TrueSpac 알림 채널"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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