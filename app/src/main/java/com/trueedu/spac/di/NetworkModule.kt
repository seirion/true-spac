package com.trueedu.spac.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trueedu.spac.BuildConfig
import com.trueedu.spac.network.TokenInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import timber.log.Timber
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
    private const val TAG = "OkHttp"
    private val connectTimeout = 20.seconds
    private val callTimeout = 20.seconds
    private val writeTimeout = 20.seconds
    private val readTimeout = 20.seconds

    @Provides
    @BaseUrl
    fun providesBaseUrl(): String {
        return "https://openapi.koreainvestment.com:9443"
    }

    @Provides
    @WebSocketUrl
    fun providesWebsocketUrl(): String {
        return "ws://ops.koreainvestment.com:21000"
    }

    @Provides
    @Singleton
    fun providesLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(PrettyPrintLogger()).apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun providesChuckerInterceptor(@ApplicationContext context: Context): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context)
            .alwaysReadResponseBody(true)
            .collector(
                ChuckerCollector(
                    context,
                    showNotification = true,
                    retentionPeriod = RetentionManager.Period.ONE_WEEK
                )
            )
            .build()
    }

    @Provides
    @Singleton
    fun providesFlipperOkhttpInterceptor(networkFlipperPlugin: NetworkFlipperPlugin): FlipperOkhttpInterceptor {
        return FlipperOkhttpInterceptor(networkFlipperPlugin)
    }

    @Provides
    @Singleton
    @KisOkHttp
    fun providesOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
        tokenInterceptor: TokenInterceptor,
        flipperOkhttpInterceptor: FlipperOkhttpInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(chuckerInterceptor)
            .addNetworkInterceptor(flipperOkhttpInterceptor)
            .connectTimeout(connectTimeout.toJavaDuration())
            .callTimeout(callTimeout.toJavaDuration())
            .writeTimeout(writeTimeout.toJavaDuration())
            .readTimeout(readTimeout.toJavaDuration())
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    @KisRetrofit
    fun providesRetrofit(
        @BaseUrl baseUrl: String,
        @KisOkHttp okHttpClient: OkHttpClient
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = false
            serializersModule = Json.serializersModule
            explicitNulls = false
        }

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory(contentType))
            .callFactory { okHttpClient.newCall(it) }
            .build()
    }

    private class PrettyPrintLogger : HttpLoggingInterceptor.Logger {
        private val jsonFormatter = Json {
            prettyPrint = true
            isLenient = true
        }

        override fun log(message: String) {
            val formattedMessage = try {
                // Try to parse and format as JSON
                val jsonElement = jsonFormatter.parseToJsonElement(message)
                jsonFormatter.encodeToString(JsonElement.serializer(), jsonElement)
            } catch (e: Exception) {
                // If not valid JSON, return original message
                message
            }

            Timber.d(formattedMessage)
        }
    }
}
