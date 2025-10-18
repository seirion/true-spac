# 관리자 모드 vs 일반 사용자 모드 가이드

앱의 두 가지 작동 모드를 구분하여 Firebase 데이터 관리를 효율적으로 수행합니다.

## 🔧 모드 구분

### 관리자 모드 (UserKey 유효)
마스터 파일을 다운로드하고 Firebase에 업로드하는 역할을 수행합니다.

**활성화 기능:**
- ✅ `setupPeriodicWork()`: 15분마다 마스터 파일 다운로드 + Firebase 업로드
- ✅ `setupStockPriceAlarm()`: 거래 시간 중 5분마다 시세 업데이트 + Firebase 업로드

**용도:**
- 서버 역할을 하는 관리자 앱
- Firebase에 최신 데이터 공급
- 하루 1대의 기기만 실행 권장

### 일반 사용자 모드 (UserKey 없음)
Firebase에서 데이터를 읽기만 수행합니다.

**활성화 기능:**
- ✅ Firebase에서 종목 정보 읽기
- ✅ 로컬 DB 동기화
- ❌ Firebase 업로드 없음

**용도:**
- 일반 사용자 앱
- Firebase에서 최신 데이터 받아오기만

## 📝 설정 방법

### UserKey 설정으로 관리자 모드 활성화

관리자 모드는 **UserKey가 유효한지**로 자동 판단됩니다.

```kotlin
// App.kt
private fun isAdminMode(local: Local): Boolean {
    return local.getUserKey().isValid()
}

// UserKey.kt
fun isValid(): Boolean = 
    !appKey.isNullOrBlank() && !appSecret.isNullOrBlank()
```

**관리자 모드 활성화 방법:**
1. 앱 설정에서 UserKey 입력 (appKey, appSecret)
2. 또는 코드로 직접 설정:

```kotlin
// 관리자 기기에서만
local.setUserKey(
    UserKey(
        appKey = "YOUR_APP_KEY",
        appSecret = "YOUR_APP_SECRET",
        accountNum = "12345678-01",
        htsId = "admin_user"
    )
)
```

**일반 사용자 모드 (기본값):**
- UserKey가 설정되지 않음 (또는 비어있음)
- 자동으로 읽기 전용 모드로 동작

## 🔄 데이터 흐름

### 관리자 모드 데이터 흐름

```
[마스터 파일 서버]
       ↓ (다운로드)
[PeriodicSyncWorker - 15분 간격]
       ↓ (업로드)
[Firebase Database]
       
[시세 API 서버]
       ↓ (조회)
[StockPriceWorker - 5분 간격, 거래 시간만]
       ↓ (업로드)
[Firebase Database]
```

### 일반 사용자 데이터 흐름

```
[Firebase Database]
       ↓ (읽기)
[StockPool.loadStockInfo()]
       ↓ (저장)
[로컬 Database]
       ↓ (로드)
[메모리 (stocks Map)]
```

## 📊 각 컴포넌트 역할

### StockPool.loadStockInfo() (일반 사용자용)
```kotlin
// 1순위: Firebase에서 최신 데이터 확인
if (Firebase에 새 데이터 있음) {
    Firebase에서 다운로드 → 로컬 DB 저장
}
// 2순위: 로컬 DB 사용
else {
    로컬 DB에서 로드
}
```

**주의:** 마스터 파일 다운로드 및 Firebase 업로드 기능 제거됨

### StockPool.downloadMasterFiles() (관리자 전용)
```kotlin
마스터 파일 다운로드
   ↓
Firebase에 업로드
   ↓
로컬 DB에 저장
```

**주의:** 관리자 모드에서만 PeriodicSyncWorker를 통해 호출됨

## 🚀 배포 전략

### 방법 1: UserKey 기반 (현재 구현, 권장)

**장점:**
- 동일한 APK를 모든 사용자에게 배포
- 관리자만 UserKey 입력으로 관리자 기능 활성화
- 별도 빌드 불필요

**사용:**
```kotlin
// 일반 사용자
// → UserKey 없음 → 자동으로 읽기 전용

// 관리자
// → 설정에서 UserKey 입력 → 관리자 모드 활성화
```

### 방법 2: UI에서 UserKey 설정 화면 추가

```kotlin
@Composable
fun AdminSettingsScreen(
    local: Local = hiltViewModel()
) {
    var appKey by remember { mutableStateOf("") }
    var appSecret by remember { mutableStateOf("") }
    
    Column {
        Text("관리자 모드 설정")
        
        TextField(
            value = appKey,
            onValueChange = { appKey = it },
            label = { Text("App Key") }
        )
        
        TextField(
            value = appSecret,
            onValueChange = { appSecret = it },
            label = { Text("App Secret") },
            visualTransformation = PasswordVisualTransformation()
        )
        
        Button(onClick = {
            local.setUserKey(
                UserKey(
                    appKey = appKey,
                    appSecret = appSecret,
                    accountNum = null,
                    htsId = null
                )
            )
            // 앱 재시작 필요
        }) {
            Text("관리자 모드 활성화")
        }
        
        Button(onClick = {
            local.clearUserKey()
            // 앱 재시작 필요
        }) {
            Text("관리자 모드 비활성화")
        }
    }
}
```

