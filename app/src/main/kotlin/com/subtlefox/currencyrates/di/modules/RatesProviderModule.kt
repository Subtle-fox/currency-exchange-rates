package com.subtlefox.currencyrates.di.modules

import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.subtlefox.currencyrates.BuildConfig
import com.subtlefox.currencyrates.data.RemoteApi
import com.subtlefox.currencyrates.domain.Config
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// NOTE: Dependencies are all unscoped as far as they all belong to ViewModel and managed by it's lifecycle
@Module
class RatesProviderModule {
    @Provides
    fun provideConfig(computationScheduler: Scheduler): Config {
        return Config(
            computation = computationScheduler,
            intervalSec = 2,
            delaySec = 0,
            timeoutSec = 10,
            maxConcurrency = 10
        )
    }

    @Provides
    fun provideRemoteApi(gson: Gson, okHttpClient: OkHttpClient): RemoteApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
            .create(RemoteApi::class.java)
    }
}