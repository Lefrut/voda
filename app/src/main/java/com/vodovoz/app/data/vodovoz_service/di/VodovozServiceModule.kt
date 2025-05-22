package com.vodovoz.app.data.vodovoz_service.di

import com.squareup.moshi.Moshi
import com.vodovoz.app.core.network.VodovozWebConfig
import com.vodovoz.app.core.network.interceptor.BaseUrlInterceptor
import com.vodovoz.app.core.network.interceptor.CookieHandlerInterceptor
import com.vodovoz.app.data.vodovoz_service.VodovozService
import com.vodovoz.app.data.vodovoz_service.repository.VodovozServiceRepositoryImpl
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class VodovozServiceModule {

    @Binds
    @Singleton
    abstract fun providesVodovozServiceRepository(vodovozServiceRepository: VodovozServiceRepositoryImpl): VodovozServiceRepository

    @Binds
    @Singleton
    abstract fun providerBaseUrlInterceptor(
        baseUrlInterceptor: BaseUrlInterceptor,
    ): Interceptor


    companion object {

        const val BASE_URL = "https://vodovoz.net/"
        const val URL = "https://vodovoz.net/newmobile_new/"

        @Provides
        @Singleton
        @Named("vodovoz")
        fun providesVodovozRetrofit(@Named("vodovoz") okHttpClient: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .baseUrl(VodovozWebConfig.VODOVOZ_URL + VodovozWebConfig.VODOVOZ_PATH)
                .addCallAdapterFactory(NoOpCallAdapterFactory.create())
                .addConverterFactory(NoOpConverterFactory.create())
                .client(okHttpClient)
                .build()
        }

        @Provides
        @Singleton
        fun providesVodovozService(@Named("vodovoz") retrofit: Retrofit): VodovozService {
            return retrofit.create(VodovozService::class.java)
        }

        @Provides
        @Singleton
        @Named("vodovoz")
        fun providesOkHttpClient(
            cookieHandlerInterceptor: CookieHandlerInterceptor,
            baseUrlInterceptor: BaseUrlInterceptor,
        ): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(cookieHandlerInterceptor)
                .addInterceptor(baseUrlInterceptor)
                .addInterceptor(HttpLoggingInterceptor())
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .build()
        }

    }

}

fun String.toFullUrl(): String {
    return VodovozServiceModule.BASE_URL.removePrefix("/") + this
}

class NoOpCallAdapterFactory private constructor() : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *> {

        return object : CallAdapter<Any?, Any?> {
            override fun responseType(): Type {
                return Any::class.java
            }

            override fun adapt(call: Call<Any?>): Any {
                return call
            }
        }

    }

    companion object {
        fun create(): NoOpCallAdapterFactory {
            return NoOpCallAdapterFactory()
        }
    }
}

class NoOpConverterFactory private constructor(): Converter.Factory() {

    override fun responseBodyConverter(
        type: Type, annotations: Array<Annotation>, retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        return Converter<ResponseBody, Any?> { value ->
            value.string()
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit,
    ): Converter<Any?, RequestBody?> {
        return Converter<Any?, RequestBody?> {
            null
        }
    }

    companion object {
        fun create(): NoOpConverterFactory {
            return NoOpConverterFactory()
        }
    }
}
