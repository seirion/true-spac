# Worker 실행 추적 가이드

앱이 종료된 후에도 백그라운드 Worker가 정상적으로 실행되는지 확인할 수 있는 시스템입니다.

## 🎯 문제 해결

### 문제점
- 앱이 종료되면 Logcat도 끊겨서 Worker 실행 여부를 알 수 없음
- WorkManager의 실행 간격이 정확한지 확인 어려움
- Doze 모드나 배터리 최적화로 인한 지연 파악 어려움

### 해결책
- SharedPreferences에 실행 시간 및 횟수 기록
- 앱 재시작 후 UI에서 확인 가능
- 마지막 실행 시간으로 간격 확인

## 📊 구현 내용

### 1. WorkerExecutionTracker
실행 이력을 SharedPreferences에 저장

```kotlin
@Singleton
class WorkerExecutionTracker @Inject constructor(
    private val preferences: SharedPreferences
) {
    // 마스터 파일 Worker 실행 기록
    fun recordMasterFileExecution()
    
    // 시세 Worker 실행 기록
    fun recordPriceUpdateExecution()
    
    // 통계 조회
    fun getLastMasterFileUpdate(): String
    fun getMasterFileExecutionCount(): Int
}
```

### 2. Worker에 추적 추가

```kotlin
// PeriodicSyncWorker
override suspend fun doWork(): Result {
    tracker.recordMasterFileExecution() // 첫 줄에 추가
    // ... 작업 수행
}

// StockPriceWorker
override suspend fun doWork(): Result {
    tracker.recordPriceUpdateExecution() // 첫 줄에 추가
    // ... 작업 수행
}
```

### 3. UI에서 확인

```kotlin
@Composable
fun WorkerStatusView(
    tracker: WorkerExecutionTracker,
    local: Local
) {
    // 마지막 실행 시간 표시
    Text("마지막 실행: ${tracker.getLastMasterFileUpdate()}")
    Text("총 실행 횟수: ${tracker.getMasterFileExecutionCount()}회")
}
```

## 🔍 사용 방법

### 앱 설정 화면에 추가

```kotlin
// ProfileScreen.kt 또는 SettingsScreen.kt
@Composable
fun ProfileScreen(
    tracker: WorkerExecutionTracker = hiltViewModel(),
    local: Local = hiltViewModel()
) {
    Column {
        // 기존 설정들...
        
        // Worker 상태 섹션 추가
        WorkerStatusView(tracker = tracker, local = local)
    }
}
```

### 디버깅용 로그 출력

```kotlin
// App.kt onCreate()에서
if (BuildConfig.DEBUG) {
    val tracker = entryPointInjector(TrackerModule::class.java).getTracker()
    logD(tracker.getExecutionStats())
}
```

## 📱 실제 확인 예시

### 시나리오 1: 앱 종료 후 확인

1. 앱 실행 → 관리자 모드 활성화
2. 앱 종료 (스와이프로 종료)
3. 30분 후 앱 재시작
4. 설정 화면에서 "Worker 상태" 확인

**예상 결과:**
```
마스터 파일 업데이트
  마지막 실행: 2025-10-18 23:15:32
  총 실행 횟수: 5회
  • 15-20분 간격으로 자동 실행

시세 업데이트
  마지막 실행: 2025-10-18 14:25:18
  총 실행 횟수: 42회
  • 거래 시간 중 5분 간격으로 실행
```

### 시나리오 2: 실행 간격 확인

```
현재 시간: 23:45
마지막 실행: 23:30 (15분 전)
→ 정상 작동 ✅

현재 시간: 23:45
마지막 실행: 22:00 (1시간 45분 전)
→ Doze 모드 또는 배터리 최적화 의심 ⚠️
```

## ⚙️ WorkManager 실행 간격 특성

### 정상 범위
- **일반 상황**: 15-25분 간격
- **Doze 모드**: 30분-2시간 간격
- **극단적 상황**: 수 시간 지연 가능

### 30분 이상 지연되는 경우

#### 원인 1: Doze 모드
기기가 오랜 시간 사용되지 않고 idle 상태

**해결:**
- 기기를 주기적으로 사용
- 또는 배터리 최적화 예외 설정

#### 원인 2: 배터리 최적화
제조사별 aggressive한 배터리 관리

**해결:**
```kotlin
// 배터리 최적화 예외 요청 (사용자 동의 필요)
val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
    data = Uri.parse("package:${context.packageName}")
}
startActivity(intent)
```

#### 원인 3: 제조사 커스텀 ROM
삼성, 샤오미 등의 배터리 절약 기능

**해결:**
- 앱 설정 → 배터리 → 백그라운드 사용 제한 해제
- 제조사별로 다름

## 🛠️ 문제 해결

### 1. Worker가 전혀 실행되지 않음

**확인 사항:**
```kotlin
// Logcat 필터: "WM-WorkerWrapper"
// 예상 로그:
"Starting work for com.trueedu.spac.worker.PeriodicSyncWorker"
```

**원인:**
- UserKey가 설정되지 않음 (일반 사용자 모드)
- WorkManager가 스케줄되지 않음

**해결:**
1. UserKey 설정 확인
2. 앱 재시작
3. Logcat에서 "Admin mode enabled" 확인

### 2. 간격이 너무 불규칙함

**30분 이상 지연 시:**

```kotlin
// App.kt의 setupPeriodicWork()에서
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    // 다른 제약 제거
    .build()
```

**정확한 스케줄링 필요 시:**

AlarmManager 사용 고려 (배터리 소모 증가)

### 3. 특정 제조사에서 작동 안 함

**삼성:**
- 설정 → 배터리 → 백그라운드 사용 제한 → 해제

**샤오미:**
- 설정 → 앱 → 권한 → 자동 시작 → 허용
- 설정 → 배터리 → 제한 없음

**화웨이:**
- 설정 → 배터리 → 앱 실행 → 수동 관리

## 📊 통계 데이터 활용

### Firebase Analytics로 전송

```kotlin
// Worker에서
override suspend fun doWork(): Result {
    tracker.recordMasterFileExecution()
    
    // Firebase Analytics 이벤트
    firebaseAnalytics.logEvent("worker_executed") {
        param("worker_type", "master_file")
        param("execution_count", tracker.getMasterFileExecutionCount().toLong())
    }
    
    // ...
}
```

### 원격 모니터링

```kotlin
// Firebase Realtime Database에 기록
suspend fun reportExecution() {
    val deviceId = local.deviceId
    val data = mapOf(
        "last_execution" to tracker.getLastMasterFileUpdate(),
        "count" to tracker.getMasterFileExecutionCount(),
        "device_id" to deviceId
    )
    
    firebaseDatabase.getReference("worker_stats")
        .child(deviceId)
        .setValue(data)
}
```

## 🎯 요약

**이 시스템으로 확인 가능:**
1. ✅ 앱 종료 후에도 Worker가 실행되는지
2. ✅ 실행 간격이 적절한지
3. ✅ 총 실행 횟수
4. ✅ 마지막 실행 시간

**정상 작동 기준:**
- 마스터 파일: 15-25분 간격
- 시세 업데이트: 거래 시간 중 5-7분 간격
- 30분 이상 지연 시: Doze 모드 또는 배터리 최적화 확인

**비정상 작동 시:**
1. 배터리 최적화 설정 확인
2. 제조사별 백그라운드 제한 확인
3. UserKey 설정 확인

