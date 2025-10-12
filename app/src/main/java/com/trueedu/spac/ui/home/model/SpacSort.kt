package com.trueedu.spac.ui.home.model

enum class SpacSort(val title: String) {
    ISSUE_DATE("상장일"), // 상장일
    MARKET_CAP("시가총액"), // 시가총액
    GROWTH_RATE("공모가 대비 상승률"), // 공모가 대비 상승률
    REDEMPTION_VALUE("청산시 예상 수익률"), // (청산가격 - 현재가) 1년 환산 수익률
    VOLUME("거래량"), // 거래량
}
