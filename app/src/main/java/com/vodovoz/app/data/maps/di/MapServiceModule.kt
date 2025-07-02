package com.vodovoz.app.data.maps.di

import com.squareup.moshi.Moshi
import com.vodovoz.app.core.network.ApiConfig
import com.vodovoz.app.core.network.retrofit.NoOpCallAdapterFactory
import com.vodovoz.app.core.network.retrofit.NoOpConverterFactory
import com.vodovoz.app.data.maps.YandexMapAPI
import com.vodovoz.app.data.maps.YandexMapSDK
import com.vodovoz.app.data.maps.YandexMapSDKImpl
import com.vodovoz.app.data.maps.repository.MapServiceRepositoryImpl
import com.vodovoz.app.domain.general.respository.MapServiceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MapServiceModule {

    @Binds
    @Singleton
    abstract fun bindMapServiceRepository(mapServiceRepository: MapServiceRepositoryImpl): MapServiceRepository

    @Binds
    @Singleton
    abstract fun bindYandexMapSDK(yandexMapSDK: YandexMapSDKImpl): YandexMapSDK


    companion object {
        @Provides
        @Singleton
        @Named("yandex_map")
        fun providesYandexMapRetrofit(moshi: Moshi): Retrofit {
            return Retrofit.Builder()
                .baseUrl(ApiConfig.MAPKIT_URL)
                .addCallAdapterFactory(NoOpCallAdapterFactory.create())
                .addConverterFactory(NoOpConverterFactory.create(moshi))
                .build()
        }

        @Provides
        @Singleton
        fun providesYandexMapService(@Named("yandex_map") retrofit: Retrofit): YandexMapAPI {
            return retrofit.create(YandexMapAPI::class.java)
        }
    }


}