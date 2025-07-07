package com.vodovoz.app.core.network.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vodovoz.app.BuildConfig
import com.vodovoz.app.core.network.converters.LocalDateTimeJsonAdapter
import com.vodovoz.app.core.network.converters.UnicodeJsonAdapter
import com.vodovoz.app.core.network.interceptor.BaseUrlInterceptor
import com.vodovoz.app.core.network.interceptor.CookieHandlerInterceptor
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
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VodovozInterceptor

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {


    @Binds
    @Singleton
    @IntoSet
    @VodovozInterceptor
    abstract fun providerBaseUrlInterceptor(
        baseUrlInterceptor: BaseUrlInterceptor,
    ): Interceptor


    @Binds
    @Singleton
    @IntoSet
    @VodovozInterceptor
    abstract fun bindCookieHandlerInterceptor(
        interceptor : CookieHandlerInterceptor
    ): Interceptor



    companion object {

        @Provides
        @Singleton
        @Named("vodovoz")
        fun providesOkHttpClient(
            @VodovozInterceptor
            interceptors: Set<@JvmSuppressWildcards Interceptor>,
        ): OkHttpClient {
            val okHttpClient = OkHttpClient.Builder()
            for (interceptor in interceptors) {
                if(!BuildConfig.DEBUG && interceptor is HttpLoggingInterceptor){
                    continue
                }
                okHttpClient.addInterceptor(interceptor)
            }


            return okHttpClient
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

        }

        @Provides
        @Singleton
        @IntoSet
        @VodovozInterceptor
        fun provideLoggingInterceptor(): Interceptor {
            return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        @Provides
        @Singleton
        fun provideMoshi(): Moshi {
            return Moshi.Builder()
                .add(LocalDateTime::class.java, LocalDateTimeJsonAdapter().nullSafe())
                .add(KotlinJsonAdapterFactory())
                .build()
        }
    }
}