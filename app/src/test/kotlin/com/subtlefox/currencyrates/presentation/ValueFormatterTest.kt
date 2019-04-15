package com.subtlefox.currencyrates.presentation

import com.subtlefox.currencyrates.presentation.utils.toBigDecimalOrZero
import com.subtlefox.currencyrates.presentation.utils.toUiString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class ValueFormatterTest {
    @Test
    fun `should return empty string when 0`() {
        assertEquals("", BigDecimal.ZERO.toUiString())
        assertEquals("", BigDecimal("0.00").toUiString())
    }

    @Test
    fun `should trim zeros`() {
        assertEquals("0.1", BigDecimal("0.100").toUiString())
        assertEquals("1", BigDecimal("1").toUiString())
        assertEquals("2.02", BigDecimal("02.0200").toUiString())
    }

    @Test
    fun `should have no more than 4 digits after dot`() {
        assertEquals("0.1122", BigDecimal("0.11223344").toUiString())
        assertEquals("12.1122", BigDecimal("12.11223344").toUiString())
    }

    @Test
    fun `should round HALF EVEN`() {
        assertEquals("0.1234", BigDecimal("0.12345").toUiString())
        assertEquals("0.1236", BigDecimal("0.12355").toUiString())
    }

    ////

    @Test
    fun `should return 0 if bad string`() {
        assertEquals(BigDecimal.ZERO, ".".toBigDecimalOrZero())
        assertEquals(BigDecimal.ZERO, "".toBigDecimalOrZero())
        assertEquals(BigDecimal.ZERO, "0,3".toBigDecimalOrZero())
    }

    @Test
    fun `should add 0 prefix`() {
        assertEquals(BigDecimal("0.3"), ".3".toBigDecimalOrZero())
    }
}