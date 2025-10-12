package com.trueedu.spac.ui.home.model

data class SpacFilter(
    val listedOverTwoYears: Boolean = false, // 3년 차 종목
    val underParValue: Boolean = false, // 액면가 이하 가격 종목
    val onlyWatching: Boolean = false, // 관심 종목만 보기
)
