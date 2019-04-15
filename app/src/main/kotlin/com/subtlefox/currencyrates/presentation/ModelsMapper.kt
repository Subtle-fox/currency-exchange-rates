package com.subtlefox.currencyrates.presentation

import com.subtlefox.currencyrates.domain.CurrencyInfo
import com.subtlefox.currencyrates.domain.CurrencyValues
import com.subtlefox.currencyrates.presentation.utils.toUiString
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class ModelsMapper
@Inject constructor() :
    BiFunction<CurrencyValues, Map<String, CurrencyInfo>, List<CurrencyUiModel>> {

    override fun apply(
        currencyValues: CurrencyValues,
        infos: Map<String, CurrencyInfo>
    ): List<CurrencyUiModel> {
        return currencyValues.values.map {
            val info = infos[it.iso]
            val uiModel = CurrencyUiModel(
                iconUrl = info?.let { inf -> getUrl(inf.countryCode) } ?: "",
                name = info?.name ?: "",
                value = it.value.toUiString(),
                iso = it.iso,
                isBase = it.iso == currencyValues.base
            )
            uiModel
        }
    }

    private fun getUrl(countryCode: String) =
        "https://raw.githubusercontent.com/hjnilsson/country-flags/master/png100px/$countryCode.png"

}