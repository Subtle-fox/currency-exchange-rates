package com.subtlefox.exchange.di.components

import com.subtlefox.exchange.di.modules.AppBindingModule
import com.subtlefox.exchange.di.modules.AppProviderModule
import com.subtlefox.exchange.di.modules.NetworkModule
import com.subtlefox.exchange.presentation.App
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppProviderModule::class,
        AppBindingModule::class,
        NetworkModule::class
    ]
)
interface AppComponent {
    fun inject(app: App)
    fun plus(): RatesComponent
}