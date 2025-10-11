package com.trueedu.spac.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.trueedu.spac.api.model.dao.StockInfoLocal
import com.trueedu.spac.db.stocks.StockInfoLocalDao

@Database(entities = [StockInfoLocal::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockInfoLocalDao(): StockInfoLocalDao
}
