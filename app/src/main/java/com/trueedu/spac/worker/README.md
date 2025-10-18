# WorkManager 사용 가이드

WorkManager를 이용한 주기적인 백그라운드 작업 구현이 완료되었습니다.

## 구현 내용

### 1. 의존성 추가
- `androidx.work:work-runtime-ktx` - WorkManager 라이브러리
- `androidx.hilt:hilt-work` - Hilt와 WorkManager 통합

### 2. 주요 파일

#### PeriodicSyncWorker.kt
앱이 꺼진 상태에서도 주기적으로 실행되는 Worker 클래스입니다.

```kotlin
@HiltWorker
class PeriodicSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val stockPool: StockPool,
) : CoroutineWorker(appContext, workerParams)
```

**특징:**
- `@HiltWorker` 어노테이션으로 의존성 주입 지원
- `doWork()` 메서드에서 실제 작업 수행
- 성공, 실패, 재시도를 반환하여 작업 결과 전달

#### App.kt
WorkManager 초기화 및 주기적 작업 스케줄링을 담당합니다.

**주요 설정:**
- 실행 간격: 15분 (WorkManager의 최소 주기)
- 실행 조건:
  - 네트워크 연결 필요
  - 배터리가 낮지 않을 때만 실행

#### WorkManagerHelper.kt
WorkManager 작업을 쉽게 관리하기 위한 헬퍼 클래스입니다.

## 커스터마이징 방법

### 1. 작업 내용 변경

`PeriodicSyncWorker.kt`의 `doWork()` 메서드를 수정하세요:

```kotlin
override suspend fun doWork(): Result {
    return try {
        // 여기에 주기적으로 수행할 작업을 추가
        // 예: API 호출, 데이터베이스 동기화, 알림 전송 등

        Result.success()
    } catch (e: Exception) {
        Result.failure() // 또는 Result.retry()
    }
}
```

### 2. 실행 주기 변경

`App.kt`의 `setupPeriodicWork()` 메서드에서 주기를 수정하세요:

```kotlin
val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(
    repeatInterval = 15,  // 간격 (최소 15분)
    repeatIntervalTimeUnit = TimeUnit.MINUTES  // 또는 HOURS, DAYS
)
```

**주의:** WorkManager의 최소 주기는 15분입니다.

### 3. 실행 조건 변경

`App.kt`의 `Constraints.Builder()` 부분을 수정하세요:

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)  // 네트워크 조건
    // .setRequiredNetworkType(NetworkType.UNMETERED)  // WiFi만
    .setRequiresBatteryNotLow(true)  // 배터리 조건
    .setRequiresCharging(false)  // 충전 조건
    .setRequiresDeviceIdle(false)  // 대기 모드 조건
    .build()
```

### 4. 여러 개의 Worker 추가

새로운 Worker 클래스를 만들고 스케줄링하세요:

```kotlin
// 1. 새로운 Worker 생성
@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        // 알림 관련 작업
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "notification_work"
    }
}

// 2. App.kt에서 스케줄링
private fun setupNotificationWork() {
    val request = PeriodicWorkRequestBuilder<NotificationWorker>(
        repeatInterval = 1,
        repeatIntervalTimeUnit = TimeUnit.HOURS
    ).build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        NotificationWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}
```

## 작업 모니터링 및 관리

### ViewModel에서 작업 상태 확인

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val workManagerHelper: WorkManagerHelper
) : ViewModel() {

    val workStatus = workManagerHelper.getPeriodicWorkStatus()

    fun cancelWork() {
        workManagerHelper.cancelPeriodicWork()
    }
}
```

### Composable에서 작업 상태 표시

```kotlin
@Composable
fun WorkStatusScreen(viewModel: MyViewModel = hiltViewModel()) {
    val workInfos by viewModel.workStatus.observeAsState()

    workInfos?.forEach { workInfo ->
        Text("상태: ${workInfo.state}")
        when (workInfo.state) {
            WorkInfo.State.ENQUEUED -> Text("대기 중")
            WorkInfo.State.RUNNING -> Text("실행 중")
            WorkInfo.State.SUCCEEDED -> Text("성공")
            WorkInfo.State.FAILED -> Text("실패")
            WorkInfo.State.BLOCKED -> Text("차단됨")
            WorkInfo.State.CANCELLED -> Text("취소됨")
        }
    }
}
```

## 일회성 작업 (One-time Work)

주기적이 아닌 일회성 작업이 필요한 경우:

```kotlin
val oneTimeWork = OneTimeWorkRequestBuilder<PeriodicSyncWorker>()
    .setConstraints(constraints)
    .build()

WorkManager.getInstance(context).enqueue(oneTimeWork)
```

## 지연된 작업 (Delayed Work)

특정 시간 후에 작업을 실행하려면:

```kotlin
val delayedWork = OneTimeWorkRequestBuilder<PeriodicSyncWorker>()
    .setInitialDelay(30, TimeUnit.MINUTES)
    .build()

WorkManager.getInstance(context).enqueue(delayedWork)
```

## 디버깅

작업 실행 여부를 확인하려면 Logcat에서 다음을 확인하세요:

```
PeriodicSyncWorker started
PeriodicSyncWorker completed successfully
Periodic work scheduled: periodic_sync_work
```

## 주의사항

1. **최소 주기**: PeriodicWorkRequest의 최소 주기는 15분입니다.
2. **실행 시점**: WorkManager는 배터리 최적화를 위해 정확한 시간에 실행되지 않을 수 있습니다.
3. **Doze 모드**: Android의 Doze 모드에서는 작업이 지연될 수 있습니다.
4. **정확한 시간**: 정확한 시간에 실행이 필요하면 AlarmManager를 사용하세요.
5. **백그라운드 제한**: Android 12+ 에서는 백그라운드 작업이 제한될 수 있습니다.

## 테스트

개발 중에는 실행 주기를 짧게 설정하고 테스트할 수 있습니다:

```kotlin
// 테스트용 - 최소 15분
val testRequest = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(
    repeatInterval = 15,
    repeatIntervalTimeUnit = TimeUnit.MINUTES
).build()
```

## 추가 리소스

- [WorkManager 공식 문서](https://developer.android.com/topic/libraries/architecture/workmanager)
- [WorkManager 코드랩](https://developer.android.com/codelabs/android-workmanager)

