package com.subtlefox.exchange.domain

import io.reactivex.Observable

interface ObserveConnectivityUseCase {
    fun stream(): Observable<Boolean>
}