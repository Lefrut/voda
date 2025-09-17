package com.m.vodovoz.data.water_app.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.m.vodovoz.common.moshi.adapter.DurationJsonAdapter
import com.m.vodovoz.common.moshi.adapter.LocalDateJsonAdapter
import com.m.vodovoz.common.moshi.adapter.LocalTimeJsonAdapter
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.data.water_app.datastore.WaterAppStorage
import com.m.vodovoz.data.water_app.datastore.WaterAppDataStoreImpl
import com.m.vodovoz.data.water_app.moshi.WaterAppStageJsonAdapter
import com.m.vodovoz.data.water_app.repository.WaterAppRepositoryImpl
import com.m.vodovoz.domain.general.respository.WaterAppRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
                .add(WaterApp.Stage::class.java, WaterAppStageJsonAdapter())
                .add(Duration::class.java, DurationJsonAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()
        }
    }


}


@Qualifier
annotation class WaterAppMoshiQualifier