package com.trueedu.spac

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.trueedu.spac.data.log.FileNameTree
import com.trueedu.spac.data.log.ReleaseTree
import com.trueedu.spac.data.stocks.StockPool
import com.trueedu.spac.repo.local.Local
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.internal.Contexts
import dagger.hilt.components.SingletonComponent
import timber.log.Timber

@HiltAndroidApp
class App : Application(), LifecycleEventObserver {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface InjectModule {
        fun getLocal(): Local
        fun getStockPool(): StockPool
    }

    override fun onCreate() {
        super.onCreate()

        val local = entryPointInjector(InjectModule::class.java).getLocal()
        local.migrate()
        init()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private fun init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(FileNameTree())
        } else {
            Timber.plant(ReleaseTree())
        }
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
