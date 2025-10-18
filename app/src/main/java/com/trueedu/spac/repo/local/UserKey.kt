package com.trueedu.spac.repo.local

import kotlinx.serialization.Serializable

@Serializable
data class UserKey(
    val appKey: String?,
    val appSecret: String?,
    val accountNum: String?,
    val htsId: String?,
) {
    fun isValid(): Boolean =
        !appKey.isNullOrBlank() && !appSecret.isNullOrBlank()
}
