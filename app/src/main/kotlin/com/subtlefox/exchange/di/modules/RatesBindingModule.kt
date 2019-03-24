package com.subtlefox.exchange.di.modules

import android.arch.lifecycle.ViewModel
import com.subtlefox.exchange.data.InternetConnectivity
import com.subtlefox.exchange.data.LocalRepository
import com.subtlefox.exchange.data.RemoteRepository
import com.subtlefox.exchange.di.FeatureScope
import com.subtlefox.exchange.di.ViewModelKey
import com.subtlefox.exchange.domain.*
import com.subtlefox.exchange.domain.implementation.ConvertCurrenciesUseCaseImpl
import com.subtlefox.exchange.domain.implementation.GetDetailsUseCaseImpl
import com.subtlefox.exchange.domain.implementation.ObserveConnectivityUseCaseImpl
import com.subtlefox.exchange.domain.repository.ConnectivityRepository
import com.subtlefox.exchange.domain.repository.CurrencyInfoRepository
import com.subtlefox.exchange.domain.repository.CurrencyRatesRepository
import com.subtlefox.exchange.presentation.RatesViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.reactivex.functions.BiFunction

// NOTE: Most dependencies are unscoped as far as they all belong to ViewModel and managed by it's lifecycle
@Module
interface RatesBindingModule {
    // 'scoped' with Android framework
    @Binds
    @IntoMap
    @ViewModelKey(RatesViewModel::class)
    fun bindViewModel(viewModel: RatesViewModel): ViewModel

    // Injected twice -> need a scope
    @Binds
    @FeatureScope
    fun bindConnectivityRepository(impl: InternetConnectivity): ConnectivityRepository

    @Binds
    fun bindRatesRepository(impl: RemoteRepository): CurrencyRatesRepository

    @Binds
    fun bindInfoRepository(impl: LocalRepository): CurrencyInfoRepository

    @Binds
    fun bindDetailsUseCase(impl: GetDetailsUseCaseImpl): GetDetailsUseCase

    @Binds
    fun bindConverterUseCase(impl: ConvertCurrenciesUseCaseImpl): ConverterUseCase

    @Binds
    fun bindConnectivityUseCase(impl: ObserveConnectivityUseCaseImpl): ObserveConnectivityUseCase

    @Binds
    fun bindConverter(impl: CurrencyConverter): BiFunction<CurrencyValue, Rates, CurrencyValues>
}

