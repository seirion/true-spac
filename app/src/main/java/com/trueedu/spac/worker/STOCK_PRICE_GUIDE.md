# 주식 시세 주기적 업데이트 가이드

주식 시세를 거래 시간 중 5분마다 자동으로 가져와서 Firebase Database에 저장하는 기능이 구현되었습니다.

## 🎯 주요 특징

- ✅ **거래 시간에만 작동**: 평일 09:00 - 15:30 (공휴일 제외)
- ✅ **5분 간격 업데이트**: AlarmManager로 정확한 시간에 실행
- ✅ **배터리 효율적**: 거래 시간 외에는 작동하지 않음
- ✅ **자동 시작/종료**: 거래 시작/종료 시간에 자동으로 알람 시작/중지
- ✅ **앱 종료 후에도 작동**: AlarmManager로 구현되어 백그라운드 실행

## 📁 구현된 파일

### 1. TradingTimeHelper.kt
거래 시간 관련 유틸리티 함수 제공

```kotlin
// 현재가 거래 시간인지 체크
val isTradingNow = TradingTimeHelper.isTradingTime()

// 다음 거래 시작까지 남은 시간(밀리초)
val millisUntilStart = TradingTimeHelper.getMillisUntilTradingStart()
```

**주요 기능:**
- `isTradingTime()`: 현재 거래 시간 여부 확인
- `getNextTradingStartTime()`: 다음 거래 시작 시간
- `getNextTradingEndTime()`: 다음 거래 종료 시간
- 공휴일 및 주말 자동 체크

### 2. StockPriceWorker.kt
실제 시세를 가져와서 Firebase에 저장하는 Worker

```kotlin
@HiltWorker
class StockPriceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firebaseDatabase: FirebaseRealtimeDatabase,
) : CoroutineWorker(appContext, workerParams)
```

**작업 내용:**
1. 거래 시간 체크
2. 시세 데이터 가져오기 (API 호출)
3. Firebase Database에 저장

### 3. StockPriceAlarmManager.kt
AlarmManager를 관리하는 클래스

**주요 메서드:**
- `startTradingTimeAlarm()`: 거래 시간 알람 시작
- `stopAlarm()`: 알람 중지

**포함된 BroadcastReceiver:**
- `StockPriceAlarmReceiver`: 5분마다 트리거되어 Worker 실행
- `TradingStartReceiver`: 거래 시작 시간에 알람 시작
- `TradingEndReceiver`: 거래 종료 시간에 알람 중지

## 🔧 실제 시세 데이터 연동 방법

현재 `StockPriceWorker.kt`의 `doWork()` 메서드는 빈 Map을 반환하는 예제 코드입니다.
실제 시세 API를 연동하려면 다음과 같이 수정하세요:

### 방법 1: KIS API 사용

```kotlin
@HiltWorker
class StockPriceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firebaseDatabase: FirebaseRealtimeDatabase,
    private val kisApiService: KisApiService, // KIS API 서비스 주입
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            if (!TradingTimeHelper.isTradingTime()) {
                return Result.success()
            }

            val now = LocalDateTime.now()
            val timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")).toLong()

            // KIS API로 현재가 조회
            val prices = kisApiService.getCurrentPrices(stockCodes)

            // StockInfo 형태로 변환
            val stockInfoMap = prices.associate { priceData ->
                priceData.code to StockInfo(
                    code = priceData.code,
                    name = priceData.name,
                    currentPrice = priceData.currentPrice,
                    // 기타 필드...
                )
            }

            // Firebase에 저장
            firebaseDatabase.writeStockInfo(timestamp, stockInfoMap)
            logD("Stock prices updated: ${stockInfoMap.size} stocks")

            Result.success()
        } catch (e: Exception) {
            logE(e, "StockPriceWorker failed")
            Result.failure()
        }
    }
}
```

### 방법 2: 다른 시세 API 사용

```kotlin
// 예: Yahoo Finance, Alpha Vantage 등
private val stockApiService: StockApiService

override suspend fun doWork(): Result {
    // API 호출
    val pricesResponse = stockApiService.fetchStockPrices()

    // 데이터 변환
    val stockInfoMap = pricesResponse.data.map { stock ->
        stock.symbol to StockInfo(
            code = stock.symbol,
            name = stock.companyName,
            // 데이터 매핑...
        )
    }.toMap()

    // Firebase 저장
    firebaseDatabase.writeStockInfo(timestamp, stockInfoMap)
}
```

