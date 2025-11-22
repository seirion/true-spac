package com.trueedu.spac.repo.firebase

/**
 * Firebase Realtime Database 경로 상수 관리
 */
object FirebasePaths {
    // Root paths
    const val USERS = "users"
    const val META = "meta"
    const val APP_CONFIG = "app_config"
    const val STOCKS = "stocks"
    const val SPAC = "spac"
    const val DART = "dart"

    // User sub-paths
    const val USER_CONFIG = "config"
    const val USER_WATCH = "watch"
    const val USER_WATCH_NAMES = "watch-names"

    // Other paths
    const val DART_LIST = "list"
    const val REFUND = "refund"
}

