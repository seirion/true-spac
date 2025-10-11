package com.trueedu.spac.api.model.dto.firebase

data class AppNotice(
    val id: Int,
    val title: String,
    val body: String,
    val cancellable: Boolean,
) {
    // No-argument constructor required for Firebase
    constructor() : this(0, "", "", true)

    fun available(): Boolean {
        return id > 0 && title.isNotEmpty() && body.isNotEmpty()
    }
}
