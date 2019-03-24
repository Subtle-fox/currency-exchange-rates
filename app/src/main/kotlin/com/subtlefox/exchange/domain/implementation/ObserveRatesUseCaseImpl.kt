package com.subtlefox.exchange.domain.implementation

import com.subtlefox.exchange.domain.CurrencyRate
import com.subtlefox.exchange.domain.ObserveRatesUseCase
import com.subtlefox.exchange.domain.repository.ConnectivityRepository
import com.subtlefox.exchange.domain.repository.CurrencyRatesRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit

class Config(
    val computation: Scheduler,
    val intervalSec: Long,
    val delaySec: Long,
    val timeoutSec: Long
)

class ObserveRatesUseCaseImpl(
    private val repository: CurrencyRatesRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val cfg: Config
) : ObserveRatesUseCase {

    override fun stream(iso: String): Observable<List<CurrencyRate>> {
        return Observable
            .interval(cfg.delaySec, cfg.intervalSec, TimeUnit.SECONDS, cfg.computation)
            .flatMapSingle { connectivityRepository.isConnected() }
            .filter { it }
            .timestamp(cfg.computation)
            .flatMapMaybe { time ->
                repository.fetchRates(iso)
                    .timeout(cfg.timeoutSec, TimeUnit.SECONDS, cfg.computation)
                    .toMaybe()
                    .doOnError { it.message }
                    .onErrorComplete()
                    .map { Pair(time, it.toHashSet()) }
            }
            .scan { prev, current -> if (prev.first.time() >= current.first.time()) prev else current }
            .map { timed -> timed.second.sortedBy { it.iso } }
    }

}