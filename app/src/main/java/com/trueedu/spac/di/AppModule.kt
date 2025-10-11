package com.trueedu.spac.di

import android.app.Application
import android.content.Context
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.repo.local.Local
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModuleProvider {
    @Provides
    @Singleton
    fun providesLocal(
        @ApplicationContext context: Context
    ): Local {
        return Local(context.getSharedPreferences("local", Context.MODE_PRIVATE))
    }

    @Provides
    @Singleton
    fun providesTrueAnalytics(
        @ApplicationContext context: Context,
    ): TrueAnalytics {
        return TrueAnalytics(context as Application)
    }
}
