package com.subtlefox.currencyrates.domain

import io.reactivex.Observable

interface ObserveRatesUseCase {
    fun stream(iso: String): Observable<List<CurrencyRate>>
}