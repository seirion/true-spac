package com.trueedu.spac.repo.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.spac.api.model.dto.firebase.AppConfig
import com.trueedu.spac.api.model.dto.firebase.AppNotice
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.firebase.StockInfoKosdaq
import com.trueedu.spac.api.model.dto.firebase.StockInfoKospi
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.api.model.dto.firebase.UserRemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRealtimeDatabase @Inject constructor() {
    companion object {
        private val TAG = FirebaseRealtimeDatabase::class.java.simpleName
    }

    // Firebase Realtime Database 인스턴스 가져오기
    private val database = FirebaseDatabase.getInstance()
    private val metaRef = database.getReference("meta") // 마지막 업데이트 시각
    private val configRef = database.getReference("app_config") // 앱 속성 관련
    private val stocksRef = database.getReference("stocks") // 종목 데이터
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun getAppConfig(): AppConfig {
        try {
            val snapshot = configRef.get().await()
            val minVersion = snapshot.child("minVersion").getValue(String::class.java)
            val notice = snapshot.child("notice").getValue(AppNotice::class.java)
            return AppConfig(minVersion, notice)
        } catch (e: Exception) {
            logE("Error getting app config", e)
            return AppConfig()
        }
    }

    suspend fun lastUpdatedTime(): Long {
        try {
            val snapshot = metaRef.get().await()
            val lastUpdatedAt = snapshot.child("stockLastUpdatedAt").getValue(Long::class.java)
            return lastUpdatedAt ?: 0L
        } catch (e: Exception) {
            return 0L
        }
    }

    suspend fun loadStocks(): Pair<Long, Map<String, StockInfo>> {
        logD("loadStocks()")
        try {
            val snapshotMeta = metaRef.get().await()
            val snapshot = stocksRef.get().await()
            val lastUpdatedAt = snapshotMeta.child("stockLastUpdatedAt").getValue(Long::class.java)
            val kospi = snapshot.child("kospi").getValue(object : GenericTypeIndicator<Map<String, StockInfoKospi>>() {})
                ?: emptyMap()
            val kosdaq = snapshot.child("kosdaq").getValue(object : GenericTypeIndicator<Map<String, StockInfoKosdaq>>() {})
                ?: emptyMap()

            if (lastUpdatedAt == null) {
                logD("cannot read values: \"lastUpdatedAt\"")
                return 0L to emptyMap()
            }
            logD("loading stocks completed - lastUpdatedAt: $lastUpdatedAt")

            return lastUpdatedAt to (kospi + kosdaq)
        } catch (e: Exception) {
            // 오류 처리
            logE("Failed to get stocks", e)
            return 0L to emptyMap()
        }
    }

    private suspend fun firebaseCurrentUser(): FirebaseUser? {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            logD("Firebase currentUser is null - user needs to re-login")
        }

        return currentUser
    }

    suspend fun loadDelistedStocks(): List<String> {
        logD("loadDelistedStocks()")
        try {
            val snapshot = withTimeout(5_000L) {
                stocksRef.child("delisted").get().await()
            }
            return snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
        } catch (e: Exception) {
            // 오류 처리
            logE("Failed to get delisted stocks", e)
            return emptyList()
        }
    }

    /**
     * @param lastUpdatedAt: 'yyyyMMddHHmm'
     */
    suspend fun writeStockInfo(lastUpdatedAt: Long, stocks: Map<String, StockInfo>) {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            logD("cannot write values: \"currentUser\"")
            return
        }

        val kospi = stocks.filter { it.value.isKospi }
        val kosdaq = stocks.filter { it.value.isKosdaq }
        try {
            stocksRef.child("kospi").setValue(kospi)
            stocksRef.child("kosdaq").setValue(kosdaq)
            metaRef.child("stockLastUpdatedAt").setValue(lastUpdatedAt)
        } catch (e: Exception) {
            logE("Failed to update stocks", e)
        }
    }

    fun deleteUser(
        onSuccess: () -> Unit,
        onFail: () -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentUser = firebaseCurrentUser()
            if (currentUser == null) {
                logD("deleteUser() failed: currentUser null")
            }
            val userId = currentUser?.uid ?: return@launch

            val ref = database.getReference("users")
            ref.child(userId).removeValue()
                .addOnSuccessListener {
                    MainScope().launch {
                        onSuccess()
                    }
                }
                .addOnFailureListener {
                    MainScope().launch {
                        onFail()
                    }
                }
        }
    }

    suspend fun loadUserConfig(): UserRemoteConfig {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            logD("loadUserConfig() failed: currentUser null")
            return UserRemoteConfig()
        }
        val userId = currentUser.uid

        val ref = database.getReference("users")
        val snapshot = ref.child(userId).child("config")
        val config = snapshot.get().await()
            .getValue(UserRemoteConfig::class.java)
        return config ?: UserRemoteConfig()
    }

    suspend fun writeUserConfig(config: UserRemoteConfig) {
        val currentUser = firebaseCurrentUser()
        if (currentUser == null) {
            logD("writeUserConfig() failed: currentUser null")
            return
        }
        val userId = currentUser.uid

        val ref = database.getReference("users")
        val snapshot = ref.child(userId).child("config")
        snapshot.setValue(config)
    }
}
