package com.subtlefox.currencyrates

import android.app.Application
import com.google.gson.GsonBuilder
import com.subtlefox.currencyrates.di.components.AppComponent
import com.subtlefox.currencyrates.di.components.DaggerAppComponent
import io.reactivex.schedulers.Schedulers

class App : Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .appContext(this)
            .computationScheduler(Schedulers.computation())
            .gson(GsonBuilder().create())
            .build()
        appComponent.inject(this)
    }
}