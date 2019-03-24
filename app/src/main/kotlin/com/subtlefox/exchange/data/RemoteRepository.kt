package com.subtlefox.exchange.data

import com.subtlefox.exchange.domain.CurrencyRate
import com.subtlefox.exchange.domain.repository.CurrencyRatesRepository
import io.reactivex.Single
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
