package com.subtlefox.currencyrates.data

import com.subtlefox.currencyrates.domain.CurrencyRate
import com.subtlefox.currencyrates.domain.repository.CurrencyRatesRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import javax.inject.Inject

class RemoteRepository
@Inject constructor(
    private val api: RemoteApi
) : CurrencyRatesRepository {

    override fun fetchRates(baseCurrencyIso: String): Single<List<CurrencyRate>> {
        return api
            .fetchRates(baseCurrencyIso)
            .map(::transform)
            .subscribeOn(Schedulers.io())
    }

    private fun transform(jsonRateModel: JsonRateModel): List<CurrencyRate> {
        val list = ArrayList(jsonRateModel.rates.map {
            CurrencyRate(iso = it.key, rate = BigDecimal(it.value))
        })
        list.add(
            CurrencyRate(
                iso = jsonRateModel.baseIso,
                rate = BigDecimal.ONE
            )
        )
        return list
    }

}
