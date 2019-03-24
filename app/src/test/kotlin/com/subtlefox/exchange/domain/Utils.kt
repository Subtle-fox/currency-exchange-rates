package com.subtlefox.exchange.domain

fun rateOf(iso: String, value: String = "1") = CurrencyRate(iso, value.toBigDecimal())

fun valueOf(iso: String, value: String = "1") = CurrencyValue(iso, value.toBigDecimal())