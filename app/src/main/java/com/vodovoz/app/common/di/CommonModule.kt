package com.vodovoz.app.common.di

import com.vodovoz.app.common.block_app_signal.BlockAppSignal
import com.vodovoz.app.common.block_app_signal.BlockAppSignalImpl
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
        impl: BlockAppSignalImpl
    ): BlockAppSignal

    companion object {
        @Provides
        @IoDispatcher
        @Singleton
        fun provideIoDispatcher() : CoroutineDispatcher = Dispatchers.IO

        @Provides
        @DefaultDispatcher
        @Singleton
        fun provideDefaultDispatcher() : CoroutineDispatcher = Dispatchers.Default

        @Provides
        @MainDispatcher
        @Singleton
        fun provideMainDispatcher() : CoroutineDispatcher = Dispatchers.Main
    }

}