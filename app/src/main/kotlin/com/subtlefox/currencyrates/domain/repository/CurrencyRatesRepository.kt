package com.subtlefox.currencyrates.domain.repository

import com.subtlefox.currencyrates.domain.CurrencyRate
import io.reactivex.Single

interface CurrencyRatesRepository {
    fun fetchRates(baseCurrencyIso: String): Single<List<CurrencyRate>>
}