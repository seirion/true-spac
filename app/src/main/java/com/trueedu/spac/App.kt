package com.trueedu.spac

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.trueedu.spac.data.log.FileNameTree
import com.trueedu.spac.data.log.ReleaseTree
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.stocks.PriceManager
import com.trueedu.spac.data.stocks.StockPool
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.worker.DartAlarmManager
import com.trueedu.spac.worker.PeriodicSyncWorker
import com.trueedu.spac.worker.StockPriceAlarmManager
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.internal.Contexts
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), LifecycleEventObserver, Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var stockPriceAlarmManager: StockPriceAlarmManager

    @Inject
    lateinit var dartAlarmManager: DartAlarmManager

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface InjectModule {
        fun getLocal(): Local
        fun getStockPool(): StockPool
        fun getPriceManager(): PriceManager
        fun getAdmobManager(): com.trueedu.spac.ui.ads.AdmobManager
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        val local = entryPointInjector(InjectModule::class.java).getLocal()
        local.migrate()
        init()

        // UserKey가 유효하면 관리자 모드로 동작
        val adminMode = isAdminMode(local)
        logD("🔍 관리자 모드 체크: $adminMode")

        if (adminMode) {
            // 관리자 모드: 마스터 파일 다운로드 + Firebase 업로드
            logD("⚙️ 관리자 모드 - Worker 및 알람 설정 시작")
            setupPeriodicWork() // 15분 내외로 마스터 파일 업데이트
            setupStockPriceAlarm() // 거래 시간 중 5분마다 시세 업데이트
            setupDartUpdateAlarm() // 평일 9:00-23:00, 30분마다 DART 공시 업데이트
            logD("✅ Admin mode enabled - UserKey is valid, periodic updates active")
        } else {
            // 일반 사용자 모드: Firebase에서 읽기만
            // 종목 정보는 앱 시작 시 자동으로 로드됨 (onStateChanged 참조)
            logD("ℹ️ User mode - UserKey is invalid, read-only from Firebase")
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * 관리자 모드 여부 확인
     * UserKey가 유효하면 관리자로 간주
     */
    private fun isAdminMode(local: Local): Boolean {
        return local.getUserKey().isValid()
    }

    private fun init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(FileNameTree())
        } else {
            Timber.plant(ReleaseTree())
        }
    }

    /**
     * 주기적인 마스터 파일 업데이트 작업 설정
     * WorkManager를 사용하여 15-20분 간격으로 실행
     * 배터리 최적화를 고려하여 정확한 시간이 아닐 수 있음
     */
    private fun setupPeriodicWork() {
        logD("🔧 setupPeriodicWork() 호출됨")

        // 작업 실행 조건 설정
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 네트워크 연결 필요
            .build()

        // 주기적 작업 요청 생성 (약 15분 간격)
        val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(
            repeatInterval = 15, // 반복 간격
            repeatIntervalTimeUnit = TimeUnit.MINUTES // 시간 단위
        )
            .setConstraints(constraints)
            .build()

        // WorkManager에 작업 등록
        // KEEP: 기존 작업이 있으면 유지, REPLACE: 새로운 작업으로 교체
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            PeriodicSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )

        logD("✅ Periodic work scheduled: ${PeriodicSyncWorker.WORK_NAME}")
        logD("📝 정책: KEEP (기존 작업이 있으면 유지)")
        logD("⏰ 반복 간격: 15분")
        logD("🌐 네트워크 제약: CONNECTED")
    }

    /**
     * 주식 시세 업데이트를 위한 알람 설정
     * 거래 시간 중 5분 간격으로 실행
     */
    private fun setupStockPriceAlarm() {
        stockPriceAlarmManager.startTradingTimeAlarm()
        logD("Stock price alarm initialized")
    }

    /**
     * DART 공시 업데이트를 위한 알람 설정
     * 평일 9:00-23:00 사이에 30분 간격으로 실행
     */
    private fun setupDartUpdateAlarm() {
        dartAlarmManager.startUpdateTimeAlarm()
        logD("DART update alarm initialized")
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val injector = entryPointInjector(InjectModule::class.java)
        val stockPool = injector.getStockPool()
        val priceManager = injector.getPriceManager()
        val admobManager = injector.getAdmobManager()

        when (event) {
            Lifecycle.Event.ON_CREATE -> {}
            Lifecycle.Event.ON_START -> {
                stockPool.loadStockInfo()
                priceManager.onStart()
                admobManager.start()
            }
            Lifecycle.Event.ON_STOP -> {
                priceManager.onStop()
                admobManager.stop()
            }
            Lifecycle.Event.ON_DESTROY -> {}
            Lifecycle.Event.ON_RESUME -> {}
            Lifecycle.Event.ON_PAUSE -> {}
            Lifecycle.Event.ON_ANY -> {}
        }
    }
}

fun <T> Context.entryPointInjector(clazz: Class<T>): T {
    return EntryPoints.get(Contexts.getApplication(applicationContext), clazz)
}
