package com.subtlefox.exchange.domain.implementation

import com.subtlefox.exchange.domain.ObserveConnectivityUseCase
import com.subtlefox.exchange.domain.repository.ConnectivityRepository
import io.reactivex.Observable
import javax.inject.Inject

class ObserveConnectivityUseCaseImpl
@Inject constructor(
    private val repository: ConnectivityRepository
) : ObserveConnectivityUseCase {

    override fun stream(): Observable<Boolean> {
        return repository
            .observeConnectivity()
            .distinctUntilChanged()
    }

}