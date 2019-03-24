package com.subtlefox.exchange.domain.implementation

import com.subtlefox.exchange.domain.*
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class ConvertCurrenciesUseCaseImpl
@Inject constructor(
    private val ratesUseCase: ObserveRatesUseCase,
    private val currencyConverter: BiFunction<CurrencyValue, Rates, CurrencyValues>
) : ConverterUseCase {

    // survives after resubscribing
    private val lastValues = AtomicReference<Rates?>()

    override fun stream(selectionObservable: Observable<CurrencyValue>): Observable<CurrencyValues> {
        var ratesObservable = selectionObservable
            .switchMap { cv ->
                ratesUseCase
                    .stream(cv.iso)
                    .map { Rates(cv.iso, it) }
            }
            .doOnNext { lastValues.set(it) }

        if (lastValues.get() != null) {
            ratesObservable = ratesObservable.startWith(lastValues.get())
        }

        return Observable
            .combineLatest(
                selectionObservable,
                ratesObservable,
                currencyConverter
            )
    }

}