package com.subtlefox.currencyrates.presentation

import com.subtlefox.currencyrates.domain.CurrencyInfo
import com.subtlefox.currencyrates.domain.CurrencyValue
import com.subtlefox.currencyrates.domain.CurrencyValues
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class ModelsMapperTest {
    val mapper = ModelsMapper()

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