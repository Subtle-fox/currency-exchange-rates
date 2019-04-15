package com.subtlefox.currencyrates.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.subtlefox.currencyrates.domain.repository.CurrencyInfoRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.InputStreamReader
import javax.inject.Inject

class LocalRepository
@Inject constructor(
    private val ctx: Context,
    private val gson: Gson
) : CurrencyInfoRepository {

    private companion object {
        const val CURRENCY_INFO = "currencies.json"
        const val COUNTRY_INFO = "countries.json"
    }

    override fun readCurrencyNames(): Single<Map<String, String>> {
        return Single
            .create<Map<String, String>> { emitter ->
                val fd = ctx.assets.open(CURRENCY_INFO)
                val infoList = gson
                    .fromJson<Array<JsonInfoModel>>(
                        JsonReader(InputStreamReader(fd)),
                        Array<JsonInfoModel>::class.java
                    )
                    .map { it.code to it.name }.toMap()
                emitter.onSuccess(infoList)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun readCountryCodesInfo(): Single<Map<String, String>> {
        return Single
            .create<Map<String, String>> { emitter ->
                val fd = ctx.assets.open(COUNTRY_INFO)
                val type = object : TypeToken<Map<String, String>>() {}.type
                val infoList = gson
                    .fromJson<Map<String, String>>(JsonReader(InputStreamReader(fd)), type)
                    .map { it.value to it.key.toLowerCase() }.toMap()
                emitter.onSuccess(infoList)
            }
            .subscribeOn(Schedulers.io())
    }

}
