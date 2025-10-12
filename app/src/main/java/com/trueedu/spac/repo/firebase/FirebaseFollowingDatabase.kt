package com.trueedu.spac.repo.firebase

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.stocks.FollowingManager.Companion.MAX_GROUP_SIZE
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseFollowingDatabase @Inject constructor() : FirebaseDatabaseBase() {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logD("FirebaseFollowingDatabase error: ${throwable.message}")
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    suspend fun loadGroupNames(): List<String?> {
        val currentUser = firebaseCurrentUser() ?: run {
            logD("loadGroupNames() failed: currentUser null")
            return emptyList()
        }
        val userId = currentUser.uid

        return try {
            val ref = database.getReference("users")
            val snapshot = ref.child(userId).child("watch-names")
            val m = snapshot.get().await()
                .getValue(object : GenericTypeIndicator<Map<String, String>>() {})
                ?: return emptyList()

            List(MAX_GROUP_SIZE) {
                m[it.toString()]
            }
        } catch (e: Exception) {
            logD("loadGroupNames() error: ${e.message}")
            emptyList()
        }
    }

    fun writeGroupNames(list: List<String?>) {
        scope.launch {
            try {
                writeToDatabase("watch-names", list, "writeGroupNames")
            } catch (e: Exception) {
                logD("writeGroupNames() error: ${e.message}")
            }
        }
    }

    suspend fun loadWatchList(): List<List<String>> {
        val currentUser = firebaseCurrentUser() ?: run {
            logD("loadWatchList() failed: currentUser null")
            return emptyList()
        }
        val userId = currentUser.uid

        val ref = database.getReference("users")
        val snapshot = ref.child(userId).child("watch")
        return try {
            val list = snapshot.get().await()
                .getValue(object : GenericTypeIndicator<List<List<String>>>() {})

            list ?: emptyList()
        } catch (e: Exception) {
            // 이전 버전과의 호환성: Map 형식으로 저장된 데이터를 처리
            try {
                val list = snapshot.get().await()
                    .getValue(object : GenericTypeIndicator<Map<String, List<String>>>() {})
                    ?.let { m ->
                        List(MAX_GROUP_SIZE) {
                            m[it.toString()] ?: emptyList()
                        }
                    }
                list ?: emptyList()
            } catch (e2: Exception) {
                logD("loadWatchList() error: ${e2.message}")
                emptyList()
            }
        }
    }

    fun writeWatchList(list: List<List<String>>) {
        scope.launch {
            try {
                writeToDatabase("watch", list, "writeWatchList")
            } catch (e: Exception) {
                logD("writeWatchList() error: ${e.message}")
            }
        }
    }

    private suspend fun <T> writeToDatabase(
        childPath: String,
        data: T,
        operationName: String
    ) {
        val currentUser = firebaseCurrentUser() ?: run {
            logD("$operationName() failed: currentUser null")
            return
        }
        val userId = currentUser.uid

        val ref = database.getReference("users")
        val snapshot = ref.child(userId).child(childPath)
        snapshot.setValue(data).await()
    }
}
