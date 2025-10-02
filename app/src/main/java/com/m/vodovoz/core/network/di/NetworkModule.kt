package com.m.vodovoz.core.network.di

import com.m.vodovoz.BuildConfig
import com.m.vodovoz.common.moshi.adapter.LocalDateTimeJsonAdapter
import com.m.vodovoz.core.network.interceptor.BaseUrlInterceptor
import com.m.vodovoz.core.network.interceptor.BlockAppInterceptor
import com.m.vodovoz.core.network.interceptor.CookieHandlerInterceptor
import com.m.vodovoz.core.network.interceptor.LastErrorInterceptor
import com.m.vodovoz.core.network.interceptor.VersionQueryInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VodovozInterceptorDI

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VodovozQualifier

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {


    @Binds
    @Singleton
    @IntoSet
    @VodovozInterceptorDI
    abstract fun providerVersionQueryInterceptor(
        baseUrlInterceptor: VersionQueryInterceptor,
    ): Interceptor

    @Binds
    @Singleton
    @IntoSet
    @VodovozInterceptorDI
    abstract fun providerBaseUrlInterceptor(
        baseUrlInterceptor: BaseUrlInterceptor,
    ): Interceptor

    @Binds
    @Singleton
    @IntoSet
    @VodovozInterceptorDI
    abstract fun providerErrorCacheInterceptor(
        lastErrorInterceptor: LastErrorInterceptor,
    ): Interceptor


    @Binds
    @Singleton
    @IntoSet
    @VodovozInterceptorDI
    abstract fun bindCookieHandlerInterceptor(
        interceptor: CookieHandlerInterceptor,
    ): Interceptor

    @Binds
    @Singleton
    @IntoSet
    @VodovozInterceptorDI
    abstract fun bindAppSignalInterceptor(
        interceptor: BlockAppInterceptor,
    ): Interceptor


    companion object {

        @Provides
        @Singleton
        @VodovozQualifier
        fun providesOkHttpClient(
            @VodovozInterceptorDI
            interceptors: Set<@JvmSuppressWildcards Interceptor>,
        ): OkHttpClient {
            val okHttpClient = OkHttpClient.Builder()
            for (interceptor in interceptors) {
                if (!BuildConfig.DEBUG && interceptor is HttpLoggingInterceptor) {
                    continue
                }
                okHttpClient.addInterceptor(interceptor)
            }


            return okHttpClient
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

        }

        @Provides
        @Singleton
        @IntoSet
        @VodovozInterceptorDI
        fun provideLoggingInterceptor(): Interceptor {
            return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        @Provides
        @Singleton
        fun provideMoshi(): Moshi {
            return Moshi.Builder()
                .add(
                    LocalDateTime::class.java,
                    LocalDateTimeJsonAdapter().lenient().nullSafe()
                )
                .add(KotlinJsonAdapterFactory())
                .build()
        }
    }
}