package com.subtlefox.exchange.di.modules

import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.subtlefox.exchange.BuildConfig
import com.subtlefox.exchange.data.RemoteApi
import com.subtlefox.exchange.domain.ObserveRatesUseCase
import com.subtlefox.exchange.domain.implementation.Config
import com.subtlefox.exchange.domain.implementation.ObserveRatesUseCaseImpl
import com.subtlefox.exchange.domain.repository.ConnectivityRepository
import com.subtlefox.exchange.domain.repository.CurrencyRatesRepository
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// NOTE: Dependencies are all unscoped as far as they all belong to ViewModel and managed by it's lifecycle
@Module
class RatesProviderModule {
    @Provides
    fun provideFetcherUseCase(
        apiRepository: CurrencyRatesRepository,
        connectivityRepository: ConnectivityRepository
    ): ObserveRatesUseCase {
        return ObserveRatesUseCaseImpl(
            apiRepository,
            connectivityRepository,
            Config(
                computation = Schedulers.computation(),
                delaySec = 0,
                intervalSec = BuildConfig.REFRESH_RATE_SEC,
                timeoutSec = BuildConfig.TIMEOUT_SEC
            )
        )
    }

    @Provides
    fun provideRemoteApi(gson: Gson, okHttpClient: OkHttpClient): RemoteApi {
        return Retrofit.Builder()
            .baseUrl("https://revolut.duckdns.org/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
            .create(RemoteApi::class.java)
    }
}