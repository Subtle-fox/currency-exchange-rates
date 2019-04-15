package com.subtlefox.currencyrates.domain.repository

import io.reactivex.Single

interface CurrencyInfoRepository {
    fun readCurrencyNames(): Single<Map<String, String>>
    fun readCountryCodesInfo(): Single<Map<String, String>>
}