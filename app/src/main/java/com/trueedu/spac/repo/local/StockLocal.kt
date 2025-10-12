package com.trueedu.spac.repo.local

import com.trueedu.spac.api.model.dao.StockInfoLocal
import com.trueedu.spac.db.stocks.StockInfoLocalDao
import javax.inject.Inject

class StockLocal @Inject constructor(
    private val stockInfoLocalDao: StockInfoLocalDao
) {
    suspend fun getAllStocks(): List<StockInfoLocal> {
        return stockInfoLocalDao.getAllStocks()
    }

    suspend fun setAllStocks(stocks: List<StockInfoLocal>)  {
        return stockInfoLocalDao.insertAll(stocks)
    }

    suspend fun deleteAllStocks()  {
        return stockInfoLocalDao.deleteAllStocks()
    }
}
