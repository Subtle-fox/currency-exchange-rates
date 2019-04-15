package com.subtlefox.currencyrates.domain

import com.subtlefox.currencyrates.domain.implementation.ConvertRatesUseCaseImpl
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class ConvertRatesUseCaseImplTest {
    val ratesMock = mockk<ObserveRatesUseCase>()
    val mapperMock = mockk<BiFunction<CurrencyValue, Rates, CurrencyValues>>(relaxed = true)
    val valuesObservable = PublishSubject.create<CurrencyValue>()
    val ratesObservable = PublishSubject.create<List<CurrencyRate>>()
    val testScheduler = TestScheduler()

    val usecase = ConvertRatesUseCaseImpl(
        ratesMock, mapperMock, Config(computation = testScheduler, intervalSec = 1)
    )

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
        usecase.apply(valuesObservable).subscribe(testObserver)

        // initial emit:
        testObserver.apply {
            valuesObservable.onNext(currencyValue)
            ratesObservable.onNext(rates)
            testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        }.assertValueCount(1)

        testObserver.apply {
            valuesObservable.onNext(currencyValue)
        }.assertValueCount(2)

        testObserver.apply {
            valuesObservable.onNext(currencyValue)
            ratesObservable.onNext(rates)
            testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
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
        usecase.apply(valuesObservable).subscribe(testObserver)

        val request_one_euro = valueOf("EUR", "1")
        valuesObservable.onNext(request_one_euro)
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // base currency is same -> previous observable used (mock EURO)
        val request_ten_euro = valueOf("EUR", "10")
        valuesObservable.onNext(request_ten_euro)
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // base currency changed -> new observable expected (mock USD)
        val request_one_usd = valueOf("USD", "2")
        valuesObservable.onNext(request_one_usd)
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        verify(ordering = Ordering.ORDERED) {
            mapperMock.apply(request_one_euro, match { it.values == ratesEuro })
            mapperMock.apply(request_ten_euro, match { it.values == ratesEuro })
            mapperMock.apply(request_one_usd, match { it.values == ratesEuro })
            mapperMock.apply(request_one_usd, match { it.values == ratesUsd })
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
        usecase.apply(valuesObservable).subscribe(testObserver)

        val request_one_euro = valueOf("EUR", "1")
        valuesObservable.onNext(request_one_euro)
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        // base currency changed -> new observable expected (mock USD)
        val request_one_usd = valueOf("USD", "2")
        valuesObservable.onNext(request_one_usd)
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        verify(ordering = Ordering.SEQUENCE) {
            mapperMock.apply(request_one_euro, match { it.values == ratesEuro })
            mapperMock.apply(request_one_usd, match { it.values == ratesEuro })
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
        usecase.apply(valuesObservable).subscribe(testObserver)

        valuesObservable.onNext(valueOf("EUR", "1"))
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testObserver.assertValueCount(1)
        testObserver.dispose()

        // resubscribe:
        val testObserver2 = TestObserver<CurrencyValues>()
        usecase.apply(valuesObservable).subscribe(testObserver2)

        valuesObservable.onNext(valueOf("USD", "1"))
        testObserver2.assertValueCount(1)
    }

    @Test
    fun `should throttle fast values updates`() {
        every { ratesMock.stream("EUR") } returns Observable.just(
            listOf(
                rateOf("EUR", "1"),
                rateOf("USD", "1.5")
            )
        ).repeat(3)

        val testObserver = TestObserver<CurrencyValues>()
        usecase.apply(valuesObservable).subscribe(testObserver)

        valuesObservable.onNext(valueOf("EUR", "1"))
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testObserver.assertValueCount(1)
    }

    @Test
    fun `should not switch if base currency not changed`() {
        val ratesEuro = listOf(
            rateOf("EUR", "1"),
            rateOf("USD", "1.5")
        )
        every { ratesMock.stream("EUR") } returns Observable.just(ratesEuro)

        val testObserver = TestObserver<CurrencyValues>()
        usecase.apply(valuesObservable).subscribe(testObserver)

        valuesObservable.onNext(valueOf("EUR", "1"))
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        valuesObservable.onNext(valueOf("EUR", "10"))
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        verify(exactly = 1) {
            ratesMock.stream("EUR")
        }
    }
}