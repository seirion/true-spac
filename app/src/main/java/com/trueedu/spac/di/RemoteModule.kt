package com.trueedu.spac.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trueedu.spac.repo.kis.AuthRemote
import com.trueedu.spac.repo.kis.AuthRemoteImpl
import com.trueedu.spac.repo.kis.services.AuthService
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

    /*
    @Provides
    @Singleton
    @NormalService
    fun providesAccountService(@KisRetrofit retrofit: Retrofit): AccountService {
        return retrofit.create(AccountService::class.java)
    }

    @Provides
    @Singleton
    @NormalService
    fun providesPriceService(@KisRetrofit retrofit: Retrofit): PriceService {
        return retrofit.create(PriceService::class.java)
    }
     */

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

    @Singleton
    @Provides
    fun providesAuthRemote(
        @NormalService
        authService: AuthService
    ): AuthRemote = AuthRemoteImpl(authService = authService)

    /*
    @Singleton
    @Provides
    fun providesAccountRemote(
        @NormalService
        accountService: AccountService
    ): AccountRemote = AccountRemoteImpl(accountService = accountService)

    @Singleton
    @Provides
    fun providesPriceRemote(
        @NormalService
        priceService: PriceService
    ): PriceRemote = PriceRemoteImpl(priceService = priceService)
     */
}
