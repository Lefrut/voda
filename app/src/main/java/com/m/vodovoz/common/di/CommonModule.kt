package com.m.vodovoz.common.di

import android.app.Application
import com.m.vodovoz.common.block_app_signal.BlockAppSignal
import com.m.vodovoz.common.block_app_signal.BlockAppSignalImpl
import com.m.vodovoz.common.cache.HttpErrorCache
import com.m.vodovoz.common.cache.HttpErrorCacheMappers
import com.m.vodovoz.common.cache.VodovozHttpError
import com.m.vodovoz.common.cache.VodovozHttpErrorCache
import com.m.vodovoz.common.cache.VodovozHttpErrorCacheMappers
import com.m.vodovoz.common.datastore.DataStorePrefs
import com.m.vodovoz.common.datastore.DataStorePrefsImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention
@Qualifier
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule {

    @Singleton
    @Binds
    abstract fun bindAppSignal(
        impl: BlockAppSignalImpl,
    ): BlockAppSignal

    @Singleton
    @Binds
    abstract fun bindVodovozCache(
        impl: VodovozHttpErrorCache,
    ): HttpErrorCache

    @Singleton
    @Binds
    abstract fun bindHttpErrorCacheMappers(
        impl: VodovozHttpErrorCacheMappers,
    ): HttpErrorCacheMappers<VodovozHttpError>

    @Singleton
    @Binds
    abstract fun bindDataStorePrefs(
        impl: DataStorePrefsImpl
    ): DataStorePrefs



    companion object {
        @Provides
        @IoDispatcher
        @Singleton
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

        @Provides
        @DefaultDispatcher
        @Singleton
        fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

        @Provides
        @MainDispatcher
        @Singleton
        fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    }

}