## ⚙️ 설정 변경

### 업데이트 간격 변경

`StockPriceAlarmManager.kt`에서 간격 수정:

```kotlin
companion object {
    private const val INTERVAL_MINUTES = 3L // 3분으로 변경
}
```

### 거래 시간 변경

`TradingTimeHelper.kt`에서 시간 수정:

```kotlin
private val MARKET_OPEN_TIME = LocalTime.of(9, 0)   // 시작 시간
private val MARKET_CLOSE_TIME = LocalTime.of(15, 30) // 종료 시간
```

### 특정 종목만 업데이트

```kotlin
// StockPriceWorker에서 필터링
private val targetStockCodes = listOf("005930", "000660", "035420") // 삼성전자, SK하이닉스, NAVER

override suspend fun doWork(): Result {
    val allPrices = kisApiService.getCurrentPrices()
    val filteredPrices = allPrices.filter { it.code in targetStockCodes }
    // ...
}
```

## 🔍 디버깅 및 모니터링

### Logcat으로 확인

```
// 알람 시작
"Stock price alarm initialized"

// 거래 시작 시간
"Trading start time - starting alarms"

// 5분마다
"Stock price alarm triggered"
"StockPriceWorker started"
"Stock prices updated: 50 stocks"

// 거래 종료 시간
"Trading end time - stopping alarms"
```

### 수동으로 알람 시작/중지

```kotlin
// Activity나 Fragment에서
@Inject
lateinit var stockPriceAlarmManager: StockPriceAlarmManager

// 시작
stockPriceAlarmManager.startTradingTimeAlarm()

// 중지
stockPriceAlarmManager.stopAlarm()
```

## 📱 권한 안내

### Android 12 (API 31) 이상

정확한 알람 권한이 필요합니다. 사용자에게 권한을 요청하세요:

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (!alarmManager.canScheduleExactAlarms()) {
        // 권한 요청 화면으로 이동
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        startActivity(intent)
    }
}
```

**AndroidManifest.xml에 이미 추가됨:**
```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

## 🎨 UI에서 상태 표시

### Composable 예제

```kotlin
@Composable
fun StockPriceUpdateStatus(
    stockPriceAlarmManager: StockPriceAlarmManager = hiltViewModel()
) {
    val isTradingTime = remember {
        TradingTimeHelper.isTradingTime()
    }

    Column {
        Text(
            text = if (isTradingTime) "거래 시간 (업데이트 중)" else "거래 시간 외",
            color = if (isTradingTime) Color.Green else Color.Gray
        )

        if (isTradingTime) {
            Text("다음 업데이트: 5분 이내")
        } else {
            val nextStart = TradingTimeHelper.getNextTradingStartTime()
            Text("다음 거래: ${nextStart.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))}")
        }

        Button(onClick = { stockPriceAlarmManager.startTradingTimeAlarm() }) {
            Text("수동 시작")
        }
    }
}
```

## ⚠️ 주의사항

1. **네트워크 연결 필요**: 시세 데이터를 가져오려면 인터넷 연결이 필요합니다
2. **API 호출 제한**: 시세 API의 호출 제한(Rate Limit)을 확인하세요
3. **배터리 소모**: 5분 간격은 적절하지만, 더 짧은 간격은 배터리 소모가 클 수 있습니다
4. **Firebase 비용**: 데이터 쓰기 횟수가 많으면 비용이 증가할 수 있습니다
5. **앱 강제 종료**: 사용자가 설정에서 앱을 강제 종료하면 알람이 중지될 수 있습니다

## 🚀 다음 단계

1. **실제 시세 API 연동**
   - KIS API 또는 다른 시세 제공 API 선택
   - API 키 발급 및 설정
   - `StockPriceWorker.kt`에 API 호출 로직 추가

2. **에러 처리 강화**
   - 네트워크 오류 시 재시도 로직
   - API 실패 시 알림
   - Firebase 쓰기 실패 처리

3. **성능 최적화**
   - 변경된 종목만 업데이트
   - 배치 처리로 Firebase 쓰기 횟수 감소
   - 캐싱 전략 적용

4. **사용자 설정 추가**
   - 업데이트 간격 선택 (1분, 3분, 5분 등)
   - 특정 종목만 업데이트
   - 알림 on/off 설정

## 📚 참고 자료

- [AlarmManager 공식 문서](https://developer.android.com/reference/android/app/AlarmManager)
- [WorkManager 가이드](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Firebase Realtime Database](https://firebase.google.com/docs/database)

