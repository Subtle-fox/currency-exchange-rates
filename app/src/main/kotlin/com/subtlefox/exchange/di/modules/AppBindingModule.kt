package com.subtlefox.exchange.di.modules

import android.arch.lifecycle.ViewModelProvider
import com.subtlefox.exchange.di.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
interface AppBindingModule {
    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
