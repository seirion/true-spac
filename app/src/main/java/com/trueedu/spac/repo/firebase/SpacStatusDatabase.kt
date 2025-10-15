package com.trueedu.spac.repo.firebase

import com.google.firebase.database.GenericTypeIndicator
import com.trueedu.spac.api.model.dto.firebase.SpacSchedule
import com.trueedu.spac.api.model.dto.firebase.SpacStatus
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.util.toDateCompactString
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseRealtimeDatabase 에서 spac/status 데이터 처리
 */
@Singleton
class SpacStatusDatabase @Inject constructor(): FirebaseDatabaseBase() {
    companion object {
        private const val META_KEY = "meta"
        private const val SNAPSHOT_KEY = "spac"
    }

    // caching
    private var spacList: List<SpacStatus> = emptyList()

    private var spacScheduleList: Map<String, SpacSchedule> = emptyMap()

    /**
     * @return yyyyMMdd 포맷의 스트링
     */
    suspend fun serverLastUpdated(): Long? {
        logD("serverLastUpdated()")
        return try {
            val currentUser = firebaseCurrentUser()
            if (currentUser == null) {
                logD("serverLastUpdated() failed: currentUser null")
                return null
            }
            val ref = database.getReference(META_KEY)
            val snapshot = ref.child("spacLastUpdatedAt")
            val lastUpdated = snapshot.get().await()
                .getValue(Long::class.java)
            lastUpdated
        } catch (e: Exception) {
            logE("Failed to get server last updated time", e)
            null
        }
    }

    suspend fun load(): List<SpacStatus> {
        logD("load()")
        if (spacList.isNotEmpty()) {
            return spacList
        }

        return try {
            val currentUser = firebaseCurrentUser()
            if (currentUser == null) {
                logD("load() failed: currentUser null")
                return emptyList()
            }
            val ref = database.getReference(SNAPSHOT_KEY)
            val snapshot = ref.child("status")
            val list = snapshot.get().await()
                .getValue(object : GenericTypeIndicator<List<SpacStatus>>() {})

            if (list != null) spacList = list
            list ?: emptyList()
        } catch (e: Exception) {
            logE("Failed to load spac status from Firebase", e)
            emptyList()
        }
    }

    /**
     * admin only
     */
    suspend fun write(list: List<SpacStatus>, onSuccess: () -> Unit, onFail: () -> Unit) {
        try {
            val currentUser = firebaseCurrentUser()
            if (currentUser == null) {
                logD("write() failed: currentUser null")
                onFail()
                return
            }
            val meta = database.getReference(META_KEY)
            val ref = database.getReference(SNAPSHOT_KEY)
            meta.child("spacLastUpdatedAt").setValue(
                LocalDate.now().toDateCompactString().toLong()
            ).await()

            val snapshot = ref.child("status")
            snapshot.setValue(list)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    logE("Failed to write spac status to Firebase", e)
                    onFail()
                }
        } catch (e: Exception) {
            logE("Failed to write spac status", e)
            onFail()
        }
    }

    suspend fun loadSpacSchedule(force: Boolean = false): Map<String, SpacSchedule> {
        logD("loadSpacSchedule()")
        if (spacScheduleList.isNotEmpty() && !force) {
            return spacScheduleList
        }

        return try {
            val currentUser = firebaseCurrentUser()
            if (currentUser == null) {
                logD("loadSpacSchedule() failed: currentUser null")
                return spacScheduleList
            }
            val ref = database.getReference(SNAPSHOT_KEY)
            val snapshot = ref.child("schedule")
            val m = snapshot.get().await()
                .getValue(object : GenericTypeIndicator<Map<String, SpacSchedule>>() {})
            if (m != null) spacScheduleList = m
            spacScheduleList
        } catch (e: Exception) {
            logE("Failed to load spac schedule from Firebase", e)
            spacScheduleList
        }
    }

    suspend fun writeSpacSchedule(
        list: List<Pair<String, SpacSchedule>>,
        onSuccess: () -> Unit,
        onFail: () -> Unit,
    ) {
        try {
            val currentUser = firebaseCurrentUser()
            if (currentUser == null) {
                logD("writeSpacSchedule() failed: currentUser null")
                onFail()
                return
            }
            val ref = database.getReference(SNAPSHOT_KEY)
            val snapshot = ref.child("schedule")
            val m = list.associate { (date, schedule) -> date to schedule }

            snapshot.setValue(m)
                .addOnSuccessListener {
                    spacScheduleList = m
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    logE("Failed to write spac schedule to Firebase", e)
                    onFail()
                }
        } catch (e: Exception) {
            logE("Failed to write spac schedule", e)
            onFail()
        }
    }
}
