package com.subtlefox.exchange.domain

import com.subtlefox.exchange.domain.implementation.ConvertCurrenciesUseCaseImpl
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test

class ConvertCurrenciesUseCaseImplTest {
    val ratesMock = mockk<ObserveRatesUseCase>()
    val converterMock = mockk<BiFunction<CurrencyValue, Rates, CurrencyValues>>(relaxed = true)
    val valuesObservable = PublishSubject.create<CurrencyValue>()
    val ratesObservable = PublishSubject.create<List<CurrencyRate>>()
    val usecase = ConvertCurrenciesUseCaseImpl(ratesMock, converterMock)

    @Before
    fun before() {
        every { ratesMock.stream(any()) } returns ratesObservable
    }

    @Test
    fun `should combine ui events and rates updates`() {
        val currencyValue = valueOf("USD", "1.5")
        val rates = listOf(
            rateOf("EUR", "1"),
            rateOf("USD", "1.5")
        )
        val testObserver = TestObserver<CurrencyValues>()
        usecase.stream(valuesObservable).subscribe(testObserver)

        // initial emit:
        testObserver.apply {
            valuesObservable.onNext(currencyValue)
            ratesObservable.onNext(rates)
        }.assertValueCount(1)

        testObserver.apply {
            valuesObservable.onNext(currencyValue)
        }.assertValueCount(2)

        testObserver.apply {
            valuesObservable.onNext(currencyValue)
            ratesObservable.onNext(rates)
        }.assertValueCount(4)
    }

    @Test
    fun `should switch observable when base currency changed`() {
        val ratesEuro = listOf(
            rateOf("EUR", "1"),
            rateOf("USD", "1.5")
        )
        every { ratesMock.stream("EUR") } returns Observable.just(ratesEuro)

        val ratesUsd = listOf(
            rateOf("EUR", "0.1"),
            rateOf("USD", "1")
        )
        every { ratesMock.stream("USD") } returns Observable.just(ratesUsd)

        val testObserver = TestObserver<CurrencyValues>()
        usecase.stream(valuesObservable).subscribe(testObserver)

        val request_one_euro = valueOf("EUR", "1")
        valuesObservable.onNext(request_one_euro)

        // base currency is same -> previous observable used (mock EURO)
        val request_ten_euro = valueOf("EUR", "10")
        valuesObservable.onNext(request_ten_euro)

        // base currency changed -> new observable expected (mock USD)
        val request_one_usd = valueOf("USD", "2")
        valuesObservable.onNext(request_one_usd)
        verify(ordering = Ordering.ORDERED) {
            converterMock.apply(request_one_euro, match { it.values == ratesEuro })
            converterMock.apply(request_ten_euro, match { it.values == ratesEuro })
            converterMock.apply(request_one_usd, match { it.values == ratesEuro })
            converterMock.apply(request_one_usd, match { it.values == ratesUsd })
        }
    }

    @Test
    fun `should switch observable when base currency changed without sync`() {
        val ratesEuro = listOf(
            rateOf("EUR", "1"),
            rateOf("USD", "1.5")
        )
        every { ratesMock.stream("EUR") } returns Observable.just(ratesEuro)
        every { ratesMock.stream("USD") } returns Observable.never()

        val testObserver = TestObserver<CurrencyValues>()
        usecase.stream(valuesObservable).subscribe(testObserver)

        val request_one_euro = valueOf("EUR", "1")
        valuesObservable.onNext(request_one_euro)

        // base currency changed -> new observable expected (mock USD)
        val request_one_usd = valueOf("USD", "2")
        valuesObservable.onNext(request_one_usd)
        verify(ordering = Ordering.SEQUENCE) {
            converterMock.apply(request_one_euro, match { it.values == ratesEuro })
            converterMock.apply(request_one_usd, match { it.values == ratesEuro })
        }
    }


    @Test
    fun `should return cached list just after resubscribing`() {
        every { ratesMock.stream("EUR") } returns Observable.just(
            listOf(
                rateOf("EUR", "1"),
                rateOf("USD", "1.5")
            )
        )
        every { ratesMock.stream("USD") } returns Observable.empty()

        val testObserver = TestObserver<CurrencyValues>()
        usecase.stream(valuesObservable).subscribe(testObserver)

        valuesObservable.onNext(valueOf("EUR", "1"))
        testObserver.assertValueCount(1)
        testObserver.dispose()

        // resubscribe:
        val testObserver2 = TestObserver<CurrencyValues>()
        usecase.stream(valuesObservable).subscribe(testObserver2)

        valuesObservable.onNext(valueOf("USD", "1"))
        testObserver2.assertValueCount(1)
    }
}