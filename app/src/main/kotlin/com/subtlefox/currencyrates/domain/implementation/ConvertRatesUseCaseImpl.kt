package com.subtlefox.currencyrates.domain.implementation

import com.subtlefox.currencyrates.domain.*
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class ConvertRatesUseCaseImpl
@Inject constructor(
    private val ratesUseCase: ObserveRatesUseCase,
    private val ratesToValuesMapper: BiFunction<CurrencyValue, Rates, CurrencyValues>,
    private val cfg: Config
) : ConvertRatesUseCase {

    // NOTE: survives after resubscribing
    private val lastValue = AtomicReference<Rates?>()

    override fun apply(upstream: Observable<CurrencyValue>): ObservableSource<CurrencyValues> {
        val ratesObservable = upstream
            .distinct { it.iso }
            .switchMap { cv ->
                ratesUseCase
                    .stream(cv.iso)
                    .map { Rates(cv.iso, it) }
            }
            .sample(cfg.intervalSec, TimeUnit.SECONDS, cfg.computation)
            .doOnNext(lastValue::set)
            .compose(withLastValue)

        return Observable.combineLatest(upstream, ratesObservable, ratesToValuesMapper)
    }

    private val withLastValue = ObservableTransformer<Rates, Rates> { upstream ->
        lastValue.get()
            ?.let { upstream.startWith(it) }
            ?: upstream
    }
}
