package com.subtlefox.currencyrates.domain

import io.reactivex.Observable

interface ObserveConnectivityUseCase {
    fun stream(): Observable<Boolean>
}