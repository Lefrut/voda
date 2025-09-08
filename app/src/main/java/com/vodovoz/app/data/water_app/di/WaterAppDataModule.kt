package com.vodovoz.app.data.water_app.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vodovoz.app.common.moshi.adapter.DurationAdapter
import com.vodovoz.app.common.moshi.adapter.LocalDateJsonAdapter
import com.vodovoz.app.common.moshi.adapter.LocalTimeJsonAdapter
import com.vodovoz.app.common.water_app.WaterApp
import com.vodovoz.app.data.water_app.datastore.WaterAppStorage
import com.vodovoz.app.data.water_app.datastore.WaterAppDataStoreImpl
import com.vodovoz.app.data.water_app.moshi.StageAdapter
import com.vodovoz.app.data.water_app.repository.WaterAppRepositoryImpl
import com.vodovoz.app.domain.general.respository.WaterAppRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.time.Duration

@Module
@InstallIn(SingletonComponent::class)
abstract class WaterAppDataModule {

    @Binds
    @Singleton
    abstract fun bindWaterAppRepository(
        impl: WaterAppRepositoryImpl,
    ): WaterAppRepository

    @Binds
    @Singleton
    abstract fun bindWaterAppStorageImpl(
        impl: WaterAppDataStoreImpl,
    ): WaterAppStorage

    companion object {

        @WaterAppMoshiQualifier
        @Singleton
        @Provides
        fun provideMoshi(): Moshi {
            return Moshi.Builder()
                .add(LocalTimeJsonAdapter())
                .add(LocalDateJsonAdapter())
                .add(WaterApp.Stage::class.java, StageAdapter())
                .add(Duration::class.java, DurationAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()
        }
    }


}


@Qualifier
annotation class WaterAppMoshiQualifier