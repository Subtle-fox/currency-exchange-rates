package com.subtlefox.currencyrates.domain.implementation

import com.subtlefox.currencyrates.data.LocalRepository
import com.subtlefox.currencyrates.domain.CurrencyInfo
import com.subtlefox.currencyrates.domain.GetDetailsUseCase
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class GetDetailsUseCaseImpl
@Inject constructor(
    private val repo: LocalRepository
) : GetDetailsUseCase {

    // NOTE: save the only one result for resubscribing
    private val lastValues = AtomicReference<Map<String, CurrencyInfo>?>()

    override fun load(): Observable<Map<String, CurrencyInfo>> {
        val loadSingle = lastValues.get()
            ?.let { Single.just(it) }
            ?: Single
                .zip(
                    repo.readCurrencyNames(),
                    repo.readCountryCodesInfo(),
                    zipper
                )
                .doOnSuccess(lastValues::set)
                .onErrorReturn { emptyMap() }

        return loadSingle.toObservable()
    }

    private val zipper =
        BiFunction<Map<String, String>, Map<String, String>, Map<String, CurrencyInfo>> { names, countries ->
            names.map {
                it.key to CurrencyInfo(
                    iso = it.key,
                    name = it.value,
                    countryCode = countries[it.key] ?: ""
                )
            }.toMap()
        }

}