package com.subtlefox.currencyrates.domain

import io.reactivex.ObservableTransformer

interface ConvertRatesUseCase : ObservableTransformer<CurrencyValue, CurrencyValues>