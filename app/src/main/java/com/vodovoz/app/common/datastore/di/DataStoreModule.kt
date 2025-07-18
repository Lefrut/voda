package com.vodovoz.app.common.datastore.di

import android.app.Application
import com.vodovoz.app.common.datastore.DataStorePrefs
import com.vodovoz.app.common.datastore.DataStorePrefsImpl
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
    fun providesDataStore(context: Application): DataStorePrefs {
        return DataStorePrefsImpl(context)
    }

}