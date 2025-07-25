package com.vodovoz.app.ui.insets.di

import com.vodovoz.app.ui.insets.DefaultInsetsVisibilityState
import com.vodovoz.app.ui.insets.InsetsVisibilityState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InsetsVisibilityModule {

    @Singleton
    @Binds
    abstract fun bindDefaultInsetsVisibilityState(
        impl: DefaultInsetsVisibilityState
    ): InsetsVisibilityState

}