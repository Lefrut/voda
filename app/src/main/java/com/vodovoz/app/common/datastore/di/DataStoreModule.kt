package com.vodovoz.app.common.datastore.di

import android.app.Application
import com.vodovoz.app.common.datastore.DataStoreRepository
import com.vodovoz.app.common.datastore.DataStoreRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {

    @Provides
    @Singleton
    fun providesDataStore(context: Application): DataStoreRepository {
        return DataStoreRepositoryImpl(context)
    }

}