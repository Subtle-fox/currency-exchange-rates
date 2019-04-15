package com.subtlefox.currencyrates.domain

import com.subtlefox.currencyrates.domain.implementation.ObserveRatesUseCaseImpl
import com.subtlefox.currencyrates.domain.repository.ConnectivityRepository
import com.subtlefox.currencyrates.domain.repository.CurrencyRatesRepository
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


class ObserveRatesUseCaseImplTest {
    val currencyIso = "EUR"
    val testScheduler: TestScheduler = TestScheduler()
    val testObserver = TestObserver<List<CurrencyRate>>()
    val apiRepo = mockk<CurrencyRatesRepository>()
    val connectivityRepo = mockk<ConnectivityRepository>()
    val cfg = Config(computation = testScheduler, intervalSec = 1L, delaySec = 1L, timeoutSec = 10L)
    val usecase = ObserveRatesUseCaseImpl(apiRepo, connectivityRepo, cfg)

    private fun stubResponse(): List<CurrencyRate> {
        return listOf(
            CurrencyRate("EUR", BigDecimal.ONE),
            CurrencyRate("USD", BigDecimal("1.5"))
        )
    }

    private fun CurrencyRatesRepository.mockReturn(iso: String, list: List<CurrencyRate>) {
        every { fetchRates(iso) } returns Single.just(list)
    }

    private fun CurrencyRatesRepository.mockError(iso: String, throwable: Throwable) {
        every { fetchRates(iso) } returns Single.error(throwable)
    }

    private fun ConnectivityRepository.mockInternet(isConnected: Boolean) {
        every { connectivityRepo.isConnected() } returns Single.just(isConnected)
    }

    @Before
    fun before() {
        connectivityRepo.mockInternet(true)
    }

    @Test
    fun `should emit values`() {
        apiRepo.mockReturn(currencyIso, stubResponse())

        usecase.stream(currencyIso).subscribe(testObserver)

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        testObserver.assertValueCount(3)
    }

    @Test
    fun `should not complete on error`() {
        apiRepo.mockReturn(currencyIso, stubResponse())

        usecase.stream(currencyIso).subscribe(testObserver)

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        apiRepo.mockError(currencyIso, IOException("server down"))
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        testObserver.assertNoErrors()
        testObserver.assertNotComplete()
        testObserver.assertValueCount(1)
    }

    @Test
    fun `should not emit if no internet connection`() {
        apiRepo.mockReturn(currencyIso, stubResponse())
        usecase.stream(currencyIso).subscribe(testObserver)

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        connectivityRepo.mockInternet(false)

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        testObserver.assertValueCount(1)

        connectivityRepo.mockInternet(true)
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        testObserver.assertValueCount(2)
    }

    @Test
    fun `should discard 1st value if it appears after the 2nd value`() {
        fun `deliver 1st value _after_ the second one`() {
            val value1 = stubResponse()
            every { apiRepo.fetchRates(currencyIso) } returns Single.just(value1).delay(
                1500,
                TimeUnit.MILLISECONDS,
                testScheduler
            )
        }

        fun `deliver 2nd value _before_ the first one`() {
            val value2 = emptyList<CurrencyRate>()
            every { apiRepo.fetchRates(currencyIso) } returns Single.just(value2).delay(
                50,
                TimeUnit.MILLISECONDS,
                testScheduler
            )
        }

        `deliver 1st value _after_ the second one`()
        usecase.stream(currencyIso).subscribe(testObserver)

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        `deliver 2nd value _before_ the first one`()
        testScheduler.advanceTimeBy(1050, TimeUnit.MILLISECONDS)

        testObserver.assertValue { it.isEmpty() } // ok, only value2 ( which is empty ) arrived

        testScheduler.advanceTimeBy(450, TimeUnit.MILLISECONDS)

        // value1 arrived, but outdated => it is replaced by previous one
        testObserver.assertValueCount(2)
        testObserver.assertValueAt(1) { it == testObserver.values().first() }
    }

    @Test
    fun `should not discard 1st value if it appears right before the 2nd one`() {
        val value1 = stubResponse()
        val value2 = emptyList<CurrencyRate>()
        every { apiRepo.fetchRates(currencyIso) } returns Single.just(value1).delay(
            1050,
            TimeUnit.MILLISECONDS,
            testScheduler
        )

        usecase.stream(currencyIso).subscribe(testObserver)

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        every { apiRepo.fetchRates(currencyIso) } returns Single.just(value2).delay(
            100,
            TimeUnit.MILLISECONDS,
            testScheduler
        )

        testScheduler.advanceTimeBy(1100, TimeUnit.MILLISECONDS)

        testObserver.assertValueCount(2)
        testObserver.assertValueAt(0) { it.isNotEmpty() }
        testObserver.assertValueAt(1) { it.isEmpty() }
    }

    @Test
    fun `should terminate too long requests`() {
        every { apiRepo.fetchRates(currencyIso) } returns Single.just(stubResponse()).delay(
            cfg.timeoutSec + 1,
            TimeUnit.SECONDS,
            testScheduler
        )

        usecase.stream(currencyIso).subscribe(testObserver)
        testScheduler.advanceTimeTo(20, TimeUnit.SECONDS)

        testObserver.assertNoErrors()
        testObserver.assertNotComplete()
        testObserver.assertNoValues()
    }

    @Test
    fun `should sort by iso`() {
        val rates = listOf(
            CurrencyRate("RUB", BigDecimal.ONE),
            CurrencyRate("EUR", BigDecimal.ONE),
            CurrencyRate("USD", BigDecimal.ONE)
        )
        apiRepo.mockReturn(currencyIso, rates)

        usecase.stream(currencyIso).subscribe(testObserver)

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        testObserver.values()[0].let {
            assertEquals("EUR", it[0].iso)
            assertEquals("RUB", it[1].iso)
            assertEquals("USD", it[2].iso)
        }
    }

    @Test
    fun `should not contains duplicates`() {
        val rates = listOf(
            CurrencyRate("RUB", BigDecimal.ONE),
            CurrencyRate("RUB", BigDecimal.ONE),
            CurrencyRate("RUB", BigDecimal.ONE)
        )
        apiRepo.mockReturn(currencyIso, rates)

        usecase.stream(currencyIso).subscribe(testObserver)

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        testObserver.assertValue { it.size == 1 }
    }
}
