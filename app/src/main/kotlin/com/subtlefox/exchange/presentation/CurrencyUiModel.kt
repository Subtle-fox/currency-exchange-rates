package com.subtlefox.exchange.presentation

data class CurrencyUiModel(
    val iconUrl: String,
    val name: String,
    val value: String,
    val iso: String,
    val isBase: Boolean
)