package com.subtlefox.exchange.presentation

import android.app.Application
import com.subtlefox.exchange.di.components.AppComponent
import com.subtlefox.exchange.di.components.DaggerAppComponent
import com.subtlefox.exchange.di.modules.AppProviderModule

class App : Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .appProviderModule(AppProviderModule(this))
            .build()
        appComponent.inject(this)
    }
}