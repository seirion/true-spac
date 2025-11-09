package com.trueedu.spac.api.model.dto.firebase

data class UserFeedback(
    val title: String = "",
    val email: String = "",
    val content: String = "",
    val timestamp: Long = 0L, // yyyyMMddHHmmss
    val userId: String = "",
    val userEmail: String = ""
)