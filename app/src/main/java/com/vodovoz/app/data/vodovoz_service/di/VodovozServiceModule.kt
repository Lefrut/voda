package com.vodovoz.app.data.vodovoz_service.di

import com.squareup.moshi.Moshi
import com.vodovoz.app.core.network.VodovozWebConfig
import com.vodovoz.app.core.network.interceptor.BaseUrlInterceptor
import com.vodovoz.app.core.network.interceptor.CookieHandlerInterceptor
import com.vodovoz.app.core.network.retrofit.NoOpCallAdapterFactory
import com.vodovoz.app.core.network.retrofit.NoOpConverterFactory
import com.vodovoz.app.data.vodovoz_service.VodovozService
import com.vodovoz.app.data.vodovoz_service.datastore.ForAdultsDataStore
import com.vodovoz.app.data.vodovoz_service.datastore.ForAdultsDataStoreImpl
import com.vodovoz.app.data.vodovoz_service.datastore.StoriesDataStore
import com.vodovoz.app.data.vodovoz_service.datastore.StoriesDataStoreImpl
import com.vodovoz.app.data.vodovoz_service.repository.UserPreferencesRepositoryImpl
import com.vodovoz.app.data.vodovoz_service.repository.VodovozServiceRepositoryImpl
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class VodovozServiceModule {

    @Binds
    @Singleton
    abstract fun bindVodovozServiceRepository(vodovozServiceRepository: VodovozServiceRepositoryImpl): VodovozServiceRepository

    @Binds
    @Singleton
    abstract fun bindForAdultsDataStore(
        forAdultsDataStore: ForAdultsDataStoreImpl,
    ): ForAdultsDataStore

    @Binds
    @Singleton
    abstract fun bindStoriesDataStore(
        storiesDataStore: StoriesDataStoreImpl,
    ): StoriesDataStore

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository


    companion object {
        @Provides
        @Singleton
        @Named("vodovoz")
        fun providesVodovozRetrofit(
            @Named("vodovoz")
            okHttpClient: OkHttpClient,
            moshi: Moshi,
        ): Retrofit {

            return Retrofit.Builder()
                .baseUrl(VodovozWebConfig.VODOVOZ_URL + VodovozWebConfig.VODOVOZ_PATH)
                .addCallAdapterFactory(NoOpCallAdapterFactory.create())
                .addConverterFactory(NoOpConverterFactory.create(moshi))
                .client(okHttpClient)
                .build()
        }

        @Provides
        @Singleton
        fun providesVodovozService(@Named("vodovoz") retrofit: Retrofit): VodovozService {
            return retrofit.create(VodovozService::class.java)
        }
    }

}

fun String.toVodovozUrl(): String {
    return VodovozWebConfig.VODOVOZ_URL.removeSuffix("\"") + this
}
