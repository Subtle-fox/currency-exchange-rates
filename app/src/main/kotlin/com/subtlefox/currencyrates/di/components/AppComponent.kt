package com.subtlefox.currencyrates.di.components

import android.content.Context
import com.google.gson.Gson
import com.subtlefox.currencyrates.App
import com.subtlefox.currencyrates.di.modules.AppModule
import com.subtlefox.currencyrates.di.modules.NetworkModule
import dagger.BindsInstance
import dagger.Component
import io.reactivex.Scheduler
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        NetworkModule::class
    ]
)
interface AppComponent {
    fun inject(app: App)
    fun plus(): RatesComponent

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun appContext(ctx: Context): Builder

        @BindsInstance
        fun computationScheduler(scheduler: Scheduler): Builder

        @BindsInstance
        fun gson(gson: Gson): Builder

        fun build(): AppComponent
    }
}