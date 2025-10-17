package com.trueedu.spac.data.user

import androidx.compose.runtime.mutableStateOf
import com.trueedu.spac.api.model.dto.firebase.UserAsset
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.repo.firebase.FirebaseAssetsManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManualAssets @Inject constructor(
    private val userCycle: UserCycle,
    private val firebaseAssets: FirebaseAssetsManager,
) {
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logE("ManualAssets error: ${throwable.message}")
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    val assets = mutableStateOf<List<UserAsset>>(emptyList())

    init {
        scope.launch {
            userCycle.loginEvent
                .collect { isLogin ->
                    when (isLogin) {
                        true -> load()
                        false -> withContext(Dispatchers.Main) {
                            assets.value = emptyList()
                        }
                        else -> {}
                    }
                }
        }
    }

    private suspend fun load() {
        logD("load()")
        try {
            val assetList = firebaseAssets.loadAssets()
            logD("assetList: $assetList")
            withContext(Dispatchers.Main) {
                assets.value = assetList
            }
        } catch (e: Exception) {
            logE(e, "Failed to load assets")
        }
    }

    fun addAsset(asset: UserAsset, onSuccess: () -> Unit) {
        val hasAsset = assets.value.any { asset.code == it.code }
        val previousAssets = assets.value

        val newAssets = if (hasAsset) {
            assets.value.map {
                if (it.code == asset.code) asset else it
            }
        } else {
            assets.value + asset
        }
        assets.value = newAssets

        scope.launch {
            try {
                firebaseAssets.writeAssets(newAssets)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                logE(e, "Failed to add asset: ${asset.code}")
                withContext(Dispatchers.Main) {
                    // 실패 시 이전 상태로 롤백
                    assets.value = previousAssets
                }
            }
        }
    }

    fun deleteAsset(code: String, onSuccess: () -> Unit) {
        val previousAssets = assets.value

        val newAssets = assets.value.filterNot { it.code == code }
        assets.value = newAssets

        scope.launch {
            try {
                firebaseAssets.writeAssets(newAssets)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                logE(e, "Failed to delete asset: $code")
                withContext(Dispatchers.Main) {
                    // 실패 시 이전 상태로 롤백
                    assets.value = previousAssets
                }
            }
        }
    }

    fun get(code: String): UserAsset? {
        return assets.value.firstOrNull { it.code == code }
    }
}
