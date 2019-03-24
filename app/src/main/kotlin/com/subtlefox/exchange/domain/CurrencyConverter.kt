package com.subtlefox.exchange.domain

import io.reactivex.functions.BiFunction
import java.math.MathContext
import javax.inject.Inject

private val mc = MathContext.DECIMAL128

fun CurrencyRate.rebase(currency: CurrencyRate) = copy(rate = this.rate.divide(currency.rate, mc))

class CurrencyConverter
@Inject constructor() : BiFunction<CurrencyValue, Rates, CurrencyValues> {

    override fun apply(request: CurrencyValue, rates: Rates): CurrencyValues {
        val rateList = rebaseList(request, rates)

        val baseIndex = rates.values.indexOfFirst { it.iso == request.iso }
        val converted = arrayOfNulls<CurrencyValue>(rateList.size)
        rateList.forEachIndexed { idx, value ->
            val convIdx = when {
                idx == baseIndex -> 0
                idx < baseIndex -> idx + 1
                else -> idx
            }
            converted[convIdx] = CurrencyValue(value.iso, value.rate.multiply(request.value, mc))
        }
        return CurrencyValues(request.iso, converted.map { it!! })
    }

    fun rebaseList(request: CurrencyValue, rates: Rates): List<CurrencyRate> {
        if (request.iso == rates.base) {
            return rates.values
        }

        return rates.values
            .singleOrNull { it.iso == request.iso }
            ?.let { newBase -> rates.values.map { it.rebase(newBase) } }
            ?: emptyList()
    }
}