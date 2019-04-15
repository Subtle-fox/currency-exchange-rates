package com.subtlefox.currencyrates.domain

fun rateOf(iso: String, value: String = "1") = CurrencyRate(iso, value.toBigDecimal())

fun valueOf(iso: String, value: String = "1") = CurrencyValue(iso, value.toBigDecimal())