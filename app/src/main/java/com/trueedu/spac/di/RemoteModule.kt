package com.trueedu.spac.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trueedu.spac.repo.kis.AuthRemote
import com.trueedu.spac.repo.kis.AuthRemoteImpl
import com.trueedu.spac.repo.kis.PriceRemote
import com.trueedu.spac.repo.kis.PriceRemoteImpl
import com.trueedu.spac.repo.kis.services.AuthService
import com.trueedu.spac.repo.kis.services.PriceService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RemoteModule {
    @Provides
    @Singleton
    @NormalService
    fun providesAuthService(@KisRetrofit retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    @Singleton
    @NormalService
    fun providesPriceService(@KisRetrofit retrofit: Retrofit): PriceService {
        return retrofit.create(PriceService::class.java)
    }

    @Provides
    @Singleton
    @TokenRefreshService
    fun providesTokenRefreshService(
        @BaseUrl baseUrl: String,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
    ): AuthService {
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = false
            explicitNulls = false
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(chuckerInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthService::class.java)
    }

    /**
     * 토큰 발급/갱신은 반드시 @TokenRefreshService(전용 OkHttpClient)를 써야 한다.
     *
     * KIS 클라이언트를 쓰면 시세 조회처럼 동시에 여러 요청이 나가는 상황에서
     * 토큰 갱신 요청이 영영 나가지 못한다. OkHttp의 maxRequestsPerHost 기본값은 5인데,
     * 만료를 감지한 요청들이 TokenRefreshInterceptor 안에서 갱신을 기다리며 그 슬롯을 붙잡고 있어
     * 정작 갱신 요청은 큐에서 대기만 하다가 callTimeout으로 죽는다.
     */
    @Singleton
    @Provides
    fun providesAuthRemote(
        @TokenRefreshService
        authService: AuthService
    ): AuthRemote = AuthRemoteImpl(authService = authService)

    @Singleton
    @Provides
    fun providesPriceRemote(
        @NormalService
        priceService: PriceService
    ): PriceRemote = PriceRemoteImpl(priceService = priceService)
}
