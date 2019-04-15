package com.subtlefox.currencyrates.di.modules

import android.arch.lifecycle.ViewModel
import com.subtlefox.currencyrates.data.InternetConnectivity
import com.subtlefox.currencyrates.data.LocalRepository
import com.subtlefox.currencyrates.data.RemoteRepository
import com.subtlefox.currencyrates.di.FeatureScope
import com.subtlefox.currencyrates.di.ViewModelKey
import com.subtlefox.currencyrates.domain.*
import com.subtlefox.currencyrates.domain.implementation.*
import com.subtlefox.currencyrates.domain.repository.ConnectivityRepository
import com.subtlefox.currencyrates.domain.repository.CurrencyInfoRepository
import com.subtlefox.currencyrates.domain.repository.CurrencyRatesRepository
import com.subtlefox.currencyrates.presentation.RatesViewModel
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

    // Injected twice in rate's feature -> set the scope
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
    fun bindConnectivityUseCase(impl: ObserveConnectivityUseCaseImpl): ObserveConnectivityUseCase

    @Binds
    fun bindRatesUseCase(impl: ObserveRatesUseCaseImpl): ObserveRatesUseCase

    @Binds
    fun bindConverterUseCase(impl: ConvertRatesUseCaseImpl): ConvertRatesUseCase

    @Binds
    fun bindRatesToValuesConverter(impl: RatesToValuesMapper): BiFunction<CurrencyValue, Rates, CurrencyValues>
}

