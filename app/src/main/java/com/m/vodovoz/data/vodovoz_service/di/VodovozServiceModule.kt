package com.m.vodovoz.data.vodovoz_service.di

import com.m.vodovoz.core.network.VodovozWebConfig
import com.m.vodovoz.core.network.di.VodovozQualifier
import com.m.vodovoz.core.network.retrofit.NoOpCallAdapterFactory
import com.m.vodovoz.core.network.retrofit.NoOpConverterFactory
import com.m.vodovoz.data.vodovoz_service.VodovozService
import com.m.vodovoz.data.vodovoz_service.datastore.ForAdultsDataStore
import com.m.vodovoz.data.vodovoz_service.datastore.ForAdultsDataStoreImpl
import com.m.vodovoz.data.vodovoz_service.datastore.StoriesDataStore
import com.m.vodovoz.data.vodovoz_service.datastore.StoriesDataStoreImpl
import com.m.vodovoz.data.vodovoz_service.repository.UserPreferencesRepositoryImpl
import com.m.vodovoz.data.vodovoz_service.repository.VodovozServiceRepositoryImpl
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class VodovozServiceModule {

    @Binds
    @Singleton
    abstract fun bindVodovozServiceRepository(
        vodovozServiceRepository: VodovozServiceRepositoryImpl,
    ): VodovozServiceRepository

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
        @VodovozQualifier
        fun providesVodovozRetrofit(
            @VodovozQualifier
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
        fun providesVodovozService(@VodovozQualifier retrofit: Retrofit): VodovozService {
            return retrofit.create(VodovozService::class.java)
        }
    }

}

fun String.toVodovozUrl(): String {
    if (startsWith("http://") || startsWith("https://")) return this

    return VodovozWebConfig.VODOVOZ_URL.trimEnd('/') + "/" + trimStart('/')
}
