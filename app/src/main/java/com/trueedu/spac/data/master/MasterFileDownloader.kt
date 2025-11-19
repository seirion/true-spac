package com.trueedu.spac.data.master

import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.stocks.StockInfoDownloader
import com.trueedu.spac.repo.firebase.FirebaseUsStockDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasterFileDownloader @Inject constructor(
    private val stockInfoDownloader: StockInfoDownloader,
    private val firebaseUsStockDatabase: FirebaseUsStockDatabase,
) {
    suspend fun downloadUsMasterFile() {
        try {
            val stocks = stockInfoDownloader.getUsStockInfoList()

            if (stocks.isEmpty()) {
                logD("No US stocks downloaded")
                return
            }

            logD("US stocks downloaded: ${stocks.size} stocks")
            val stockMap = stocks.associateBy { it.code }
            firebaseUsStockDatabase.writeStocks(stockMap)
            logD("US stocks uploaded to Firebase successfully")
        } catch (e: Exception) {
            logD("downloadUsMasterFile failed: $e")
            throw e
        }
    }
}
