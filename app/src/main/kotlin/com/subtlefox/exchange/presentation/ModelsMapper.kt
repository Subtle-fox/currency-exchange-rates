package com.subtlefox.exchange.presentation

import com.subtlefox.exchange.domain.CurrencyInfo
import com.subtlefox.exchange.domain.CurrencyValues
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class ModelsMapper
@Inject constructor() : BiFunction<CurrencyValues, Map<String, CurrencyInfo>, List<CurrencyUiModel>> {
    override fun apply(currencyValues: CurrencyValues, infos: Map<String, CurrencyInfo>): List<CurrencyUiModel> {
        if (currencyValues.values.isEmpty()) {
            return emptyList()
        }

        return currencyValues.values.map { value ->
            val info = infos[value.iso]
            val uiModel = CurrencyUiModel(
                iconUrl = info?.let { getUrl(it.countryCode) } ?: "",
                name = info?.name ?: "",
                value = toUiString(value.value),
                iso = value.iso,
                isBase = value.iso == currencyValues.base
            )
            uiModel
        }
    }

    fun getUrl(countryCode: String) = "https://raw.githubusercontent.com/hjnilsson/country-flags/master/png100px/$countryCode.png"

    fun toUiString(value: BigDecimal): String {
        return if (value.compareTo(BigDecimal.ZERO) != 0) {
            value.setScale(4, RoundingMode.HALF_EVEN)
                .stripTrailingZeros()
                .toPlainString()
        } else {
            "0"
        }
    }
}