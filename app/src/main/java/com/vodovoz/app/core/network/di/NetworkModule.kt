package com.vodovoz.app.core.network.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vodovoz.app.BuildConfig
import com.vodovoz.app.core.network.ApiConfig
import com.vodovoz.app.core.network.VodovozWebConfig
import com.vodovoz.app.core.network.interceptor.BaseUrlInterceptor
import com.vodovoz.app.core.network.interceptor.ChangeUrlInterceptor
import com.vodovoz.app.core.network.interceptor.VodovozInterceptor
import com.vodovoz.app.data.MainApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {


    @Binds
    @Singleton
    @IntoSet
    abstract fun providerBaseUrlInterceptor(
        baseUrlInterceptor: BaseUrlInterceptor,
    ): Interceptor

    @Binds
    @Singleton
    @IntoSet
    abstract fun providerUrlInterceptor(
        changeUrlInterceptor: ChangeUrlInterceptor
    ): Interceptor

    @Binds
    @Singleton
    @IntoSet
    abstract fun providerCookieInterceptor(
        vodovozInterceptor: VodovozInterceptor,
    ): Interceptor



    companion object {

        @Provides
        @Singleton
        fun providesOkHttpClient(
            interceptors: Set<@JvmSuppressWildcards Interceptor>,
        ): OkHttpClient {
            val okHttpClient = OkHttpClient.Builder()
            for (inter in interceptors) {
                if(!BuildConfig.DEBUG && inter is HttpLoggingInterceptor) {
                    continue
                }
                okHttpClient.addInterceptor(inter)
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
        fun provideLoggingInterceptor(): Interceptor {
            return HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        @Provides
        @Singleton
        @Named("main")
        fun providesMainRetrofit(okHttpClient: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .baseUrl(VodovozWebConfig.VODOVOZ_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()
        }



        @Provides
        @Singleton
        fun providesMoshi(): Moshi {
            return Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        }

        @Provides
        @Singleton
        fun provideMainApi(@Named("main") retrofit: Retrofit): MainApi = retrofit.create()
    }
}