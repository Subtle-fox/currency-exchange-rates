package com.subtlefox.currencyrates.domain.implementation

import com.subtlefox.currencyrates.domain.ObserveConnectivityUseCase
import com.subtlefox.currencyrates.domain.repository.ConnectivityRepository
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