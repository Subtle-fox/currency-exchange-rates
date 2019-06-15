package com.subtlefox.currencyrates.di.modules

import androidx.lifecycle.ViewModelProvider
import com.subtlefox.currencyrates.di.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
interface AppModule {
    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
