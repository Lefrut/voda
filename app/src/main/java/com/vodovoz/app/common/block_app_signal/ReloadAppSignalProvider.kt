package com.vodovoz.app.common.block_app_signal

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

interface ReloadAppSignalProvider {

    val reloadAppSignal: ReloadAppSignal

}


@Module
@InstallIn(SingletonComponent::class)
abstract class ReloadAppSignalModule{

    @Binds
    abstract fun bindReloadAppSignal(
        impl: ReloadAppSignalImpl
    ): ReloadAppSignalProvider

}