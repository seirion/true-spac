# FCM 푸시 전송 테스트 가이드 (HTTP v1 API)

⚠️ **주의**: 이 기능은 테스트 전용입니다. 프로덕션 환경에서는 절대 사용하지 마세요!

## 1. 설정

### Firebase Admin SDK JSON 파일

이미 프로젝트에 포함되어 있습니다:
- 위치: `app/src/main/assets/firebase-service-account.json`
- 원본: `keys/true-project-9bd97-firebase-adminsdk-d76vz-a564e9dc1e.json`

⚠️ **보안 경고**:
- 이 파일은 **모든 사용자에게 푸시를 보낼 수 있는** 강력한 권한을 가지고 있습니다
- 앱을 디컴파일하면 노출될 수 있습니다
- **Debug 빌드에서만 포함**되도록 설정되어 있습니다
- **절대 Release 빌드에 포함하지 마세요!**

## 2. 사용 방법

### Kotlin 코드에서 사용

**주요 변경사항**: HTTP v1 API는 `Context`가 필요합니다 (assets에서 JSON 파일을 읽기 위함)

```kotlin
import com.trueedu.spac.util.FcmPushSender
import kotlinx.coroutines.launch

// ViewModel이나 Repository에서
viewModelScope.launch {
    val result = FcmPushSender.sendPush(
        context = context,  // ⬅️ Context 추가!
        token = "상대방의_FCM_토큰",
        title = "테스트 푸시",
        body = "안녕하세요!",
        data = mapOf(
            "type" to "test",
            "message" to "추가 데이터"
        ),
        deepLink = "truespac://app/home?id=123"  // ⬅️ 딥링크 (선택)
    )

    result.onSuccess {
        logD("푸시 전송 성공: $it")
    }.onFailure {
        logD("푸시 전송 실패: ${it.message}")
    }
}
```

### 예시: Admin 화면에서 테스트

```kotlin
// AdminViewModel.kt
@HiltViewModel
class AdminViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun sendTestPush(targetToken: String) {
        viewModelScope.launch {
            val result = FcmPushSender.sendPush(
                context = context,
                token = targetToken,
                title = "관리자 테스트",
                body = "푸시 알림 테스트입니다"
            )

            result.onSuccess {
                // 성공 처리
            }.onFailure { error ->
                // 실패 처리
                logD("푸시 전송 실패: ${error.message}")
            }
        }
    }
}
```

## 3. HTTP v1 API vs Legacy API

### HTTP v1 API (현재 구현)
✅ Firebase가 권장하는 최신 방식
✅ OAuth2 토큰 자동 갱신
✅ 더 풍부한 메시지 옵션
✅ 더 나은 에러 메시지

## 4. 안전 장치

### 자동으로 차단되는 경우:
- ✅ **Release 빌드**: 자동으로 실행 차단
- ✅ **JSON 파일 없음**: assets에 파일이 없으면 실행 차단
- ✅ **네트워크 오류**: 자동으로 에러 반환

## 5. 프로덕션 전환

테스트 완료 후 실제 서비스에서는:

1. **Firebase Cloud Functions 사용** (권장)
   ```javascript
   // Cloud Function 예시
   exports.sendNotification = functions.https.onCall(async (data, context) => {
     const message = {
       token: data.token,
       notification: {
         title: data.title,
         body: data.body
       }
     };
     return admin.messaging().send(message);
   });
   ```

2. **백엔드 서버 구축** (Node.js, Spring Boot 등)
3. **앱에서 이 코드 제거 또는 완전 비활성화**

## 6. 문제 해결

### "firebase-service-account.json not found"
→ `app/src/main/assets/` 폴더에 JSON 파일이 있는지 확인하세요

### "FcmPushSender is only available in debug builds"
→ Debug 빌드인지 확인하세요

### "401 Unauthorized" 또는 "403 Forbidden"
→ JSON 파일이 올바른지, Firebase 프로젝트 ID가 맞는지 확인하세요

### "토큰 없음"
→ 상대방이 로그인하여 토큰이 RemoteConfig에 저장되었는지 확인하세요

### 딥링크 추가 방법:

**AdminScreen에서:**
1. "딥링크 (선택)" 필드에 URL 입력
2. 푸시 전송 시 자동으로 포함됨

**코드에서:**
```kotlin
FcmPushSender.sendPush(
    context = context,
    token = token,
    title = "새로운 알림",
    body = "확인해보세요!",
    deepLink = "truespac://app/home?id=123"
)
```

**알림 동작:**
- 앱이 포그라운드: 알림 표시 + 클릭 시 딥링크로 이동
- 앱이 백그라운드: 시스템 알림 표시 + 클릭 시 앱 실행 + 딥링크로 이동
- 앱이 종료 상태: 시스템 알림 표시 + 클릭 시 앱 실행 + 딥링크로 이동

## 8. 기술 세부사항

### FCM 메시지 구조:

딥링크가 포함된 FCM 메시지는 **data only 메시지**를 사용합니다:

```json
{
  "message": {
    "token": "FCM_TOKEN",
    "data": {
      "title": "제목",
      "body": "내용",
      "deepLink": "truespac://app/home",
      "type": "test",
      "timestamp": "1234567890"
    },
    "android": {
      "priority": "high"
    }
  }
}
```

**data only 메시지의 장점:**
- ✅ 앱 상태(포그라운드/백그라운드/종료)와 관계없이 항상 `onMessageReceived` 호출
- ✅ 딥링크가 모든 상황에서 정상 동작
- ✅ 커스텀 알림 표시 및 처리 가능

### 사용하는 라이브러리:
- `com.google.auth:google-auth-library-oauth2-http:1.19.0`

### 자동화된 기능:
- ✅ **프로젝트 ID 자동 감지**: JSON 파일에서 프로젝트 ID를 자동으로 읽어옵니다
- ✅ **토큰 캐싱**: OAuth2 토큰을 캐싱하여 성능 최적화 (만료 5분 전까지 재사용)
- ✅ **리소스 관리**: InputStream 자동 close로 메모리 누수 방지

### FCM HTTP v1 API 엔드포인트:
```
POST https://fcm.googleapis.com/v1/projects/{project-id}/messages:send
```

### OAuth2 스코프:
```
https://www.googleapis.com/auth/firebase.messaging
```

### 성능 최적화:
OAuth2 토큰은 1시간 동안 유효하며, 자동으로 캐싱되어 재사용됩니다. 이로 인해:
- 첫 번째 푸시 전송: 토큰 생성 시간 포함 (~500ms)
- 이후 푸시 전송: 캐시된 토큰 사용 (~100ms)
