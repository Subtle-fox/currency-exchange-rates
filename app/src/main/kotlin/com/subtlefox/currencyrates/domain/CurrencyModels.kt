package com.subtlefox.currencyrates.domain

import java.math.BigDecimal

data class CurrencyValue(
    val iso: String,
    val value: BigDecimal
)

data class CurrencyRate(
    val iso: String,
    val rate: BigDecimal
)

data class CurrencyInfo(
    val iso: String,
    val name: String,
    val countryCode: String
)

data class CurrencyValues(
    val base: String,
    val values: List<CurrencyValue>
)

data class Rates(
    val base: String,
    val values: List<CurrencyRate>
)