package com.subtlefox.exchange.domain

import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class CurrencyConverterTest {
    val converter = CurrencyConverter()

    @Test
    fun `convert rate with new base '1-2' to '1-4'`() {
        val currencyA = rateOf("EUR", "2")
        val currencyB = rateOf("USD", "4")
        assertEquals(BigDecimal("2"), currencyB.rebase(currencyA).rate)
    }

    @Test
    fun `convert rate with new base '1-2' to '1-1'`() {
        val currencyA = rateOf("EUR", "2")
        val currencyB = rateOf("USD", "1")
        assertEquals(BigDecimal("0.5"), currencyB.rebase(currencyA).rate)
    }

    @Test
    fun `convert rate with new base '1-0,25' to '1-0,5'`() {
        val currencyA = rateOf("EUR", "0.25")
        val currencyB = rateOf("USD", "0.5")
        assertEquals(BigDecimal("2"), currencyB.rebase(currencyA).rate)
    }

    @Test
    fun `convert rate with unlimited decimal`() {
        val currencyA = rateOf("EUR", "3")
        val currencyB = rateOf("USD", "1")
        val actual = currencyB.rebase(currencyA).rate.setScale(4, RoundingMode.HALF_EVEN)
        assertEquals(BigDecimal("0.3333"), actual)
    }

    @Test
    fun `should not convert rate if 1-1`() {
        val currencyA = rateOf("EUR", "1")
        val currencyB = rateOf("USD", "3")
        assertEquals(BigDecimal("3"), currencyB.rebase(currencyA).rate)
    }

    ////////////

    @Test
    fun `should return empty list if no base currency found`() {
        val rates = Rates(
            "USD", listOf(
                CurrencyRate("USD", "1".toBigDecimal()),
                CurrencyRate("RUB", "75".toBigDecimal())
            )
        )

        val rebased = converter.rebaseList(valueOf("EUR"), rates)
        assertTrue(rebased.isEmpty())
    }

    @Test
    fun `should return same list if base equals to request`() {
        val rates = Rates(
            "USD", listOf(
                CurrencyRate("USD", "1".toBigDecimal()),
                CurrencyRate("RUB", "75".toBigDecimal())
            )
        )

        val rebased = converter.rebaseList(valueOf("USD"), rates)
        assertSame(rates.values, rebased)
    }

    @Test
    fun `should rebase list according to request iso`() {
        val rates = Rates(
            "USD", listOf(
                CurrencyRate("EUR", "0.5".toBigDecimal()),
                CurrencyRate("USD", "1".toBigDecimal())
            )
        )

        val rebased = converter.rebaseList(valueOf("EUR"), rates)
        assertEquals(2, rebased.size)

        assertEquals("EUR", rebased[0].iso)
        assertEquals(BigDecimal("1"), rebased[0].rate)

        assertEquals("USD", rebased[1].iso)
        assertEquals(BigDecimal("2"), rebased[1].rate)
    }

    ////////////

    @Test
    fun `should convert synced list`() {
        val rates = Rates(
            "EUR", listOf(
                rateOf("EUR", "1"),
                rateOf("RUB", "20"),
                rateOf("USD", "2")
            )
        )

        val result = converter.apply(valueOf("EUR", "10"), rates)
        result.values.apply {
            assertEquals(BigDecimal("10"), single { it.iso == "EUR" }.value)
            assertEquals(BigDecimal("20"), single { it.iso == "USD" }.value)
            assertEquals(BigDecimal("200"), single { it.iso == "RUB" }.value)
        }
    }

    @Test
    fun `should convert unsync rates`() {
        val rates = Rates(
            "USD",
            listOf(
                rateOf("EUR", "0.5"),
                rateOf("USD", "1"),
                rateOf("RUB", "40")
            )
        )

        val result = converter.apply(valueOf("EUR", "10"), rates)
        result.values.apply {
            assertEquals(BigDecimal("10"), single { it.iso == "EUR" }.value)
            assertEquals(BigDecimal("20"), single { it.iso == "USD" }.value)
            assertEquals(BigDecimal("800"), single { it.iso == "RUB" }.value.setScale(0))
        }
    }

    @Test
    fun `should return empty if no base currency found`() {
        val rates = Rates(
            "USD", listOf(
                CurrencyRate("USD", "1".toBigDecimal()),
                CurrencyRate("RUB", "75".toBigDecimal())
            )
        )

        val convertedRates = converter.apply(valueOf("EUR"), rates)
        assertEquals("EUR", convertedRates.base)
        assertTrue(convertedRates.values.isEmpty())
    }

    @Test
    fun `should return all zeros`() {
        val rates = Rates(
            "EUR",
            listOf(
                rateOf("EUR", "1"),
                rateOf("USD", "2"),
                rateOf("RUB", "80")
            )
        )

        val syncRates = converter.apply(valueOf("EUR", "0"), rates)
        assertTrue(syncRates.values.all { BigDecimal.ZERO.compareTo(it.value) == 0 })

        val unsyncRates = converter.apply(valueOf("USD", "0"), rates)
        assertTrue(unsyncRates.values.all { BigDecimal.ZERO.compareTo(it.value) == 0 })
    }

    ////////////

    @Test
    fun `should place base currency from middle to top`() {
        val rates = Rates(
            "EUR", listOf(
                rateOf("AUD"),
                rateOf("CAD"),
                rateOf("EUR"),
                rateOf("RUB")
            )
        )

        val list = converter.apply(valueOf("EUR"), rates).values
        assertEquals("EUR", list[0].iso)
        assertEquals("AUD", list[1].iso)
        assertEquals("CAD", list[2].iso)
        assertEquals("RUB", list[3].iso)
    }

    @Test
    fun `should not change order if base currency on top`() {
        val rates = Rates(
            "EUR", listOf(
                rateOf("AUD"),
                rateOf("CAD"),
                rateOf("EUR"),
                rateOf("RUB")
            )
        )

        val list = converter.apply(valueOf("AUD"), rates).values
        assertEquals("AUD", list[0].iso)
        assertEquals("CAD", list[1].iso)
        assertEquals("EUR", list[2].iso)
        assertEquals("RUB", list[3].iso)
    }

    @Test
    fun `should move base from bottom to top`() {
        val rates = Rates(
            "EUR", listOf(
                rateOf("AUD"),
                rateOf("CAD"),
                rateOf("EUR"),
                rateOf("RUB")
            )
        )

        val list = converter.apply(valueOf("RUB"), rates).values
        assertEquals("RUB", list[0].iso)
        assertEquals("AUD", list[1].iso)
        assertEquals("CAD", list[2].iso)
        assertEquals("EUR", list[3].iso)
    }
}