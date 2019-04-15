package com.subtlefox.currencyrates.domain

import io.reactivex.Observable

interface GetDetailsUseCase {
    fun load(): Observable<Map<String, CurrencyInfo>>
}