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
import com.trueedu.spac.data.stocks.StockPool
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.worker.PeriodicSyncWorker
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
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface InjectModule {
        fun getLocal(): Local
        fun getStockPool(): StockPool
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
        setupPeriodicWork()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private fun init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(FileNameTree())
        } else {
            Timber.plant(ReleaseTree())
        }
    }

    /**
     * 주기적인 백그라운드 작업을 설정합니다.
     * 앱이 종료되어도 WorkManager가 작업을 스케줄하고 실행합니다.
     */
    private fun setupPeriodicWork() {
        // 작업 실행 조건 설정
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 네트워크 연결 필요
            .setRequiresBatteryNotLow(true) // 배터리가 낮지 않을 때만 실행
            .build()

        // 주기적 작업 요청 생성 (최소 15분 간격)
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

        logD("Periodic work scheduled: ${PeriodicSyncWorker.WORK_NAME}")
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val stockPool = entryPointInjector(InjectModule::class.java).getStockPool()

        when (event) {
            Lifecycle.Event.ON_CREATE -> {}
            Lifecycle.Event.ON_START -> {
                stockPool.loadStockInfo()
            }
            Lifecycle.Event.ON_STOP -> {
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
