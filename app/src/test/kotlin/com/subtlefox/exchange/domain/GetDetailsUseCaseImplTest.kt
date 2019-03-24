package com.subtlefox.exchange.domain

import com.subtlefox.exchange.data.LocalRepository
import com.subtlefox.exchange.domain.implementation.GetDetailsUseCaseImpl
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class GetDetailsUseCaseImplTest {
    val repo = mockk<LocalRepository>()
    val usecase = GetDetailsUseCaseImpl(repo)

    @Test
    fun `should combine from repository`() {
        val testObserver = TestObserver<Map<String, CurrencyInfo>>()
        every { repo.readCurrencyNames() } returns Single.just(mapOf("EUR" to "Euros", "USD" to "Dollars"))
        every { repo.readCountryCodesInfo() } returns Single.just(mapOf("EUR" to "eu", "USD" to "usa"))

        usecase.load().subscribe(testObserver)

        testObserver.assertComplete()

        testObserver.values().first().let {
            assertEquals(2, it.size)
            assertEquals(CurrencyInfo("EUR", "Euros", "eu"), it["EUR"])
            assertEquals(CurrencyInfo("USD", "Dollars", "usa"), it["USD"])
        }
    }

    @Test
    fun `should insert empty country code if not presented`() {
        val testObserver = TestObserver<Map<String, CurrencyInfo>>()
        every { repo.readCurrencyNames() } returns Single.just(mapOf("EUR" to "Euros", "USD" to "Dollars"))
        every { repo.readCountryCodesInfo() } returns Single.just(mapOf("EUR" to "eu"))

        usecase.load().subscribe(testObserver)

        testObserver.values().first().let {
            assertEquals(2, it.size)
            assertEquals(CurrencyInfo("EUR", "Euros", "eu"), it["EUR"])
            assertEquals(CurrencyInfo("USD", "Dollars", ""), it["USD"])
        }
    }

    @Test
    fun `should return empty map if error occurs`() {
        val testObserver = TestObserver<Map<String, CurrencyInfo>>()
        every { repo.readCurrencyNames() } returns Single.just(mapOf("EUR" to "Euros"))
        every { repo.readCountryCodesInfo() } returns Single.error(IOException())

        usecase.load().subscribe(testObserver)

        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValue { it.isEmpty() }
    }
}