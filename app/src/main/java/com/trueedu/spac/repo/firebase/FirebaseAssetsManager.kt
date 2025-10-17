package com.trueedu.spac.repo.firebase

import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.spac.api.model.dto.firebase.UserAsset
import com.trueedu.spac.data.log.logD
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseRealtimeDatabase 에서 user/assets 데이터만 처리
 */
@Singleton
class FirebaseAssetsManager @Inject constructor(
) : FirebaseDatabaseBase() {

    suspend fun loadAssets(): List<UserAsset> {
        logD("loadAssets()")
        val currentUser = firebaseCurrentUser()
        val userId = currentUser?.uid ?: run {
            logD("loadAssets() failed: currentUser null")
            return emptyList()
        }
        val ref = database.getReference("users") // 종목 데이터
        val snapshot = ref.child(userId).child("assets")
        val list = snapshot.get().await()
            .getValue(object : GenericTypeIndicator<List<UserAsset>>() {})
        return list ?: emptyList()
    }

    suspend fun writeAssets(list: List<UserAsset>) {
        logD("writeAssets()")
        val currentUser = firebaseCurrentUser()
        val userId = currentUser?.uid ?: run {
            logD("writeAssets() failed: currentUser null")
            return
        }

        val ref = database.getReference("users") // 종목 데이터
        val snapshot = ref.child(userId).child("assets")
        try {
            snapshot.setValue(list).await()
            logD("writeAssets() success")
        } catch (e: Exception) {
            logD("writeAssets() failed: ${e.message}")
            throw e
        }
    }
}
