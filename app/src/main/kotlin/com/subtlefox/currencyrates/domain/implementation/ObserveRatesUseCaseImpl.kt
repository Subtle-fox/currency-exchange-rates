package com.subtlefox.currencyrates.domain.implementation

import com.subtlefox.currencyrates.domain.Config
import com.subtlefox.currencyrates.domain.CurrencyRate
import com.subtlefox.currencyrates.domain.ObserveRatesUseCase
import com.subtlefox.currencyrates.domain.repository.ConnectivityRepository
import com.subtlefox.currencyrates.domain.repository.CurrencyRatesRepository
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Timed
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ObserveRatesUseCaseImpl
@Inject constructor(
    private val repository: CurrencyRatesRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val cfg: Config
) : ObserveRatesUseCase {

    override fun stream(iso: String): Observable<List<CurrencyRate>> {
        val apiSingleMapper: (Timed<Boolean>) -> Single<Pair<Timed<Boolean>, HashSet<CurrencyRate>>> =
            { time ->
                repository.fetchRates(iso)
                    .timeout(cfg.timeoutSec, TimeUnit.SECONDS, cfg.computation)
                    .map { Pair(time, it.toHashSet()) }
                    .doOnError { System.err.println("api error > $it") }
            }


        return Flowable
            .interval(cfg.delaySec, cfg.intervalSec, TimeUnit.SECONDS, cfg.computation)
            .onBackpressureDrop()
            .flatMapSingle { connectivityRepository.isConnected() }
            .filter { it }
            .timestamp(cfg.computation)
            .flatMapSingle(apiSingleMapper, true, cfg.maxConcurrency)
            .scan { prev, current -> if (prev.first.time() >= current.first.time()) prev else current }
            .map { timed -> timed.second.sortedBy { it.iso } }
            .toObservable()
    }

}