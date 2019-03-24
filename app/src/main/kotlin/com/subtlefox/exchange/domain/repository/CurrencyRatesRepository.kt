package com.subtlefox.exchange.domain.repository

import com.subtlefox.exchange.domain.CurrencyRate
import io.reactivex.Single

interface CurrencyRatesRepository {
    fun fetchRates(baseCurrencyIso: String): Single<List<CurrencyRate>>
}