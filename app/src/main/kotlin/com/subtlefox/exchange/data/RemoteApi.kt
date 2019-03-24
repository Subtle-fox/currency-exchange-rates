package com.subtlefox.exchange.data

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface RemoteApi {
    @GET("latest")
    fun fetchRates(@Query("base") iso: String): Single<JsonRateModel>
}
