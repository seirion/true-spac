package com.trueedu.spac.data.user

sealed class TokenKeyEvent

// 유효한 토큰으로 확인 될 때
data object TokenOk : TokenKeyEvent()

// 새 토큰이 발행 될 때
data object TokenIssued : TokenKeyEvent()

data object TokenIssueFail : TokenKeyEvent()

data object TokenExpired : TokenKeyEvent()

data object TokenRevoked : TokenKeyEvent()

// 새 websocket key가 발행 될 때
data object WebSocketKeyIssued : TokenKeyEvent()
