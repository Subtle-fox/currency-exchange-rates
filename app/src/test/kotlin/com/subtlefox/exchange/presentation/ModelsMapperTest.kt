package com.subtlefox.exchange.presentation

import com.subtlefox.exchange.domain.CurrencyInfo
import com.subtlefox.exchange.domain.CurrencyValue
import com.subtlefox.exchange.domain.CurrencyValues
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class ModelsMapperTest {
    val mapper = ModelsMapper()

    @Test
    fun `should trim zeros when 0`() {
        assertEquals("0", mapper.toUiString(BigDecimal.ZERO))
    }

    @Test
    fun `should trim zeros`() {
        assertEquals("0.1", mapper.toUiString(BigDecimal("0.100")))
        assertEquals("1", mapper.toUiString(BigDecimal("1")))
        assertEquals("2.02", mapper.toUiString(BigDecimal("02.0200")))
    }

    @Test
    fun `should have no more than 4 digits after dot`() {
        assertEquals("0.1122", mapper.toUiString(BigDecimal("0.11223344")))
        assertEquals("12.1122", mapper.toUiString(BigDecimal("12.11223344")))
    }

    @Test
    fun `should round HALF EVEN`() {
        assertEquals("0.1234", mapper.toUiString(BigDecimal("0.12345")))
        assertEquals("0.1236", mapper.toUiString(BigDecimal("0.12355")))
    }

    ////

    @Test
    fun `should convert to ui model`() {
        val rates = CurrencyValues(
            "EUR", listOf(
                CurrencyValue("EUR", BigDecimal("1")),
                CurrencyValue("USD", BigDecimal("1.5"))
            )
        )
        val infos = mapOf(
            "EUR" to CurrencyInfo("EUR", "Euros", "eu"),
            "USD" to CurrencyInfo("USD", "Dollars", "usa")
        )

        val list = mapper.apply(rates, infos)

        assertEquals(2, list.size)

        val usd = list.single { it.iso == "USD" }
        assertEquals("1.5", usd.value)
        assertEquals("Dollars", usd.name)
        assertTrue(usd.iconUrl.isNotEmpty())
        assertFalse(usd.isBase)

        val euro = list.single { it.iso == "EUR" }
        assertEquals("1", euro.value)
        assertEquals("Euros", euro.name)
        assertTrue(euro.iconUrl.isNotEmpty())
        assertTrue(euro.isBase)
    }

    @Test
    fun `should convert to ui model if no info provided`() {
        val rates = CurrencyValues(
            "EUR", listOf(
                CurrencyValue("EUR", BigDecimal("1")),
                CurrencyValue("USD", BigDecimal("1.5"))
            )
        )

        val list = mapper.apply(rates, emptyMap())

        assertEquals(2, list.size)

        val usd = list.single { it.iso == "USD" }
        assertEquals("1.5", usd.value)
        assertTrue(usd.iconUrl.isEmpty())
        assertTrue(usd.name.isEmpty())
        assertFalse(usd.isBase)

        val euro = list.single { it.iso == "EUR" }
        assertEquals("1", euro.value)
        assertTrue(euro.name.isEmpty())
        assertTrue(euro.iconUrl.isEmpty())
        assertTrue(euro.isBase)
    }

    @Test
    fun `should not crash when empty()`() {
        val list = mapper.apply(CurrencyValues("EUR", emptyList()), emptyMap())
        assertTrue(list.isEmpty())
    }
}