package com.subtlefox.exchange.domain.implementation

import com.subtlefox.exchange.data.LocalRepository
import com.subtlefox.exchange.domain.CurrencyInfo
import com.subtlefox.exchange.domain.GetDetailsUseCase
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class GetDetailsUseCaseImpl
@Inject constructor(
    private val repo: LocalRepository
) : GetDetailsUseCase {

    override fun load(): Observable<Map<String, CurrencyInfo>> {
        return Single
            .zip(
                repo.readCurrencyNames(),
                repo.readCountryCodesInfo(),
                zipper
            )
            .onErrorReturn { emptyMap() }
            .toObservable()
    }

    private val zipper = BiFunction<Map<String, String>, Map<String, String>, Map<String, CurrencyInfo>> { names, countries ->
        names.map {
            it.key to CurrencyInfo(iso = it.key, name = it.value, countryCode = countries[it.key] ?: "")
        }.toMap()
    }

}