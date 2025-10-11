package com.trueedu.spac.di

import android.content.Context
import androidx.room.Room
import com.trueedu.spac.db.AppDatabase
import com.trueedu.spac.db.stocks.StockInfoLocalDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my-database"
        ).build()
    }

    @Provides
    fun provideStockDao(appDatabase: AppDatabase): StockInfoLocalDao {
        return appDatabase.stockInfoLocalDao()
    }
}