### 방법 3: 환경 변수로 기본 UserKey 설정

```kotlin
// local.properties
ADMIN_APP_KEY=your_app_key_here
ADMIN_APP_SECRET=your_app_secret_here

// build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "ADMIN_APP_KEY", "\"${localProperties.getProperty("ADMIN_APP_KEY") ?: ""}\"")
        buildConfigField("String", "ADMIN_APP_SECRET", "\"${localProperties.getProperty("ADMIN_APP_SECRET") ?: ""}\"")
    }
}

// App.kt
override fun onCreate() {
    super.onCreate()
    
    // 빌드 시 설정된 키가 있으면 자동 설정
    if (BuildConfig.ADMIN_APP_KEY.isNotBlank()) {
        local.setUserKey(
            UserKey(
                appKey = BuildConfig.ADMIN_APP_KEY,
                appSecret = BuildConfig.ADMIN_APP_SECRET,
                accountNum = null,
                htsId = null
            )
        )
    }
}
```

## ⚙️ 실제 사용 예시

### 시나리오 1: 개발/테스트 환경

```kotlin
// 테스트용 UserKey 설정
local.setUserKey(
    UserKey(
        appKey = "TEST_APP_KEY",
        appSecret = "TEST_APP_SECRET",
        accountNum = null,
        htsId = null
    )
)
// → 관리자 모드 활성화
```

### 시나리오 2: 별도 관리자 기기

1. 동일한 APK를 설치
2. 앱 설정에서 UserKey 입력
3. 앱 재시작 → 관리자 모드 활성화
4. 항상 켜두고 백그라운드에서 실행
5. Firebase에 최신 데이터 계속 업로드

### 시나리오 3: 일반 사용자

1. Play Store 배포 버전 (동일 APK)
2. UserKey 설정 안 함 (기본값)
3. Firebase에서 읽기만 수행
4. 배터리 효율적

## 📱 UI에서 모드 확인

```kotlin
@Composable
fun AdminModeIndicator(
    local: Local = hiltViewModel()
) {
    val isAdmin = remember { local.getUserKey().isValid() }
    
    if (isAdmin) {
        Surface(
            color = Color.Red,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚠️ 관리자 모드 - Firebase 업로드 활성화",
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun AdminModeStatus(
    local: Local = hiltViewModel()
) {
    val userKey = local.getUserKey()
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = if (userKey.isValid()) "관리자 모드" else "일반 사용자 모드",
            style = MaterialTheme.typography.titleMedium
        )
        
        if (userKey.isValid()) {
            Text(
                text = "App Key: ${userKey.appKey?.take(10)}...",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
```

## ⚠️ 주의사항

### 1. 중복 업로드 방지
- 관리자 모드는 1대 기기만 실행하세요
- 여러 기기가 동시에 업로드하면 데이터 충돌 가능

### 2. Firebase 비용
- 관리자 모드는 Firebase 쓰기 작업이 많습니다
- Firebase Realtime Database 또는 Firestore 요금제 확인

### 3. 권한 관리
- Firebase Rules에서 관리자만 쓰기 가능하도록 설정
- 일반 사용자는 읽기 권한만 부여

```json
// Firebase Realtime Database Rules
{
  "rules": {
    "stocks": {
      ".read": true,
      ".write": "auth.uid === 'ADMIN_UID'"
    },
    "meta": {
      ".read": true,
      ".write": "auth.uid === 'ADMIN_UID'"
    }
  }
}
```

### 4. 배터리 소모
- 관리자 모드는 배터리 소모가 큽니다
- 충전 상태에서 실행 권장

## 🔍 로그로 모드 확인

### 관리자 모드 (UserKey 유효)
```
Admin mode enabled - UserKey is valid, periodic updates active
PeriodicSyncWorker started - downloading master files
Master file downloaded and uploaded to Firebase: 2500 stocks
Stock price alarm triggered
```

### 일반 사용자 모드 (UserKey 없음)
```
User mode - UserKey is invalid, read-only from Firebase
업데이트 체크 - Firebase 리모트(true)
remote stocks(2500) loaded
```

## 📚 요약

| 항목 | 관리자 모드 | 일반 사용자 모드 |
|------|------------|----------------|
| UserKey 상태 | 유효 (appKey + appSecret) | 없음 (기본값) |
| Firebase 쓰기 | ✅ | ❌ |
| Firebase 읽기 | ✅ | ✅ |
| 마스터 파일 다운로드 | ✅ (15분) | ❌ |
| 시세 업데이트 | ✅ (5분, 거래시간) | ❌ |
| 배터리 소모 | 높음 | 낮음 |
| 권장 기기 수 | 1대 | 무제한 |
| 배포 대상 | 동일 APK | 동일 APK |
| 활성화 방법 | UserKey 입력 | 기본 상태 유지 |

**핵심 차이점:**
- 동일한 APK를 배포
- UserKey 유무로 관리자/사용자 모드 자동 전환
- 관리자만 설정에서 UserKey 입력

