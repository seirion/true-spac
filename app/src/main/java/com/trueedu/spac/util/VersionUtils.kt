package com.trueedu.spac.util

object VersionUtils {
    /**
     * 버전 비교 함수 (숫자로 변환하여 비교)
     * @return version1이 크면 양수, version2가 크면 음수, 같으면 0
     */
    fun compareVersions(version1: String, version2: String): Int {
        val v1Parts = version1.split(".")
        val v2Parts = version2.split(".")

        val maxLength = maxOf(v1Parts.size, v2Parts.size)
        repeat(maxLength) {
            val v1Part = v1Parts.getOrNull(it)?.toIntOrNull() ?: 0
            val v2Part = v2Parts.getOrNull(it)?.toIntOrNull() ?: 0

            if (v1Part != v2Part) {
                return v1Part - v2Part
            }
        }

        return 0
    }
}

