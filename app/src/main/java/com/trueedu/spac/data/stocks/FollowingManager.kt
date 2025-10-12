package com.trueedu.spac.data.stocks

import androidx.compose.runtime.mutableStateOf
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.user.UserCycle
import com.trueedu.spac.repo.firebase.FirebaseFollowingDatabase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowingManager @Inject constructor(
    private val userCycle: UserCycle,
    private val firebaseFollowingDatabase: FirebaseFollowingDatabase,
) {
    companion object {
        // 관심 그룹 개수
        const val MAX_GROUP_SIZE = 10
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logD("FollowingManager error: ${throwable.message}")
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    val groupNames = mutableStateOf<List<String?>>(emptyList())
    val list = mutableStateOf<List<List<String>>>(emptyList())

    init {
        scope.launch {
            userCycle.loginEvent
                .filterNotNull()
                .collect { login ->
                    if (login) {
                        try {
                            // 관심 그룹 이름
                            val names = firebaseFollowingDatabase.loadGroupNames()
                            logD("loadWatchGroupNames: $names")

                            // 관심 종목 데이터
                            val temp = firebaseFollowingDatabase.loadWatchList()
                            logD("loadWatchList: ${temp.size}")

                            withContext(Dispatchers.Main) {
                                groupNames.value = names
                                fillDefaultList(temp)
                            }
                        } catch (e: Exception) {
                            logD("Failed to load following data: ${e.message}")
                            withContext(Dispatchers.Main) {
                                groupNames.value = emptyList()
                                fillDefaultList(emptyList())
                            }
                        }
                    } else { // logout
                        withContext(Dispatchers.Main) {
                            groupNames.value = emptyList()
                            fillDefaultList(emptyList())
                        }
                    }
                }
        }
    }


    fun getGroupName(index: Int): String? {
        return groupNames.value.getOrNull(index)
    }

    fun updateGroupName(index: Int, name: String) {
        val list = List(MAX_GROUP_SIZE) {
            if (it == index) name
            else groupNames.value.getOrNull(it)
        }

        groupNames.value = list
        firebaseFollowingDatabase.writeGroupNames(list)
    }

    fun get(index: Int): List<String> {
        return list.value.getOrElse(index) { emptyList() }
    }

    fun add(index: Int, code: String) {
        if (list.value.isEmpty()) return

        require(index in list.value.indices)

        if (list.value[index].contains(code)) {
            logD("trying to insert already existing code: $code")
            return
        }

        val temp = list.value
            .mapIndexed { i, list ->
                if (i == index) {
                    list + code
                } else {
                    list
                }
            }
        list.value = temp
        firebaseFollowingDatabase.writeWatchList(temp)
    }

    fun removeAt(targetPage: Int, index: Int) {
        require(targetPage in list.value.indices)
        require(index in list.value[targetPage].indices)

        val temp = list.value
            .mapIndexed { p, items ->
                if (p == targetPage) {
                    items.toMutableList().also {
                        it.removeAt(index)
                    }
                } else {
                    items
                }
            }
        list.value = temp
        firebaseFollowingDatabase.writeWatchList(temp)
    }

    fun remove(targetPage: Int, code: String) {
        require(targetPage in list.value.indices)

        if (!list.value[targetPage].contains(code)) {
            logD("trying to remove not existing code: $code")
            return
        }

        val temp = list.value
            .mapIndexed { i, list ->
                if (i == targetPage) {
                    list.filter { it != code }
                } else {
                    list
                }
            }
        list.value = temp
        firebaseFollowingDatabase.writeWatchList(temp)
    }

    // 편집한 관심종목 목록을 갱신
    fun replace(index: Int, codes: List<String>) {
        val temp = list.value
            .mapIndexed { i, list ->
                if (i == index) {
                    codes
                } else {
                    list
                }
            }
        list.value = temp
        firebaseFollowingDatabase.writeWatchList(temp)
    }

    // 특정 페이지에 관심 종목이 존재하는 지 여부
    fun contains(index: Int, code: String): Boolean {
        return list.value.getOrNull(index)?.contains(code) == true
    }

    // 전체 관심 종목에 존재하는 지 여부
    fun contains(code: String): Boolean {
        return list.value.any { watchList ->
            watchList.contains(code)
        }
    }

    // 관심 종목이 하나라도 있는 지 여부
    fun hasWatchingStock(): Boolean {
        return list.value.any { it.isNotEmpty() }
    }

    /**
     * 크기가 MAX_GROUP_SIZE 인 리스트를 만들어서 list 에 넣는다.
     */
    private fun fillDefaultList(loadedData: List<List<String>>) {
        list.value = MutableList(MAX_GROUP_SIZE) {
            loadedData.getOrNull(it) ?: emptyList()
        }
    }
}
