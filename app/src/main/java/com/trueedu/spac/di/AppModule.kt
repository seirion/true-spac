package com.trueedu.spac.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.repo.local.Local
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModuleProvider {
    @Provides
    @Singleton
    fun providesSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("local", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providesLocal(
        sharedPreferences: SharedPreferences
    ): Local {
        return Local(sharedPreferences)
    }

    @Provides
    @Singleton
    fun providesTrueAnalytics(
        application: Application
    ): TrueAnalytics {
        return TrueAnalytics(application)
    }

    @Provides
    @Singleton
    fun providesFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    @ApplicationScope
    fun providesApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
