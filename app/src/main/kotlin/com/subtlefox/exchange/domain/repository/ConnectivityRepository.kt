package com.subtlefox.exchange.domain.repository

import io.reactivex.Observable
import io.reactivex.Single

interface ConnectivityRepository {
    fun observeConnectivity(): Observable<Boolean>
    fun isConnected(): Single<Boolean>
}