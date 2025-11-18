package com.trueedu.spac.repo.firebase

import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.spac.api.model.dto.firebase.UsStockInfo
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUsStockDatabase @Inject constructor() : FirebaseDatabaseBase() {
    private val stocksRef = database.getReference(PATH_STOCKS)

    suspend fun loadStocks(): Map<String, UsStockInfo> {
        logD("loadStocks()")
        try {
            val snapshot = stocksRef.get().await()
            val nasdaq = snapshot.child(PATH_NASDAQ).getValue(object : GenericTypeIndicator<Map<String, UsStockInfo>>() {})
                ?: emptyMap()

            logD("loading us stocks completed")

            return nasdaq
        } catch (e: Exception) {
            // 오류 처리
            logE("Failed to get us stocks", e)
            return emptyMap()
        }
    }

    suspend fun writeStocks(stocks: Map<String, UsStockInfo>): Boolean {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            logD("cannot write values: currentUser is null")
            return false
        }

        return try {
            stocksRef.child(PATH_NASDAQ).setValue(stocks).await()
            logD("writing us stocks completed")
            true
        } catch (e: Exception) {
            logE("Failed to update us stocks", e)
            false
        }
    }

    companion object {
        private const val PATH_STOCKS = "stocks"
        private const val PATH_NASDAQ = "nasdaq"
    }
}
