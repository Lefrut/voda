package com.vodovoz.app.common.di

import android.app.Application
import com.vodovoz.app.common.block_app_signal.BlockAppSignal
import com.vodovoz.app.common.block_app_signal.BlockAppSignalImpl
import com.vodovoz.app.common.cache.HttpErrorCache
import com.vodovoz.app.common.cache.HttpErrorCacheMappers
import com.vodovoz.app.common.cache.VodovozHttpError
import com.vodovoz.app.common.cache.VodovozHttpErrorCache
import com.vodovoz.app.common.cache.VodovozHttpErrorCacheMappers
import com.vodovoz.app.common.datastore.DataStorePrefs
import com.vodovoz.app.common.datastore.DataStorePrefsImpl
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