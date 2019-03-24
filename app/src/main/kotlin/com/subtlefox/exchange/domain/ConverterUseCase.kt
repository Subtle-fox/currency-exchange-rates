package com.subtlefox.exchange.domain

import io.reactivex.Observable

interface ConverterUseCase {
    fun stream(selectionObservable: Observable<CurrencyValue>): Observable<CurrencyValues>
}