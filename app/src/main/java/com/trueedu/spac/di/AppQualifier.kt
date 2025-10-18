package com.trueedu.spac.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersion

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersionCode

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TokenRefreshService

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NormalService

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebSocketUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KisRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DartRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KisOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DartOkHttp
